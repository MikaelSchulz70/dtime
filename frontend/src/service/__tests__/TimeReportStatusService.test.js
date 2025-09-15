import axios from 'axios';
import TimeReportStatusService from '../TimeReportStatusService';
import { Headers } from '../ServiceUtil';

// Mock axios
jest.mock('axios');
const mockedAxios = axios;

// Mock ServiceUtil
jest.mock('../ServiceUtil', () => ({
  Headers: jest.fn(() => ({ headers: { 'Content-Type': 'application/json' } }))
}));

describe('TimeReportStatusService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getCurrentUnclosedUsers', () => {
    it('should fetch current unclosed users successfully', async () => {
      const mockUnclosedUsers = [
        {
          userId: 1,
          userName: 'John Doe',
          email: 'john@example.com',
          department: 'Engineering',
          unclosedDate: '2023-01-15',
          totalUnclosedHours: 8.5,
          lastEntry: '2023-01-15T17:30:00Z'
        },
        {
          userId: 2,
          userName: 'Jane Smith',
          email: 'jane@example.com',
          department: 'Marketing',
          unclosedDate: '2023-01-15',
          totalUnclosedHours: 7.25,
          lastEntry: '2023-01-15T16:45:00Z'
        }
      ];
      mockedAxios.get.mockResolvedValue({ data: mockUnclosedUsers });

      const result = await TimeReportStatusService.getCurrentUnclosedUsers();

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/timereportstatus',
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result).toEqual(mockUnclosedUsers);
    });

    it('should handle empty unclosed users list', async () => {
      mockedAxios.get.mockResolvedValue({ data: [] });

      const result = await TimeReportStatusService.getCurrentUnclosedUsers();

      expect(result).toEqual([]);
    });

    it('should handle errors when fetching current unclosed users', async () => {
      const mockError = new Error('Service unavailable');
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(TimeReportStatusService.getCurrentUnclosedUsers()).rejects.toThrow('Service unavailable');
    });

    it('should handle unauthorized access', async () => {
      const mockError = { 
        response: { 
          status: 403, 
          data: { message: 'Access denied to time report status' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(TimeReportStatusService.getCurrentUnclosedUsers()).rejects.toEqual(mockError);
    });

    it('should include authentication headers', async () => {
      mockedAxios.get.mockResolvedValue({ data: [] });

      await TimeReportStatusService.getCurrentUnclosedUsers();

      expect(Headers).toHaveBeenCalled();
    });
  });

  describe('getPreviousUnclosedUsers', () => {
    it('should fetch previous unclosed users successfully', async () => {
      const date = '2023-01-10';
      const mockUnclosedUsers = [
        {
          userId: 3,
          userName: 'Bob Wilson',
          unclosedDate: '2023-01-09',
          totalUnclosedHours: 6.0
        }
      ];
      mockedAxios.get.mockResolvedValue({ data: mockUnclosedUsers });

      const result = await TimeReportStatusService.getPreviousUnclosedUsers(date);

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/timereportstatus/previous?date=2023-01-10',
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result).toEqual(mockUnclosedUsers);
    });

    it('should handle different date formats', async () => {
      const testDates = [
        '2023-01-15',
        '2023-12-25',
        '2024-02-29' // leap year
      ];

      for (const date of testDates) {
        mockedAxios.get.mockResolvedValue({ data: [] });
        
        await TimeReportStatusService.getPreviousUnclosedUsers(date);
        
        expect(mockedAxios.get).toHaveBeenCalledWith(
          `/api/timereportstatus/previous?date=${date}`,
          { headers: { 'Content-Type': 'application/json' } }
        );
      }
    });

    it('should handle date parameter with special characters', async () => {
      const date = '2023-01-15T10:30:00Z';
      mockedAxios.get.mockResolvedValue({ data: [] });

      await TimeReportStatusService.getPreviousUnclosedUsers(date);

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/timereportstatus/previous?date=2023-01-15T10:30:00Z',
        { headers: { 'Content-Type': 'application/json' } }
      );
    });

    it('should handle errors when fetching previous unclosed users', async () => {
      const mockError = { 
        response: { 
          status: 400, 
          data: { message: 'Invalid date parameter' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(TimeReportStatusService.getPreviousUnclosedUsers('invalid-date')).rejects.toEqual(mockError);
    });
  });

  describe('getNextUnclosedUsers', () => {
    it('should fetch next unclosed users successfully', async () => {
      const date = '2023-01-20';
      const mockUnclosedUsers = [
        {
          userId: 4,
          userName: 'Alice Johnson',
          unclosedDate: '2023-01-21',
          totalUnclosedHours: 9.75
        }
      ];
      mockedAxios.get.mockResolvedValue({ data: mockUnclosedUsers });

      const result = await TimeReportStatusService.getNextUnclosedUsers(date);

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/timereportstatus/next?date=2023-01-20',
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result).toEqual(mockUnclosedUsers);
    });

    it('should handle future dates', async () => {
      const futureDate = '2024-12-31';
      mockedAxios.get.mockResolvedValue({ data: [] });

      const result = await TimeReportStatusService.getNextUnclosedUsers(futureDate);

      expect(result).toEqual([]);
      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/timereportstatus/next?date=2024-12-31',
        { headers: { 'Content-Type': 'application/json' } }
      );
    });

    it('should handle errors when fetching next unclosed users', async () => {
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'Database connection failed' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(TimeReportStatusService.getNextUnclosedUsers('2023-01-20')).rejects.toEqual(mockError);
    });
  });

  describe('closeUserTimeReport', () => {
    it('should close user time report successfully', async () => {
      const userId = 123;
      const closeDate = '2023-01-15';
      const mockResponse = {
        success: true,
        userId: userId,
        closeDate: closeDate,
        message: 'Time report closed successfully',
        closedEntries: 5
      };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await TimeReportStatusService.closeUserTimeReport(userId, closeDate);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/timereportstatus/close',
        JSON.stringify({ userId: userId, closeDate: closeDate }),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result).toEqual(mockResponse);
    });

    it('should handle string and numeric user IDs', async () => {
      const testCases = [
        { userId: 123, closeDate: '2023-01-15' },
        { userId: '456', closeDate: '2023-01-16' },
        { userId: '0', closeDate: '2023-01-17' }
      ];

      for (const testCase of testCases) {
        mockedAxios.post.mockResolvedValue({ data: { success: true } });
        
        await TimeReportStatusService.closeUserTimeReport(testCase.userId, testCase.closeDate);
        
        expect(mockedAxios.post).toHaveBeenCalledWith(
          '/api/timereportstatus/close',
          JSON.stringify({ userId: testCase.userId, closeDate: testCase.closeDate }),
          { headers: { 'Content-Type': 'application/json' } }
        );
        jest.clearAllMocks();
      }
    });

    it('should handle different date formats for close operation', async () => {
      const userId = 123;
      const dateCases = [
        '2023-01-15',
        '2023-12-31T23:59:59Z',
        '2024-02-29' // leap year date
      ];

      for (const closeDate of dateCases) {
        mockedAxios.post.mockResolvedValue({ data: { success: true } });
        
        await TimeReportStatusService.closeUserTimeReport(userId, closeDate);
        
        expect(mockedAxios.post).toHaveBeenCalledWith(
          '/api/timereportstatus/close',
          JSON.stringify({ userId: userId, closeDate: closeDate }),
          { headers: { 'Content-Type': 'application/json' } }
        );
        jest.clearAllMocks();
      }
    });

    it('should handle validation errors during close', async () => {
      const userId = null;
      const closeDate = 'invalid-date';
      const mockError = { 
        response: { 
          status: 400, 
          data: { 
            message: 'Validation failed',
            errors: {
              userId: 'User ID is required',
              closeDate: 'Invalid date format'
            }
          } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(TimeReportStatusService.closeUserTimeReport(userId, closeDate)).rejects.toEqual(mockError);
    });

    it('should handle user not found errors', async () => {
      const userId = 999;
      const closeDate = '2023-01-15';
      const mockError = { 
        response: { 
          status: 404, 
          data: { message: 'User not found' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(TimeReportStatusService.closeUserTimeReport(userId, closeDate)).rejects.toEqual(mockError);
    });

    it('should handle already closed time report', async () => {
      const userId = 123;
      const closeDate = '2023-01-15';
      const mockResponse = {
        success: false,
        message: 'Time report already closed for this date',
        userId: userId,
        closeDate: closeDate
      };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await TimeReportStatusService.closeUserTimeReport(userId, closeDate);

      expect(result.success).toBe(false);
      expect(result.message).toContain('already closed');
    });
  });

  describe('openUserTimeReport', () => {
    it('should open user time report successfully', async () => {
      const userId = 123;
      const closeDate = '2023-01-15';
      const mockResponse = {
        success: true,
        userId: userId,
        closeDate: closeDate,
        message: 'Time report opened successfully',
        openedEntries: 3
      };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await TimeReportStatusService.openUserTimeReport(userId, closeDate);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/timereportstatus/open',
        JSON.stringify({ userId: userId, closeDate: closeDate }),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result).toEqual(mockResponse);
    });

    it('should handle opening already open time report', async () => {
      const userId = 123;
      const closeDate = '2023-01-15';
      const mockResponse = {
        success: false,
        message: 'Time report is already open for this date',
        userId: userId,
        closeDate: closeDate
      };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await TimeReportStatusService.openUserTimeReport(userId, closeDate);

      expect(result.success).toBe(false);
      expect(result.message).toContain('already open');
    });

    it('should handle unauthorized open attempts', async () => {
      const userId = 123;
      const closeDate = '2023-01-15';
      const mockError = { 
        response: { 
          status: 403, 
          data: { message: 'Not authorized to open time reports for this user' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(TimeReportStatusService.openUserTimeReport(userId, closeDate)).rejects.toEqual(mockError);
    });

    it('should handle server errors during open', async () => {
      const userId = 123;
      const closeDate = '2023-01-15';
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'Failed to update time report status' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(TimeReportStatusService.openUserTimeReport(userId, closeDate)).rejects.toEqual(mockError);
    });

    it('should properly serialize request payload for open', async () => {
      const userId = 456;
      const closeDate = '2023-01-20T00:00:00Z';
      mockedAxios.post.mockResolvedValue({ data: { success: true } });

      await TimeReportStatusService.openUserTimeReport(userId, closeDate);

      const expectedPayload = JSON.stringify({ userId: userId, closeDate: closeDate });
      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/timereportstatus/open',
        expectedPayload,
        { headers: { 'Content-Type': 'application/json' } }
      );
    });
  });

  describe('BaseService inheritance', () => {
    it('should inherit BaseService methods', () => {
      expect(TimeReportStatusService.url).toBe('/api/timereportstatus');
      expect(typeof TimeReportStatusService.getAll).toBe('function');
      expect(typeof TimeReportStatusService.get).toBe('function');
      expect(typeof TimeReportStatusService.create).toBe('function');
      expect(typeof TimeReportStatusService.update).toBe('function');
      expect(typeof TimeReportStatusService.delete).toBe('function');
      expect(typeof TimeReportStatusService.validate).toBe('function');
    });

    it('should be a singleton instance', () => {
      // The service is exported as a singleton instance
      expect(TimeReportStatusService).toBeDefined();
      expect(typeof TimeReportStatusService).toBe('object');
    });
  });

  describe('Headers usage consistency', () => {
    it('should use Headers for all requests', async () => {
      mockedAxios.get.mockResolvedValue({ data: [] });
      mockedAxios.post.mockResolvedValue({ data: { success: true } });

      await TimeReportStatusService.getCurrentUnclosedUsers();
      await TimeReportStatusService.getPreviousUnclosedUsers('2023-01-15');
      await TimeReportStatusService.getNextUnclosedUsers('2023-01-15');
      await TimeReportStatusService.closeUserTimeReport(123, '2023-01-15');
      await TimeReportStatusService.openUserTimeReport(123, '2023-01-15');

      expect(Headers).toHaveBeenCalledTimes(5);
    });
  });

  describe('HTTP methods usage', () => {
    it('should use GET for fetching operations', async () => {
      mockedAxios.get.mockResolvedValue({ data: [] });

      await TimeReportStatusService.getCurrentUnclosedUsers();
      await TimeReportStatusService.getPreviousUnclosedUsers('2023-01-15');
      await TimeReportStatusService.getNextUnclosedUsers('2023-01-15');

      expect(mockedAxios.get).toHaveBeenCalledTimes(3);
    });

    it('should use POST for action operations', async () => {
      mockedAxios.post.mockResolvedValue({ data: { success: true } });

      await TimeReportStatusService.closeUserTimeReport(123, '2023-01-15');
      await TimeReportStatusService.openUserTimeReport(123, '2023-01-15');

      expect(mockedAxios.post).toHaveBeenCalledTimes(2);
    });
  });

  describe('Integration scenarios', () => {
    it('should handle complete time report status workflow', async () => {
      const userId = 123;
      const date = '2023-01-15';
      
      // First get current unclosed users
      const unclosedUsers = [{ userId, userName: 'Test User', unclosedDate: date }];
      mockedAxios.get.mockResolvedValueOnce({ data: unclosedUsers });
      
      const currentUsers = await TimeReportStatusService.getCurrentUnclosedUsers();
      expect(currentUsers).toHaveLength(1);

      // Then close the user's time report
      mockedAxios.post.mockResolvedValueOnce({ 
        data: { success: true, message: 'Closed successfully' } 
      });
      
      const closeResult = await TimeReportStatusService.closeUserTimeReport(userId, date);
      expect(closeResult.success).toBe(true);

      // Verify user is no longer in unclosed list
      mockedAxios.get.mockResolvedValueOnce({ data: [] });
      
      const updatedUsers = await TimeReportStatusService.getCurrentUnclosedUsers();
      expect(updatedUsers).toHaveLength(0);
    });

    it('should handle navigation through historical data', async () => {
      const baseDate = '2023-01-15';
      
      // Get previous unclosed users
      mockedAxios.get.mockResolvedValueOnce({ data: [{ userId: 1, unclosedDate: '2023-01-10' }] });
      const previousUsers = await TimeReportStatusService.getPreviousUnclosedUsers(baseDate);
      expect(previousUsers).toHaveLength(1);

      // Get current unclosed users
      mockedAxios.get.mockResolvedValueOnce({ data: [{ userId: 2, unclosedDate: baseDate }] });
      const currentUsers = await TimeReportStatusService.getCurrentUnclosedUsers();
      expect(currentUsers).toHaveLength(1);

      // Get next unclosed users
      mockedAxios.get.mockResolvedValueOnce({ data: [{ userId: 3, unclosedDate: '2023-01-20' }] });
      const nextUsers = await TimeReportStatusService.getNextUnclosedUsers(baseDate);
      expect(nextUsers).toHaveLength(1);
    });

    it('should handle close and reopen workflow', async () => {
      const userId = 123;
      const date = '2023-01-15';
      
      // Close time report
      mockedAxios.post.mockResolvedValueOnce({ 
        data: { success: true, message: 'Time report closed' } 
      });
      
      const closeResult = await TimeReportStatusService.closeUserTimeReport(userId, date);
      expect(closeResult.success).toBe(true);

      // Then reopen it
      mockedAxios.post.mockResolvedValueOnce({ 
        data: { success: true, message: 'Time report reopened' } 
      });
      
      const openResult = await TimeReportStatusService.openUserTimeReport(userId, date);
      expect(openResult.success).toBe(true);
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
      it(`should handle ${scenario.status} ${scenario.description} errors consistently`, async () => {
        const mockError = { 
          response: { 
            status: scenario.status, 
            data: { message: scenario.description } 
          } 
        };
        
        mockedAxios.get.mockRejectedValue(mockError);
        mockedAxios.post.mockRejectedValue(mockError);

        await expect(TimeReportStatusService.getCurrentUnclosedUsers()).rejects.toEqual(mockError);
        await expect(TimeReportStatusService.closeUserTimeReport(123, '2023-01-15')).rejects.toEqual(mockError);
      });
    });

    it('should handle network errors', async () => {
      const networkError = { code: 'ECONNREFUSED', message: 'Connection refused' };
      mockedAxios.get.mockRejectedValue(networkError);

      await expect(TimeReportStatusService.getCurrentUnclosedUsers()).rejects.toEqual(networkError);
    });

    it('should handle timeout errors', async () => {
      const timeoutError = { code: 'ECONNABORTED', message: 'timeout exceeded' };
      mockedAxios.post.mockRejectedValue(timeoutError);

      await expect(TimeReportStatusService.closeUserTimeReport(123, '2023-01-15')).rejects.toEqual(timeoutError);
    });
  });
});