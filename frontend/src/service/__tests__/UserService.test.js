import axios from 'axios';
import UserService from '../UserService';
import { Headers } from '../ServiceUtil';

jest.mock('axios');
const mockedAxios = axios;

describe('UserService', () => {
  let userService;

  beforeEach(() => {
    userService = new UserService();
    jest.clearAllMocks();
  });

  describe('getAll', () => {
    it('should fetch all users successfully', async () => {
      const mockResponse = {
        data: [
          { id: 1, firstName: 'John', lastName: 'Doe', email: 'john@example.com' },
          { id: 2, firstName: 'Jane', lastName: 'Smith', email: 'jane@example.com' }
        ]
      };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await userService.getAll();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/users');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('get', () => {
    it('should fetch user by id successfully', async () => {
      const mockResponse = {
        data: { id: 1, firstName: 'John', lastName: 'Doe', email: 'john@example.com' }
      };
      const userId = 1;
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await userService.get(userId);

      expect(mockedAxios.get).toHaveBeenCalledWith(`/api/users/${userId}`);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('deactivate', () => {
    it('should deactivate user successfully', async () => {
      const mockResponse = { status: 200 };
      const userId = 1;
      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await userService.deactivate(userId);

      expect(mockedAxios.post).toHaveBeenCalledWith(`/api/users/${userId}/deactivate`, null, Headers());
      expect(result).toEqual(mockResponse);
    });
  });

  describe('activate', () => {
    it('should activate user successfully', async () => {
      const mockResponse = { status: 200 };
      const userId = 1;
      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await userService.activate(userId);

      expect(mockedAxios.post).toHaveBeenCalledWith(`/api/users/${userId}/activate`, null, Headers());
      expect(result).toEqual(mockResponse);
    });
  });
});
