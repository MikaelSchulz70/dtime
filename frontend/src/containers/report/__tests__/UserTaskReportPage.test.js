import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import UserTaskReportPage from '../UserTaskReportPage';
import ReportService from '../../../service/ReportService';
import * as Constants from '../../../common/Constants';

jest.mock('../../../service/ReportService');
jest.mock('../UserTaskReport', () => {
    return function MockUserTaskReportTable({ variant }) {
        return <div data-testid="user-task-report-table" data-variant={variant} />;
    };
});

jest.mock('../../../components/Toast', () => ({
    useToast: () => ({
        showError: jest.fn(),
    }),
}));

const MockedReportService = ReportService;

describe('UserTaskReportPage', () => {
    let mockReportService;

    beforeEach(() => {
        mockReportService = {
            getCurrentUserReport: jest.fn(),
            getPreviousUserReport: jest.fn(),
            getNextUserReport: jest.fn(),
        };
        MockedReportService.mockImplementation(() => mockReportService);
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    it('loads the current user report and renders self variant table', async () => {
        mockReportService.getCurrentUserReport.mockResolvedValue({
            data: {
                fromDate: '2024-01-01',
                toDate: '2024-01-31',
                workableHours: 160,
                userReports: [],
            },
        });

        render(<UserTaskReportPage />);

        await waitFor(() => {
            expect(mockReportService.getCurrentUserReport).toHaveBeenCalledWith(Constants.MONTH_VIEW);
        });

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'My Time Report' })).toBeInTheDocument();
        });

        expect(screen.getByText(/160/)).toBeInTheDocument();
        expect(screen.getByTestId('user-task-report-table')).toHaveAttribute('data-variant', 'self');
    });
});
