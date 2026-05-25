import axios from 'axios';
import TimeService from '../TimeService';

jest.mock('axios');

describe('TimeService', () => {
  let timeService;

  beforeEach(() => {
    timeService = new TimeService();
    jest.clearAllMocks();
  });

  describe('getTimeReportForMonth', () => {
    it('should call monthly path endpoint', async () => {
      axios.get.mockResolvedValue({ data: {} });

      await timeService.getTimeReportForMonth(2023, 1);

      expect(axios.get).toHaveBeenCalledWith('/api/timereport/year/2023/month/1');
    });
  });

  describe('getTimeReportForWeek', () => {
    it('should call weekly path endpoint', async () => {
      axios.get.mockResolvedValue({ data: {} });

      await timeService.getTimeReportForWeek(2023, 36);

      expect(axios.get).toHaveBeenCalledWith('/api/timereport/year/2023/week/36');
    });
  });

  describe('getVacationReportForMonth', () => {
    it('should call vacation path endpoint', async () => {
      axios.get.mockResolvedValue({ data: {} });

      await timeService.getVacationReportForMonth(2023, 1);

      expect(axios.get).toHaveBeenCalledWith('/api/timereport/vacations/year/2023/month/1');
    });
  });
});
