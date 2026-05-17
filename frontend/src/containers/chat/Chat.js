import React, { useCallback, useRef, useState } from 'react';
import { Alert, Button, Card, Form, Spinner } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import {
  formatToolCallStatus,
  getDefaultModel,
  isChatAbortedError,
  sendChat,
} from '../../service/ChatService';

const Chat = () => {
  const { t } = useTranslation();
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [model, setModel] = useState(getDefaultModel());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const listRef = useRef(null);
  const abortControllerRef = useRef(null);

  const scrollToBottom = useCallback(() => {
    if (listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight;
    }
  }, []);

  const handleInputKeyDown = (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      if (!loading && input.trim()) {
        handleSend(event);
      }
    }
  };

  const handleStop = () => {
    abortControllerRef.current?.abort();
  };

  const handleSend = async (event) => {
    event.preventDefault();
    const text = input.trim();
    if (!text || loading) {
      return;
    }

    const userMessage = { role: 'user', content: text };
    const nextMessages = [...messages, userMessage];
    setMessages(nextMessages);
    setInput('');
    setError(null);
    setLoading(true);

    const abortController = new AbortController();
    abortControllerRef.current = abortController;

    const assistantPlaceholder = { role: 'assistant', content: '', toolStatus: null };
    setMessages([...nextMessages, assistantPlaceholder]);

    try {
      const assistantMessage = await sendChat(nextMessages, {
        model,
        stream: true,
        signal: abortController.signal,
        onChunk: (chunk) => {
          const toolStatus = formatToolCallStatus(chunk.message?.tool_calls);
          if (!toolStatus && !chunk.message?.content) {
            return;
          }
          setMessages((prev) => {
            const updated = [...prev];
            const last = updated[updated.length - 1];
            if (last?.role === 'assistant') {
              updated[updated.length - 1] = {
                role: 'assistant',
                content: chunk.message?.content
                  ? (last.content || '') + chunk.message.content
                  : last.content || '',
                toolStatus: toolStatus || last.toolStatus,
              };
            }
            return updated;
          });
          scrollToBottom();
        },
      });

      setMessages((prev) => {
        const updated = [...prev];
        updated[updated.length - 1] = assistantMessage;
        return updated;
      });
    } catch (err) {
      if (isChatAbortedError(err)) {
        return;
      }
      setMessages(nextMessages);
      const message = err.message || t('chat.errorGeneric');
      setError(message);
      console.error('Chat error:', err);
    } finally {
      abortControllerRef.current = null;
      setLoading(false);
      scrollToBottom();
    }
  };

  const handleClear = () => {
    setMessages([]);
    setError(null);
  };

  return (
    <div className="row justify-content-center">
      <div className="col-lg-10 col-xl-8">
        <Card>
          <Card.Header className="d-flex justify-content-between align-items-center">
            <span>{t('chat.title')}</span>
            <Button variant="outline-secondary" size="sm" onClick={handleClear} disabled={loading || messages.length === 0}>
              {t('chat.clear')}
            </Button>
          </Card.Header>
          <Card.Body>
            <p className="text-muted small mb-3">{t('chat.description')}</p>
            {error && (
              <Alert variant="danger" dismissible onClose={() => setError(null)}>
                {error}
              </Alert>
            )}
            <div
              ref={listRef}
              className="border rounded p-3 mb-3 bg-light"
              style={{ minHeight: '320px', maxHeight: '50vh', overflowY: 'auto' }}
            >
              {messages.length === 0 && (
                <p className="text-muted mb-0">{t('chat.empty')}</p>
              )}
              {messages.map((msg, index) => (
                <div
                  key={`${index}-${msg.role}`}
                  className={`mb-2 ${msg.role === 'user' ? 'text-end' : ''}`}
                >
                  <span className="badge bg-secondary me-2">{msg.role === 'user' ? t('chat.you') : t('chat.assistant')}</span>
                  {msg.toolStatus && (
                    <div className="text-muted small fst-italic mb-1">{msg.toolStatus}</div>
                  )}
                  <span style={{ whiteSpace: 'pre-wrap' }}>{msg.content}</span>
                  {loading && index === messages.length - 1 && msg.role === 'assistant' && !msg.content && !msg.toolStatus && (
                    <Spinner animation="border" size="sm" className="ms-2" />
                  )}
                </div>
              ))}
            </div>
            <Form onSubmit={handleSend}>
              <Form.Group className="mb-2">
                <Form.Label className="small text-muted">{t('chat.model')}</Form.Label>
                <Form.Control
                  type="text"
                  value={model}
                  onChange={(e) => setModel(e.target.value)}
                  disabled={loading}
                  size="sm"
                />
              </Form.Group>
              <Form.Group className="mb-2">
                <Form.Control
                  as="textarea"
                  rows={3}
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  onKeyDown={handleInputKeyDown}
                  placeholder={t('chat.placeholder')}
                  disabled={loading}
                  style={{ minHeight: '4.5rem', resize: 'vertical' }}
                />
              </Form.Group>
              <div className="d-flex gap-2 align-items-center">
                {loading ? (
                  <Button type="button" variant="danger" onClick={handleStop}>
                    {t('chat.stop')}
                  </Button>
                ) : (
                  <Button type="submit" variant="success" disabled={!input.trim()}>
                    {t('chat.send')}
                  </Button>
                )}
                {loading && (
                  <span className="text-muted small">{t('chat.sending')}</span>
                )}
              </div>
            </Form>
          </Card.Body>
        </Card>
      </div>
    </div>
  );
};

export default Chat;
