import { useState, useCallback } from 'react';
import { useToast } from '../components/ToastProvider';

const useApi = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { showError, showSuccess } = useToast();

  const execute = useCallback(async (apiCall, options = {}) => {
    const {
      onSuccess,
      onError,
      showErrorToast = true,
      loadingDelay = 0
    } = options;

    try {
      setLoading(true);
      setError(null);

      // Add a small delay for loading state if specified
      if (loadingDelay > 0) {
        await new Promise(resolve => setTimeout(resolve, loadingDelay));
      }

      const result = await apiCall();

      if (onSuccess) {
        onSuccess(result);
      }

      return result;
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'An unexpected error occurred';
      setError(errorMessage);

      if (showErrorToast) {
        showError(errorMessage);
      }

      if (onError) {
        onError(err);
      } else {
        console.error('API Error:', err);
      }

      throw err;
    } finally {
      setLoading(false);
    }
  }, [showError]);

  const executeWithToast = useCallback(async (apiCall, successMessage, options = {}) => {
    return execute(apiCall, {
      ...options,
      onSuccess: (result) => {
        if (successMessage) {
          showSuccess(successMessage);
        }
        if (options.onSuccess) {
          options.onSuccess(result);
        }
      }
    });
  }, [execute, showSuccess]);

  const reset = useCallback(() => {
    setLoading(false);
    setError(null);
  }, []);

  return {
    loading,
    error,
    execute,
    executeWithToast,
    reset
  };
};

export default useApi;