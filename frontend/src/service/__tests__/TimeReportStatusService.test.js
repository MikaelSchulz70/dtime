import axios from 'axios';
import TimeReportStatusService from '../TimeReportStatusService';

jest.mock('axios');

describe('TimeReportStatusService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getUnclosedUsersForMonth', () => {
    it('should call path-based endpoint for unclosed users', async () => {
      axios.get.mockResolvedValue({ data: { unclosedUsers: [] } });

      const result = await TimeReportStatusService.getUnclosedUsersForMonth(2023, 1);

      expect(axios.get).toHaveBeenCalledWith('/api/timereportstatus/year/2023/month/1', expect.any(Object));
      expect(result).toEqual({ unclosedUsers: [] });
    });
  });

  describe('getClosedUsersForMonth', () => {
    it('should call path-based endpoint with closed query param', async () => {
      axios.get.mockResolvedValue({ data: { unclosedUsers: [] } });

      const result = await TimeReportStatusService.getClosedUsersForMonth(2023, 1);

      expect(axios.get).toHaveBeenCalledWith('/api/timereportstatus/year/2023/month/1?closed=true', expect.any(Object));
      expect(result).toEqual({ unclosedUsers: [] });
    });
  });
});
