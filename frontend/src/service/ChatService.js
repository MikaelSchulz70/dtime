import env from '../config/env';

const CHAT_PATH = '/api/ollama/api/chat';
const BRIDGE_HEALTH_PATH = '/api/ollama/health';
const OLLAMA_TAGS_PATH = '/api/ollama/api/tags';
const OLLAMA_PULL_PATH = '/api/ollama/api/pull';
const OLLAMA_DELETE_PATH = '/api/ollama/api/delete';
const MODEL_MANIFEST_PATH = '/ollama-models.manifest.json';
const CHAT_MODEL_STORAGE_KEY = 'dtime.chat.model';

/** Ollama options tuned for CPU Docker (Intel Mac). Shorter num_predict = faster replies. */
const OLLAMA_CHAT_KEEP_ALIVE = '24h';
const OLLAMA_CHAT_OPTIONS = {
  num_thread: 8,
  num_predict: 384,
  temperature: 0.2,
};

/**
 * Build POST /api/chat body forwarded to ollama-mcp-bridge (and Ollama).
 * @param {{ model: string, messages: object[], stream?: boolean }} params
 */
export function buildChatRequestBody({ model, messages, stream = true }) {
  return {
    model,
    messages,
    stream,
    keep_alive: OLLAMA_CHAT_KEEP_ALIVE,
    options: OLLAMA_CHAT_OPTIONS,
  };
}

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

export function getStoredChatModel() {
  try {
    return localStorage.getItem(CHAT_MODEL_STORAGE_KEY) || null;
  } catch {
    return null;
  }
}

export function storeChatModel(model) {
  try {
    if (model) {
      localStorage.setItem(CHAT_MODEL_STORAGE_KEY, model);
    }
  } catch {
    // ignore quota / private browsing
  }
}

/** @returns {string} */
export function getInitialChatModel() {
  return getStoredChatModel() || getDefaultModel();
}

/**
 * @param {{ models?: Array<{ name?: string, model?: string }> }|null|undefined} payload
 * @returns {string[]}
 */
export function parseOllamaModelNames(payload) {
  if (!payload?.models?.length) {
    return [];
  }
  const names = payload.models
    .map((entry) => entry.name || entry.model)
    .filter(Boolean);
  return [...new Set(names)].sort((a, b) => a.localeCompare(b));
}

/**
 * List model names available in the local Ollama instance (via bridge proxy).
 * @returns {Promise<string[]|null>} null when Ollama is unreachable
 */
export async function fetchOllamaModels() {
  try {
    const response = await fetch(OLLAMA_TAGS_PATH);
    if (!response.ok) {
      return null;
    }
    const data = await response.json();
    return parseOllamaModelNames(data);
  } catch {
    return null;
  }
}

/**
 * @typedef {{ name: string, model?: string, size?: number, modified_at?: string, details?: object }} OllamaModelEntry
 */

/**
 * Full installed model list from Ollama tags API.
 * @returns {Promise<OllamaModelEntry[]|null>}
 */
export async function fetchInstalledOllamaModels() {
  try {
    const response = await fetch(OLLAMA_TAGS_PATH);
    if (!response.ok) {
      return null;
    }
    const data = await response.json();
    if (!data?.models?.length) {
      return [];
    }
    return data.models.map((entry) => ({
      name: entry.name || entry.model,
      model: entry.model,
      size: entry.size,
      modified_at: entry.modified_at,
      details: entry.details,
    }));
  } catch {
    return null;
  }
}

/**
 * Recommended models from git-tracked manifest (synced to public/).
 * @returns {Promise<{ default_model?: string, models?: Array<{ name: string, required?: boolean, description?: string }> }|null>}
 */
export async function fetchModelManifest() {
  try {
    const response = await fetch(MODEL_MANIFEST_PATH);
    if (!response.ok) {
      return null;
    }
    return await response.json();
  } catch {
    return null;
  }
}

/**
 * Match manifest short name to installed tag (e.g. llama3.2 ↔ llama3.2:latest).
 * @param {string} installedName
 * @param {string} manifestName
 */
export function modelNameMatches(installedName, manifestName) {
  if (!installedName || !manifestName) {
    return false;
  }
  if (installedName === manifestName) {
    return true;
  }
  const base = (name) => name.split(':')[0];
  return base(installedName) === base(manifestName);
}

/**
 * @param {object} chunk
 * @returns {{ status?: string, message?: string, percent?: number|null }}
 */
export function parsePullProgress(chunk) {
  const err = extractErrorMessage(chunk);
  if (err) {
    return { status: 'error', message: err, percent: null };
  }
  const status = chunk.status;
  if (status === 'success') {
    return { status: 'success', message: status, percent: 100 };
  }
  if (typeof chunk.total === 'number' && chunk.total > 0 && typeof chunk.completed === 'number') {
    const percent = Math.min(100, Math.round((chunk.completed / chunk.total) * 100));
    return { status: status || 'downloading', message: status, percent };
  }
  return { status: status || 'pulling', message: status, percent: null };
}

