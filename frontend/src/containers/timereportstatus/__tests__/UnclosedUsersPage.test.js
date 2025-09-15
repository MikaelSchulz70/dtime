import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import UnclosedUsersPage from '../UnclosedUsersPage';
import TimeReportStatusService from '../../../service/TimeReportStatusService';
import SystemService from '../../../service/SystemService';
import { useToast } from '../../../components/Toast';

// Mock the services
jest.mock('../../../service/TimeReportStatusService');
jest.mock('../../../service/SystemService');
jest.mock('../../../components/Toast');

// Mock window.confirm and window.location.reload
const mockConfirm = jest.fn();
const mockReload = jest.fn();

Object.defineProperty(window, 'confirm', {
  value: mockConfirm,
  writable: true,
});

Object.defineProperty(window, 'location', {
  value: { reload: mockReload },
  writable: true,
});

describe('UnclosedUsersPage', () => {
  const mockToast = {
    showError: jest.fn(),
    showSuccess: jest.fn(),
    showWarning: jest.fn()
  };

  const mockReport = {
    fromDate: '2023-01-01',
    toDate: '2023-01-31',
    workableHours: 160,
    unclosedUsers: [
      {
        userId: 1,
        fullName: 'John Doe',
        email: 'john@example.com',
        totalTime: 150,
        closed: false
      },
      {
        userId: 2,
        fullName: 'Jane Smith',
        email: 'jane@example.com',
        totalTime: 165,
        closed: false // Changed to false since this is unclosed users view
      }
    ]
  };

  const mockSystemService = {
    isMailEnabled: jest.fn(),
    sendEmailReminderToUnclosedUsers: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
    useToast.mockReturnValue(mockToast);
    SystemService.mockImplementation(() => mockSystemService);
    
    // Default mocks
    TimeReportStatusService.getCurrentUnclosedUsers.mockResolvedValue(mockReport);
    mockSystemService.isMailEnabled.mockResolvedValue({ data: true });
    mockConfirm.mockReturnValue(true);
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('Component Loading', () => {
    it('should show loading spinner initially', () => {
      TimeReportStatusService.getCurrentUnclosedUsers.mockImplementation(() => new Promise(() => {})); // Never resolves
      
      render(<UnclosedUsersPage />);
      
      expect(screen.getByRole('status')).toBeInTheDocument();
      expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    it('should load and display unclosed users report on mount', async () => {
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(TimeReportStatusService.getCurrentUnclosedUsers).toHaveBeenCalled();
      });

      expect(screen.getByText('⚠️ Unclosed Time Reports')).toBeInTheDocument();
      expect(screen.getByText('📅 2023-01-01 - 2023-01-31')).toBeInTheDocument();
    });

    it('should check mail enabled status on mount', async () => {
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(mockSystemService.isMailEnabled).toHaveBeenCalled();
      });
    });

    it('should handle error when loading current report', async () => {
      const error = new Error('Failed to load report');
      TimeReportStatusService.getCurrentUnclosedUsers.mockRejectedValue(error);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(mockToast.showError).toHaveBeenCalledWith('Failed to load report');
      });
    });
  });

  describe('UnclosedUsersTable Component', () => {
    it('should render table headers correctly', async () => {
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('👤 User')).toBeInTheDocument();
        expect(screen.getByText('📧 Email')).toBeInTheDocument();
        expect(screen.getByText('⏱️ Total Hours')).toBeInTheDocument();
        expect(screen.getByText('📊 Workable Hours')).toBeInTheDocument();
        expect(screen.getByText('📋 Status')).toBeInTheDocument();
        expect(screen.getByText('⚙️ Action')).toBeInTheDocument();
      });
    });

    it('should render user data correctly', async () => {
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.getByText('john@example.com')).toBeInTheDocument();
        expect(screen.getByText('150 hrs')).toBeInTheDocument();
        // Check for workable hours in the table structure - there should be multiple instances
        const workableHoursCells = screen.getAllByText('160 hrs');
        expect(workableHoursCells.length).toBeGreaterThan(0);
      });
    });

    it('should show danger text for hours below workable hours', async () => {
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        const johnHours = screen.getByText('150 hrs');
        expect(johnHours).toHaveClass('text-danger');
      });
    });

    it('should not show danger text for hours equal or above workable hours', async () => {
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        const janeHours = screen.getByText('165 hrs');
        expect(janeHours).not.toHaveClass('text-danger');
      });
    });

    it('should show correct status badges', async () => {
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        // Both users are open in the default mock
        const openBadges = screen.getAllByText('⏳ Open');
        expect(openBadges.length).toBe(2);
      });
    });

    it('should render empty state when no unclosed users', async () => {
      const emptyReport = { ...mockReport, unclosedUsers: [] };
      TimeReportStatusService.getCurrentUnclosedUsers.mockResolvedValue(emptyReport);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('All users have closed their time reports for this month')).toBeInTheDocument();
      });
    });

    it('should handle null/undefined users gracefully', async () => {
      const reportWithNullUsers = {
        ...mockReport,
        unclosedUsers: [mockReport.unclosedUsers[0], null, mockReport.unclosedUsers[1]]
      };
      TimeReportStatusService.getCurrentUnclosedUsers.mockResolvedValue(reportWithNullUsers);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      });
    });

    it('should handle users with missing data', async () => {
      const reportWithIncompleteUsers = {
        ...mockReport,
        unclosedUsers: [
          {
            userId: 3,
            // Missing fullName, email, totalTime
          }
        ]
      };
      TimeReportStatusService.getCurrentUnclosedUsers.mockResolvedValue(reportWithIncompleteUsers);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('Unknown User')).toBeInTheDocument();
        expect(screen.getByText('No Email')).toBeInTheDocument();
        expect(screen.getByText('0 hrs')).toBeInTheDocument();
      });
    });
  });

  describe('User Actions', () => {
    it('should handle closing user time report', async () => {
      TimeReportStatusService.closeUserTimeReport.mockResolvedValue({ success: true });
      
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      // Target John Doe's checkbox specifically by ID 
      const checkbox = document.getElementById('1');
      fireEvent.click(checkbox);

      expect(mockConfirm).toHaveBeenCalledWith('Do you really want to close the time report for John Doe?');
      
      await waitFor(() => {
        expect(TimeReportStatusService.closeUserTimeReport).toHaveBeenCalledWith(1, '2023-01-01');
      });
    });

    it('should handle opening user time report', async () => {
      // Create a specific mock where Jane is closed for this test
      const reportWithClosedUser = {
        ...mockReport,
        unclosedUsers: [
          mockReport.unclosedUsers[0], // John (open)
          { ...mockReport.unclosedUsers[1], closed: true } // Jane (closed)
        ]
      };
      TimeReportStatusService.getCurrentUnclosedUsers.mockResolvedValueOnce(reportWithClosedUser);
      TimeReportStatusService.openUserTimeReport.mockResolvedValue({ success: true });
      
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      });

      const checkbox = screen.getByLabelText('🔓 Reopen');
      fireEvent.click(checkbox);

      // No confirmation should be shown for opening
      expect(mockConfirm).not.toHaveBeenCalled();
      
      await waitFor(() => {
        expect(TimeReportStatusService.openUserTimeReport).toHaveBeenCalledWith(2, '2023-01-01');
      });
    });

    it('should cancel action when user refuses confirmation', async () => {
      mockConfirm.mockReturnValue(false);
      
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const checkbox = document.getElementById('1');
      fireEvent.click(checkbox);

      expect(mockConfirm).toHaveBeenCalled();
      expect(TimeReportStatusService.closeUserTimeReport).not.toHaveBeenCalled();
      expect(checkbox.checked).toBe(false);
    });

    it('should handle error when toggling report status', async () => {
      const error = { response: { data: { message: 'Permission denied' } } };
      TimeReportStatusService.closeUserTimeReport.mockRejectedValue(error);
      
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const checkbox = document.getElementById('1');
      fireEvent.click(checkbox);

      await waitFor(() => {
        expect(mockToast.showError).toHaveBeenCalledWith('Permission denied');
      });

      // Checkbox should be reverted to original state
      expect(checkbox.checked).toBe(false);
    });

    it('should filter out closed users from unclosed view after closing', async () => {
      TimeReportStatusService.closeUserTimeReport.mockResolvedValue({ success: true });
      
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const checkbox = document.getElementById('1');
      fireEvent.click(checkbox);

      await waitFor(() => {
        expect(screen.queryByText('John Doe')).not.toBeInTheDocument();
      });
    });
  });

  describe('Navigation', () => {
    it('should load previous report when previous button is clicked', async () => {
      const previousReport = { ...mockReport, fromDate: '2022-12-01', toDate: '2022-12-31' };
      TimeReportStatusService.getPreviousUnclosedUsers.mockResolvedValue(previousReport);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('📅 2023-01-01 - 2023-01-31')).toBeInTheDocument();
      });

      const previousButton = screen.getByTitle('Previous Period');
      fireEvent.click(previousButton);

      await waitFor(() => {
        expect(TimeReportStatusService.getPreviousUnclosedUsers).toHaveBeenCalledWith('2023-01-01');
      });
    });

    it('should load next report when next button is clicked', async () => {
      const nextReport = { ...mockReport, fromDate: '2023-02-01', toDate: '2023-02-28' };
      TimeReportStatusService.getNextUnclosedUsers.mockResolvedValue(nextReport);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('📅 2023-01-01 - 2023-01-31')).toBeInTheDocument();
      });

      const nextButton = screen.getByTitle('Next Period');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(TimeReportStatusService.getNextUnclosedUsers).toHaveBeenCalledWith('2023-01-31');
      });
    });

    it('should handle error when loading previous report', async () => {
      const error = new Error('Failed to load previous report');
      TimeReportStatusService.getPreviousUnclosedUsers.mockRejectedValue(error);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByTitle('Previous Period')).toBeInTheDocument();
      });

      const previousButton = screen.getByTitle('Previous Period');
      fireEvent.click(previousButton);

      await waitFor(() => {
        expect(mockToast.showError).toHaveBeenCalledWith('Failed to load previous report');
      });
    });

    it('should handle error when loading next report', async () => {
      const error = new Error('Failed to load next report');
      TimeReportStatusService.getNextUnclosedUsers.mockRejectedValue(error);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByTitle('Next Period')).toBeInTheDocument();
      });

      const nextButton = screen.getByTitle('Next Period');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(mockToast.showError).toHaveBeenCalledWith('Failed to load next report');
      });
    });
  });

  describe('Email Reminders', () => {
    it('should show email reminder button when mail is enabled', async () => {
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('📧 Send Reminders')).toBeInTheDocument();
      });
    });

    it('should not show email reminder button when mail is disabled', async () => {
      mockSystemService.isMailEnabled.mockResolvedValue({ data: false });

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.queryByText('📧 Send Reminders')).not.toBeInTheDocument();
      });
    });

    it('should handle error when checking mail enabled status', async () => {
      mockSystemService.isMailEnabled.mockRejectedValue(new Error('Service unavailable'));

      render(<UnclosedUsersPage />);

      // Should default to not showing the button
      await waitFor(() => {
        expect(screen.queryByText('📧 Send Reminders')).not.toBeInTheDocument();
      });
    });

    it('should send email reminders to unclosed users', async () => {
      mockSystemService.sendEmailReminderToUnclosedUsers.mockResolvedValue({ success: true });

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('📧 Send Reminders')).toBeInTheDocument();
      });

      const sendButton = screen.getByText('📧 Send Reminders');
      fireEvent.click(sendButton);

      expect(mockConfirm).toHaveBeenCalledWith('Send email reminders to 2 users who have unclosed time reports for this month?');
      
      await waitFor(() => {
        expect(mockSystemService.sendEmailReminderToUnclosedUsers).toHaveBeenCalled();
        expect(mockToast.showSuccess).toHaveBeenCalledWith('Email reminders sent successfully to 2 users with unclosed time reports');
      });
    });

    it('should handle single user in email reminder message', async () => {
      const singleUserReport = {
        ...mockReport,
        unclosedUsers: [mockReport.unclosedUsers[0]]
      };
      TimeReportStatusService.getCurrentUnclosedUsers.mockResolvedValue(singleUserReport);
      mockSystemService.sendEmailReminderToUnclosedUsers.mockResolvedValue({ success: true });

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('📧 Send Reminders')).toBeInTheDocument();
      });

      const sendButton = screen.getByText('📧 Send Reminders');
      fireEvent.click(sendButton);

      expect(mockConfirm).toHaveBeenCalledWith('Send email reminders to 1 user who have unclosed time reports for this month?');
      
      await waitFor(() => {
        expect(mockToast.showSuccess).toHaveBeenCalledWith('Email reminders sent successfully to 1 user with unclosed time reports');
      });
    });

    it('should show warning when no unclosed users for email reminder', async () => {
      const noUsersReport = { ...mockReport, unclosedUsers: [] };
      TimeReportStatusService.getCurrentUnclosedUsers.mockResolvedValue(noUsersReport);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('📧 Send Reminders')).toBeInTheDocument();
      });

      const sendButton = screen.getByText('📧 Send Reminders');
      fireEvent.click(sendButton);

      expect(mockToast.showWarning).toHaveBeenCalledWith('No users have unclosed time reports. No emails will be sent.');
      expect(mockSystemService.sendEmailReminderToUnclosedUsers).not.toHaveBeenCalled();
    });

    it('should cancel email sending when user refuses confirmation', async () => {
      mockConfirm.mockReturnValue(false);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('📧 Send Reminders')).toBeInTheDocument();
      });

      const sendButton = screen.getByText('📧 Send Reminders');
      fireEvent.click(sendButton);

      expect(mockConfirm).toHaveBeenCalled();
      expect(mockSystemService.sendEmailReminderToUnclosedUsers).not.toHaveBeenCalled();
    });

    it('should handle error when sending email reminders', async () => {
      const error = { response: { data: { message: 'SMTP server unavailable' } } };
      mockSystemService.sendEmailReminderToUnclosedUsers.mockRejectedValue(error);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('📧 Send Reminders')).toBeInTheDocument();
      });

      const sendButton = screen.getByText('📧 Send Reminders');
      fireEvent.click(sendButton);

      await waitFor(() => {
        expect(mockToast.showError).toHaveBeenCalledWith('Failed to send email reminders: SMTP server unavailable');
      });
    });
  });

  describe('Report Updates', () => {
    it('should update report when user status changes', async () => {
      TimeReportStatusService.closeUserTimeReport.mockResolvedValue({ success: true });
      
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const checkbox = document.getElementById('1');
      fireEvent.click(checkbox);

      await waitFor(() => {
        // John should be filtered out since he's now closed (filtered from unclosed view)
        expect(screen.queryByText('John Doe')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      // Jane should still be there since she remains open
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    });

    it('should handle report with missing fromDate and toDate', async () => {
      const reportWithMissingDates = { ...mockReport, fromDate: undefined, toDate: undefined };
      TimeReportStatusService.getCurrentUnclosedUsers.mockResolvedValue(reportWithMissingDates);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        // Just verify the page renders without crashing when dates are missing
        expect(screen.getByText('⚠️ Unclosed Time Reports')).toBeInTheDocument();
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });
    });

    it('should handle report with missing workableHours', async () => {
      const reportWithMissingHours = { ...mockReport, workableHours: undefined };
      TimeReportStatusService.getCurrentUnclosedUsers.mockResolvedValue(reportWithMissingHours);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        // When workableHours is undefined, it should default to 0 hrs in each row
        const zeroHoursCells = screen.getAllByText('0 hrs');
        expect(zeroHoursCells.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Edge Cases', () => {
    it('should handle loading state with null report', () => {
      TimeReportStatusService.getCurrentUnclosedUsers.mockImplementation(() => new Promise(() => {}));
      
      render(<UnclosedUsersPage />);
      
      expect(screen.getByRole('status')).toBeInTheDocument();
    });

    it('should handle report with undefined unclosedUsers', async () => {
      const reportWithUndefinedUsers = { ...mockReport, unclosedUsers: undefined };
      TimeReportStatusService.getCurrentUnclosedUsers.mockResolvedValue(reportWithUndefinedUsers);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('Loading users...')).toBeInTheDocument();
      });
    });

    it('should handle missing user information gracefully', async () => {
      const reportWithMinimalUserData = {
        ...mockReport,
        unclosedUsers: [
          {
            userId: 999
            // Missing all other fields
          }
        ]
      };
      TimeReportStatusService.getCurrentUnclosedUsers.mockResolvedValue(reportWithMinimalUserData);

      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('Unknown User')).toBeInTheDocument();
        expect(screen.getByText('No Email')).toBeInTheDocument();
        expect(screen.getByText('0 hrs')).toBeInTheDocument();
      });
    });

    it('should handle error messages without response data', async () => {
      const error = new Error('Network timeout');
      TimeReportStatusService.closeUserTimeReport.mockRejectedValue(error);
      
      render(<UnclosedUsersPage />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const checkbox = document.getElementById('1');
      fireEvent.click(checkbox);

      await waitFor(() => {
        expect(mockToast.showError).toHaveBeenCalledWith('Network timeout');
      });
    });
  });
});