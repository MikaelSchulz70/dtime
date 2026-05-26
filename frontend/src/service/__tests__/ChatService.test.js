import {
  applyStreamChunk,
  buildChatRequestBody,
  ChatAbortedError,
  extractErrorMessage,
  fetchBridgeHealth,
  fetchOllamaModels,
  formatModelSize,
  formatToolCallStatus,
  getInitialChatModel,
  getStoredChatModel,
  isChatAbortedError,
  looksLikeDataQuestion,
  modelNameMatches,
  parseNdjsonBuffer,
  parseOllamaModelNames,
  parsePullProgress,
  resolveBridgeHealthState,
  storeChatModel,
} from '../ChatService';

describe('extractErrorMessage', () => {
  it('reads Ollama error field from object', () => {
    expect(
      extractErrorMessage({
        error: 'model requires more system memory (2.3 GiB) than is available (1.8 GiB)',
      })
    ).toBe('model requires more system memory (2.3 GiB) than is available (1.8 GiB)');
  });

  it('reads error from JSON string', () => {
    expect(
      extractErrorMessage(
        '{"error":"model requires more system memory (2.3 GiB) than is available (1.8 GiB)"}'
      )
    ).toBe('model requires more system memory (2.3 GiB) than is available (1.8 GiB)');
  });

  it('returns null for invalid JSON object fragments', () => {
    expect(extractErrorMessage('{"model":"llama3.2"}{"model":"llama3.2"}')).toBeNull();
  });
});

describe('buildChatRequestBody', () => {
  it('includes keep_alive and CPU-friendly Ollama options', () => {
    const body = buildChatRequestBody({
      model: 'llama3.2',
      messages: [{ role: 'user', content: 'hi' }],
    });
    expect(body.model).toBe('llama3.2');
    expect(body.stream).toBe(true);
    expect(body.keep_alive).toBe('24h');
    expect(body.options.num_thread).toBe(8);
    expect(body.options.num_predict).toBe(384);
    expect(body.options.temperature).toBe(0.2);
  });
});

describe('parseNdjsonBuffer', () => {
  it('parses multiple concatenated JSON objects', () => {
    const input =
      '{"message":{"content":"Hi"},"done":false}{"message":{"content":"!"},"done":true}';
    const { objects, remainder } = parseNdjsonBuffer(input);
    expect(objects).toHaveLength(2);
    expect(objects[0].message.content).toBe('Hi');
    expect(objects[1].message.content).toBe('!');
    expect(remainder).toBe('');
  });

  it('keeps incomplete JSON in remainder', () => {
    const input = '{"message":{"content":"Hi"}';
    const { objects, remainder } = parseNdjsonBuffer(input);
    expect(objects).toHaveLength(0);
    expect(remainder).toBe(input);
  });
});

describe('applyStreamChunk', () => {
  it('accumulates content across chunks', () => {
    const msg = { role: 'assistant', content: '' };
    applyStreamChunk(msg, { message: { role: 'assistant', content: 'Hello' }, done: false });
    applyStreamChunk(msg, { message: { role: 'assistant', content: ' world' }, done: true });
    expect(msg.content).toBe('Hello world');
  });

  it('does not wipe content on done with empty message', () => {
    const msg = { role: 'assistant', content: 'Answer' };
    applyStreamChunk(msg, { message: { role: 'assistant', content: '' }, done: true });
    expect(msg.content).toBe('Answer');
  });

  it('records tool call status', () => {
    const msg = { role: 'assistant', content: '' };
    applyStreamChunk(msg, {
      message: {
        tool_calls: [{ function: { name: 'dtime.getPagedUsers' } }],
      },
      done: false,
    });
    expect(msg.toolStatus).toBe('Calling: dtime.getPagedUsers');
  });
});

describe('ChatAbortedError', () => {
  it('is detected by isChatAbortedError', () => {
    const err = new ChatAbortedError();
    expect(isChatAbortedError(err)).toBe(true);
  });
});

describe('formatToolCallStatus', () => {
  it('formats tool names', () => {
    expect(formatToolCallStatus([{ function: { name: 'dtime.getPagedUsers' } }])).toBe(
      'Calling: dtime.getPagedUsers'
    );
  });
});

