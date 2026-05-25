import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router';
import Vacations from '../Vacations';
import TimeService from '../../../service/TimeService';

jest.mock('../../../service/TimeService');
const MockedTimeService = TimeService;

const mockNavigate = jest.fn();
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useNavigate: () => mockNavigate,
}));

function renderVacations(path = '/vacations/year/2023/month/1') {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/vacations/year/:year/month/:month" element={<Vacations />} />
      </Routes>
    </MemoryRouter>
  );
}

describe('Vacations', () => {
  let mockTimeService;

  beforeEach(() => {
    mockNavigate.mockClear();
    mockTimeService = {
      getVacationReportForMonth: jest.fn()
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
    mockTimeService.getVacationReportForMonth.mockResolvedValue({ data: mockVacationData });

    renderVacations();

    await waitFor(() => {
      expect(screen.getByText('Vacation Calendar')).toBeInTheDocument();
    });
  });

  it('should load vacation data on mount', async () => {
    mockTimeService.getVacationReportForMonth.mockResolvedValue({ data: mockVacationData });

    renderVacations();

    await waitFor(() => {
      expect(mockTimeService.getVacationReportForMonth).toHaveBeenCalledWith(2023, 1);
    });
  });

  it('should navigate to previous month', async () => {
    mockTimeService.getVacationReportForMonth.mockResolvedValue({ data: mockVacationData });
    const user = userEvent.setup();

    renderVacations('/vacations/year/2023/month/2');

    await waitFor(() => {
      expect(screen.getByText('Vacation Calendar')).toBeInTheDocument();
    });

    await user.click(screen.getAllByRole('button')[0]);

    expect(mockNavigate).toHaveBeenCalledWith('/vacations/year/2023/month/1');
  });

  it('should navigate to next month', async () => {
    mockTimeService.getVacationReportForMonth.mockResolvedValue({ data: mockVacationData });
    const user = userEvent.setup();

    renderVacations('/vacations/year/2023/month/1');

    await waitFor(() => {
      expect(screen.getByText('Vacation Calendar')).toBeInTheDocument();
    });

    await user.click(screen.getAllByRole('button')[1]);

    expect(mockNavigate).toHaveBeenCalledWith('/vacations/year/2023/month/2');
  });

  it('should show inline error on load failure', async () => {
    mockTimeService.getVacationReportForMonth.mockRejectedValue(new Error('Network error'));

    renderVacations();

    await waitFor(() => {
      expect(screen.getByText('Invalid period')).toBeInTheDocument();
      expect(screen.getByText('Network error')).toBeInTheDocument();
    });
  });
});
