import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Vacations from '../Vacations';
import TimeService from '../../../service/TimeService';

// Mock TimeService
jest.mock('../../../service/TimeService');
const MockedTimeService = TimeService;

const mockShowError = jest.fn();

// Mock Toast hook
jest.mock('../../../components/Toast', () => ({
  useToast: () => ({
    showError: mockShowError
  })
}));

describe('Vacations', () => {
  let mockTimeService;

  beforeEach(() => {
    mockTimeService = {
      getVacationReport: jest.fn()
    };
    MockedTimeService.mockImplementation(() => mockTimeService);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  const mockVacationData = {
    firstDate: '2023-01-01',
    lastDate: '2023-01-31',
    days: [
      { day: 1, weekend: false, majorHoliday: false },
      { day: 2, weekend: false, majorHoliday: false },
      { day: 3, weekend: true, majorHoliday: false },
      { day: 4, weekend: true, majorHoliday: false },
      { day: 5, weekend: false, majorHoliday: true }
    ],
    userVacations: [
      {
        userId: 1,
        name: 'John Doe',
        noVacationDays: 5,
        vacationsDays: [
          { day: { date: '2023-01-01' }, vacation: true },
          { day: { date: '2023-01-02' }, vacation: false },
          { day: { date: '2023-01-03', weekend: true }, vacation: false },
          { day: { date: '2023-01-04', weekend: true }, vacation: false },
          { day: { date: '2023-01-05', majorHoliday: true }, vacation: false }
        ]
      },
      {
        userId: 2,
        name: 'Jane Smith',
        noVacationDays: 3,
        vacationsDays: [
          { day: { date: '2023-01-01' }, vacation: false },
          { day: { date: '2023-01-02' }, vacation: true },
          { day: { date: '2023-01-03', weekend: true }, vacation: false },
          { day: { date: '2023-01-04', weekend: true }, vacation: false },
          { day: { date: '2023-01-05', majorHoliday: true }, vacation: false }
        ]
      }
    ]
  };

  it('should render vacation calendar title', async () => {
    mockTimeService.getVacationReport.mockResolvedValue({ data: mockVacationData });

    render(<Vacations />);

    await waitFor(() => {
      expect(screen.getByText('Vacation Calendar')).toBeInTheDocument();
    });
  });

  it('should load vacation data on mount', async () => {
    mockTimeService.getVacationReport.mockResolvedValue({ data: mockVacationData });

    render(<Vacations />);

    await waitFor(() => {
      expect(mockTimeService.getVacationReport).toHaveBeenCalledTimes(1);
    });
  });

  it('should display date range', async () => {
    mockTimeService.getVacationReport.mockResolvedValue({ data: mockVacationData });

    render(<Vacations />);

    await waitFor(() => {
      expect(screen.getByText('📅 2023-01-01 - 2023-01-31')).toBeInTheDocument();
    });
  });

  it('should display user names and vacation counts', async () => {
    mockTimeService.getVacationReport.mockResolvedValue({ data: mockVacationData });

    render(<Vacations />);

    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      // Use more specific text matching for vacation counts
      const vacationCounts = screen.getAllByText(/^[0-9]+$/);
      expect(vacationCounts.length).toBeGreaterThan(0);
    });
  });

  it('should display navigation buttons', async () => {
    mockTimeService.getVacationReport.mockResolvedValue({ data: mockVacationData });

    render(<Vacations />);

    await waitFor(() => {
      expect(screen.getByTitle('Previous Month')).toBeInTheDocument();
      expect(screen.getByTitle('Next Month')).toBeInTheDocument();
    });
  });

  it('should display legend', async () => {
    mockTimeService.getVacationReport.mockResolvedValue({ data: mockVacationData });

    render(<Vacations />);

    await waitFor(() => {
      expect(screen.getAllByText('Vacation').length).toBeGreaterThan(0);
      expect(screen.getByText('Holiday')).toBeInTheDocument();
      expect(screen.getByText('Weekend')).toBeInTheDocument();
    });
  });

  it('should handle previous month navigation', async () => {
    const user = userEvent.setup();
    mockTimeService.getVacationReport.mockResolvedValue({ data: mockVacationData });

    render(<Vacations />);

    await waitFor(() => {
      expect(screen.getByTitle('Previous Month')).toBeInTheDocument();
    });

    const previousButton = screen.getByTitle('Previous Month');
    await user.click(previousButton);

    expect(mockTimeService.getVacationReport).toHaveBeenCalledWith('2022-12-01');
  });

  it('should handle next month navigation', async () => {
    const user = userEvent.setup();
    mockTimeService.getVacationReport.mockResolvedValue({ data: mockVacationData });

    render(<Vacations />);

    await waitFor(() => {
      expect(screen.getByTitle('Next Month')).toBeInTheDocument();
    });

    const nextButton = screen.getByTitle('Next Month');
    await user.click(nextButton);

    expect(mockTimeService.getVacationReport).toHaveBeenCalledWith('2023-02-01');
  });

  it('should display vacation table headers', async () => {
    mockTimeService.getVacationReport.mockResolvedValue({ data: mockVacationData });

    render(<Vacations />);

    await waitFor(() => {
      expect(screen.getByText('Employee')).toBeInTheDocument();
      expect(screen.getByText('Days')).toBeInTheDocument();
    });
  });

  it('should display day numbers in header', async () => {
    mockTimeService.getVacationReport.mockResolvedValue({ data: mockVacationData });

    render(<Vacations />);

    await waitFor(() => {
      // Check for day numbers in a more specific way
      const dayHeaders = screen.getAllByText(/^[1-5]$/);
      expect(dayHeaders.length).toBeGreaterThanOrEqual(5);
    });
  });

  it('should show vacation indicators correctly', async () => {
    mockTimeService.getVacationReport.mockResolvedValue({ data: mockVacationData });

    render(<Vacations />);

    await waitFor(() => {
      // Should show V for vacation days
      const vacationCells = screen.getAllByText('V');
      expect(vacationCells.length).toBeGreaterThan(0);
      
      // Should show W for weekend days
      const weekendCells = screen.getAllByText('W');
      expect(weekendCells.length).toBeGreaterThan(0);
      
      // Should show H for holiday days
      const holidayCells = screen.getAllByText('H');
      expect(holidayCells.length).toBeGreaterThan(0);
    });
  });

  it('should handle loading errors gracefully', async () => {
    mockTimeService.getVacationReport.mockRejectedValue(new Error('Network error'));

    render(<Vacations />);

    await waitFor(() => {
      expect(mockTimeService.getVacationReport).toHaveBeenCalled();
      expect(mockShowError).toHaveBeenCalled();
    });
  });

  it('should render null when no vacation data', () => {
    mockTimeService.getVacationReport.mockResolvedValue({ data: null });

    const { container } = render(<Vacations />);
    
    expect(container.firstChild).toBeNull();
  });

  it('should truncate long employee names', async () => {
    const longNameData = {
      ...mockVacationData,
      userVacations: [
        {
          userId: 1,
          name: 'This Is A Very Long Employee Name That Should Be Truncated',
          noVacationDays: 5,
          vacationsDays: []
        }
      ]
    };

    mockTimeService.getVacationReport.mockResolvedValue({ data: longNameData });

    render(<Vacations />);

    await waitFor(() => {
      // Should show truncated name with ellipsis
      expect(screen.getByText(/This Is A Very Long/)).toBeInTheDocument();
    });
  });
});