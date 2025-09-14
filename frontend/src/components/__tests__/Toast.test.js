import React from 'react';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ToastProvider, useToast } from '../Toast';

// Test component that uses the toast hook
const TestComponent = () => {
  const { showSuccess, showError, showWarning, showInfo, toasts } = useToast();

  return (
    <div>
      <button onClick={() => showSuccess('Success message')}>Show Success</button>
      <button onClick={() => showError('Error message')}>Show Error</button>
      <button onClick={() => showWarning('Warning message')}>Show Warning</button>
      <button onClick={() => showInfo('Info message')}>Show Info</button>
      <div data-testid="toast-count">{toasts.length}</div>
    </div>
  );
};

describe('Toast System', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  describe('ToastProvider', () => {
    it('should provide toast context to children', () => {
      render(
        <ToastProvider>
          <TestComponent />
        </ToastProvider>
      );

      expect(screen.getByRole('button', { name: /show success/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /show error/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /show warning/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /show info/i })).toBeInTheDocument();
    });

    it('should throw error when useToast is used outside provider', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      
      expect(() => {
        render(<TestComponent />);
      }).toThrow('useToast must be used within a ToastProvider');

      consoleSpy.mockRestore();
    });
  });

  describe('Toast Display', () => {
    it('should display success toast with correct styling', async () => {
      const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

      render(
        <ToastProvider>
          <TestComponent />
        </ToastProvider>
      );

      const successButton = screen.getByRole('button', { name: /show success/i });
      await user.click(successButton);

      expect(screen.getByText('Success')).toBeInTheDocument();
      expect(screen.getByText('Success message')).toBeInTheDocument();
      expect(screen.getByText('✅')).toBeInTheDocument();

      // Check CSS classes
      const toast = screen.getByRole('alert');
      expect(toast).toHaveClass('toast', 'show', 'border-success');
    });

    it('should display error toast with correct styling', async () => {
      const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

      render(
        <ToastProvider>
          <TestComponent />
        </ToastProvider>
      );

      const errorButton = screen.getByRole('button', { name: /show error/i });
      await user.click(errorButton);

      expect(screen.getByText('Error')).toBeInTheDocument();
      expect(screen.getByText('Error message')).toBeInTheDocument();
      expect(screen.getByText('❌')).toBeInTheDocument();

      const toast = screen.getByRole('alert');
      expect(toast).toHaveClass('toast', 'show', 'border-danger');
    });

    it('should display warning toast with correct styling', async () => {
      const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

      render(
        <ToastProvider>
          <TestComponent />
        </ToastProvider>
      );

      const warningButton = screen.getByRole('button', { name: /show warning/i });
      await user.click(warningButton);

      expect(screen.getByText('Warning')).toBeInTheDocument();
      expect(screen.getByText('Warning message')).toBeInTheDocument();
      expect(screen.getByText('⚠️')).toBeInTheDocument();

      const toast = screen.getByRole('alert');
      expect(toast).toHaveClass('toast', 'show', 'border-warning');
    });

    it('should display info toast with correct styling', async () => {
      const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

      render(
        <ToastProvider>
          <TestComponent />
        </ToastProvider>
      );

      const infoButton = screen.getByRole('button', { name: /show info/i });
      await user.click(infoButton);

      expect(screen.getByText('Info')).toBeInTheDocument();
      expect(screen.getByText('Info message')).toBeInTheDocument();
      expect(screen.getByText('ℹ️')).toBeInTheDocument();

      const toast = screen.getByRole('alert');
      expect(toast).toHaveClass('toast', 'show', 'border-info');
    });
  });

  describe('Toast Management', () => {
    it('should display multiple toasts simultaneously', async () => {
      const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

      render(
        <ToastProvider>
          <TestComponent />
        </ToastProvider>
      );

      await user.click(screen.getByRole('button', { name: /show success/i }));
      await user.click(screen.getByRole('button', { name: /show error/i }));
      await user.click(screen.getByRole('button', { name: /show warning/i }));

      expect(screen.getByText('Success message')).toBeInTheDocument();
      expect(screen.getByText('Error message')).toBeInTheDocument();
      expect(screen.getByText('Warning message')).toBeInTheDocument();
      expect(screen.getByTestId('toast-count')).toHaveTextContent('3');
    });

    it('should auto-remove toasts after default duration', async () => {
      const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

      render(
        <ToastProvider>
          <TestComponent />
        </ToastProvider>
      );

      await user.click(screen.getByRole('button', { name: /show success/i }));

      expect(screen.getByText('Success message')).toBeInTheDocument();
      expect(screen.getByTestId('toast-count')).toHaveTextContent('1');

      // Advance time by 5 seconds (default duration)
      act(() => {
        jest.advanceTimersByTime(5000);
      });

      await waitFor(() => {
        expect(screen.queryByText('Success message')).not.toBeInTheDocument();
      });

      expect(screen.getByTestId('toast-count')).toHaveTextContent('0');
    });

    it('should remove toast when close button is clicked', async () => {
      const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

      render(
        <ToastProvider>
          <TestComponent />
        </ToastProvider>
      );

      await user.click(screen.getByRole('button', { name: /show success/i }));

      expect(screen.getByText('Success message')).toBeInTheDocument();

      const closeButton = screen.getByRole('button', { name: /close/i });
      await user.click(closeButton);

      await waitFor(() => {
        expect(screen.queryByText('Success message')).not.toBeInTheDocument();
      });

      expect(screen.getByTestId('toast-count')).toHaveTextContent('0');
    });

    it('should handle custom duration', async () => {
      const TestCustomDuration = () => {
        const { showSuccess } = useToast();
        return (
          <button onClick={() => showSuccess('Custom duration', 1000)}>
            Show Custom Duration
          </button>
        );
      };

      const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

      render(
        <ToastProvider>
          <TestCustomDuration />
        </ToastProvider>
      );

      await user.click(screen.getByRole('button', { name: /show custom duration/i }));

      expect(screen.getByText('Custom duration')).toBeInTheDocument();

      // Advance time by 1 second (custom duration)
      act(() => {
        jest.advanceTimersByTime(1000);
      });

      await waitFor(() => {
        expect(screen.queryByText('Custom duration')).not.toBeInTheDocument();
      });
    });

    it('should not auto-remove toasts with duration 0', async () => {
      const TestPersistentToast = () => {
        const { showSuccess } = useToast();
        return (
          <button onClick={() => showSuccess('Persistent toast', 0)}>
            Show Persistent
          </button>
        );
      };

      const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

      render(
        <ToastProvider>
          <TestPersistentToast />
        </ToastProvider>
      );

      await user.click(screen.getByRole('button', { name: /show persistent/i }));

      expect(screen.getByText('Persistent toast')).toBeInTheDocument();

      // Advance time significantly
      act(() => {
        jest.advanceTimersByTime(10000);
      });

      // Toast should still be there
      expect(screen.getByText('Persistent toast')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA attributes', async () => {
      const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

      render(
        <ToastProvider>
          <TestComponent />
        </ToastProvider>
      );

      await user.click(screen.getByRole('button', { name: /show success/i }));

      const toast = screen.getByRole('alert');
      expect(toast).toHaveAttribute('aria-live', 'assertive');
      expect(toast).toHaveAttribute('aria-atomic', 'true');

      const closeButton = screen.getByRole('button', { name: /close/i });
      expect(closeButton).toHaveAttribute('aria-label', 'Close');
    });
  });

  describe('Toast Container', () => {
    it('should not render when no toasts are present', () => {
      render(
        <ToastProvider>
          <div>No toasts</div>
        </ToastProvider>
      );

      const toastContainer = document.querySelector('.toast-container');
      expect(toastContainer).not.toBeInTheDocument();
    });

    it('should have proper positioning classes', async () => {
      const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

      render(
        <ToastProvider>
          <TestComponent />
        </ToastProvider>
      );

      await user.click(screen.getByRole('button', { name: /show success/i }));

      const toastContainer = document.querySelector('.toast-container');
      expect(toastContainer).toHaveClass('position-fixed', 'top-0', 'end-0', 'p-3');
      expect(toastContainer).toHaveStyle({ zIndex: '1055' });
    });
  });
});