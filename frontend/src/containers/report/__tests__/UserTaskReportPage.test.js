import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter, Routes, Route } from 'react-router';
import UserTaskReportPage from '../UserTaskReportPage';
import ReportService from '../../../service/ReportService';

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

function renderPage(path = '/userreport/year/2025/month/6') {
    return render(
        <MemoryRouter initialEntries={[path]}>
            <Routes>
                <Route path="/userreport/year/:year/month/:month" element={<UserTaskReportPage />} />
                <Route path="/userreport/year/:year" element={<UserTaskReportPage />} />
            </Routes>
        </MemoryRouter>
    );
}

describe('UserTaskReportPage', () => {
    let mockReportService;

    beforeEach(() => {
        mockReportService = {
            getUserReportForMonth: jest.fn(),
            getUserReportForYear: jest.fn(),
        };
        MockedReportService.mockImplementation(() => mockReportService);
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    it('loads the current user report and renders self variant table', async () => {
        mockReportService.getUserReportForMonth.mockResolvedValue({
            data: {
                fromDate: '2025-06-01',
                toDate: '2025-06-30',
                workableHours: 160,
                userReports: [],
            },
        });

        renderPage();

        await waitFor(() => {
            expect(mockReportService.getUserReportForMonth).toHaveBeenCalledWith(2025, 6);
        });

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'My Time Report' })).toBeInTheDocument();
        });

        expect(screen.getByText(/160/)).toBeInTheDocument();
        expect(screen.getByTestId('user-task-report-table')).toHaveAttribute('data-variant', 'self');
    });
});
