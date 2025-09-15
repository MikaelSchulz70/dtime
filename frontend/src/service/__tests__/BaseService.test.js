import axios from 'axios';
import { BaseService } from '../BaseService';
import { Headers } from '../ServiceUtil';

// Mock axios
jest.mock('axios');
const mockedAxios = axios;

// Mock ServiceUtil
jest.mock('../ServiceUtil', () => ({
  Headers: jest.fn(() => ({ headers: { 'Content-Type': 'application/json' } }))
}));

describe('BaseService', () => {
  let baseService;
  const TEST_URL = '/api/test';

  beforeEach(() => {
    baseService = new BaseService(TEST_URL);
    jest.clearAllMocks();
  });

  describe('constructor', () => {
    it('should initialize with the provided URL', () => {
      expect(baseService.url).toBe(TEST_URL);
    });

    it('should handle different URL formats', () => {
      const service1 = new BaseService('/api/users');
      const service2 = new BaseService('/api/reports/');
      
      expect(service1.url).toBe('/api/users');
      expect(service2.url).toBe('/api/reports/');
    });
  });

  describe('getAll', () => {
    it('should fetch all entities successfully', async () => {
      const mockData = [{ id: 1, name: 'Test 1' }, { id: 2, name: 'Test 2' }];
      mockedAxios.get.mockResolvedValue({ data: mockData });

      const result = await baseService.getAll();

      expect(mockedAxios.get).toHaveBeenCalledWith(TEST_URL);
      expect(result.data).toEqual(mockData);
    });

    it('should handle errors when fetching all entities', async () => {
      const mockError = new Error('Network error');
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(baseService.getAll()).rejects.toThrow('Network error');
    });
  });

  describe('getByStatus', () => {
    it('should fetch entities by active status true', async () => {
      const mockData = [{ id: 1, name: 'Active Entity', active: true }];
      mockedAxios.get.mockResolvedValue({ data: mockData });

      const result = await baseService.getByStatus(true);

      expect(mockedAxios.get).toHaveBeenCalledWith(`${TEST_URL}?active=true`);
      expect(result.data).toEqual(mockData);
    });

    it('should fetch entities by active status false', async () => {
      const mockData = [{ id: 2, name: 'Inactive Entity', active: false }];
      mockedAxios.get.mockResolvedValue({ data: mockData });

      const result = await baseService.getByStatus(false);

      expect(mockedAxios.get).toHaveBeenCalledWith(`${TEST_URL}?active=false`);
      expect(result.data).toEqual(mockData);
    });

    it('should handle string boolean values', async () => {
      const mockData = [];
      mockedAxios.get.mockResolvedValue({ data: mockData });

      await baseService.getByStatus('true');

      expect(mockedAxios.get).toHaveBeenCalledWith(`${TEST_URL}?active=true`);
    });
  });

  describe('get', () => {
    it('should fetch entity by ID successfully', async () => {
      const entityId = 123;
      const mockData = { id: entityId, name: 'Test Entity' };
      mockedAxios.get.mockResolvedValue({ data: mockData });

      const result = await baseService.get(entityId);

      expect(mockedAxios.get).toHaveBeenCalledWith(`${TEST_URL}/${entityId}`);
      expect(result.data).toEqual(mockData);
    });

    it('should handle string ID values', async () => {
      const entityId = '456';
      const mockData = { id: 456, name: 'Test Entity' };
      mockedAxios.get.mockResolvedValue({ data: mockData });

      const result = await baseService.get(entityId);

      expect(mockedAxios.get).toHaveBeenCalledWith(`${TEST_URL}/${entityId}`);
      expect(result.data).toEqual(mockData);
    });

    it('should handle errors when fetching entity by ID', async () => {
      const mockError = { response: { status: 404, data: { message: 'Not found' } } };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(baseService.get(999)).rejects.toEqual(mockError);
    });
  });

  describe('delete', () => {
    it('should delete entity by ID successfully', async () => {
      const entityId = 123;
      const mockResponse = { success: true };
      mockedAxios.delete.mockResolvedValue({ data: mockResponse });

      const result = await baseService.delete(entityId);

      expect(mockedAxios.delete).toHaveBeenCalledWith(
        `${TEST_URL}/${entityId}`,
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle errors during delete', async () => {
      const entityId = 123;
      const mockError = { response: { status: 404, data: { message: 'Entity not found' } } };
      mockedAxios.delete.mockRejectedValue(mockError);

      await expect(baseService.delete(entityId)).rejects.toEqual(mockError);
    });

    it('should handle authorization errors during delete', async () => {
      const entityId = 123;
      const mockError = { response: { status: 403, data: { message: 'Forbidden' } } };
      mockedAxios.delete.mockRejectedValue(mockError);

      await expect(baseService.delete(entityId)).rejects.toEqual(mockError);
    });
  });

  describe('create', () => {
    it('should create entity successfully', async () => {
      const entityData = { name: 'New Entity', description: 'Test description' };
      const mockResponse = { id: 1, ...entityData };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await baseService.create(entityData);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        TEST_URL,
        JSON.stringify(entityData),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle complex entity data', async () => {
      const complexEntity = {
        name: 'Complex Entity',
        metadata: { key: 'value', nested: { prop: 'test' } },
        tags: ['tag1', 'tag2'],
        active: true
      };
      const mockResponse = { id: 2, ...complexEntity };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await baseService.create(complexEntity);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        TEST_URL,
        JSON.stringify(complexEntity),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle validation errors during create', async () => {
      const entityData = { name: '' }; // Invalid data
      const mockError = { 
        response: { 
          status: 400, 
          data: { 
            message: 'Validation failed',
            errors: { name: 'Name is required' }
          } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(baseService.create(entityData)).rejects.toEqual(mockError);
    });

    it('should handle empty entity data', async () => {
      const entityData = {};
      const mockResponse = { id: 3 };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await baseService.create(entityData);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        TEST_URL,
        JSON.stringify(entityData),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result.data).toEqual(mockResponse);
    });
  });

  describe('update', () => {
    it('should update entity successfully', async () => {
      const entityData = { id: 1, name: 'Updated Entity', description: 'Updated description' };
      const mockResponse = { ...entityData, updatedAt: '2023-01-01' };
      mockedAxios.put.mockResolvedValue({ data: mockResponse });

      const result = await baseService.update(entityData);

      expect(mockedAxios.put).toHaveBeenCalledWith(
        TEST_URL,
        JSON.stringify(entityData),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle partial entity updates', async () => {
      const partialUpdate = { id: 1, name: 'Only Name Updated' };
      const mockResponse = { id: 1, name: 'Only Name Updated', description: 'Original description' };
      mockedAxios.put.mockResolvedValue({ data: mockResponse });

      const result = await baseService.update(partialUpdate);

      expect(mockedAxios.put).toHaveBeenCalledWith(
        TEST_URL,
        JSON.stringify(partialUpdate),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle errors during update', async () => {
      const entityData = { id: 999, name: 'Non-existent Entity' };
      const mockError = { response: { status: 404, data: { message: 'Entity not found' } } };
      mockedAxios.put.mockRejectedValue(mockError);

      await expect(baseService.update(entityData)).rejects.toEqual(mockError);
    });

    it('should handle validation errors during update', async () => {
      const entityData = { id: 1, name: '' }; // Invalid data
      const mockError = { 
        response: { 
          status: 400, 
          data: { 
            message: 'Validation failed',
            errors: { name: 'Name cannot be empty' }
          } 
        } 
      };
      mockedAxios.put.mockRejectedValue(mockError);

      await expect(baseService.update(entityData)).rejects.toEqual(mockError);
    });
  });

  describe('validate', () => {
    it('should validate entity attribute successfully', async () => {
      const entityId = 1;
      const attribute = 'email';
      const value = 'test@example.com';
      const mockResponse = { valid: true, message: 'Email is available' };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await baseService.validate(entityId, attribute, value);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        `${TEST_URL}/validate`,
        JSON.stringify({ id: entityId, name: attribute, value: value }),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle validation failure', async () => {
      const entityId = 2;
      const attribute = 'username';
      const value = 'taken_username';
      const mockResponse = { valid: false, message: 'Username already exists' };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await baseService.validate(entityId, attribute, value);

      expect(result.data).toEqual(mockResponse);
    });

    it('should handle validation with null ID (new entity)', async () => {
      const entityId = null;
      const attribute = 'name';
      const value = 'New Name';
      const mockResponse = { valid: true };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await baseService.validate(entityId, attribute, value);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        `${TEST_URL}/validate`,
        JSON.stringify({ id: null, name: attribute, value: value }),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle validation errors', async () => {
      const mockError = { response: { status: 500, data: { message: 'Validation service unavailable' } } };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(baseService.validate(1, 'name', 'test')).rejects.toEqual(mockError);
    });

    it('should handle different attribute types', async () => {
      const testCases = [
        { attribute: 'email', value: 'user@example.com' },
        { attribute: 'age', value: 25 },
        { attribute: 'active', value: true },
        { attribute: 'metadata', value: { key: 'value' } }
      ];

      for (const testCase of testCases) {
        const mockResponse = { valid: true };
        mockedAxios.post.mockResolvedValue({ data: mockResponse });

        await baseService.validate(1, testCase.attribute, testCase.value);

        expect(mockedAxios.post).toHaveBeenCalledWith(
          `${TEST_URL}/validate`,
          JSON.stringify({ id: 1, name: testCase.attribute, value: testCase.value }),
          { headers: { 'Content-Type': 'application/json' } }
        );
      }
    });
  });

  describe('Headers usage', () => {
    it('should call Headers function for authenticated requests', async () => {
      const entityData = { name: 'Test' };
      mockedAxios.post.mockResolvedValue({ data: {} });
      mockedAxios.put.mockResolvedValue({ data: {} });
      mockedAxios.delete.mockResolvedValue({ data: {} });

      await baseService.create(entityData);
      await baseService.update(entityData);
      await baseService.delete(1);
      await baseService.validate(1, 'name', 'test');

      expect(Headers).toHaveBeenCalledTimes(4);
    });

    it('should not call Headers for GET requests', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await baseService.getAll();
      await baseService.getByStatus(true);
      await baseService.get(1);

      expect(Headers).not.toHaveBeenCalled();
    });
  });

  describe('Error handling patterns', () => {
    const errorScenarios = [
      { status: 400, description: 'Bad Request' },
      { status: 401, description: 'Unauthorized' },
      { status: 403, description: 'Forbidden' },
      { status: 404, description: 'Not Found' },
      { status: 500, description: 'Internal Server Error' }
    ];

    errorScenarios.forEach(scenario => {
      it(`should handle ${scenario.status} ${scenario.description} errors`, async () => {
        const mockError = { 
          response: { 
            status: scenario.status, 
            data: { message: scenario.description } 
          } 
        };
        mockedAxios.get.mockRejectedValue(mockError);

        await expect(baseService.getAll()).rejects.toEqual(mockError);
      });
    });

    it('should handle network errors', async () => {
      const mockError = { code: 'ECONNREFUSED', message: 'Connection refused' };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(baseService.getAll()).rejects.toEqual(mockError);
    });

    it('should handle timeout errors', async () => {
      const mockError = { code: 'ECONNABORTED', message: 'timeout of 5000ms exceeded' };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(baseService.create({})).rejects.toEqual(mockError);
    });
  });

  describe('URL handling', () => {
    it('should handle URLs with trailing slashes', () => {
      const service = new BaseService('/api/entities/');
      expect(service.url).toBe('/api/entities/');
    });

    it('should handle URLs without leading slashes', () => {
      const service = new BaseService('api/entities');
      expect(service.url).toBe('api/entities');
    });

    it('should handle complex URL patterns', () => {
      const service = new BaseService('/api/v1/namespace/entities');
      expect(service.url).toBe('/api/v1/namespace/entities');
    });
  });
});