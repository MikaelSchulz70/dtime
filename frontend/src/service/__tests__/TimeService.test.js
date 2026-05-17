import axios from 'axios';
import TimeService from '../TimeService';

jest.mock('axios');

const mockedAxios = axios;

describe('TimeService', () => {
  let timeService;

  beforeEach(() => {
    timeService = new TimeService();
    jest.clearAllMocks();
  });

  describe('getTimeReport', () => {
    it('should fetch time report with view', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await timeService.getTimeReport('MONTH');

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/timereport?view=MONTH');
    });

    it('should fetch time report with view and date', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await timeService.getTimeReport('WEEK', '2023-01-15');

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/timereport?view=WEEK&date=2023-01-15');
    });
  });

  describe('getVacationReport', () => {
    it('should fetch vacation report for a month', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await timeService.getVacationReport('2023-01-15');

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/timereport/vacations?date=2023-01-15');
    });
  });
});