describe('fetchBridgeHealth', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('returns health JSON when ok', async () => {
    jest.spyOn(global, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'healthy', tools: 22 }),
    });
    await expect(fetchBridgeHealth()).resolves.toEqual({ status: 'healthy', tools: 22 });
    expect(global.fetch).toHaveBeenCalledWith('/api/ollama/health');
  });

  it('returns null when fetch fails', async () => {
    jest.spyOn(global, 'fetch').mockRejectedValue(new Error('network'));
    await expect(fetchBridgeHealth()).resolves.toBeNull();
  });
});

describe('looksLikeDataQuestion', () => {
  it('detects factual DTime questions', () => {
    expect(looksLikeDataQuestion('How many users are in DTime?')).toBe(true);
    expect(looksLikeDataQuestion('List all tasks')).toBe(true);
    expect(looksLikeDataQuestion('Have all users closed their time reports?')).toBe(true);
  });

  it('ignores general chat', () => {
    expect(looksLikeDataQuestion('Hello there')).toBe(false);
    expect(looksLikeDataQuestion('What is MCP?')).toBe(false);
  });
});

describe('parseOllamaModelNames', () => {
  it('extracts and sorts unique model names', () => {
    expect(
      parseOllamaModelNames({
        models: [{ name: 'qwen3:latest' }, { model: 'llama3.2:latest' }, { name: 'qwen3:latest' }],
      })
    ).toEqual(['llama3.2:latest', 'qwen3:latest']);
  });

  it('returns empty array when no models', () => {
    expect(parseOllamaModelNames({ models: [] })).toEqual([]);
    expect(parseOllamaModelNames(null)).toEqual([]);
  });
});

describe('fetchOllamaModels', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('returns model names when ok', async () => {
    jest.spyOn(global, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ models: [{ name: 'llama3.2:latest' }] }),
    });
    await expect(fetchOllamaModels()).resolves.toEqual(['llama3.2:latest']);
    expect(global.fetch).toHaveBeenCalledWith('/api/ollama/api/tags');
  });

  it('returns null when fetch fails', async () => {
    jest.spyOn(global, 'fetch').mockRejectedValue(new Error('network'));
    await expect(fetchOllamaModels()).resolves.toBeNull();
  });
});

describe('chat model persistence', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('stores and reads selected model', () => {
    storeChatModel('qwen3:latest');
    expect(getStoredChatModel()).toBe('qwen3:latest');
    expect(getInitialChatModel()).toBe('qwen3:latest');
  });
});

describe('modelNameMatches', () => {
  it('matches short name to tagged install', () => {
    expect(modelNameMatches('llama3.2:latest', 'llama3.2')).toBe(true);
    expect(modelNameMatches('qwen3:latest', 'llama3.2')).toBe(false);
  });
});

describe('parsePullProgress', () => {
  it('computes download percent', () => {
    expect(parsePullProgress({ status: 'downloading', completed: 50, total: 100 })).toEqual({
      status: 'downloading',
      message: 'downloading',
      percent: 50,
    });
  });

  it('reports success', () => {
    expect(parsePullProgress({ status: 'success' }).percent).toBe(100);
  });

  it('surfaces errors', () => {
    expect(parsePullProgress({ error: 'out of memory' }).status).toBe('error');
  });
});

describe('formatModelSize', () => {
  it('formats bytes', () => {
    expect(formatModelSize(2048)).toBe('2.0 KB');
  });
});

describe('resolveBridgeHealthState', () => {
  const t = (key) => key;

  it('returns tools count when healthy', () => {
    expect(resolveBridgeHealthState({ tools: 22 }, t)).toEqual({
      toolsCount: 22,
      toolsWarning: null,
    });
  });

  it('warns when tools are zero', () => {
    expect(resolveBridgeHealthState({ tools: 0 }, t)).toEqual({
      toolsCount: 0,
      toolsWarning: 'chat.noMcpTools',
    });
  });

  it('warns when health is unreachable', () => {
    expect(resolveBridgeHealthState(null, t)).toEqual({
      toolsCount: null,
      toolsWarning: 'chat.errorGeneric',
    });
  });
});
