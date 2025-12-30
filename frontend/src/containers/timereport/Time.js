import React, { useState, useEffect, useCallback } from "react";
import TimeService from '../../service/TimeService';
import * as Constants from '../../common/Constants';
import ReportService from '../../service/ReportService';
import { useToast } from '../../components/Toast';

function TimeReportTableEntry({ timeReportDay: initialTimeReportDay, timeChanged, id }) {
    const [timeReportDay, setTimeReportDay] = useState(initialTimeReportDay);
    const [fieldError, setFieldError] = useState(false);
    const { showError } = useToast();

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

    const addUpdate = useCallback((event) => {
        if (timeReportDay.closed) {
            return;
        }

        var value = event.target.value;

        var isInputOk = isTextAllowed(value);
        if (!isInputOk) {
            return;
        }

        const timeService = new TimeService();
        timeService.updateTime(timeReportDay)
            .then(response => {
                timeChangedCallback(timeReportDay, response.data);
            })
            .catch(error => {
                handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }, [timeReportDay, isTextAllowed, timeChangedCallback]);

    const handleError = useCallback((status, error) => {
        if (status === 400 && error != null) {
            showError(error);
        } else if (status === 500) {
            showError("Internal server error: " + error);
        } else {
            showError("Error: " + error);
        }
    }, [showError]);

    const handleChange = useCallback((event) => {
        if (timeReportDay.closed) {
            return;
        }

        var value = event.target.value;
        if (value != null) {
            value = value.replace(",", ".")
        }

        var newFieldError = !isTextAllowed(value);

        let updatedTimeReportDay = JSON.parse(JSON.stringify(timeReportDay));
        updatedTimeReportDay['time'] = value;
        setTimeReportDay(updatedTimeReportDay);
        setFieldError(newFieldError);
    }, [timeReportDay, isTextAllowed]);

    if (timeReportDay == null) return null;

    var isClosed = timeReportDay.closed;
    var backGroundColor = '';
    if (isClosed) {
        backGroundColor = Constants.CLOSED_COLOR;
        if (timeReportDay.day.weekend) {
            backGroundColor = Constants.CLOSED_WEEK_END_COLOR;
        } else if (timeReportDay.day.majorHoliday) {
            backGroundColor = Constants.CLOSED_MAJOR_HOLIDAY_COLOR;
        } else if (timeReportDay.day.halfDay) {
            backGroundColor = Constants.CLOSED_HALF_DAY_COLOR;
        }
    } else if (timeReportDay.day.weekend) {
        backGroundColor = Constants.WEEKEND_COLOR;
    } else if (timeReportDay.day.majorHoliday) {
        backGroundColor = Constants.MAJOR_HOLIDAY_COLOR;
    } else if (timeReportDay.day.halfDay) {
        backGroundColor = Constants.HALF_DAY_COLOR;
    } else {
        backGroundColor = Constants.DAY_COLOR;
    }

    var time = (timeReportDay.time == null || timeReportDay.time === 0 ? '' : timeReportDay.time);
    var classes = "time " + (fieldError ? "border border-danger" : '');
    var inputName = JSON.stringify(timeReportDay.day.date);

    const inputStyle = {
        backgroundColor: backGroundColor,
        width: '40px'
    }

    return (
        <td key={id} style={{ padding: "0px" }}>
            <input style={inputStyle} className={classes} readOnly={isClosed} name={inputName} type="text" value={time} maxLength="5" onChange={handleChange} onBlur={addUpdate} />
        </td>
    );
}

function TimeReportTableRow({ timeReportTask, totalTaskTime, timeChanged }) {
    const timeChangedCallback = useCallback((timeReportDay) => {
        timeChanged(timeReportDay);
    }, [timeChanged]);

    if (timeReportTask == null) return null;

    var keyBase = timeReportTask.task.id;

    var entries = [];
    if (timeReportTask != null) {
        var i = 3;
        timeReportTask.timeEntries.forEach(function (timeReportDay) {
            var key = keyBase + '-' + i;
            entries.push(
                <TimeReportTableEntry key={key} id={key} timeReportDay={timeReportDay} timeChanged={timeChangedCallback} />);
            i++;
        });
    }

    var accountName = timeReportTask.task.account.name;
    var accountShortName = accountName.substring(0, Math.min(15, accountName.length));
    var taskName = timeReportTask.task.name;
    var taskShortName = taskName.substring(0, Math.min(15, taskName.length));

    return (
        <tr key={keyBase}>
            <th key={keyBase + '-0'} className="text-nowrap" title={accountName}>{accountShortName}</th>
            <th key={keyBase + '-1'} className="text-nowrap" title={taskName}>{taskShortName}</th>
            <th key={keyBase + '-2  '}><input className="time" style={{ width: "50px" }} readOnly={true} name={timeReportTask.task.name} type="text" value={totalTaskTime} /></th>
            {entries}
        </tr>
    );
}

function TimeReportTableHeaderRow({ days }) {
    if (days == null)
        return null;

    var columns = [];
    if (days != null) {
        var i = 3;
        days.forEach(function (day) {
            var backGroundColor = '';
            if (day.weekend) {
                backGroundColor = Constants.WEEKEND_COLOR;
            } else if (day.majorHoliday) {
                backGroundColor = Constants.MAJOR_HOLIDAY_COLOR;
            } else if (day.halfDay) {
                backGroundColor = Constants.HALF_DAY_COLOR;
            } else {
                backGroundColor = Constants.DAY_COLOR;
            }

            var key = 'header-' + i;
            columns.push(
                <th key={key}><font color={backGroundColor}>{day.day}</font></th>);
            i++;
        });
    }

    return (
        <tr key="0">
            <th key="header-0"><font color={Constants.DAY_COLOR}>Account</font></th>
            <th key="header-1"><font color={Constants.DAY_COLOR}>Task</font></th>
            <th key="header-2"><font color={Constants.DAY_COLOR}>Time</font></th>
            {columns}
        </tr>
    );
}

function TimeReportTableFooterRow({ days, time, label, id }) {
    if (days == null)
        return null;

    var columns = [];
    if (days != null) {
        var i = 3;
        var baseKey = id + '-';
        days.forEach(function (day) {
            var key = baseKey + i;
            columns.push(<th key={key}></th>);
            i++;
        });
    }

    return (
        <tr key={id} className="table-secondary">
            <th key={id + '-0'}></th>
            <th key={id + '-1'} className="text-nowrap">{label}</th>
            <th key={id + '-2'}>{time}</th>
            {columns}
        </tr>
    );
}

function TimeReportTable({ timeReport: initialTimeReport }) {
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

    if (timeReport == null) return null;

    var headerRow = <TimeReportTableHeaderRow days={timeReport.days} />;

    var rows = [];
    var totalTime = 0;

    if (timeReport.timeReportTasks != null) {
        timeReport.timeReportTasks.forEach(function (timeReportTask) {
            var totalTaskTime = 0;
            timeReportTask.timeEntries.forEach(function (timeReportDay) {
                if (timeReportDay.time != null) {
                    var time = parseFloat(timeReportDay.time);
                    if (!isNaN(time)) {
                        totalTaskTime += time;
                    }
                }
            });

            totalTime += totalTaskTime;

            rows.push(
                <TimeReportTableRow key={timeReportTask.task.id} timeReportTask={timeReportTask} totalTaskTime={totalTaskTime} timeChanged={timeChanged} />);
        });
    }

    rows.push(<TimeReportTableFooterRow key="footer3" id="footer3" days={timeReport.days} time={totalTime} label='Sum total' />);
    rows.push(<TimeReportTableFooterRow key="footer4" id="footer4" days={timeReport.days} time={timeReport.workableHours} label='Workable hours' />);

    return (
        <table className="table-sm time-report-table">
            <thead className="bg-success">
                {headerRow}
            </thead>
            <tbody>
                {rows}
            </tbody>
        </table>
    );
}


function Times(props) {
    const [timeReport, setTimeReport] = useState(null);
    const [reportView, setReportView] = useState(Constants.MONTH_VIEW);
    const { showError, showSuccess } = useToast();

    useEffect(() => {
        loadCurrentTimes(Constants.MONTH_VIEW);
    }, []);

    const loadCurrentTimes = useCallback((view) => {
        var timeService = new TimeService();
        timeService.getTimes(view)
            .then(response => {
                setTimeReport(response.data);
                setReportView(view);
            })
            .catch(error => {
                showError('Failed to load time report');
            });
    }, [showError]);

    const loadPreviousTimes = useCallback((event) => {
        const date = event.target.name;

        const timeService = new TimeService();
        timeService.getPreviousTimes(reportView, date)
            .then(response => {
                setTimeReport(response.data);
            })
            .catch(error => {
                showError('Failed to load time report');
            });
    }, [reportView, showError]);

    const loadNextTimes = useCallback((event) => {
        const date = event.target.name;

        const timeService = new TimeService();
        timeService.getNextTimes(reportView, date)
            .then(response => {
                setTimeReport(response.data);
            })
            .catch(error => {
                showError('Failed to load time report');
            });
    }, [reportView, showError]);

    const closeReport = useCallback((event) => {
        const shallClose = confirm('Are you really sure you want close this month?\nYou will not be able to open it again.');
        if (!shallClose) {
            return;
        }
        const date = event.target.name;

        var payLoad = '{ "userId": "' + timeReport.user.id + '", "closeDate": "' + date + '"}';
        var service = new ReportService();
        service.updateOpenCloseReport(payLoad, 'close')
            .then(response => {
                loadCurrentTimes(Constants.MONTH_VIEW);
                showSuccess('Report closed successfully');
            })
            .catch(error => {
                showError('Failed to close report');
            });
    }, [timeReport, loadCurrentTimes, showSuccess, showError]);

    const viewChange = useCallback((event) => {
        const view = event.target.value;
        loadCurrentTimes(view);
    }, [loadCurrentTimes]);

    if (timeReport == null) return null;

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>Time</h2>
            <div className="row mb-2">
                <div className="col-auto">
                    <button className="btn btn-success btn-sm" name={timeReport.firstDate} onClick={loadPreviousTimes}>&lt;&lt;</button>
                </div>
                <div className="col-auto">
                    <button className="btn btn-success btn-sm" name={timeReport.lastDate} onClick={loadNextTimes}>&gt;&gt;</button>
                </div>
                <div className="col-auto">
                    <select className="form-control form-control-sm" value={reportView} name="reportView" onChange={viewChange}>
                        <option value={Constants.WEEK_VIEW}>Week</option>
                        <option value={Constants.MONTH_VIEW}>Month</option>
                    </select>
                </div>
                <div className="col">
                    {reportView === Constants.MONTH_VIEW && !timeReport.closed ? (
                        <span className="float-right">
                            <button className="btn btn-success btn-sm" name={timeReport.firstDate} onClick={closeReport}>Close report</button>
                        </span>
                    ) : ''}
                </div>
                <div className="col-auto">
                    <span className="badge bg-secondary fs-6 py-2 px-3 text-nowrap">
                        📅 {timeReport.firstDate} - {timeReport.lastDate}
                    </span>
                </div>
            </div>
            <div className="row">
                <div className="table-responsive">
                    <TimeReportTable timeReport={timeReport} />
                </div>
            </div>
        </div >
    );
}

export default Times;