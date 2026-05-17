import React from 'react';
import { render } from '@testing-library/react';
import { ConfirmProvider } from '../components/ConfirmProvider';
import { ToastProvider } from '../components/Toast';

export function renderWithProviders(ui, options) {
    return render(
        <ConfirmProvider>
            <ToastProvider>
                {ui}
            </ToastProvider>
        </ConfirmProvider>,
        options
    );
}
