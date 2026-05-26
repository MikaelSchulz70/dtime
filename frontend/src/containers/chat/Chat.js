import React, { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { Alert, Button, Card, Form, Spinner, Tab, Tabs } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import ChatModels from './ChatModels';
import ChatMessageContent from './ChatMessageContent';
import './Chat.css';
import {
  fetchBridgeHealth,
  fetchOllamaModels,
  formatToolCallStatus,
  getInitialChatModel,
  isChatAbortedError,
  looksLikeDataQuestion,
  resolveBridgeHealthState,
  sendChat,
  storeChatModel,
} from '../../service/ChatService';

const Chat = () => {
  const { t } = useTranslation();
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [model, setModel] = useState(getInitialChatModel());
  const [availableModels, setAvailableModels] = useState([]);
  const [modelsLoading, setModelsLoading] = useState(true);
  const [modelsError, setModelsError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [toolsWarning, setToolsWarning] = useState(null);
  const [toolsCount, setToolsCount] = useState(null);
  const [noToolsUsedHint, setNoToolsUsedHint] = useState(null);
  const [activeTab, setActiveTab] = useState('chat');
  const listRef = useRef(null);
  const abortControllerRef = useRef(null);

  const refreshBridgeHealth = useCallback(async () => {
    const health = await fetchBridgeHealth();
    const { toolsCount: count, toolsWarning: warning } = resolveBridgeHealthState(health, t);
    setToolsCount(count);
    setToolsWarning(warning);
    return health;
  }, [t]);

  const loadModels = useCallback(async () => {
    setModelsLoading(true);
    setModelsError(null);
    const models = await fetchOllamaModels();
    setModelsLoading(false);
    if (models == null) {
      setModelsError(t('chat.modelsLoadError'));
      return;
    }
    if (models.length === 0) {
      setModelsError(t('chat.noModels'));
    }
    setAvailableModels(models);
  }, [t]);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      await Promise.all([refreshBridgeHealth(), loadModels()]);
      if (cancelled) {
        return;
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [refreshBridgeHealth, loadModels]);

  const modelOptions = availableModels.includes(model)
    ? availableModels
    : model
      ? [model, ...availableModels]
      : availableModels;

  const handleModelChange = (event) => {
    const nextModel = event.target.value;
    setModel(nextModel);
    storeChatModel(nextModel);
  };

  const scrollToBottom = useCallback(() => {
    if (listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight;
    }
  }, []);

  useLayoutEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

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
    setNoToolsUsedHint(null);
    setLoading(true);

    const health = await refreshBridgeHealth();
    const currentToolsCount = health?.tools ?? 0;

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
        },
      });

      setMessages((prev) => {
        const updated = [...prev];
        updated[updated.length - 1] = assistantMessage;
        return updated;
      });

      if (
        !assistantMessage.toolStatus &&
        looksLikeDataQuestion(text) &&
        (health == null || currentToolsCount > 0)
      ) {
        setNoToolsUsedHint(t('chat.noToolsUsedHint'));
      }
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
    }
  };

  const handleClear = () => {
    setMessages([]);
    setError(null);
    setNoToolsUsedHint(null);
  };

  return (
    <div className="row justify-content-center">
      <div className="col-lg-10 col-xl-8">
        <Card>
          <Card.Header className="d-flex justify-content-between align-items-center flex-wrap gap-2">
            <div className="d-flex align-items-center gap-2 flex-wrap">
              <span>{t('chat.title')}</span>
              {toolsCount != null && toolsCount > 0 && (
                <span className="badge bg-success">{t('chat.toolsConnected', { count: toolsCount })}</span>
              )}
            </div>
            <Button
              variant="outline-secondary"
              size="sm"
              onClick={handleClear}
              disabled={loading || messages.length === 0 || activeTab !== 'chat'}
            >
              {t('chat.clear')}
            </Button>
          </Card.Header>
          <Card.Body>
            <Tabs activeKey={activeTab} onSelect={(key) => setActiveTab(key || 'chat')} className="mb-3">
              <Tab eventKey="chat" title={t('chat.tabChat')}>
                <p className="text-muted small mb-3">{t('chat.description')}</p>
                {toolsWarning && (
                  <Alert variant="warning" className="mb-3">
                    {toolsWarning}
                  </Alert>
                )}
                {noToolsUsedHint && (
                  <Alert variant="info" className="mb-3" dismissible onClose={() => setNoToolsUsedHint(null)}>
                    {noToolsUsedHint}
                  </Alert>
                )}
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
                      className={`chat-message-row chat-message-row--${msg.role}`}
                    >
                      <div className="chat-message-meta">
                        <span className="badge bg-secondary">
                          {msg.role === 'user' ? t('chat.you') : t('chat.assistant')}
                        </span>
                      </div>
                      {msg.toolStatus && (
                        <div className="chat-message-tool-status mb-1">{msg.toolStatus}</div>
                      )}
                      <ChatMessageContent content={msg.content} role={msg.role} />
                      {loading && index === messages.length - 1 && msg.role === 'assistant' && !msg.content && !msg.toolStatus && (
                        <Spinner animation="border" size="sm" className="ms-2" />
                      )}
                    </div>
                  ))}
                </div>
                <Form onSubmit={handleSend}>
                  <Form.Group className="mb-2">
                    <Form.Label className="small text-muted">{t('chat.model')}</Form.Label>
                    <div className="d-flex gap-2 align-items-center">
                      <Form.Select
                        value={model}
                        onChange={handleModelChange}
                        disabled={loading || modelsLoading || modelOptions.length === 0}
                        size="sm"
                        aria-label={t('chat.model')}
                      >
                        {modelOptions.map((name) => (
                          <option key={name} value={name}>
                            {name}
                          </option>
                        ))}
                      </Form.Select>
                      <Button
                        type="button"
                        variant="outline-secondary"
                        size="sm"
                        onClick={loadModels}
                        disabled={loading || modelsLoading}
                        title={t('chat.refreshModels')}
                      >
                        {modelsLoading ? (
                          <Spinner animation="border" size="sm" role="status" />
                        ) : (
                          t('chat.refreshModels')
                        )}
                      </Button>
                    </div>
                    {modelsError && (
                      <Form.Text className="text-warning">{modelsError}</Form.Text>
                    )}
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
              </Tab>
              <Tab eventKey="models" title={t('chat.tabModels')}>
                <ChatModels onModelsChanged={loadModels} />
              </Tab>
            </Tabs>
          </Card.Body>
        </Card>
      </div>
    </div>
  );
};

export default Chat;
