import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { formatChatContentForDisplay } from './formatChatContent';
import './Chat.css';

/**
 * Renders chat message body with markdown (headings, lists, fenced code/JSON).
 * @param {{ content: string, role: 'user' | 'assistant' }} props
 */
const ChatMessageContent = ({ content, role }) => {
  if (!content?.trim()) {
    return null;
  }

  const markdown = role === 'assistant' ? formatChatContentForDisplay(content) : content;

  return (
    <div className={`chat-message-body chat-message-body--${role}`}>
      <ReactMarkdown remarkPlugins={[remarkGfm]}>{markdown}</ReactMarkdown>
    </div>
  );
};

export default ChatMessageContent;
