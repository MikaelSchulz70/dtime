import {
  applyStreamChunk,
  ChatAbortedError,
  extractErrorMessage,
  formatToolCallStatus,
  isChatAbortedError,
  parseNdjsonBuffer,
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
