import axios from 'axios';
import ReportService from '../ReportService';

jest.mock('axios');

describe('ReportService', () => {
  let reportService;

  beforeEach(() => {
    reportService = new ReportService();
    jest.clearAllMocks();
  });

  describe('getReportForMonth', () => {
    it('should call monthly path endpoint with type', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await reportService.getReportForMonth(2023, 1, 'USER');

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report/year/2023/month/1?type=USER');
    });
  });

  describe('getReportForYear', () => {
    it('should call yearly path endpoint with type', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await reportService.getReportForYear(2023, 'USER');

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/report/year/2023?type=USER');
    });
  });
});

const mockedAxios = axios;
