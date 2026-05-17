import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import TimeReportTable, { calcTaskTotalTime } from '../TimeReportGrid';

jest.mock('../Toast', () => ({
    useToast: () => ({
        showError: jest.fn(),
    }),
}));

const mockTimeReport = {
    workableHours: 160,
    days: [
        { day: 1, weekend: false, majorHoliday: false, halfDay: false },
        { day: 2, weekend: true, majorHoliday: false, halfDay: false },
    ],
    timeReportTasks: [
        {
            task: {
                id: 1,
                name: 'Development task with a very long name',
                account: { name: 'Client account with a long name' },
            },
            timeEntries: [
                { day: { date: '2024-01-01' }, time: 8, closed: false },
                { day: { date: '2024-01-02' }, time: 4, closed: false },
            ],
        },
    ],
};

describe('TimeReportGrid', () => {
    it('renders editable grid with footers on main time page', () => {
        render(<TimeReportTable timeReport={mockTimeReport} />);

        expect(screen.getByText('Client account with a long name')).toBeInTheDocument();
        expect(screen.getByText('Sum total')).toBeInTheDocument();
        expect(screen.getByText('Workable hours')).toBeInTheDocument();
    });

    it('renders read-only grid for view details', () => {
        render(
            <TimeReportTable
                timeReport={mockTimeReport}
                readOnly={true}
                headerVariant="modal"
            />
        );

        const inputs = screen.getAllByRole('textbox');
        inputs.forEach((input) => {
            expect(input).toHaveAttribute('readonly');
        });
    });

    it('calcTaskTotalTime sums entry hours', () => {
        expect(calcTaskTotalTime(mockTimeReport.timeReportTasks[0])).toBe(12);
    });
});
