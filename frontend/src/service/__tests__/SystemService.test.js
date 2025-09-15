import axios from 'axios';
import SystemService from '../SystemService';
import { Headers } from '../ServiceUtil';

// Mock axios
jest.mock('axios');
const mockedAxios = axios;

// Mock ServiceUtil
jest.mock('../ServiceUtil', () => ({
  Headers: jest.fn(() => ({ headers: { 'Content-Type': 'application/json' } }))
}));

describe('SystemService', () => {
  let systemService;

  beforeEach(() => {
    systemService = new SystemService();
    jest.clearAllMocks();
  });

  describe('getSystemConfig', () => {
    it('should fetch system configuration successfully', async () => {
      const mockConfig = {
        appName: 'DTime',
        version: '1.0.0',
        environment: 'production',
        features: {
          emailReminders: true,
          timeTracking: true,
          reporting: true
        },
        limits: {
          maxUsers: 100,
          maxProjects: 50
        }
      };
      mockedAxios.get.mockResolvedValue({ data: mockConfig });

      const result = await systemService.getSystemConfig();

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/system/config');
      expect(result.data).toEqual(mockConfig);
    });

    it('should handle errors when fetching system config', async () => {
      const mockError = new Error('Config service unavailable');
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(systemService.getSystemConfig()).rejects.toThrow('Config service unavailable');
    });

    it('should handle unauthorized access to system config', async () => {
      const mockError = { 
        response: { 
          status: 403, 
          data: { message: 'Access denied to system configuration' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(systemService.getSystemConfig()).rejects.toEqual(mockError);
    });

    it('should handle server errors', async () => {
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'Internal server error' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(systemService.getSystemConfig()).rejects.toEqual(mockError);
    });
  });

  describe('udateProperty', () => {
    // Note: Method name has typo "udateProperty" instead of "updateProperty"
    it('should update system property successfully', async () => {
      const propertyData = {
        key: 'email.smtp.host',
        value: 'smtp.example.com',
        description: 'SMTP server host'
      };
      const mockResponse = { success: true, updated: propertyData };
      mockedAxios.put.mockResolvedValue({ data: mockResponse });

      const result = await systemService.udateProperty(propertyData);

      expect(mockedAxios.put).toHaveBeenCalledWith(
        '/api/system/systemproperty',
        JSON.stringify(propertyData),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle complex property values', async () => {
      const complexProperty = {
        key: 'app.settings',
        value: {
          theme: 'dark',
          features: ['reporting', 'analytics'],
          timeout: 30000
        }
      };
      const mockResponse = { success: true };
      mockedAxios.put.mockResolvedValue({ data: mockResponse });

      const result = await systemService.udateProperty(complexProperty);

      expect(mockedAxios.put).toHaveBeenCalledWith(
        '/api/system/systemproperty',
        JSON.stringify(complexProperty),
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle validation errors during property update', async () => {
      const invalidProperty = { key: '', value: null };
      const mockError = { 
        response: { 
          status: 400, 
          data: { 
            message: 'Validation failed',
            errors: { key: 'Property key is required' }
          } 
        } 
      };
      mockedAxios.put.mockRejectedValue(mockError);

      await expect(systemService.udateProperty(invalidProperty)).rejects.toEqual(mockError);
    });

    it('should handle unauthorized property updates', async () => {
      const propertyData = { key: 'admin.secret', value: 'new-secret' };
      const mockError = { 
        response: { 
          status: 403, 
          data: { message: 'Insufficient permissions to update system property' } 
        } 
      };
      mockedAxios.put.mockRejectedValue(mockError);

      await expect(systemService.udateProperty(propertyData)).rejects.toEqual(mockError);
    });

    it('should handle property not found errors', async () => {
      const propertyData = { key: 'nonexistent.property', value: 'value' };
      const mockError = { 
        response: { 
          status: 404, 
          data: { message: 'System property not found' } 
        } 
      };
      mockedAxios.put.mockRejectedValue(mockError);

      await expect(systemService.udateProperty(propertyData)).rejects.toEqual(mockError);
    });
  });

  describe('sendEmailReminder', () => {
    it('should send email reminder successfully', async () => {
      const mockResponse = { 
        success: true, 
        emailsSent: 15,
        message: 'Email reminders sent successfully' 
      };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await systemService.sendEmailReminder();

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/system/emailreminder',
        null,
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle email service unavailable errors', async () => {
      const mockError = { 
        response: { 
          status: 503, 
          data: { message: 'Email service temporarily unavailable' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(systemService.sendEmailReminder()).rejects.toEqual(mockError);
    });

    it('should handle mail configuration errors', async () => {
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'SMTP configuration invalid' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(systemService.sendEmailReminder()).rejects.toEqual(mockError);
    });

    it('should handle unauthorized email reminder requests', async () => {
      const mockError = { 
        response: { 
          status: 403, 
          data: { message: 'Not authorized to send email reminders' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(systemService.sendEmailReminder()).rejects.toEqual(mockError);
    });

    it('should send request with null body', async () => {
      mockedAxios.post.mockResolvedValue({ data: { success: true } });

      await systemService.sendEmailReminder();

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/system/emailreminder',
        null,
        { headers: { 'Content-Type': 'application/json' } }
      );
    });
  });

  describe('sendEmailReminderToUnclosedUsers', () => {
    it('should send email reminder to unclosed users successfully', async () => {
      const mockResponse = { 
        success: true, 
        unclosedUsers: 8,
        emailsSent: 8,
        message: 'Email reminders sent to users with unclosed time entries' 
      };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await systemService.sendEmailReminderToUnclosedUsers();

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/system/emailreminder/unclosed',
        null,
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle no unclosed users scenario', async () => {
      const mockResponse = { 
        success: true, 
        unclosedUsers: 0,
        emailsSent: 0,
        message: 'No users with unclosed time entries found' 
      };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await systemService.sendEmailReminderToUnclosedUsers();

      expect(result.data).toEqual(mockResponse);
    });

    it('should handle email service errors for unclosed reminders', async () => {
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'Failed to query unclosed time entries' } 
        } 
      };
      mockedAxios.post.mockRejectedValue(mockError);

      await expect(systemService.sendEmailReminderToUnclosedUsers()).rejects.toEqual(mockError);
    });

    it('should handle partial email failures', async () => {
      const mockResponse = { 
        success: false, 
        unclosedUsers: 10,
        emailsSent: 7,
        failures: 3,
        message: 'Some email reminders failed to send' 
      };
      mockedAxios.post.mockResolvedValue({ data: mockResponse });

      const result = await systemService.sendEmailReminderToUnclosedUsers();

      expect(result.data.success).toBe(false);
      expect(result.data.failures).toBe(3);
    });

    it('should send request with null body for unclosed reminders', async () => {
      mockedAxios.post.mockResolvedValue({ data: { success: true } });

      await systemService.sendEmailReminderToUnclosedUsers();

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/system/emailreminder/unclosed',
        null,
        { headers: { 'Content-Type': 'application/json' } }
      );
    });
  });

  describe('isMailEnabled', () => {
    it('should check mail enabled status successfully', async () => {
      const mockResponse = { 
        enabled: true, 
        configured: true,
        smtpHost: 'smtp.example.com',
        testConnection: 'success'
      };
      mockedAxios.get.mockResolvedValue({ data: mockResponse });

      const result = await systemService.isMailEnabled();

      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/system/mail/enabled',
        { headers: { 'Content-Type': 'application/json' } }
      );
      expect(Headers).toHaveBeenCalled();
      expect(result.data).toEqual(mockResponse);
    });

    it('should handle mail disabled scenario', async () => {
      const mockResponse = { 
        enabled: false, 
        configured: false,
        reason: 'SMTP configuration missing'
      };
      mockedAxios.get.mockResolvedValue({ data: mockResponse });

      const result = await systemService.isMailEnabled();

      expect(result.data.enabled).toBe(false);
      expect(result.data.reason).toBe('SMTP configuration missing');
    });

    it('should handle mail configuration check errors', async () => {
      const mockError = { 
        response: { 
          status: 500, 
          data: { message: 'Unable to verify mail configuration' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(systemService.isMailEnabled()).rejects.toEqual(mockError);
    });

    it('should handle unauthorized mail status check', async () => {
      const mockError = { 
        response: { 
          status: 403, 
          data: { message: 'Not authorized to check mail configuration' } 
        } 
      };
      mockedAxios.get.mockRejectedValue(mockError);

      await expect(systemService.isMailEnabled()).rejects.toEqual(mockError);
    });

    it('should include headers in mail enabled request', async () => {
      mockedAxios.get.mockResolvedValue({ data: { enabled: true } });

      await systemService.isMailEnabled();

      expect(Headers).toHaveBeenCalled();
      expect(mockedAxios.get).toHaveBeenCalledWith(
        '/api/system/mail/enabled',
        { headers: { 'Content-Type': 'application/json' } }
      );
    });
  });

  describe('Headers usage', () => {
    it('should use Headers for authenticated requests', async () => {
      mockedAxios.put.mockResolvedValue({ data: {} });
      mockedAxios.post.mockResolvedValue({ data: {} });
      mockedAxios.get.mockResolvedValue({ data: {} });

      await systemService.udateProperty({});
      await systemService.sendEmailReminder();
      await systemService.sendEmailReminderToUnclosedUsers();
      await systemService.isMailEnabled();

      expect(Headers).toHaveBeenCalledTimes(4);
    });

    it('should not use Headers for public config requests', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await systemService.getSystemConfig();

      // Headers should not be called for getSystemConfig
      expect(Headers).not.toHaveBeenCalled();
    });
  });

  describe('HTTP methods usage', () => {
    it('should use GET for reading operations', async () => {
      mockedAxios.get.mockResolvedValue({ data: {} });

      await systemService.getSystemConfig();
      await systemService.isMailEnabled();

      expect(mockedAxios.get).toHaveBeenCalledTimes(2);
    });

    it('should use PUT for update operations', async () => {
      mockedAxios.put.mockResolvedValue({ data: {} });

      await systemService.udateProperty({ key: 'test', value: 'value' });

      expect(mockedAxios.put).toHaveBeenCalledTimes(1);
    });

    it('should use POST for action operations', async () => {
      mockedAxios.post.mockResolvedValue({ data: {} });

      await systemService.sendEmailReminder();
      await systemService.sendEmailReminderToUnclosedUsers();

      expect(mockedAxios.post).toHaveBeenCalledTimes(2);
    });
  });

  describe('Error handling patterns', () => {
    const errorScenarios = [
      { status: 400, method: 'udateProperty', description: 'Bad Request' },
      { status: 401, method: 'sendEmailReminder', description: 'Unauthorized' },
      { status: 403, method: 'isMailEnabled', description: 'Forbidden' },
      { status: 404, method: 'getSystemConfig', description: 'Not Found' },
      { status: 500, method: 'sendEmailReminderToUnclosedUsers', description: 'Internal Server Error' }
    ];

    errorScenarios.forEach(scenario => {
      it(`should handle ${scenario.status} ${scenario.description} errors for ${scenario.method}`, async () => {
        const mockError = { 
          response: { 
            status: scenario.status, 
            data: { message: scenario.description } 
          } 
        };
        
        if (scenario.method === 'udateProperty') {
          mockedAxios.put.mockRejectedValue(mockError);
          await expect(systemService[scenario.method]({})).rejects.toEqual(mockError);
        } else if (scenario.method === 'sendEmailReminder' || scenario.method === 'sendEmailReminderToUnclosedUsers') {
          mockedAxios.post.mockRejectedValue(mockError);
          await expect(systemService[scenario.method]()).rejects.toEqual(mockError);
        } else {
          mockedAxios.get.mockRejectedValue(mockError);
          await expect(systemService[scenario.method]()).rejects.toEqual(mockError);
        }
      });
    });
  });

  describe('Integration scenarios', () => {
    it('should handle complete email reminder workflow', async () => {
      // First check if mail is enabled
      mockedAxios.get.mockResolvedValueOnce({ data: { enabled: true, configured: true } });
      const mailStatus = await systemService.isMailEnabled();
      expect(mailStatus.data.enabled).toBe(true);

      // Then send general reminders
      mockedAxios.post.mockResolvedValueOnce({ data: { success: true, emailsSent: 10 } });
      const generalReminders = await systemService.sendEmailReminder();
      expect(generalReminders.data.success).toBe(true);

      // Finally send reminders to unclosed users
      mockedAxios.post.mockResolvedValueOnce({ data: { success: true, unclosedUsers: 5, emailsSent: 5 } });
      const unclosedReminders = await systemService.sendEmailReminderToUnclosedUsers();
      expect(unclosedReminders.data.unclosedUsers).toBe(5);
    });

    it('should handle mail disabled scenario', async () => {
      mockedAxios.get.mockResolvedValueOnce({ data: { enabled: false, reason: 'Not configured' } });
      const mailStatus = await systemService.isMailEnabled();
      expect(mailStatus.data.enabled).toBe(false);

      // Reminders might still be attempted but would likely fail
      const mailError = { response: { status: 503, data: { message: 'Mail service disabled' } } };
      mockedAxios.post.mockRejectedValueOnce(mailError);
      
      await expect(systemService.sendEmailReminder()).rejects.toEqual(mailError);
    });
  });
});