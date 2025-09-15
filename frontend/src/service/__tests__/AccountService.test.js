import axios from 'axios';
import AccountService from '../AccountService';

// Mock axios
jest.mock('axios');
const mockedAxios = axios;

describe('AccountService', () => {
  let accountService;

  beforeEach(() => {
    accountService = new AccountService();
    jest.clearAllMocks();
  });

  describe('Constructor', () => {
    it('should initialize with correct base URL', () => {
      expect(accountService.url).toBe('/api/account');
    });
  });

  describe('getAll', () => {
    it('should fetch all accounts successfully', async () => {
      const mockResponse = { data: [{ id: 1, name: 'Test Account' }] };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await accountService.getAll();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/account');
      expect(result).toEqual(mockResponse);
    });

    it('should handle errors when fetching all accounts', async () => {
      const mockError = new Error('Network error');
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(accountService.getAll()).rejects.toThrow('Network error');
    });
  });

  describe('get', () => {
    it('should fetch account by id successfully', async () => {
      const accountId = 1;
      const mockResponse = { data: { id: 1, name: 'Test Account' } };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await accountService.get(accountId);

      expect(mockedAxios.get).toHaveBeenCalledWith(`/api/account/${accountId}`);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('create', () => {
    it('should create account successfully', async () => {
      const accountData = {
        name: 'New Account',
        description: 'Test description'
      };
      const mockResponse = { data: { id: 1, ...accountData } };
      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await accountService.create(accountData);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/account',
        JSON.stringify(accountData),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });
  });

  describe('update', () => {
    it('should update account successfully', async () => {
      const accountData = {
        id: 1,
        name: 'Updated Account',
        description: 'Updated description'
      };
      const mockResponse = { data: accountData };
      mockedAxios.put.mockResolvedValue(mockResponse);

      const result = await accountService.update(accountData);

      expect(mockedAxios.put).toHaveBeenCalledWith(
        '/api/account',
        JSON.stringify(accountData),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });
  });

  describe('delete', () => {
    it('should delete account successfully', async () => {
      const accountId = 1;
      const mockResponse = { data: { success: true } };
      mockedAxios.delete.mockResolvedValue(mockResponse);

      const result = await accountService.delete(accountId);

      expect(mockedAxios.delete).toHaveBeenCalledWith(
        `/api/account/${accountId}`,
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });
  });

  describe('validate', () => {
    it('should validate account field successfully', async () => {
      const accountId = 1;
      const field = 'name';
      const value = 'Account Name';
      const mockResponse = { data: { valid: true } };
      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await accountService.validate(accountId, field, value);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/account/validate',
        JSON.stringify({ id: accountId, name: field, value: value }),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getByStatus', () => {
    it('should fetch accounts by status', async () => {
      const status = 'ACTIVE';
      const mockResponse = { data: [{ id: 1, name: 'Active Account' }] };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await accountService.getByStatus(status);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/account?active=ACTIVE');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getAllPaged', () => {
    it('should fetch paged accounts with all parameters', async () => {
      const mockResponse = { 
        data: { 
          content: [{ id: 1, name: 'Test Account' }],
          totalPages: 1,
          totalElements: 1
        }
      };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await accountService.getAllPaged(0, 10, 'name', 'ASC', true, 'Test');

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/account/paged?page=0&size=10&sort=name&direction=ASC&active=true&name=Test',
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });

    it('should fetch paged accounts with minimal parameters', async () => {
      const mockResponse = { 
        data: { 
          content: [{ id: 1, name: 'Test Account' }],
          totalPages: 1,
          totalElements: 1
        }
      };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await accountService.getAllPaged();

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/account/paged?',
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });

    it('should handle null active parameter correctly', async () => {
      const mockResponse = { data: { content: [], totalPages: 0, totalElements: 0 } };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await accountService.getAllPaged(0, 10, null, null, null, null);

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/account/paged?page=0&size=10',
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });

    it('should handle empty string active parameter correctly', async () => {
      const mockResponse = { data: { content: [], totalPages: 0, totalElements: 0 } };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await accountService.getAllPaged(0, 10, null, null, '', null);

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/account/paged?page=0&size=10',
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });
  });

  describe('Error Handling', () => {
    it('should handle network errors gracefully', async () => {
      const mockError = new Error('Network Error');
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(accountService.getAll()).rejects.toThrow('Network Error');
    });

    it('should handle server errors gracefully', async () => {
      const mockError = { response: { status: 500, data: { message: 'Internal Server Error' } } };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(accountService.getAll()).rejects.toEqual(mockError);
    });
  });
});