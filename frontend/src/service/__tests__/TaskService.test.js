import axios from 'axios';
import TaskService from '../TaskService';

jest.mock('axios');
const mockedAxios = axios;

describe('TaskService', () => {
  let taskService;

  beforeEach(() => {
    taskService = new TaskService();
    jest.clearAllMocks();
  });

  describe('getAll', () => {
    it('should fetch all tasks successfully', async () => {
      const mockResponse = { 
        data: [
          { id: 1, name: 'Task 1', account: { id: 1, name: 'Company A' } },
          { id: 2, name: 'Task 2', account: { id: 2, name: 'Company B' } }
        ] 
      };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await taskService.getAll();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/task');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('get', () => {
    it('should fetch task by id successfully', async () => {
      const mockResponse = { 
        data: { id: 1, name: 'Task 1', account: { id: 1, name: 'Company A' } }
      };
      const taskId = 1;
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await taskService.get(taskId);

      expect(mockedAxios.get).toHaveBeenCalledWith(`/api/task/${taskId}`);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('create', () => {
    it('should create task successfully', async () => {
      const mockResponse = { data: { id: 1, created: true } };
      const taskData = { 
        name: 'New Task', 
        account: { id: 1 },
        activationStatus: 'ACTIVE',
        taskType: 'NORMAL'
      };
      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await taskService.create(taskData);

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/task', JSON.stringify(taskData), {'headers': {'Content-Type': 'application/json'}});
      expect(result).toEqual(mockResponse);
    });
  });

  describe('update', () => {
    it('should update task successfully', async () => {
      const mockResponse = { data: { id: 1, updated: true } };
      const taskData = { 
        id: 1,
        name: 'Updated Task', 
        account: { id: 1 }
      };
      mockedAxios.put.mockResolvedValue(mockResponse);

      const result = await taskService.update(taskData);

      expect(mockedAxios.put).toHaveBeenCalledWith('/api/task', JSON.stringify(taskData), {'headers': {'Content-Type': 'application/json'}});
      expect(result).toEqual(mockResponse);
    });
  });

  describe('delete', () => {
    it('should delete task successfully', async () => {
      const mockResponse = { data: { deleted: true } };
      const taskId = 1;
      mockedAxios.delete.mockResolvedValue(mockResponse);

      const result = await taskService.delete(taskId);

      expect(mockedAxios.delete).toHaveBeenCalledWith(`/api/task/${taskId}`, {'headers': {'Content-Type': 'application/json'}});
      expect(result).toEqual(mockResponse);
    });
  });

  describe('validate', () => {
    it('should validate task field successfully', async () => {
      const mockResponse = { data: { valid: true } };
      const taskId = 1;
      const field = 'name';
      const value = 'Task Name';
      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await taskService.validate(taskId, field, value);

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/task/validate', JSON.stringify({id: taskId, name: field, value: value}), {'headers': {'Content-Type': 'application/json'}});
      expect(result).toEqual(mockResponse);
    });
  });
});