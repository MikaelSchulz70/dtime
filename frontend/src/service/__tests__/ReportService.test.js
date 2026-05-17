import axios from 'axios';
import ReportService from '../ReportService';

jest.mock('axios');

const mockedAxios = axios;

describe('ReportService', () => {
  let reportService;

  beforeEach(() => {
    reportService = new ReportService();
    jest.clearAllMocks();
  });

  describe('getReport', () => {
    it('should fetch report with view, type, and date', async () => {
      const mockResponse = { data: { fromDate: '2023-01-01', toDate: '2023-01-31' } };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await reportService.getReport('MONTH', 'USER', '2023-01-15');

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report?view=MONTH&type=USER&date=2023-01-15');
      expect(result).toEqual(mockResponse);
    });

    it('should fetch report without date for current period', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await reportService.getReport('MONTH', 'USER');

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report?view=MONTH&type=USER');
    });
  });

  describe('getUserReport', () => {
    it('should fetch user report with view and date', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await reportService.getUserReport('WEEK', '2023-01-15');

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report/user?view=WEEK&date=2023-01-15');
    });
  });
});
