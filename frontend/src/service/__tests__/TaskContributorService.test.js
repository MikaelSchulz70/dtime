import axios from 'axios';
import TaskContributorService from '../TaskContributorService';
import { Headers } from '../ServiceUtil';

// Mock axios
jest.mock('axios');
const mockedAxios = axios;

// Mock ServiceUtil
jest.mock('../ServiceUtil', () => ({
  Headers: jest.fn(() => ({ headers: { 'Content-Type': 'application/json' } }))
}));

describe('TaskContributorService', () => {
  let taskContributorService;

  beforeEach(() => {
    taskContributorService = new TaskContributorService();
    jest.clearAllMocks();
  });

  describe('getTaskContributor', () => {
    it('should fetch task contributor by user ID successfully', async () => {
      const userId = 123;
      const mockTaskContributor = {
        userId: userId,
        userName: 'John Doe',
        email: 'john.doe@example.com',
        tasks: [
          { id: 1, title: 'Task 1', status: 'IN_PROGRESS', assignedDate: '2023-01-01' },
          { id: 2, title: 'Task 2', status: 'COMPLETED', assignedDate: '2023-01-02' }
        ],
        contributionStats: {
          totalTasks: 2,
          completedTasks: 1,
          inProgressTasks: 1,
          averageCompletionTime: 5.5
        }
      };
      mockedAxios.get.mockResolvedValue({ data: mockTaskContributor });

      const result = await taskContributorService.getTaskContributor(userId);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/taskcontributor/123');
      expect(result.data).toEqual(mockTaskContributor);
    });

    it('should handle string user ID values', async () => {
      const userId = '456';
      const mockTaskContributor = {
        userId: 456,
        userName: 'Jane Smith',
        tasks: []
      };
      mockedAxios.get.mockResolvedValue({ data: mockTaskContributor });

      const result = await taskContributorService.getTaskContributor(userId);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/taskcontributor/456');
      expect(result.data).toEqual(mockTaskContributor);
    });

    it('should handle user not found errors', async () => {
      const userId = 999;
      const mockError = { 
        response: { 
          status: 404, 
          data: { message: 'Task contributor not found' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(taskContributorService.getTaskContributor(userId)).rejects.toEqual(mockError);
    });

    it('should handle unauthorized access errors', async () => {
      const userId = 123;
      const mockError = { 
        response: { 
          status: 403, 
          data: { message: 'Access denied to task contributor data' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(taskContributorService.getTaskContributor(userId)).rejects.toEqual(mockError);
    });

    it('should handle server errors', async () => {
      const userId = 123;
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'Internal server error' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(taskContributorService.getTaskContributor(userId)).rejects.toEqual(mockError);
    });

    it('should handle network errors', async () => {
      const userId = 123;
      const mockError = { 
        code: 'ECONNREFUSED', 
        message: 'Connection refused' 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(taskContributorService.getTaskContributor(userId)).rejects.toEqual(mockError);
    });

    it('should handle empty task contributor data', async () => {
      const userId = 789;
      const mockTaskContributor = {
        userId: userId,
        userName: 'New User',
        tasks: [],
        contributionStats: {
          totalTasks: 0,
          completedTasks: 0,
          inProgressTasks: 0,
          averageCompletionTime: 0
        }
      };
      mockedAxios.get.mockResolvedValue({ data: mockTaskContributor });

      const result = await taskContributorService.getTaskContributor(userId);

      expect(result.data.tasks).toHaveLength(0);
      expect(result.data.contributionStats.totalTasks).toBe(0);
    });

    it('should handle numeric and string user IDs consistently', async () => {
      const numericUserId = 100;
      const stringUserId = '100';
      mockedAxios.get.mockResolvedValue({ data: { userId: 100 } });

      await taskContributorService.getTaskContributor(numericUserId);
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/taskcontributor/100');

      jest.clearAllMocks();
      mockedAxios.get.mockResolvedValue({ data: { userId: 100 } });

      await taskContributorService.getTaskContributor(stringUserId);
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/taskcontributor/100');
    });
  });

  describe('udate', () => {
    // Note: Method name has typo "udate" instead of "update"
    it('should update task contributor successfully', async () => {
      const contributorData = {
        userId: 123,
        userName: 'Updated Name',
        email: 'updated@example.com',
        preferences: {
          notifications: true,
          theme: 'dark'
        }
      };
      const mockResponse = { 
        success: true, 
        updated: contributorData,
        message: 'Task contributor updated successfully' 
      };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await taskContributorService.udate(contributorData);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/taskcontributor',
        JSON.stringify(contributorData),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle complex contributor data structures', async () => {
      const complexContributor = {
        userId: 456,
        profile: {
          firstName: 'John',
          lastName: 'Doe',
          avatar: 'avatar.jpg'
        },
        skills: ['JavaScript', 'React', 'Node.js'],
        availability: {
          hoursPerWeek: 40,
          timezone: 'UTC-5',
          workingDays: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday']
        },
        taskPreferences: {
          preferredTaskTypes: ['development', 'testing'],
          maxConcurrentTasks: 3
        }
      };
      const mockResponse = { success: true };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await taskContributorService.udate(complexContributor);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/taskcontributor',
        JSON.stringify(complexContributor),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle validation errors during update', async () => {
      const invalidContributor = {
        userId: null,
        userName: '',
        email: 'invalid-email'
      };
      const mockError = { 
        response: { 
          status: 400, 
          data: { 
            message: 'Validation failed',
            errors: {
              userId: 'User ID is required',
              userName: 'User name cannot be empty',
              email: 'Invalid email format'
            }
          } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(taskContributorService.udate(invalidContributor)).rejects.toEqual(mockError);
    });

    it('should handle unauthorized update attempts', async () => {
      const contributorData = { userId: 123, userName: 'Unauthorized Update' };
      const mockError = { 
        response: { 
          status: 403, 
          data: { message: 'Not authorized to update task contributor' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(taskContributorService.udate(contributorData)).rejects.toEqual(mockError);
    });

    it('should handle contributor not found during update', async () => {
      const contributorData = { userId: 999, userName: 'Non-existent User' };
      const mockError = { 
        response: { 
          status: 404, 
          data: { message: 'Task contributor not found' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(taskContributorService.udate(contributorData)).rejects.toEqual(mockError);
    });

    it('should handle server errors during update', async () => {
      const contributorData = { userId: 123, userName: 'Test User' };
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'Database error occurred' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(taskContributorService.udate(contributorData)).rejects.toEqual(mockError);
    });

    it('should handle empty contributor data', async () => {
      const emptyContributor = {};
      const mockResponse = { 
        success: false, 
        message: 'No data provided for update' 
      };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await taskContributorService.udate(emptyContributor);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/taskcontributor',
        JSON.stringify(emptyContributor),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result.data.success).toBe(false);
    });

    it('should properly serialize complex nested objects', async () => {
      const contributorWithNestedData = {
        userId: 123,
        metadata: {
          createdAt: new Date('2023-01-01T00:00:00Z'),
          tags: ['senior', 'frontend'],
          settings: {
            notifications: {
              email: true,
              push: false,
              sms: true
            }
          }
        }
      };
      mockedAxios.post.mockResolvedValue({ data: { success: true } });

      await taskContributorService.udate(contributorWithNestedData);

      const expectedPayload = JSON.stringify(contributorWithNestedData);
      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/taskcontributor',
        expectedPayload,
        { headers: { 'Content-Type': 'application/json' } }
      );
    });

    it('should handle network timeouts', async () => {
      const contributorData = { userId: 123, userName: 'Test User' };
      const mockError = { 
        code: 'ECONNABORTED', 
        message: 'timeout of 5000ms exceeded' 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(taskContributorService.udate(contributorData)).rejects.toEqual(mockError);
    });
  });

  describe('Headers usage', () => {
    it('should use Headers for authenticated update requests', async () => {
      mockedAxios.post.mockResolvedValue({ data: { success: true } });

      await taskContributorService.udate({ userId: 123 });

      expect(Headers).toHaveBeenCalledTimes(1);
    });

    it('should not use Headers for GET requests', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await taskContributorService.getTaskContributor(123);

      expect(Headers).not.toHaveBeenCalled();
    });
  });

  describe('HTTP methods usage', () => {
    it('should use GET for fetching task contributor data', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await taskContributorService.getTaskContributor(123);

      expect(mockedAxios.get).toHaveBeenCalledTimes(1);
      expect(mockedAxios.post).not.toHaveBeenCalled();
    });

    it('should use POST for updating task contributor data', async () => {
      mockedAxios.post.mockResolvedValue({ data: {} });

      await taskContributorService.udate({ userId: 123 });

      expect(mockedAxios.post).toHaveBeenCalledTimes(1);
      expect(mockedAxios.get).not.toHaveBeenCalled();
    });
  });

  describe('Error response structure preservation', () => {
    it('should preserve detailed error response structure', async () => {
      const detailedError = {
        response: {
          status: 422,
          statusText: 'Unprocessable Entity',
          headers: { 'content-type': 'application/json' },
          data: {
            message: 'Task contributor validation failed',
            code: 'CONTRIBUTOR_VALIDATION_ERROR',
            details: {
              field: 'email',
              rejectedValue: 'invalid-email',
              reason: 'Must be a valid email address'
            },
            timestamp: '2023-01-01T10:00:00Z'
          }
        },
        request: {},
        config: {}
      };

      mockedAxios.post.mockRejectedValue(detailedError);

      try {
        await taskContributorService.udate({ email: 'invalid-email' });
      } catch (error) {
        expect(error.response.status).toBe(422);
        expect(error.response.data.code).toBe('CONTRIBUTOR_VALIDATION_ERROR');
        expect(error.response.data.details.field).toBe('email');
      }
    });
  });

  describe('Integration scenarios', () => {
    it('should handle complete contributor workflow', async () => {
      const userId = 123;
      
      // First fetch existing contributor
      const existingContributor = {
        userId: userId,
        userName: 'John Doe',
        email: 'john@example.com',
        tasks: [{ id: 1, title: 'Existing Task' }]
      };
      mockedAxios.get.mockResolvedValueOnce({ data: existingContributor });
      
      const fetchResult = await taskContributorService.getTaskContributor(userId);
      expect(fetchResult.data).toEqual(existingContributor);

      // Then update the contributor
      const updatedContributor = {
        ...existingContributor,
        userName: 'John Updated Doe',
        email: 'john.updated@example.com'
      };
      mockedAxios.post.mockResolvedValueOnce({ 
        data: { success: true, updated: updatedContributor } 
      });
      
      const updateResult = await taskContributorService.udate(updatedContributor);
      expect(updateResult.data.success).toBe(true);
    });

    it('should handle contributor not found then create scenario', async () => {
      const userId = 999;
      
      // First attempt to fetch non-existent contributor
      const notFoundError = { 
        response: { status: 404, data: { message: 'Not found' } } 
      };
      mockedAxios.get.mockRejectedValueOnce(notFoundError);
      
      await expect(taskContributorService.getTaskContributor(userId)).rejects.toEqual(notFoundError);

      // Then create new contributor
      const newContributor = {
        userId: userId,
        userName: 'New Contributor',
        email: 'new@example.com'
      };
      mockedAxios.post.mockResolvedValueOnce({ 
        data: { success: true, created: newContributor } 
      });
      
      const createResult = await taskContributorService.udate(newContributor);
      expect(createResult.data.success).toBe(true);
    });
  });

  describe('Data type handling', () => {
    it('should handle different user ID data types', async () => {
      const testCases = [
        { input: 123, expected: '123' },
        { input: '456', expected: '456' },
        { input: '0', expected: '0' }
      ];

      for (const testCase of testCases) {
        mockedAxios.get.mockResolvedValue({ data: { userId: testCase.input } });
        
        await taskContributorService.getTaskContributor(testCase.input);
        
        expect(mockedAxios.get).toHaveBeenCalledWith(`/api/taskcontributor/${testCase.expected}`);
        jest.clearAllMocks();
      }
    });

    it('should handle boolean and numeric values in contributor data', async () => {
      const contributorWithMixedTypes = {
        userId: 123,
        active: true,
        rating: 4.5,
        totalHours: 160,
        isTeamLead: false,
        joinedDate: '2023-01-01'
      };
      mockedAxios.post.mockResolvedValue({ data: { success: true } });

      await taskContributorService.udate(contributorWithMixedTypes);

      const expectedPayload = JSON.stringify(contributorWithMixedTypes);
      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/taskcontributor',
        expectedPayload,
        { headers: { 'Content-Type': 'application/json' } }
      );
    });
  });
});