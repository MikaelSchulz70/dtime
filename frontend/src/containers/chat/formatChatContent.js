/**
 * Prepare assistant text for markdown rendering (fenced code blocks, lists, etc.).
 * @param {string} content
 * @returns {string}
 */
export function formatChatContentForDisplay(content) {
  if (!content) {
    return '';
  }
  const trimmed = content.trim();
  if (!trimmed) {
    return '';
  }
  if (trimmed.includes('```')) {
    return content;
  }
  if ((trimmed.startsWith('{') || trimmed.startsWith('[')) && isLikelyJson(trimmed)) {
    return `\`\`\`json\n${trimmed}\n\`\`\``;
  }
  return wrapEmbeddedJsonBlocks(content);
}

function isLikelyJson(text) {
  try {
    JSON.parse(text);
    return true;
  } catch {
    return false;
  }
}

function wrapEmbeddedJsonBlocks(content) {
  const lines = content.split('\n');
  const result = [];
  let jsonBuffer = [];
  let inJson = false;

  const flushJson = () => {
    if (jsonBuffer.length === 0) {
      return;
    }
    const block = jsonBuffer.join('\n').trim();
    if (isLikelyJson(block)) {
      result.push('```json', block, '```');
    } else {
      result.push(...jsonBuffer);
    }
    jsonBuffer = [];
    inJson = false;
  };

  for (const line of lines) {
    const t = line.trim();
    if (!inJson && (t.startsWith('{') || t.startsWith('['))) {
      inJson = true;
      jsonBuffer = [line];
      continue;
    }
    if (inJson) {
      jsonBuffer.push(line);
      if (t.endsWith('}') || t.endsWith(']')) {
        flushJson();
      }
      continue;
    }
    result.push(line);
  }
  flushJson();
  return result.join('\n');
}