/**
 * Pull a model via Ollama API (streamed progress).
 * @param {string} name
 * @param {{ onProgress?: (progress: object) => void, signal?: AbortSignal }} [options]
 */
export async function pullOllamaModel(name, options = {}) {
  const { onProgress, signal } = options;
  let response;
  try {
    response = await fetch(OLLAMA_PULL_PATH, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, stream: true }),
      signal,
    });
  } catch (err) {
    if (err.name === 'AbortError') {
      throw new ChatAbortedError();
    }
    throw new Error(`Cannot reach Ollama pull API. (${err.message})`);
  }

  if (!response.ok) {
    const text = await response.text();
    throw new Error(parseResponseErrorText(text) || `Pull failed (${response.status})`);
  }

  await consumeNdjsonStream(response, (chunk) => {
    const progress = parsePullProgress(chunk);
    if (onProgress) {
      onProgress(progress);
    }
    if (progress.status === 'error') {
      throw new Error(progress.message || 'Pull failed');
    }
  }, signal);

  return { name, status: 'success' };
}

/**
 * Delete a model from local Ollama storage.
 * @param {string} name
 */
export async function deleteOllamaModel(name) {
  let response;
  try {
    response = await fetch(OLLAMA_DELETE_PATH, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name }),
    });
  } catch (err) {
    throw new Error(`Cannot reach Ollama delete API. (${err.message})`);
  }

  if (!response.ok) {
    const text = await response.text();
    throw new Error(parseResponseErrorText(text) || `Delete failed (${response.status})`);
  }

  return response.json().catch(() => ({}));
}

/**
 * @param {number} bytes
 * @returns {string}
 */
export function formatModelSize(bytes) {
  if (bytes == null || Number.isNaN(bytes)) {
    return '';
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let size = bytes;
  let unit = 0;
  while (size >= 1024 && unit < units.length - 1) {
    size /= 1024;
    unit += 1;
  }
  return `${size.toFixed(unit === 0 ? 0 : 1)} ${units[unit]}`;
}

async function consumeNdjsonStream(response, onChunk, signal) {
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  const processBuffer = (text) => {
    const { objects, remainder } = parseNdjsonBuffer(text);
    for (const chunk of objects) {
      onChunk(chunk);
    }
    return remainder;
  };

  while (true) {
    if (signal?.aborted) {
      await reader.cancel();
      throw new ChatAbortedError();
    }
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });
    const parts = buffer.split('\n');
    buffer = parts.pop() || '';
    for (const line of parts) {
      const trimmed = line.trim();
      if (!trimmed) {
        continue;
      }
      buffer = processBuffer(trimmed) + (buffer ? `\n${buffer}` : '');
    }
    buffer = processBuffer(buffer);
  }

  const trimmed = buffer.trim();
  if (trimmed) {
    processBuffer(trimmed);
  }
}

/**
 * Bridge health from ollama-mcp-bridge (tools count = registered DTime MCP tools).
 * @returns {Promise<{ status?: string, tools?: number, ollama_status?: string }|null>}
 */
export async function fetchBridgeHealth() {
  try {
    const response = await fetch(BRIDGE_HEALTH_PATH);
    if (!response.ok) {
      return null;
    }
    return await response.json();
  } catch {
    return null;
  }
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

const DATA_QUESTION_PATTERN =
  /\b(how many|how much|count|total|number of|list|show|get|who are|which users|users|accounts|tasks|report|time report|hours|vacation|unclosed|closed|submitted|missing)\b/i;

/**
 * Heuristic: user message likely needs MCP tool data (not general chat).
 * @param {string} text
 * @returns {boolean}
 */
export function looksLikeDataQuestion(text) {
  return DATA_QUESTION_PATTERN.test(String(text || '').trim());
}

/**
 * Map bridge health to UI warning/tools count state.
 * @param {{ status?: string, tools?: number }|null} health
 * @param {(key: string) => string} t
 * @returns {{ toolsCount: number|null, toolsWarning: string|null }}
 */
export function resolveBridgeHealthState(health, t) {
  if (!health) {
    return { toolsCount: null, toolsWarning: t('chat.errorGeneric') };
  }
  const tools = health.tools ?? 0;
  if (tools <= 0) {
    return { toolsCount: 0, toolsWarning: t('chat.noMcpTools') };
  }
  return { toolsCount: tools, toolsWarning: null };
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
      body: JSON.stringify(buildChatRequestBody({ model, messages, stream })),
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
