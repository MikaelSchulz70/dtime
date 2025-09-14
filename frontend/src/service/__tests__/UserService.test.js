import axios from 'axios';
import UserService from '../UserService';

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

  describe('create', () => {
    it('should create user successfully', async () => {
      const mockResponse = { data: { id: 1, created: true } };
      const userData = { 
        firstName: 'John', 
        lastName: 'Doe', 
        email: 'john@example.com',
        userRole: 'USER',
        activationStatus: 'ACTIVE'
      };
      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await userService.create(userData);

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/users', JSON.stringify(userData), {'headers': {'Content-Type': 'application/json'}});
      expect(result).toEqual(mockResponse);
    });
  });

  describe('update', () => {
    it('should update user successfully', async () => {
      const mockResponse = { data: { id: 1, updated: true } };
      const userData = { 
        id: 1,
        firstName: 'John', 
        lastName: 'Doe', 
        email: 'john@example.com'
      };
      mockedAxios.put.mockResolvedValue(mockResponse);

      const result = await userService.update(userData);

      expect(mockedAxios.put).toHaveBeenCalledWith('/api/users', JSON.stringify(userData), {'headers': {'Content-Type': 'application/json'}});
      expect(result).toEqual(mockResponse);
    });
  });

  describe('delete', () => {
    it('should delete user successfully', async () => {
      const mockResponse = { data: { deleted: true } };
      const userId = 1;
      mockedAxios.delete.mockResolvedValue(mockResponse);

      const result = await userService.delete(userId);

      expect(mockedAxios.delete).toHaveBeenCalledWith(`/api/users/${userId}`, {'headers': {'Content-Type': 'application/json'}});
      expect(result).toEqual(mockResponse);
    });
  });

  describe('validate', () => {
    it('should validate user field successfully', async () => {
      const mockResponse = { data: { valid: true } };
      const userId = 1;
      const field = 'email';
      const value = 'john@example.com';
      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await userService.validate(userId, field, value);

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/users/validate', JSON.stringify({id: userId, name: field, value: value}), {'headers': {'Content-Type': 'application/json'}});
      expect(result).toEqual(mockResponse);
    });
  });
});