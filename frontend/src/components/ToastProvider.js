import React, { createContext, useContext, useState, useCallback } from 'react';
import { Toast, ToastContainer } from 'react-bootstrap';

const ToastContext = createContext();

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts(prev => prev.filter(toast => toast.id !== id));
  }, []);

  const addToast = useCallback((message, variant = 'info', duration = 5000) => {
    const id = Date.now() + Math.random();
    const toast = {
      id,
      message,
      variant,
      timestamp: new Date(),
      duration
    };

    setToasts(prev => [...prev, toast]);

    // Auto-remove toast after duration
    if (duration > 0) {
      setTimeout(() => {
        removeToast(id);
      }, duration);
    }

    return id;
  }, [removeToast]);

  const showSuccess = useCallback((message, duration = 3000) => {
    return addToast(message, 'success', duration);
  }, [addToast]);

  const showError = useCallback((message, duration = 8000) => {
    return addToast(message, 'danger', duration);
  }, [addToast]);

  const showWarning = useCallback((message, duration = 5000) => {
    return addToast(message, 'warning', duration);
  }, [addToast]);

  const showInfo = useCallback((message, duration = 4000) => {
    return addToast(message, 'info', duration);
  }, [addToast]);

  const clearAll = useCallback(() => {
    setToasts([]);
  }, []);

  const value = {
    addToast,
    removeToast,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    clearAll
  };

  return (
    <ToastContext.Provider value={value}>
      {children}
      <ToastContainer position="top-end" className="p-3" style={{ zIndex: 9999 }}>
        {toasts.map(toast => (
          <Toast
            key={toast.id}
            onClose={() => removeToast(toast.id)}
            show={true}
            bg={toast.variant}
            delay={toast.duration}
            autohide={toast.duration > 0}
          >
            <Toast.Header>
              <strong className="me-auto">
                {toast.variant === 'success' && '✅ Success'}
                {toast.variant === 'danger' && '❌ Error'}
                {toast.variant === 'warning' && '⚠️ Warning'}
                {toast.variant === 'info' && 'ℹ️ Info'}
              </strong>
              <small>{toast.timestamp.toLocaleTimeString()}</small>
            </Toast.Header>
            <Toast.Body className={toast.variant === 'danger' ? 'text-white' : ''}>
              {toast.message}
            </Toast.Body>
          </Toast>
        ))}
      </ToastContainer>
    </ToastContext.Provider>
  );
};

export default ToastProvider;