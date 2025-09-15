import axios from 'axios';
import SpecialDayService from '../SpecialDayService';

// Mock axios
jest.mock('axios');
const mockedAxios = axios;

describe('SpecialDayService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    
    // Mock console methods to avoid noise in tests
    jest.spyOn(console, 'log').mockImplementation(() => {});
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('getAllSpecialDays', () => {
    it('should fetch all special days successfully', async () => {
      const mockData = [
        { id: 1, name: 'New Year', date: '2023-01-01' },
        { id: 2, name: 'Christmas', date: '2023-12-25' }
      ];
      mockedAxios.get.mockResolvedValue({ data: mockData });

      const result = await SpecialDayService.getAllSpecialDays();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/specialday');
      expect(result).toEqual(mockData);
    });

    it('should handle errors when fetching all special days', async () => {
      const mockError = new Error('Network error');
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(SpecialDayService.getAllSpecialDays()).rejects.toThrow('Network error');
    });
  });

  describe('getAvailableYears', () => {
    it('should fetch available years successfully', async () => {
      const mockData = [2023, 2024, 2025];
      mockedAxios.get.mockResolvedValue({ data: mockData });

      const result = await SpecialDayService.getAvailableYears();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/specialday/years');
      expect(result).toEqual(mockData);
    });
  });

  describe('getSpecialDaysByYear', () => {
    it('should fetch special days by year successfully', async () => {
      const year = 2023;
      const mockData = [
        { id: 1, name: 'New Year', date: '2023-01-01' },
        { id: 2, name: 'Independence Day', date: '2023-07-04' }
      ];
      mockedAxios.get.mockResolvedValue({ data: mockData });

      const result = await SpecialDayService.getSpecialDaysByYear(year);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/specialday/year/2023');
      expect(result).toEqual(mockData);
    });
  });

  describe('getSpecialDay', () => {
    it('should fetch special day by id successfully', async () => {
      const specialDayId = 1;
      const mockData = { id: 1, name: 'New Year', date: '2023-01-01' };
      mockedAxios.get.mockResolvedValue({ data: mockData });

      const result = await SpecialDayService.getSpecialDay(specialDayId);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/specialday/1');
      expect(result).toEqual(mockData);
    });
  });

  describe('createSpecialDay', () => {
    it('should create special day successfully', async () => {
      const specialDayData = {
        name: 'Test Holiday',
        date: '2023-06-15',
        type: 'HOLIDAY'
      };
      const mockResponse = { id: 1, ...specialDayData };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await SpecialDayService.createSpecialDay(specialDayData);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/specialday',
        JSON.stringify(specialDayData),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });

    it('should handle errors when creating special day', async () => {
      const specialDayData = { name: 'Test Holiday' };
      const mockError = { response: { status: 400, data: { message: 'Invalid data' } } };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(SpecialDayService.createSpecialDay(specialDayData)).rejects.toEqual(mockError);
    });
  });

  describe('updateSpecialDay', () => {
    it('should update special day successfully', async () => {
      const specialDayId = 1;
      const specialDayData = {
        name: 'Updated Holiday',
        date: '2023-06-16',
        type: 'HOLIDAY'
      };
      const mockResponse = { id: specialDayId, ...specialDayData };
      mockedAxios.put.mockResolvedValue({ data: mockResponse });

      const result = await SpecialDayService.updateSpecialDay(specialDayId, specialDayData);

      expect(mockedAxios.put).toHaveBeenCalledWith(
        '/api/specialday/1',
        JSON.stringify({ ...specialDayData, id: specialDayId }),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });

    it('should log debug information during update', async () => {
      const specialDayId = 1;
      const specialDayData = { name: 'Test Holiday' };
      const mockResponse = { id: specialDayId, ...specialDayData };
      mockedAxios.put.mockResolvedValue({ data: mockResponse });

      await SpecialDayService.updateSpecialDay(specialDayId, specialDayData);

      expect(console.log).toHaveBeenCalledWith('Updating special day:', { 
        id: specialDayId, 
        specialDay: specialDayData 
      });
      expect(console.log).toHaveBeenCalledWith('Update headers:', { 
        headers: { 'Content-Type': 'application/json' } 
      });
    });

    it('should handle and log errors during update', async () => {
      const specialDayId = 1;
      const specialDayData = { name: 'Test Holiday' };
      const mockError = { 
        response: { status: 400, data: { message: 'Invalid data' } },
        request: {}
      };
      mockedAxios.put.mockRejectedValue(mockError);

      await expect(SpecialDayService.updateSpecialDay(specialDayId, specialDayData))
        .rejects.toEqual(mockError);

      expect(console.error).toHaveBeenCalledWith('SpecialDayService.updateSpecialDay error:', mockError);
      expect(console.error).toHaveBeenCalledWith('Error response:', mockError.response);
      expect(console.error).toHaveBeenCalledWith('Error request:', mockError.request);
    });
  });

  describe('deleteSpecialDay', () => {
    it('should delete special day successfully', async () => {
      const specialDayId = 1;
      const mockResponse = { success: true };
      mockedAxios.delete.mockResolvedValue({ data: mockResponse });

      const result = await SpecialDayService.deleteSpecialDay(specialDayId);

      expect(mockedAxios.delete).toHaveBeenCalledWith(
        '/api/specialday/1',
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });
  });

  describe('deleteSpecialDaysByYear', () => {
    it('should delete special days by year successfully', async () => {
      const year = 2023;
      const mockResponse = { deleted: 5 };
      mockedAxios.delete.mockResolvedValue({ data: mockResponse });

      const result = await SpecialDayService.deleteSpecialDaysByYear(year);

      expect(mockedAxios.delete).toHaveBeenCalledWith(
        '/api/specialday/year/2023',
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result).toEqual(mockResponse);
    });
  });

  describe('uploadSpecialDays', () => {
    it('should upload special days file successfully', async () => {
      const mockFile = new File(['special days data'], 'special_days.csv', { 
        type: 'text/csv' 
      });
      const mockResponse = { imported: 10, errors: [] };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await SpecialDayService.uploadSpecialDays(mockFile);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/specialday/upload',
        expect.any(FormData),
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      );

      // Verify FormData contains the file
      const formDataCall = mockedAxios.post.mock.calls[0][1];
      expect(formDataCall).toBeInstanceOf(FormData);
      
      expect(result).toEqual(mockResponse);
    });

    it('should handle errors during file upload', async () => {
      const mockFile = new File(['invalid data'], 'invalid.csv', { 
        type: 'text/csv' 
      });
      const mockError = { 
        response: { 
          status: 400, 
          data: { message: 'Invalid file format' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(SpecialDayService.uploadSpecialDays(mockFile)).rejects.toEqual(mockError);
    });
  });

  describe('BaseService inheritance', () => {
    it('should inherit BaseService methods', () => {
      expect(SpecialDayService.url).toBe('/api/specialday');
      expect(typeof SpecialDayService.getAll).toBe('function');
      expect(typeof SpecialDayService.get).toBe('function');
      expect(typeof SpecialDayService.create).toBe('function');
      expect(typeof SpecialDayService.update).toBe('function');
      expect(typeof SpecialDayService.delete).toBe('function');
    });
  });

  describe('Error scenarios', () => {
    it('should handle network timeout errors', async () => {
      const mockError = { code: 'ECONNABORTED', message: 'timeout of 5000ms exceeded' };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(SpecialDayService.getAllSpecialDays()).rejects.toEqual(mockError);
    });

    it('should handle server errors', async () => {
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'Internal server error' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(SpecialDayService.getAvailableYears()).rejects.toEqual(mockError);
    });

    it('should handle unauthorized errors', async () => {
      const mockError = { 
        response: { 
          status: 401, 
          data: { message: 'Unauthorized' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(SpecialDayService.createSpecialDay({})).rejects.toEqual(mockError);
    });
  });

  describe('Data validation', () => {
    it('should handle creating special day with minimal data', async () => {
      const specialDayData = { name: 'Minimal Holiday' };
      const mockResponse = { id: 1, ...specialDayData };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await SpecialDayService.createSpecialDay(specialDayData);

      expect(result).toEqual(mockResponse);
    });

    it('should handle updating special day with empty object', async () => {
      const specialDayData = {};
      const mockResponse = { id: 1, ...specialDayData };
      mockedAxios.put.mockResolvedValue({ data: mockResponse });

      const result = await SpecialDayService.updateSpecialDay(1, specialDayData);

      expect(result).toEqual(mockResponse);
    });
  });

  describe('Year parameter handling', () => {
    it('should handle string year parameter', async () => {
      const year = '2023';
      const mockData = [{ id: 1, name: 'Holiday', date: '2023-01-01' }];
      mockedAxios.get.mockResolvedValue({ data: mockData });

      const result = await SpecialDayService.getSpecialDaysByYear(year);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/specialday/year/2023');
      expect(result).toEqual(mockData);
    });

    it('should handle numeric year parameter', async () => {
      const year = 2024;
      const mockData = [{ id: 2, name: 'Holiday', date: '2024-01-01' }];
      mockedAxios.get.mockResolvedValue({ data: mockData });

      const result = await SpecialDayService.getSpecialDaysByYear(year);

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/specialday/year/2024');
      expect(result).toEqual(mockData);
    });
  });
});