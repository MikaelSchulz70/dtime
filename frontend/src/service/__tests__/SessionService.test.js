import axios from 'axios';
import SessionService from '../SessionService';

// Mock axios
jest.mock('axios');
const mockedAxios = axios;

describe('SessionService', () => {
  let sessionService;

  beforeEach(() => {
    sessionService = new SessionService();
    jest.clearAllMocks();
  });

  describe('getSessionInfo', () => {
    it('should fetch session info successfully', async () => {
      const mockSessionData = {
        id: 'session123',
        userId: 1,
        username: 'testuser',
        roles: ['USER'],
        loginTime: '2023-01-01T10:00:00Z',
        expiresAt: '2023-01-01T18:00:00Z'
      };
      mockedAxios.get.mockResolvedValue({ data: mockSessionData });

      const result = await sessionService.getSessionInfo();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/session', { timeout: 10000 });
      expect(result.data).toEqual(mockSessionData);
    });

    it('should handle unauthorized session requests', async () => {
      const mockError = { 
        response: { 
          status: 401, 
          data: { message: 'Session expired or invalid' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(sessionService.getSessionInfo()).rejects.toEqual(mockError);
    });

    it('should handle timeout errors', async () => {
      const mockError = { 
        code: 'ECONNABORTED', 
        message: 'timeout of 10000ms exceeded' 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(sessionService.getSessionInfo()).rejects.toEqual(mockError);
    });

    it('should use correct timeout configuration', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await sessionService.getSessionInfo();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/session', { timeout: 10000 });
    });

    it('should handle network errors', async () => {
      const mockError = { 
        code: 'ECONNREFUSED', 
        message: 'Connection refused' 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(sessionService.getSessionInfo()).rejects.toEqual(mockError);
    });

    it('should handle server errors', async () => {
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'Internal server error' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(sessionService.getSessionInfo()).rejects.toEqual(mockError);
    });
  });

  describe('logout', () => {
    it('should logout successfully', async () => {
      const mockResponse = { 
        success: true, 
        message: 'Logged out successfully' 
      };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await sessionService.logout();

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/session/logout');
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle logout errors', async () => {
      const mockError = { 
        response: { 
          status: 400, 
          data: { message: 'Already logged out' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(sessionService.logout()).rejects.toEqual(mockError);
    });

    it('should handle server errors during logout', async () => {
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'Server error during logout' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(sessionService.logout()).rejects.toEqual(mockError);
    });

    it('should handle network errors during logout', async () => {
      const mockError = { 
        code: 'ECONNREFUSED', 
        message: 'Connection refused' 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(sessionService.logout()).rejects.toEqual(mockError);
    });

    it('should not send any data in logout request', async () => {
      mockedAxios.post.mockResolvedValue({ data: { success: true } });

      await sessionService.logout();

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/session/logout');
      expect(mockedAxios.post).toHaveBeenCalledTimes(1);
    });
  });

  describe('getCurrentUser', () => {
    it('should fetch current user successfully', async () => {
      const mockUserData = {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
        roles: ['USER', 'ADMIN'],
        preferences: {
          theme: 'dark',
          language: 'en'
        },
        lastLogin: '2023-01-01T09:00:00Z'
      };
      mockedAxios.get.mockResolvedValue({ data: mockUserData });

      const result = await sessionService.getCurrentUser();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/session/user');
      expect(result.data).toEqual(mockUserData);
    });

    it('should handle unauthorized user requests', async () => {
      const mockError = { 
        response: { 
          status: 401, 
          data: { message: 'User not authenticated' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(sessionService.getCurrentUser()).rejects.toEqual(mockError);
    });

    it('should handle forbidden user requests', async () => {
      const mockError = { 
        response: { 
          status: 403, 
          data: { message: 'Access denied' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(sessionService.getCurrentUser()).rejects.toEqual(mockError);
    });

    it('should handle user not found errors', async () => {
      const mockError = { 
        response: { 
          status: 404, 
          data: { message: 'User not found' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(sessionService.getCurrentUser()).rejects.toEqual(mockError);
    });

    it('should handle server errors', async () => {
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'Internal server error' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(sessionService.getCurrentUser()).rejects.toEqual(mockError);
    });

    it('should handle minimal user data', async () => {
      const mockUserData = {
        id: 1,
        username: 'minimaluser'
      };
      mockedAxios.get.mockResolvedValue({ data: mockUserData });

      const result = await sessionService.getCurrentUser();

      expect(result.data).toEqual(mockUserData);
    });

    it('should handle network timeouts', async () => {
      const mockError = { 
        code: 'ECONNABORTED', 
        message: 'timeout exceeded' 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(sessionService.getCurrentUser()).rejects.toEqual(mockError);
    });
  });

  describe('BaseService inheritance', () => {
    it('should inherit BaseService methods', () => {
      expect(sessionService).toBeInstanceOf(SessionService);
      expect(typeof sessionService.getAll).toBe('function');
      expect(typeof sessionService.get).toBe('function');
      expect(typeof sessionService.create).toBe('function');
      expect(typeof sessionService.update).toBe('function');
      expect(typeof sessionService.delete).toBe('function');
      expect(typeof sessionService.validate).toBe('function');
    });

    it('should not override BaseService constructor URL', () => {
      // SessionService doesn't call super() with a URL, so url should be undefined
      expect(sessionService.url).toBeUndefined();
    });
  });

  describe('Integration scenarios', () => {
    it('should handle session validation workflow', async () => {
      // First check session
      const mockSession = { id: 'session123', userId: 1, valid: true };
      mockedAxios.get.mockResolvedValueOnce({ data: mockSession });

      const sessionInfo = await sessionService.getSessionInfo();
      expect(sessionInfo.data).toEqual(mockSession);

      // Then get user details
      const mockUser = { id: 1, username: 'testuser' };
      mockedAxios.get.mockResolvedValueOnce({ data: mockUser });

      const userInfo = await sessionService.getCurrentUser();
      expect(userInfo.data).toEqual(mockUser);

      // Finally logout
      const mockLogout = { success: true };
      mockedAxios.post.mockResolvedValueOnce({ data: mockLogout });

      const logoutResult = await sessionService.logout();
      expect(logoutResult.data).toEqual(mockLogout);
    });

    it('should handle expired session scenario', async () => {
      const expiredError = { 
        response: { 
          status: 401, 
          data: { message: 'Session expired' } 
        } 
      };
      
      // Session info fails
      mockedAxios.get.mockRejectedValueOnce(expiredError);
      await expect(sessionService.getSessionInfo()).rejects.toEqual(expiredError);

      // User info also fails
      mockedAxios.get.mockRejectedValueOnce(expiredError);
      await expect(sessionService.getCurrentUser()).rejects.toEqual(expiredError);

      // But logout might still work
      mockedAxios.post.mockResolvedValueOnce({ data: { success: true } });
      const logoutResult = await sessionService.logout();
      expect(logoutResult.data.success).toBe(true);
    });
  });

  describe('HTTP method usage', () => {
    it('should use GET for reading operations', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await sessionService.getSessionInfo();
      await sessionService.getCurrentUser();

      expect(mockedAxios.get).toHaveBeenCalledTimes(2);
      expect(mockedAxios.post).not.toHaveBeenCalled();
    });

    it('should use POST for logout operation', async () => {
      mockedAxios.post.mockResolvedValue({ data: {} });

      await sessionService.logout();

      expect(mockedAxios.post).toHaveBeenCalledTimes(1);
      expect(mockedAxios.get).not.toHaveBeenCalled();
    });
  });

  describe('Error response structure', () => {
    it('should preserve error response structure', async () => {
      const detailedError = {
        response: {
          status: 400,
          statusText: 'Bad Request',
          headers: { 'content-type': 'application/json' },
          data: {
            message: 'Invalid session',
            code: 'SESSION_INVALID',
            timestamp: '2023-01-01T10:00:00Z'
          }
        }
      };

      mockedAxios.get.mockRejectedValue(detailedError);

      try {
        await sessionService.getSessionInfo();
      } catch (error) {
        expect(error.response.status).toBe(400);
        expect(error.response.data.code).toBe('SESSION_INVALID');
        expect(error.response.data.message).toBe('Invalid session');
      }
    });
  });
});