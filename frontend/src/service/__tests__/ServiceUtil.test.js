import { Headers, handleApiError, createApiConfig } from '../ServiceUtil';

// Mock DOM methods
const mockDocument = {
  cookie: '',
  querySelector: jest.fn()
};

// Mock window.location.reload
const mockReload = jest.fn();
Object.defineProperty(window, 'location', {
  value: { reload: mockReload },
  writable: true
});

describe('ServiceUtil', () => {
  beforeEach(() => {
    // Reset mocks
    jest.clearAllMocks();
    mockDocument.cookie = '';
    mockDocument.querySelector.mockReturnValue(null);
    
    // Mock document methods
    Object.defineProperty(document, 'cookie', {
      get: () => mockDocument.cookie,
      configurable: true
    });
    
    document.querySelector = mockDocument.querySelector;
  });

  describe('Headers', () => {
    it('should return basic headers when no CSRF token is available', () => {
      const result = Headers();
      
      expect(result).toEqual({
        headers: { 'Content-Type': 'application/json' }
      });
    });

    it('should include XSRF token from cookie when available', () => {
      mockDocument.cookie = 'XSRF-TOKEN=abc123; other=value';
      
      const result = Headers();
      
      expect(result).toEqual({
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': 'abc123'
        }
      });
    });

    it('should decode URL-encoded XSRF token from cookie', () => {
      mockDocument.cookie = 'XSRF-TOKEN=abc%2B123%3D%3D; other=value';
      
      const result = Headers();
      
      expect(result).toEqual({
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': 'abc+123=='
        }
      });
    });

    it('should use CSRF token from meta tag when cookie is not available', () => {
      const mockMetaElement = {
        getAttribute: jest.fn().mockReturnValue('meta-token-123')
      };
      mockDocument.querySelector.mockReturnValue(mockMetaElement);
      
      const result = Headers();
      
      expect(mockDocument.querySelector).toHaveBeenCalledWith("meta[name='_csrf']");
      expect(mockMetaElement.getAttribute).toHaveBeenCalledWith('content');
      expect(result).toEqual({
        headers: {
          'Content-Type': 'application/json',
          'X-CSRF-TOKEN': 'meta-token-123'
        }
      });
    });

    it('should return basic headers when meta tag exists but has no content', () => {
      const mockMetaElement = {
        getAttribute: jest.fn().mockReturnValue(null)
      };
      mockDocument.querySelector.mockReturnValue(mockMetaElement);
      
      const result = Headers();
      
      expect(result).toEqual({
        headers: { 'Content-Type': 'application/json' }
      });
    });

    it('should prioritize cookie over meta tag', () => {
      mockDocument.cookie = 'XSRF-TOKEN=cookie-token; other=value';
      const mockMetaElement = {
        getAttribute: jest.fn().mockReturnValue('meta-token')
      };
      mockDocument.querySelector.mockReturnValue(mockMetaElement);
      
      const result = Headers();
      
      expect(result).toEqual({
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': 'cookie-token'
        }
      });
      expect(mockMetaElement.getAttribute).not.toHaveBeenCalled();
    });
  });

  describe('handleApiError', () => {
    it('should reload page on 401 unauthorized error', () => {
      const error = {
        response: {
          status: 401,
          data: { message: 'Unauthorized' }
        }
      };
      
      const result = handleApiError(error);
      
      expect(mockReload).toHaveBeenCalled();
      expect(result).toBeUndefined();
    });

    it('should return permission message for 403 forbidden error', () => {
      const error = {
        response: {
          status: 403,
          data: { message: 'Forbidden' }
        }
      };
      
      const result = handleApiError(error);
      
      expect(result).toBe('You do not have permission to perform this action.');
      expect(mockReload).not.toHaveBeenCalled();
    });

    it('should return server error message for 500+ errors', () => {
      const error = {
        response: {
          status: 500,
          data: { message: 'Internal Server Error' }
        }
      };
      
      const result = handleApiError(error);
      
      expect(result).toBe('A server error occurred. Please try again later.');
    });

    it('should return custom message for 500+ errors when provided', () => {
      const error = {
        response: {
          status: 502,
          data: {}
        }
      };
      const customMessage = 'Custom server error message';
      
      const result = handleApiError(error, customMessage);
      
      expect(result).toBe(customMessage);
    });

    it('should extract message from response data', () => {
      const error = {
        response: {
          status: 400,
          data: { message: 'Validation failed' }
        }
      };
      
      const result = handleApiError(error);
      
      expect(result).toBe('Validation failed');
    });

    it('should extract error from response data when message is not available', () => {
      const error = {
        response: {
          status: 400,
          data: { error: 'Invalid input' }
        }
      };
      
      const result = handleApiError(error);
      
      expect(result).toBe('Invalid input');
    });

    it('should return status-based message when no data message available', () => {
      const error = {
        response: {
          status: 404,
          data: {}
        }
      };
      
      const result = handleApiError(error);
      
      expect(result).toBe('Request failed with status 404');
    });

    it('should return custom message when no data message available', () => {
      const error = {
        response: {
          status: 404,
          data: {}
        }
      };
      const customMessage = 'Resource not found';
      
      const result = handleApiError(error, customMessage);
      
      expect(result).toBe(customMessage);
    });

    it('should handle network errors', () => {
      const error = {
        request: {}
      };
      
      const result = handleApiError(error);
      
      expect(result).toBe('Network error. Please check your connection and try again.');
    });

    it('should handle generic errors with message', () => {
      const error = {
        message: 'Something went wrong'
      };
      
      const result = handleApiError(error);
      
      expect(result).toBe('Something went wrong');
    });

    it('should handle generic errors with custom message', () => {
      const error = {};
      const customMessage = 'Custom error message';
      
      const result = handleApiError(error, customMessage);
      
      expect(result).toBe(customMessage);
    });

    it('should handle completely unknown errors', () => {
      const error = {};
      
      const result = handleApiError(error);
      
      expect(result).toBe('An unexpected error occurred.');
    });

    it('should log errors to console', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      const error = { message: 'Test error' };
      
      handleApiError(error);
      
      expect(consoleSpy).toHaveBeenCalledWith('API Error:', error);
      
      consoleSpy.mockRestore();
    });
  });

  describe('createApiConfig', () => {
    it('should create config with default headers', () => {
      const result = createApiConfig();
      
      expect(result).toEqual({
        headers: { 'Content-Type': 'application/json' },
        timeout: 30000
      });
    });

    it('should merge additional headers with default headers', () => {
      const additionalHeaders = {
        'Authorization': 'Bearer token123',
        'Custom-Header': 'custom-value'
      };
      
      const result = createApiConfig(additionalHeaders);
      
      expect(result).toEqual({
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer token123',
          'Custom-Header': 'custom-value'
        },
        timeout: 30000
      });
    });

    it('should override default headers with additional headers', () => {
      const additionalHeaders = {
        'Content-Type': 'multipart/form-data'
      };
      
      const result = createApiConfig(additionalHeaders);
      
      expect(result).toEqual({
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 30000
      });
    });

    it('should include CSRF token when available', () => {
      mockDocument.cookie = 'XSRF-TOKEN=test-token';
      const additionalHeaders = { 'Authorization': 'Bearer token123' };
      
      const result = createApiConfig(additionalHeaders);
      
      expect(result).toEqual({
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': 'test-token',
          'Authorization': 'Bearer token123'
        },
        timeout: 30000
      });
    });

    it('should handle empty additional headers object', () => {
      const result = createApiConfig({});
      
      expect(result).toEqual({
        headers: { 'Content-Type': 'application/json' },
        timeout: 30000
      });
    });
  });
});