import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import TimeReportTable, { calcTaskTotalTime, getDayBackgroundColor, isTimeReportTaskEditable } from '../TimeReportGrid';
import * as Constants from '../../common/Constants';

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
                activationStatus: Constants.ACTIVE_STATUS,
                account: { name: 'Client account with a long name', activationStatus: Constants.ACTIVE_STATUS },
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

    it('getDayBackgroundColor keeps weekend color when report is closed', () => {
        const weekend = { weekend: true, majorHoliday: false, halfDay: false };
        expect(getDayBackgroundColor(weekend, false)).toBe(Constants.WEEKEND_COLOR);
        expect(getDayBackgroundColor(weekend, true)).toBe(Constants.WEEKEND_COLOR);
    });

    it('isTimeReportTaskEditable uses backend editable flag when present', () => {
        expect(isTimeReportTaskEditable({ editable: false, task: { activationStatus: Constants.ACTIVE_STATUS } })).toBe(false);
        expect(isTimeReportTaskEditable({ editable: true, task: { activationStatus: Constants.INACTIVE_STATUS } })).toBe(true);
    });

    it('isTimeReportTaskEditable returns false when task or account is inactive', () => {
        const activeTask = {
            task: {
                activationStatus: Constants.ACTIVE_STATUS,
                account: { activationStatus: Constants.ACTIVE_STATUS },
            },
        };
        const inactiveTask = {
            task: {
                activationStatus: Constants.INACTIVE_STATUS,
                account: { activationStatus: Constants.ACTIVE_STATUS },
            },
        };
        const inactiveAccount = {
            task: {
                activationStatus: Constants.ACTIVE_STATUS,
                account: { activationStatus: Constants.INACTIVE_STATUS },
            },
        };

        expect(isTimeReportTaskEditable(activeTask)).toBe(true);
        expect(isTimeReportTaskEditable(inactiveTask)).toBe(false);
        expect(isTimeReportTaskEditable(inactiveAccount)).toBe(false);
    });

    it('renders inactive task rows as read-only in editable grid', () => {
        const reportWithInactiveTask = {
            ...mockTimeReport,
            timeReportTasks: [
                {
                    ...mockTimeReport.timeReportTasks[0],
                    editable: false,
                    task: {
                        ...mockTimeReport.timeReportTasks[0].task,
                        activationStatus: Constants.INACTIVE_STATUS,
                    },
                },
            ],
        };

        render(<TimeReportTable timeReport={reportWithInactiveTask} />);

        const dayInputs = screen.getAllByRole('textbox').filter(
            (input) => input.getAttribute('name')?.includes('2024-01')
        );
        dayInputs.forEach((input) => {
            expect(input).toBeDisabled();
        });
    });
});
