import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter, Routes, Route } from 'react-router';
import UnclosedUsersPage from '../UnclosedUsersPage';
import TimeReportStatusService from '../../../service/TimeReportStatusService';
import SystemService from '../../../service/SystemService';
import { useToast } from '../../../components/Toast';
import { installMockWindowLocation } from '../../../testUtils/mockWindowLocation';

// Mock the services
jest.mock('../../../service/TimeReportStatusService');
jest.mock('../../../service/SystemService');
jest.mock('../../../components/Toast');

const mockConfirm = jest.fn(() => Promise.resolve(true));
const mockNavigate = jest.fn();
let locationMock;

jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useNavigate: () => mockNavigate,
}));

jest.mock('../../../components/ConfirmProvider', () => ({
  useConfirm: () => mockConfirm,
}));

describe('UnclosedUsersPage', () => {
  beforeAll(() => {
    locationMock = installMockWindowLocation();
  });

  afterAll(() => {
    locationMock.restore();
  });
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
    mockNavigate.mockClear();
    useToast.mockReturnValue(mockToast);
    SystemService.mockImplementation(() => mockSystemService);

    TimeReportStatusService.getUnclosedUsersForMonth.mockReset();
    TimeReportStatusService.getUnclosedUsersForMonth.mockResolvedValue(mockReport);
    TimeReportStatusService.getClosedUsersForMonth = jest.fn().mockResolvedValue({
      ...mockReport,
      unclosedUsers: [{ ...mockReport.unclosedUsers[1], closed: true }],
    });
    mockSystemService.isMailEnabled.mockResolvedValue({ data: true });
    mockConfirm.mockResolvedValue(true);
  });

  function renderPage(path = '/timereportstatus/year/2023/month/1') {
    return render(
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/timereportstatus/year/:year/month/:month" element={<UnclosedUsersPage />} />
        </Routes>
      </MemoryRouter>
    );
  }

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('Component Loading', () => {
    it('should show loading spinner initially', () => {
      TimeReportStatusService.getUnclosedUsersForMonth.mockImplementation(() => new Promise(() => {})); // Never resolves
      
      renderPage();
      
      expect(screen.getByRole('status')).toBeInTheDocument();
      expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    it('should load and display unclosed users report on mount', async () => {
      renderPage();

      await waitFor(() => {
        expect(TimeReportStatusService.getUnclosedUsersForMonth).toHaveBeenCalled();
      });

      expect(screen.getByText('Unclosed Time Reports')).toBeInTheDocument();
      expect(screen.getByText('📅 2023-01-01 - 2023-01-31')).toBeInTheDocument();
    });

    it('should check mail enabled status on mount', async () => {
      renderPage();

      await waitFor(() => {
        expect(mockSystemService.isMailEnabled).toHaveBeenCalled();
      });
    });

    it('should handle error when loading current report', async () => {
      const error = new Error('Failed to load report');
      TimeReportStatusService.getUnclosedUsersForMonth.mockRejectedValue(error);

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('Invalid period')).toBeInTheDocument();
        expect(screen.getByText('Failed to load report')).toBeInTheDocument();
      });
      expect(mockToast.showError).not.toHaveBeenCalled();
    });
  });

  describe('UnclosedUsersTable Component', () => {
    it('should render table headers correctly', async () => {
      renderPage();

      await waitFor(() => {
        expect(screen.getByText('👤 User')).toBeInTheDocument();
        expect(screen.getByText('📧 Email')).toBeInTheDocument();
        expect(screen.getByText('⏱️ Total Hours')).toBeInTheDocument();
        expect(screen.getByText('📊 Workable Hours')).toBeInTheDocument();
        expect(screen.getByText('📋 Status')).toBeInTheDocument();
        expect(screen.getByText('⚙️ Actions')).toBeInTheDocument();
      });
    });

    it('should render user data correctly', async () => {
      renderPage();

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.getByText('john@example.com')).toBeInTheDocument();
        expect(screen.getByText('150')).toBeInTheDocument();
        // Check for workable hours in the table structure - there should be multiple instances
        const workableHoursCells = screen.getAllByText('160');
        expect(workableHoursCells.length).toBeGreaterThan(0);
      });
    });

    it('should show danger text for hours below workable hours', async () => {
      renderPage();

      await waitFor(() => {
        const johnHours = screen.getByText('150');
        expect(johnHours).toHaveClass('text-danger');
      });
    });

    it('should not show danger text for hours equal or above workable hours', async () => {
      renderPage();

      await waitFor(() => {
        const janeHours = screen.getByText('165');
        expect(janeHours).not.toHaveClass('text-danger');
      });
    });

    it('should show correct status badges', async () => {
      renderPage();

      await waitFor(() => {
        // Both users are open in the default mock
        const openBadges = screen.getAllByText('⏳ Open');
        expect(openBadges.length).toBe(2);
      });
    });

    it('should render empty state when no unclosed users', async () => {
      const emptyReport = { ...mockReport, unclosedUsers: [] };
      TimeReportStatusService.getUnclosedUsersForMonth.mockResolvedValue(emptyReport);

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('All users have closed their time reports for this month')).toBeInTheDocument();
      });
    });

    it('should handle null/undefined users gracefully', async () => {
      const reportWithNullUsers = {
        ...mockReport,
        unclosedUsers: [mockReport.unclosedUsers[0], null, mockReport.unclosedUsers[1]]
      };
      TimeReportStatusService.getUnclosedUsersForMonth.mockResolvedValue(reportWithNullUsers);

      renderPage();

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
      TimeReportStatusService.getUnclosedUsersForMonth.mockResolvedValue(reportWithIncompleteUsers);

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('Unknown User')).toBeInTheDocument();
        expect(screen.getByText('No Email')).toBeInTheDocument();
        expect(screen.getByText('0')).toBeInTheDocument();
      });
    });
  });

  describe('User Actions', () => {
    it('should handle closing user time report', async () => {
      TimeReportStatusService.closeUserTimeReport.mockResolvedValue({ success: true });
      
      renderPage();

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      // Target John Doe's checkbox specifically by ID 
      const checkbox = document.getElementById('1');
      fireEvent.click(checkbox);

      await waitFor(() => {
        expect(mockConfirm).toHaveBeenCalledWith(expect.objectContaining({
          message: 'Do you really want to close the time report for John Doe?',
        }));
      });

      await waitFor(() => {
        expect(TimeReportStatusService.closeUserTimeReport).toHaveBeenCalledWith(1, '2023-01-01');
      });
    });

    it('should handle opening user time report from closed view', async () => {
      const closedUserReport = {
        ...mockReport,
        unclosedUsers: [{ ...mockReport.unclosedUsers[1], closed: true }],
      };
      TimeReportStatusService.getClosedUsersForMonth.mockResolvedValue(closedUserReport);
      TimeReportStatusService.openUserTimeReport.mockResolvedValue({ success: true });

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      fireEvent.change(screen.getByRole('combobox'), { target: { value: 'closed' } });

      await waitFor(() => {
        expect(TimeReportStatusService.getClosedUsersForMonth).toHaveBeenCalledWith(2023, 1);
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      });

      const checkbox = screen.getByLabelText('🔓 Reopen');
      fireEvent.click(checkbox);

      await waitFor(() => {
        expect(mockConfirm).toHaveBeenCalledWith(expect.objectContaining({
          message: 'Do you really want to reopen the time report for Jane Smith?',
          confirmLabel: 'Reopen',
        }));
      });

      await waitFor(() => {
        expect(TimeReportStatusService.openUserTimeReport).toHaveBeenCalledWith(2, '2023-01-01');
        expect(screen.queryByText('Jane Smith')).not.toBeInTheDocument();
      });
    });

    it('should load closed users when closed filter is selected', async () => {
      renderPage();

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      fireEvent.change(screen.getByRole('combobox'), { target: { value: 'closed' } });

      await waitFor(() => {
        expect(TimeReportStatusService.getClosedUsersForMonth).toHaveBeenCalledWith(2023, 1);
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
        expect(screen.queryByText('John Doe')).not.toBeInTheDocument();
      });
    });

    it('should cancel action when user refuses confirmation', async () => {
      mockConfirm.mockResolvedValue(false);

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const checkbox = document.getElementById('1');
      fireEvent.click(checkbox);

      await waitFor(() => {
        expect(mockConfirm).toHaveBeenCalled();
      });
      expect(TimeReportStatusService.closeUserTimeReport).not.toHaveBeenCalled();
      expect(checkbox.checked).toBe(false);
    });

    it('should handle error when toggling report status', async () => {
      const error = { response: { data: { message: 'Permission denied' } } };
      TimeReportStatusService.closeUserTimeReport.mockRejectedValue(error);
      
      renderPage();

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

    it('should remove user from unclosed view after closing a report', async () => {
      TimeReportStatusService.closeUserTimeReport.mockResolvedValue({ success: true });
      
      renderPage();

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
      TimeReportStatusService.getUnclosedUsersForMonth
        .mockResolvedValueOnce(mockReport)
        .mockResolvedValueOnce(previousReport);

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('📅 2023-01-01 - 2023-01-31')).toBeInTheDocument();
      });

      const previousButton = screen.getByTitle('Previous Period');
      fireEvent.click(previousButton);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/timereportstatus/year/2022/month/12');
      });
    });

    it('should load next report when next button is clicked', async () => {
      const nextReport = { ...mockReport, fromDate: '2023-02-01', toDate: '2023-02-28' };
      TimeReportStatusService.getUnclosedUsersForMonth
        .mockResolvedValueOnce(mockReport)
        .mockResolvedValueOnce(nextReport);

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('📅 2023-01-01 - 2023-01-31')).toBeInTheDocument();
      });

      const nextButton = screen.getByTitle('Next Period');
      fireEvent.click(nextButton);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/timereportstatus/year/2023/month/2');
      });
    });

    it('should show inline error when loading a period fails', async () => {
      const error = new Error('Failed to load previous report');
      TimeReportStatusService.getUnclosedUsersForMonth.mockRejectedValue(error);

      renderPage('/timereportstatus/year/2022/month/12');

      await waitFor(() => {
        expect(screen.getByText('Invalid period')).toBeInTheDocument();
        expect(screen.getByText('Failed to load previous report')).toBeInTheDocument();
      });
      expect(mockToast.showError).not.toHaveBeenCalled();
    });
  });

  describe('Email Reminders', () => {
    it('should show email reminder button when mail is enabled', async () => {
      renderPage();

      await waitFor(() => {
        expect(screen.getByText('📧 Send Reminders')).toBeInTheDocument();
      });
    });

    it('should not show email reminder button when mail is disabled', async () => {
      mockSystemService.isMailEnabled.mockResolvedValue({ data: false });

      renderPage();

      await waitFor(() => {
        expect(screen.queryByText('📧 Send Reminders')).not.toBeInTheDocument();
      });
    });

    it('should handle error when checking mail enabled status', async () => {
      mockSystemService.isMailEnabled.mockRejectedValue(new Error('Service unavailable'));

      renderPage();

      // Should default to not showing the button
      await waitFor(() => {
        expect(screen.queryByText('📧 Send Reminders')).not.toBeInTheDocument();
      });
    });

    it('should send email reminders to unclosed users', async () => {
      mockSystemService.sendEmailReminderToUnclosedUsers.mockResolvedValue({ success: true });

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('📧 Send Reminders')).toBeInTheDocument();
      });

      const sendButton = screen.getByText('📧 Send Reminders');
      fireEvent.click(sendButton);

      expect(mockConfirm).toHaveBeenCalledWith(expect.objectContaining({
        message: 'Send email reminders to 2 users who have unclosed time reports for this month?',
      }));
      
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
      TimeReportStatusService.getUnclosedUsersForMonth.mockResolvedValue(singleUserReport);
      mockSystemService.sendEmailReminderToUnclosedUsers.mockResolvedValue({ success: true });

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('📧 Send Reminders')).toBeInTheDocument();
      });

      const sendButton = screen.getByText('📧 Send Reminders');
      fireEvent.click(sendButton);

      expect(mockConfirm).toHaveBeenCalledWith(expect.objectContaining({
        message: 'Send email reminders to 1 user who have unclosed time reports for this month?',
      }));
      
      await waitFor(() => {
        expect(mockToast.showSuccess).toHaveBeenCalledWith('Email reminders sent successfully to 1 user with unclosed time reports');
      });
    });

    it('should show warning when no unclosed users for email reminder', async () => {
      const noUsersReport = { ...mockReport, unclosedUsers: [] };
      TimeReportStatusService.getUnclosedUsersForMonth.mockResolvedValue(noUsersReport);

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('📧 Send Reminders')).toBeInTheDocument();
      });

      const sendButton = screen.getByText('📧 Send Reminders');
      fireEvent.click(sendButton);

      expect(mockToast.showWarning).toHaveBeenCalledWith('No users have unclosed time reports. No emails will be sent.');
      expect(mockSystemService.sendEmailReminderToUnclosedUsers).not.toHaveBeenCalled();
    });

    it('should cancel email sending when user refuses confirmation', async () => {
      mockConfirm.mockResolvedValue(false);

      renderPage();

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

      renderPage();

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
      
      renderPage();

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const checkbox = document.getElementById('1');
      fireEvent.click(checkbox);

      await waitFor(() => {
        expect(screen.queryByText('John Doe')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      // Jane should still be there since she remains open
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    });

    it('should handle report with missing fromDate and toDate', async () => {
      const reportWithMissingDates = { ...mockReport, fromDate: undefined, toDate: undefined };
      TimeReportStatusService.getUnclosedUsersForMonth.mockResolvedValue(reportWithMissingDates);

      renderPage();

      await waitFor(() => {
        // Just verify the page renders without crashing when dates are missing
        expect(screen.getByText('Unclosed Time Reports')).toBeInTheDocument();
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });
    });

    it('should handle report with missing workableHours', async () => {
      const reportWithMissingHours = { ...mockReport, workableHours: undefined };
      TimeReportStatusService.getUnclosedUsersForMonth.mockResolvedValue(reportWithMissingHours);

      renderPage();

      await waitFor(() => {
        // When workableHours is undefined, it should default to 0 in each row
        const zeroHoursCells = screen.getAllByText('0');
        expect(zeroHoursCells.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Edge Cases', () => {
    it('should handle loading state with null report', () => {
      TimeReportStatusService.getUnclosedUsersForMonth.mockImplementation(() => new Promise(() => {}));
      
      renderPage();
      
      expect(screen.getByRole('status')).toBeInTheDocument();
    });

    it('should treat undefined unclosedUsers as empty list', async () => {
      const reportWithUndefinedUsers = { ...mockReport, unclosedUsers: undefined };
      TimeReportStatusService.getUnclosedUsersForMonth.mockResolvedValue(reportWithUndefinedUsers);

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('All users have closed their time reports for this month')).toBeInTheDocument();
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
      TimeReportStatusService.getUnclosedUsersForMonth.mockResolvedValue(reportWithMinimalUserData);

      renderPage();

      await waitFor(() => {
        expect(screen.getByText('Unknown User')).toBeInTheDocument();
        expect(screen.getByText('No Email')).toBeInTheDocument();
        expect(screen.getByText('0')).toBeInTheDocument();
      });
    });

    it('should handle error messages without response data', async () => {
      const error = new Error('Network timeout');
      TimeReportStatusService.closeUserTimeReport.mockRejectedValue(error);
      
      renderPage();

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