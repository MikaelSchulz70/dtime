import axios from 'axios';
import ReportService from '../ReportService';

// Mock axios
jest.mock('axios');
const mockedAxios = axios;

describe('ReportService', () => {
  let reportService;

  beforeEach(() => {
    reportService = new ReportService();
    jest.clearAllMocks();
  });

  describe('getCurrentReport', () => {
    it('should fetch current report successfully', async () => {
      const view = 'MONTH';
      const type = 'TIME';
      const mockResponse = { data: { id: 1, view: 'MONTH', type: 'TIME' } };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await reportService.getCurrentReport(view, type);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report?view=MONTH&type=TIME');
      expect(result).toEqual(mockResponse);
    });

    it('should handle errors when fetching current report', async () => {
      const mockError = new Error('Network error');
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(reportService.getCurrentReport('MONTH', 'TIME')).rejects.toThrow('Network error');
    });
  });

  describe('getPreviousReport', () => {
    it('should fetch previous report successfully', async () => {
      const view = 'MONTH';
      const type = 'TIME';
      const date = '2023-01-15';
      const mockResponse = { data: { id: 1, view: 'MONTH', type: 'TIME', date: '2023-01-15' } };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await reportService.getPreviousReport(view, type, date);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report/previous?view=MONTH&type=TIME&date=2023-01-15');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getNextReport', () => {
    it('should fetch next report successfully', async () => {
      const view = 'MONTH';
      const type = 'TIME';
      const date = '2023-01-15';
      const mockResponse = { data: { id: 1, view: 'MONTH', type: 'TIME', date: '2023-01-15' } };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await reportService.getNextReport(view, type, date);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report/next?view=MONTH&type=TIME&date=2023-01-15');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getCurrentUserReport', () => {
    it('should fetch current user report successfully', async () => {
      const view = 'WEEK';
      const mockResponse = { data: { id: 1, view: 'WEEK', userSpecific: true } };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await reportService.getCurrentUserReport(view);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report/user?view=WEEK');
      expect(result).toEqual(mockResponse);
    });

    it('should handle errors when fetching current user report', async () => {
      const mockError = new Error('Unauthorized');
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(reportService.getCurrentUserReport('WEEK')).rejects.toThrow('Unauthorized');
    });
  });

  describe('getPreviousUserReport', () => {
    it('should fetch previous user report successfully', async () => {
      const view = 'WEEK';
      const date = '2023-01-15';
      const mockResponse = { data: { id: 1, view: 'WEEK', date: '2023-01-15', userSpecific: true } };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await reportService.getPreviousUserReport(view, date);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report/user/previous?view=WEEK&date=2023-01-15');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getNextUserReport', () => {
    it('should fetch next user report successfully', async () => {
      const view = 'WEEK';
      const date = '2023-01-15';
      const mockResponse = { data: { id: 1, view: 'WEEK', date: '2023-01-15', userSpecific: true } };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await reportService.getNextUserReport(view, date);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report/user/next?view=WEEK&date=2023-01-15');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('updateOpenCloseReport', () => {
    it('should update open/close report successfully', async () => {
      const payload = { reportId: 1, action: 'open' };
      const path = 'toggle';
      const mockResponse = { data: { success: true, reportId: 1 } };
      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await reportService.updateOpenCloseReport(payload, path);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/report/toggle',
        payload,
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });

    it('should handle errors when updating report', async () => {
      const payload = { reportId: 1, action: 'close' };
      const path = 'toggle';
      const mockError = { response: { status: 400, data: { message: 'Invalid action' } } };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(reportService.updateOpenCloseReport(payload, path)).rejects.toEqual(mockError);
    });
  });

  describe('Parameter Handling', () => {
    it('should handle undefined parameters gracefully', async () => {
      const mockResponse = { data: {} };
      mockedAxios.get.mockResolvedValue(mockResponse);

      await reportService.getCurrentReport(undefined, undefined);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report?view=undefined&type=undefined');
    });

    it('should handle null parameters gracefully', async () => {
      const mockResponse = { data: {} };
      mockedAxios.get.mockResolvedValue(mockResponse);

      await reportService.getCurrentReport(null, null);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report?view=null&type=null');
    });

    it('should handle empty string parameters', async () => {
      const mockResponse = { data: {} };
      mockedAxios.get.mockResolvedValue(mockResponse);

      await reportService.getCurrentReport('', '');

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report?view=&type=');
    });
  });

  describe('Different View Types', () => {
    const viewTypes = ['MONTH', 'WEEK', 'DAY'];
    const reportTypes = ['TIME', 'EXPENSE', 'VACATION'];

    viewTypes.forEach(view => {
      reportTypes.forEach(type => {
        it(`should handle ${view} view with ${type} type`, async () => {
          const mockResponse = { data: { view, type } };
          mockedAxios.get.mockResolvedValue(mockResponse);

          const result = await reportService.getCurrentReport(view, type);

          expect(mockedAxios.get).toHaveBeenCalledWith(`/api/report?view=${view}&type=${type}`);
          expect(result).toEqual(mockResponse);
        });
      });
    });
  });

  describe('Date Format Handling', () => {
    it('should handle ISO date format', async () => {
      const date = '2023-01-15T00:00:00.000Z';
      const mockResponse = { data: {} };
      mockedAxios.get.mockResolvedValue(mockResponse);

      await reportService.getPreviousReport('MONTH', 'TIME', date);

      expect(mockedAxios.get).toHaveBeenCalledWith(`/api/report/previous?view=MONTH&type=TIME&date=${date}`);
    });

    it('should handle simple date format', async () => {
      const date = '2023-01-15';
      const mockResponse = { data: {} };
      mockedAxios.get.mockResolvedValue(mockResponse);

      await reportService.getNextReport('WEEK', 'EXPENSE', date);

      expect(mockedAxios.get).toHaveBeenCalledWith(`/api/report/next?view=WEEK&type=EXPENSE&date=${date}`);
    });
  });

  describe('Error Response Handling', () => {
    it('should handle 401 unauthorized errors', async () => {
      const mockError = { response: { status: 401, data: { message: 'Unauthorized' } } };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(reportService.getCurrentUserReport('MONTH')).rejects.toEqual(mockError);
    });

    it('should handle 404 not found errors', async () => {
      const mockError = { response: { status: 404, data: { message: 'Report not found' } } };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(reportService.getCurrentReport('MONTH', 'TIME')).rejects.toEqual(mockError);
    });

    it('should handle 500 server errors', async () => {
      const mockError = { response: { status: 500, data: { message: 'Internal server error' } } };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(reportService.updateOpenCloseReport({}, 'path')).rejects.toEqual(mockError);
    });
  });
});