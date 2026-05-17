import React, { useState, useEffect, useCallback } from 'react';
import TimeService from '../service/TimeService';
import * as Constants from '../common/Constants';
import { useToast } from './Toast';
import { useTranslation } from 'react-i18next';

export function getDayBackgroundColor(day, isClosed) {
    if (isClosed) {
        if (day.weekend) {
            return Constants.CLOSED_WEEK_END_COLOR;
        }
        if (day.majorHoliday) {
            return Constants.CLOSED_MAJOR_HOLIDAY_COLOR;
        }
        if (day.halfDay) {
            return Constants.CLOSED_HALF_DAY_COLOR;
        }
        return Constants.CLOSED_COLOR;
    }
    if (day.weekend) {
        return Constants.WEEKEND_COLOR;
    }
    if (day.majorHoliday) {
        return Constants.MAJOR_HOLIDAY_COLOR;
    }
    if (day.halfDay) {
        return Constants.HALF_DAY_COLOR;
    }
    return Constants.DAY_COLOR;
}

export function calcTaskTotalTime(timeReportTask) {
    var total = 0;
    if (timeReportTask?.timeEntries) {
        timeReportTask.timeEntries.forEach(function (timeReportDay) {
            if (timeReportDay.time != null) {
                var time = parseFloat(timeReportDay.time);
                if (!isNaN(time)) {
                    total += time;
                }
            }
        });
    }
    return total;
}

export function TimeReportReadOnlyTotal({ value, name }) {
    const displayValue = value == null || value === '' ? '' : value;
    return (
        <input
            className="time"
            style={{ width: '50px' }}
            readOnly={true}
            name={name}
            type="text"
            value={displayValue}
        />
    );
}

function TimeReportTableEntry({ timeReportDay: initialTimeReportDay, timeChanged, id, readOnly }) {
    const [timeReportDay, setTimeReportDay] = useState(initialTimeReportDay);
    const [fieldError, setFieldError] = useState(false);
    const { showError } = useToast();
    const { t } = useTranslation();

    useEffect(() => {
        setTimeReportDay(initialTimeReportDay);
    }, [initialTimeReportDay]);

    const timeChangedCallback = useCallback((updatedTimeReportDay, newId) => {
        updatedTimeReportDay.id = newId;
        timeChanged(updatedTimeReportDay);
    }, [timeChanged]);

    const isTextAllowed = useCallback((text) => {
        if (text == null) {
            return true;
        }

        var number = parseFloat(text);

        if (isNaN(number) && !isFinite(text)) {
            return false;
        }

        if (number < 0 || number > 24) {
            return false;
        }

        var index = text.indexOf('.');
        if (index !== -1) {
            var decimals = text.substring(index, text.length - 1);
            return decimals.length === 0 || decimals.length === 1 || decimals.length === 2;
        }

        return true;
    }, []);

    const handleError = useCallback((status, error) => {
        if (status === 400 && error != null) {
            showError(error);
        } else if (status === 500) {
            showError(t('common.messages.internalServerError', { error }));
        } else {
            showError(t('common.messages.genericError', { error }));
        }
    }, [showError, t]);

    const addUpdate = useCallback(() => {
        if (timeReportDay.closed || readOnly) {
            return;
        }

        const timeService = new TimeService();
        timeService.updateTime(timeReportDay)
            .then(response => {
                timeChangedCallback(timeReportDay, response.data);
            })
            .catch(error => {
                handleError(error.response?.status, error.response?.data?.error);
            });
    }, [timeReportDay, readOnly, timeChangedCallback, handleError]);

    const handleChange = useCallback((event) => {
        if (timeReportDay.closed || readOnly) {
            return;
        }

        var value = event.target.value;
        if (value != null) {
            value = value.replace(',', '.');
        }

        var newFieldError = !isTextAllowed(value);

        let updatedTimeReportDay = JSON.parse(JSON.stringify(timeReportDay));
        updatedTimeReportDay.time = value;
        setTimeReportDay(updatedTimeReportDay);
        setFieldError(newFieldError);
    }, [timeReportDay, readOnly, isTextAllowed]);

    if (timeReportDay == null) {
        return null;
    }

    var isClosed = timeReportDay.closed || readOnly;
    var backGroundColor = getDayBackgroundColor(timeReportDay.day, timeReportDay.closed);
    var time = (timeReportDay.time == null || timeReportDay.time === 0 ? '' : timeReportDay.time);
    var classes = 'time' + (fieldError ? ' border border-danger' : '');
    var inputName = JSON.stringify(timeReportDay.day.date);

    const inputStyle = {
        backgroundColor: backGroundColor,
        width: '40px',
        ...(readOnly ? {
            textAlign: 'center',
            border: '1px solid #dee2e6',
            fontSize: '0.875rem',
            fontWeight: time ? '600' : '400',
        } : {}),
    };

    return (
        <td style={{ padding: '0px' }}>
            <input
                style={inputStyle}
                className={readOnly ? 'form-control form-control-sm' : classes}
                readOnly={isClosed}
                name={inputName}
                type="text"
                value={time}
                maxLength="5"
                onChange={readOnly ? undefined : handleChange}
                onBlur={readOnly ? undefined : addUpdate}
            />
        </td>
    );
}

