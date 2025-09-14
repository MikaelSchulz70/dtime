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

  describe('getTimes', () => {
    it('should fetch times successfully', async () => {
      const mockResponse = { data: [{ id: 1, date: '2023-01-01', hours: 8 }] };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await timeService.getTimes();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/timereport?view=undefined');
      expect(result).toEqual(mockResponse);
    });

    it('should handle errors when fetching times', async () => {
      const mockError = new Error('Network error');
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(timeService.getTimes()).rejects.toThrow('Network error');
    });
  });

  describe('getVacations', () => {
    it('should fetch vacations successfully', async () => {
      const mockResponse = { 
        data: { 
          firstDate: '2023-01-01',
          lastDate: '2023-01-31',
          userVacations: []
        }
      };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await timeService.getVacations();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/timereport/vacations');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getPreviousVacations', () => {
    it('should fetch previous vacations successfully', async () => {
      const mockResponse = { data: { userVacations: [] } };
      const date = '2023-01-15';
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await timeService.getPreviousVacations(date);

      expect(mockedAxios.get).toHaveBeenCalledWith(`/api/timereport/vacations/previous?date=${date}`);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getNextVacations', () => {
    it('should fetch next vacations successfully', async () => {
      const mockResponse = { data: { userVacations: [] } };
      const date = '2023-01-15';
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await timeService.getNextVacations(date);

      expect(mockedAxios.get).toHaveBeenCalledWith(`/api/timereport/vacations/next?date=${date}`);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('updateTime', () => {
    it('should update time entry successfully', async () => {
      const mockResponse = { data: { id: 1, saved: true } };
      const timeEntry = { date: '2023-01-01', hours: 8, taskId: 1 };
      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await timeService.updateTime(timeEntry);

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/timereport', JSON.stringify(timeEntry), {'headers': {'Content-Type': 'application/json'}});
      expect(result).toEqual(mockResponse);
    });
  });
});