import React, { createContext, useContext, useState, useCallback } from 'react';

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

    const addToast = useCallback((message, type = 'info', duration = 5000) => {
        const id = Date.now() + Math.random();
        const toast = { id, message, type, duration };
        
        setToasts(prev => [...prev, toast]);
        
        if (duration > 0) {
            setTimeout(() => {
                removeToast(id);
            }, duration);
        }
        
        return id;
    }, [removeToast]);

    const showSuccess = useCallback((message, duration) => addToast(message, 'success', duration), [addToast]);
    const showError = useCallback((message, duration) => addToast(message, 'error', duration), [addToast]);
    const showWarning = useCallback((message, duration) => addToast(message, 'warning', duration), [addToast]);
    const showInfo = useCallback((message, duration) => addToast(message, 'info', duration), [addToast]);

    const value = {
        toasts,
        addToast,
        removeToast,
        showSuccess,
        showError,
        showWarning,
        showInfo
    };

    return (
        <ToastContext.Provider value={value}>
            {children}
            <ToastContainer />
        </ToastContext.Provider>
    );
};

const ToastContainer = () => {
    const { toasts, removeToast } = useToast();

    if (toasts.length === 0) return null;

    return (
        <div className="toast-container position-fixed top-0 end-0 p-3" style={{ zIndex: 1055 }}>
            {toasts.map(toast => (
                <Toast key={toast.id} toast={toast} onClose={() => removeToast(toast.id)} />
            ))}
        </div>
    );
};

const Toast = ({ toast, onClose }) => {
    const getToastClasses = () => {
        const baseClasses = 'toast show';
        const typeClasses = {
            success: 'border-success',
            error: 'border-danger',
            warning: 'border-warning',
            info: 'border-info'
        };
        return `${baseClasses} ${typeClasses[toast.type] || typeClasses.info}`;
    };

    const getHeaderClasses = () => {
        const typeClasses = {
            success: 'bg-success text-white',
            error: 'bg-danger text-white',
            warning: 'bg-warning text-dark',
            info: 'bg-info text-white'
        };
        return `toast-header ${typeClasses[toast.type] || typeClasses.info}`;
    };

    const getIcon = () => {
        const icons = {
            success: '✅',
            error: '❌',
            warning: '⚠️',
            info: 'ℹ️'
        };
        return icons[toast.type] || icons.info;
    };

    const getTitle = () => {
        const titles = {
            success: 'Success',
            error: 'Error',
            warning: 'Warning',
            info: 'Info'
        };
        return titles[toast.type] || titles.info;
    };

    return (
        <div className={getToastClasses()} role="alert" aria-live="assertive" aria-atomic="true">
            <div className={getHeaderClasses()}>
                <span className="me-2">{getIcon()}</span>
                <strong className="me-auto">{getTitle()}</strong>
                <button
                    type="button"
                    className="btn-close btn-close-white"
                    aria-label="Close"
                    onClick={onClose}
                />
            </div>
            <div className="toast-body">
                {toast.message}
            </div>
        </div>
    );
};