function TimeReportTableRow({ timeReportTask, totalTaskTime, timeChanged, readOnly }) {
    const timeChangedCallback = useCallback((timeReportDay) => {
        if (timeChanged) {
            timeChanged(timeReportDay);
        }
    }, [timeChanged]);

    if (timeReportTask == null) {
        return null;
    }

    var keyBase = timeReportTask.task.id;

    var entries = [];
    var i = 3;
    timeReportTask.timeEntries.forEach(function (timeReportDay) {
        var key = keyBase + '-' + i;
        entries.push(
            <TimeReportTableEntry
                key={key}
                id={key}
                timeReportDay={timeReportDay}
                timeChanged={timeChangedCallback}
                readOnly={readOnly}
            />
        );
        i++;
    });

    var accountName = timeReportTask.task.account.name;
    var taskName = timeReportTask.task.name;

    return (
        <tr key={keyBase} className={readOnly ? 'border-bottom' : undefined}>
            <th className="time-report-label-col">
                <span className="time-report-label" title={accountName}>{accountName}</span>
            </th>
            <th className="time-report-label-col">
                <span className="time-report-label" title={taskName}>{taskName}</span>
            </th>
            <th style={{ padding: '0px' }}>
                <TimeReportReadOnlyTotal value={totalTaskTime} name={timeReportTask.task.name} />
            </th>
            {entries}
        </tr>
    );
}

function TimeReportTableHeaderRow({ days, headerVariant }) {
    const { t } = useTranslation();
    if (days == null) {
        return null;
    }

    var columns = [];
    var i = 3;
    days.forEach(function (day) {
        var key = 'header-' + i;

        if (headerVariant === 'modal') {
            var dayStyle = {};
            if (day.weekend) {
                dayStyle = { color: '#4a90e2', textShadow: '0 0 3px rgba(0,0,0,0.3)' };
            } else if (day.majorHoliday) {
                dayStyle = { color: '#e74c3c', textShadow: '0 0 3px rgba(0,0,0,0.3)' };
            } else if (day.halfDay) {
                dayStyle = { color: '#f39c12', textShadow: '0 0 3px rgba(0,0,0,0.3)' };
            } else {
                dayStyle = { color: 'white', textShadow: '0 0 2px rgba(0,0,0,0.5)' };
            }
            columns.push(
                <th key={key} className="fw-bold text-center p-2" style={dayStyle}>
                    {day.day}
                </th>
            );
        } else {
            var backGroundColor = getDayBackgroundColor(day, false);
            columns.push(
                <th key={key}><font color={backGroundColor}>{day.day}</font></th>
            );
        }
        i++;
    });

    const rowClassName = headerVariant === 'modal' ? 'table-success' : undefined;

    return (
        <tr className={rowClassName}>
            <th><font color={Constants.DAY_COLOR}>{t('time.headers.account')}</font></th>
            <th><font color={Constants.DAY_COLOR}>{t('time.headers.task')}</font></th>
            <th><font color={Constants.DAY_COLOR}>{t('time.headers.time')}</font></th>
            {columns}
        </tr>
    );
}

