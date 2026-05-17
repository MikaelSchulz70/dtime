import React, { createContext, useCallback, useContext, useRef, useState } from 'react';
import { Modal, Button } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';

const ConfirmContext = createContext(null);

/**
 * @typedef {Object} ConfirmOptions
 * @property {string} message - Body text (supports newlines).
 * @property {string} [title] - Dialog title.
 * @property {string} [confirmLabel] - Primary action label.
 * @property {string} [cancelLabel] - Cancel action label.
 * @property {'primary'|'danger'|'warning'} [variant] - Confirm button Bootstrap variant.
 */

export const useConfirm = () => {
    const context = useContext(ConfirmContext);
    if (!context) {
        throw new Error('useConfirm must be used within a ConfirmProvider');
    }
    return context.confirm;
};

const normalizeOptions = (optionsOrMessage) => {
    if (typeof optionsOrMessage === 'string') {
        return { message: optionsOrMessage };
    }
    return optionsOrMessage || { message: '' };
};

export const ConfirmProvider = ({ children }) => {
    const { t } = useTranslation();
    const [dialog, setDialog] = useState(null);
    const resolveRef = useRef(null);

    const confirm = useCallback((optionsOrMessage) => {
        const options = normalizeOptions(optionsOrMessage);
        return new Promise((resolve) => {
            resolveRef.current = resolve;
            setDialog(options);
        });
    }, []);

    const finish = useCallback((result) => {
        setDialog(null);
        const resolve = resolveRef.current;
        resolveRef.current = null;
        resolve?.(result);
    }, []);

    const handleHide = useCallback(() => finish(false), [finish]);

    const title = dialog?.title ?? t('common.confirm.title');
    const confirmLabel = dialog?.confirmLabel ?? t('common.confirm.confirm');
    const cancelLabel = dialog?.cancelLabel ?? t('common.buttons.cancel');
    const confirmVariant = dialog?.variant ?? 'primary';

    return (
        <ConfirmContext.Provider value={{ confirm }}>
            {children}
            <Modal
                show={dialog != null}
                onHide={handleHide}
                centered
                backdrop="static"
                style={{ zIndex: 1060 }}
            >
                <Modal.Header closeButton>
                    <Modal.Title>{title}</Modal.Title>
                </Modal.Header>
                <Modal.Body style={{ whiteSpace: 'pre-line' }}>{dialog?.message}</Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleHide}>
                        {cancelLabel}
                    </Button>
                    <Button variant={confirmVariant} onClick={() => finish(true)}>
                        {confirmLabel}
                    </Button>
                </Modal.Footer>
            </Modal>
        </ConfirmContext.Provider>
    );
};

export default ConfirmProvider;
