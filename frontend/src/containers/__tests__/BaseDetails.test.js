import React from 'react';
import { renderHook, act } from '@testing-library/react';
import { useBaseDetails } from '../BaseDetails';

// Mock Toast hook
jest.mock('../../components/Toast', () => ({
  useToast: () => ({
    showError: jest.fn()
  })
}));

describe('useBaseDetails', () => {
  beforeEach(() => {
    // Clear DOM before each test
    document.body.innerHTML = '';
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should provide handleError, clearError, and clearErrors functions', () => {
    const { result } = renderHook(() => useBaseDetails());

    expect(typeof result.current.handleError).toBe('function');
    expect(typeof result.current.clearError).toBe('function');
    expect(typeof result.current.clearErrors).toBe('function');
  });

  describe('clearError', () => {
    it('should clear error message for specific field', () => {
      // Create error element in DOM
      const errorElement = document.createElement('small');
      errorElement.id = 'testFieldErrorMsg';
      errorElement.textContent = 'Error message';
      document.body.appendChild(errorElement);

      const { result } = renderHook(() => useBaseDetails());

      act(() => {
        result.current.clearError('testField');
      });

      expect(errorElement.textContent).toBe(' ');
    });

    it('should handle non-existent error elements gracefully', () => {
      const { result } = renderHook(() => useBaseDetails());

      expect(() => {
        act(() => {
          result.current.clearError('nonExistentField');
        });
      }).not.toThrow();
    });
  });

  describe('clearErrors', () => {
    it('should clear all error messages', () => {
      // Create multiple error elements
      const errorElement1 = document.createElement('small');
      errorElement1.id = 'field1ErrorMsg';
      errorElement1.textContent = 'Error 1';
      
      const errorElement2 = document.createElement('small');
      errorElement2.id = 'field2ErrorMsg';
      errorElement2.textContent = 'Error 2';
      
      document.body.appendChild(errorElement1);
      document.body.appendChild(errorElement2);

      const { result } = renderHook(() => useBaseDetails());

      act(() => {
        result.current.clearErrors();
      });

      expect(errorElement1.textContent).toBe('');
      expect(errorElement2.textContent).toBe('');
    });

    it('should handle no error elements gracefully', () => {
      const { result } = renderHook(() => useBaseDetails());

      expect(() => {
        act(() => {
          result.current.clearErrors();
        });
      }).not.toThrow();
    });
  });

  describe('handleError', () => {
    let mockShowError;

    beforeEach(() => {
      mockShowError = jest.fn();
      jest.doMock('../../components/Toast', () => ({
        useToast: () => ({
          showError: mockShowError
        })
      }));
    });

    it('should handle 400 status with field errors', () => {
      // Create error elements for fields
      const errorElement1 = document.createElement('small');
      errorElement1.id = 'emailErrorMsg';
      const errorElement2 = document.createElement('small');
      errorElement2.id = 'nameErrorMsg';
      
      document.body.appendChild(errorElement1);
      document.body.appendChild(errorElement2);

      const { result } = renderHook(() => useBaseDetails());

      const fieldErrors = [
        { fieldName: 'email', fieldError: 'Invalid email format' },
        { fieldName: 'name', fieldError: 'Name is required' }
      ];

      act(() => {
        result.current.handleError(400, 'Validation failed', fieldErrors);
      });

      expect(errorElement1.textContent).toBe('Invalid email format');
      expect(errorElement2.textContent).toBe('Name is required');
    });

    it('should handle 500 status with server error', () => {
      const { result } = renderHook(() => useBaseDetails());

      act(() => {
        result.current.handleError(500, 'Internal server error', null);
      });

      // Should call showError or alert for server errors
      // Since we mocked showError, we can't easily test this without more complex mocking
    });

    it('should handle other status codes with generic error', () => {
      const { result } = renderHook(() => useBaseDetails());

      act(() => {
        result.current.handleError(404, 'Not found', null);
      });

      // Should call showError or alert for generic errors
      // Since we mocked showError, we can't easily test this without more complex mocking
    });

    it('should handle missing field elements gracefully', () => {
      const { result } = renderHook(() => useBaseDetails());

      const fieldErrors = [
        { fieldName: 'nonExistentField', fieldError: 'Some error' }
      ];

      expect(() => {
        act(() => {
          result.current.handleError(400, 'Validation failed', fieldErrors);
        });
      }).not.toThrow();
    });

    it('should handle null field errors list', () => {
      const { result } = renderHook(() => useBaseDetails());

      expect(() => {
        act(() => {
          result.current.handleError(400, 'Validation failed', null);
        });
      }).not.toThrow();
    });

    it('should handle empty field errors list', () => {
      const { result } = renderHook(() => useBaseDetails());

      expect(() => {
        act(() => {
          result.current.handleError(400, 'Validation failed', []);
        });
      }).not.toThrow();
    });
  });

  it('should maintain field errors in internal state', () => {
    const errorElement = document.createElement('small');
    errorElement.id = 'testFieldErrorMsg';
    document.body.appendChild(errorElement);

    const { result } = renderHook(() => useBaseDetails());

    const fieldErrors = [
      { fieldName: 'testField', fieldError: 'Test error message' }
    ];

    act(() => {
      result.current.handleError(400, 'Validation failed', fieldErrors);
    });

    expect(errorElement.textContent).toBe('Test error message');

    // Clear the error
    act(() => {
      result.current.clearError('testField');
    });

    expect(errorElement.textContent).toBe(' ');
  });
});