function TimeReportTableFooterRow({ days, time, label, id }) {
    if (days == null) {
        return null;
    }

    var columns = [];
    var i = 3;
    var baseKey = id + '-';
    days.forEach(function () {
        columns.push(<th key={baseKey + i}></th>);
        i++;
    });

    return (
        <tr className="table-secondary">
            <th></th>
            <th className="time-report-label-col">
                <span className="time-report-label" title={label}>{label}</span>
            </th>
            <th style={{ padding: '0px' }}>
                <TimeReportReadOnlyTotal value={time} name={id} />
            </th>
            {columns}
        </tr>
    );
}

/**
 * Shared month/week time entry grid used on the main time page and in report "View details".
 */
export default function TimeReportTable({
    timeReport: initialTimeReport,
    readOnly = false,
    showFooters = true,
    className = 'table-sm time-report-table',
    tableStyle,
    theadClassName = 'bg-success',
    headerVariant = 'default',
}) {
    const { t } = useTranslation();
    const [timeReport, setTimeReport] = useState(initialTimeReport);

    useEffect(() => {
        setTimeReport(initialTimeReport);
    }, [initialTimeReport]);

    const updateTime = useCallback((timeReportDayUpdated, timeReportTasks) => {
        for (var timeReportTaskKey in timeReportTasks) {
            var timeReportTask = timeReportTasks[timeReportTaskKey];
            for (var timeReportEntryKey in timeReportTask.timeEntries) {
                var timeReportEntry = timeReportTask.timeEntries[timeReportEntryKey];
                if (timeReportEntry.taskContributorId === timeReportDayUpdated.taskContributorId &&
                    timeReportEntry.day.date === timeReportDayUpdated.day.date) {
                    timeReportEntry.time = timeReportDayUpdated.time;
                }
            }
        }
    }, []);

    const timeChanged = useCallback((timeReportDayUpdated) => {
        updateTime(timeReportDayUpdated, timeReport.timeReportTasks);
        setTimeReport({ ...timeReport });
    }, [timeReport, updateTime]);

    if (timeReport == null) {
        return null;
    }

    var rows = [];
    var totalTime = 0;

    if (timeReport.timeReportTasks != null) {
        timeReport.timeReportTasks.forEach(function (timeReportTask) {
            var totalTaskTime = calcTaskTotalTime(timeReportTask);
            totalTime += totalTaskTime;

            rows.push(
                <TimeReportTableRow
                    key={timeReportTask.task.id}
                    timeReportTask={timeReportTask}
                    totalTaskTime={totalTaskTime}
                    timeChanged={readOnly ? undefined : timeChanged}
                    readOnly={readOnly}
                />
            );
        });
    }

    if (showFooters) {
        rows.push(
            <TimeReportTableFooterRow
                key="footer-sum"
                id="footer-sum"
                days={timeReport.days}
                time={totalTime}
                label={t('time.labels.sumTotal')}
            />
        );
        rows.push(
            <TimeReportTableFooterRow
                key="footer-workable"
                id="footer-workable"
                days={timeReport.days}
                time={timeReport.workableHours}
                label={t('time.labels.workableHours')}
            />
        );
    }

    return (
        <table className={className} style={tableStyle}>
            <thead className={theadClassName}>
                <TimeReportTableHeaderRow days={timeReport.days} headerVariant={headerVariant} />
            </thead>
            <tbody>
                {rows}
            </tbody>
        </table>
    );
}
