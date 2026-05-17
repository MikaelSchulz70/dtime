import env from '../config/env';

const CHAT_PATH = '/api/ollama/api/chat';

export class ChatAbortedError extends Error {
  constructor() {
    super('Chat stopped');
    this.name = 'ChatAbortedError';
  }
}

export function isChatAbortedError(err) {
  return err instanceof ChatAbortedError || err?.name === 'ChatAbortedError';
}

export function getDefaultModel() {
  return env.ollamaDefaultModel;
}

/**
 * Extract a human-readable error from Ollama / bridge JSON (string or object).
 * @param {unknown} payload
 * @returns {string|null}
 */
export function extractErrorMessage(payload) {
  if (payload == null) {
    return null;
  }
  if (typeof payload === 'string') {
    const trimmed = payload.trim();
    if (!trimmed) {
      return null;
    }
    try {
      return extractErrorMessage(JSON.parse(trimmed));
    } catch {
      if (trimmed.startsWith('{')) {
        return null;
      }
      return trimmed;
    }
  }
  if (typeof payload === 'object') {
    if (typeof payload.error === 'string') {
      return payload.error;
    }
    if (payload.error && typeof payload.error === 'object' && payload.error.message) {
      return String(payload.error.message);
    }
    if (payload.detail) {
      return typeof payload.detail === 'string' ? payload.detail : JSON.stringify(payload.detail);
    }
    if (payload.message && typeof payload.message === 'string' && payload.role === 'error') {
      return payload.message;
    }
  }
  return null;
}

/**
 * Parse one or more JSON objects from an Ollama NDJSON / concatenated stream buffer.
 * @param {string} buffer
 * @returns {{ objects: object[], remainder: string }}
 */
export function parseNdjsonBuffer(buffer) {
  const objects = [];
  let i = 0;

  while (i < buffer.length) {
    while (i < buffer.length && /\s/.test(buffer[i])) {
      i += 1;
    }
    if (i >= buffer.length || buffer[i] !== '{') {
      break;
    }

    const start = i;
    let depth = 0;
    let inString = false;
    let escaped = false;

    for (; i < buffer.length; i += 1) {
      const char = buffer[i];

      if (inString) {
        if (escaped) {
          escaped = false;
        } else if (char === '\\') {
          escaped = true;
        } else if (char === '"') {
          inString = false;
        }
        continue;
      }

      if (char === '"') {
        inString = true;
        continue;
      }
      if (char === '{') {
        depth += 1;
      } else if (char === '}') {
        depth -= 1;
        if (depth === 0) {
          const jsonText = buffer.slice(start, i + 1);
          try {
            objects.push(JSON.parse(jsonText));
          } catch {
            return { objects, remainder: buffer.slice(start) };
          }
          i += 1;
          break;
        }
      }
    }

    if (depth !== 0) {
      return { objects, remainder: buffer.slice(start) };
    }
  }

  return { objects, remainder: '' };
}

export function formatToolCallStatus(toolCalls) {
  if (!toolCalls?.length) {
    return null;
  }
  const names = toolCalls
    .map((call) => call.function?.name || call.name || 'tool')
    .filter(Boolean);
  return names.length ? `Calling: ${names.join(', ')}` : null;
}

function parseResponseErrorText(text) {
  const fromJson = extractErrorMessage(text);
  if (fromJson) {
    return fromJson;
  }
  const trimmed = text?.trim();
  return trimmed || null;
}

/**
 * Apply one Ollama stream chunk to the accumulated assistant message.
 * @param {{ role: string, content: string, toolStatus?: string|null }} assistantMessage
 * @param {object} chunk
 */
export function applyStreamChunk(assistantMessage, chunk) {
  const streamError = extractErrorMessage(chunk);
  if (streamError) {
    throw new Error(streamError);
  }

  const toolStatus = formatToolCallStatus(chunk.message?.tool_calls);
  if (toolStatus) {
    assistantMessage.toolStatus = toolStatus;
  }

  if (chunk.message?.content) {
    assistantMessage.content = (assistantMessage.content || '') + chunk.message.content;
    assistantMessage.role = chunk.message.role || 'assistant';
  } else if (chunk.done && chunk.message?.content) {
    assistantMessage.content = chunk.message.content;
    assistantMessage.role = chunk.message.role || 'assistant';
  }

  return assistantMessage;
}

/**
 * Send a chat request to the Ollama MCP bridge (proxied at /api/ollama).
 * @param {Array<{role: string, content: string}>} messages
 * @param {{ model?: string, stream?: boolean, onChunk?: (partial: object) => void, signal?: AbortSignal }} options
 * @returns {Promise<{ role: string, content: string, toolStatus?: string }>}
 */
export async function sendChat(messages, options = {}) {
  const { model = getDefaultModel(), stream = true, onChunk, signal } = options;

  let response;
  try {
    response = await fetch(CHAT_PATH, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({
        model,
        messages,
        stream,
      }),
      signal,
    });
  } catch (err) {
    if (err.name === 'AbortError') {
      throw new ChatAbortedError();
    }
    throw new Error(
      'Cannot reach the chat service. Start the Ollama bridge (port 8082) and restart npm start. ' +
        `(${err.message})`
    );
  }

  if (!response.ok) {
    const text = await response.text();
    const detail = parseResponseErrorText(text);
    if (detail && !detail.includes('Error occurred while trying to proxy')) {
      throw new Error(detail);
    }
    throw new Error(
      `Chat request failed (${response.status}). Is the Ollama bridge running on port 8082?`
    );
  }

  if (!stream) {
    const data = await response.json();
    const err = extractErrorMessage(data);
    if (err) {
      throw new Error(err);
    }
    return data.message || { role: 'assistant', content: data.response || '' };
  }

  return readStreamingChat(response, onChunk, signal);
}

async function readStreamingChat(response, onChunk, signal) {
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
  let assistantMessage = { role: 'assistant', content: '', toolStatus: null };

  const processParsedChunks = (chunks) => {
    for (const chunk of chunks) {
      applyStreamChunk(assistantMessage, chunk);
      if (onChunk) {
        onChunk(chunk);
      }
    }
  };

  while (true) {
    if (signal?.aborted) {
      await reader.cancel();
      throw new ChatAbortedError();
    }

    let readResult;
    try {
      readResult = await reader.read();
    } catch (err) {
      if (err.name === 'AbortError') {
        throw new ChatAbortedError();
      }
      throw err;
    }

    const { done, value } = readResult;
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });

    const newlineParts = buffer.split('\n');
    buffer = newlineParts.pop() || '';

    for (const line of newlineParts) {
      const trimmed = line.trim();
      if (!trimmed) {
        continue;
      }
      const { objects, remainder } = parseNdjsonBuffer(trimmed);
      if (objects.length) {
        processParsedChunks(objects);
      }
      if (remainder) {
        buffer = remainder + (buffer ? `\n${buffer}` : '');
      }
    }

    const { objects, remainder } = parseNdjsonBuffer(buffer);
    if (objects.length) {
      processParsedChunks(objects);
      buffer = remainder;
    }
  }

  const trimmed = buffer.trim();
  if (trimmed) {
    const { objects } = parseNdjsonBuffer(trimmed);
    processParsedChunks(objects);
  }

  if (!assistantMessage.content?.trim() && !assistantMessage.toolStatus) {
    const trailingError = extractErrorMessage(trimmed);
    if (trailingError) {
      throw new Error(trailingError);
    }
  }

  return assistantMessage;
}
