import axios from 'axios';
import TimeReportStatusService from '../TimeReportStatusService';

jest.mock('axios');

const mockedAxios = axios;

describe('TimeReportStatusService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getUnclosedUsers', () => {
    it('should fetch current unclosed users without date', async () => {
      mockedAxios.get.mockResolvedValue({ data: { unclosedUsers: [] } });

      const result = await TimeReportStatusService.getUnclosedUsers();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/timereportstatus', expect.any(Object));
      expect(result).toEqual({ unclosedUsers: [] });
    });

    it('should fetch unclosed users for a specific month', async () => {
      mockedAxios.get.mockResolvedValue({ data: { unclosedUsers: [] } });

      await TimeReportStatusService.getUnclosedUsers('2023-01-15');

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/timereportstatus?date=2023-01-15',
        expect.any(Object)
      );
    });
  });
});
