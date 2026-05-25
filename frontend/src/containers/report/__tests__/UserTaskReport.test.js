import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import UserTaskReportTable from '../UserTaskReport';
import TimeService from '../../../service/TimeService';
import * as Constants from '../../../common/Constants';

jest.mock('../../../service/TimeService');

jest.mock('../../../components/Charts', () => ({
    UserTaskBarChart: () => <div data-testid="user-task-bar-chart" />,
    TaskDistributionPieChart: () => <div data-testid="task-distribution-chart" />,
    AccountHoursChart: () => <div data-testid="account-hours-chart" />,
    TimeTrendChart: () => null,
    ChartViewToggle: () => null,
}));

jest.mock('../../../components/Toast', () => ({
    useToast: () => ({
        showError: jest.fn(),
    }),
}));

const mockReport = {
    fromDate: '2024-01-01',
    toDate: '2024-01-31',
    workableHours: 160,
    totalWorkableHours: 160,
    totalHoursWorked: 120,
    userReports: [
        {
            userId: 1,
            fullName: 'Arne Anka',
            totalTime: 120,
            taskReports: [
                {
                    idtask: 10,
                    accountName: 'Client A',
                    taskName: 'Development',
                    totalHours: 80,
                },
                {
                    idtask: 11,
                    accountName: 'Client B',
                    taskName: 'Support',
                    totalHours: 40,
                },
            ],
        },
    ],
};

describe('UserTaskReportTable', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('renders admin title and user name header', () => {
        render(
            <UserTaskReportTable
                report={mockReport}
                reportView={Constants.MONTH_VIEW}
                fromDate={mockReport.fromDate}
                variant="admin"
            />
        );

        expect(screen.getByText('👥 User Task Time Summary')).toBeInTheDocument();
        expect(screen.getByText('Arne Anka')).toBeInTheDocument();
        expect(screen.queryByText('Summary')).not.toBeInTheDocument();
    });

    it('renders self title, summary, and compact layout without user name row', () => {
        render(
            <UserTaskReportTable
                report={mockReport}
                reportView={Constants.MONTH_VIEW}
                fromDate={mockReport.fromDate}
                variant="self"
            />
        );

        expect(screen.getByText('📋 Task Time Breakdown')).toBeInTheDocument();
        expect(screen.getByText('Summary')).toBeInTheDocument();
        expect(screen.getAllByText('160').length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText('120').length).toBeGreaterThanOrEqual(1);
        expect(screen.queryByText('Arne Anka')).not.toBeInTheDocument();
        expect(screen.getByText('Client A')).toBeInTheDocument();
        expect(screen.getByText('📋 View Details')).toBeInTheDocument();
    });

    it('loads user time report when view details is clicked', async () => {
        const mockTimeReport = {
            days: [{ weekend: false, majorHoliday: false }],
            timeReportTasks: [],
        };
        TimeService.mockImplementation(() => ({
            getUserTimeReportForMonth: jest.fn().mockResolvedValue({ data: mockTimeReport }),
        }));

        const user = userEvent.setup();
        render(
            <UserTaskReportTable
                report={mockReport}
                reportView={Constants.MONTH_VIEW}
                fromDate={mockReport.fromDate}
                variant="admin"
            />
        );

        await user.click(screen.getByText('📋 View Details'));

        await waitFor(() => {
            const instance = TimeService.mock.results[0].value;
            expect(instance.getUserTimeReportForMonth).toHaveBeenCalledWith(1, 2024, 1);
            expect(screen.getByText('Time Report Details')).toBeInTheDocument();
        });
    });
});
