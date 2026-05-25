import { formatChatContentForDisplay } from '../formatChatContent';

describe('formatChatContentForDisplay', () => {
  it('wraps a full JSON object in a fenced block', () => {
    const input = '{"totalElements": 2}';
    expect(formatChatContentForDisplay(input)).toBe('```json\n{"totalElements": 2}\n```');
  });

  it('wraps embedded JSON blocks in mixed prose', () => {
    const input = [
      'Here is the response:',
      '',
      '{',
      '  "totalElements": 2',
      '}',
      '',
      'The total is 2.',
    ].join('\n');
    const output = formatChatContentForDisplay(input);
    expect(output).toContain('```json');
    expect(output).toContain('"totalElements": 2');
    expect(output).toContain('The total is 2.');
  });

  it('leaves plain prose unchanged', () => {
    const input = 'There are 2 users in DTime.';
    expect(formatChatContentForDisplay(input)).toBe(input);
  });
});
