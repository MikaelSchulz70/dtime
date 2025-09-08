import React, { useState, useEffect, useCallback } from "react";
import Modal from 'react-modal';
import * as Constants from '../../common/Constants';
import TimeService from '../../service/TimeService';
import { useToast } from '../../components/Toast';

Modal.setAppElement('#root');

function TimeReportTableEntry({ timeReportDay }) {
    if (timeReportDay == null) return null;
    var backGroundColor = Constants.CLOSED_COLOR;
    if (timeReportDay.day.weekend) {
        backGroundColor = Constants.WEEKEND_COLOR;
    } else if (timeReportDay.day.majorHoliday) {
        backGroundColor = Constants.WEEKEND_COLOR;
    } else {
        backGroundColor = Constants.DAY_COLOR;
    }

    var time = (timeReportDay.time == null || timeReportDay.time === 0 ? '' : timeReportDay.time);
    const inputStyle = {
        backgroundColor: backGroundColor,
        width: '55px'
    }

    return (
        <td style={{ padding: "0px" }}><input style={inputStyle} readOnly={true} type="text" value={time} /></td>
    );
}

function UserReportSummaryTable({ report }) {
    if (report == null)
        return null;

    var rows = [];
    rows.push(
        <tr key={'summary-header'} className="bg-success text-white">
            <th className="fw-bold fs-6">Summary</th>
            <th className="fw-bold fs-6">Workable Hours (Month)</th>
            <th className="fw-bold fs-6">Total Workable Hours</th>
            <th className="fw-bold fs-6">Total Hours Worked</th>
            <th></th>
        </tr>
    );

    rows.push(
        <tr key={'summary-info'} className="table-light">
            <td className="fw-bold text-muted">Totals</td>
            <td className="fw-bold">{report.workableHours}</td>
            <td className="fw-bold">{report.totalWorkableHours}</td>
            <td className="fw-bold text-success">{report.totalHoursWorked}</td>
            <td></td>
        </tr>
    );

    return (
        <tbody>
            {rows}
        </tbody>
    );
}

function TimeReportTableRow({ timeReportTask, totalTaskTime }) {
    if (timeReportTask == null) return null;
    var entries = [];
    if (timeReportTask != null) {
        timeReportTask.timeEntries.forEach(function (timeEntry) {
            entries.push(
                <TimeReportTableEntry timeReportDay={timeEntry} />);
        });
    }

    var accountName = timeReportTask.task.account.name;
    var accountShortName = accountName.substring(0, Math.min(20, accountName.length));
    var taskName = timeReportTask.task.name;
    var taskShortName = taskName.substring(0, Math.min(20, taskName.length));

    return (
        <tr>
            <th className="text-nowrap" title={accountName}>{accountShortName}</th>
            <th className="text-nowrap" title={taskName}>{taskShortName}</th>
            <th><input style={{ width: '55px' }} readOnly={true} name={timeReportTask.task.name} type="text" value={totalTaskTime} /></th>
            {entries}
        </tr>
    );
}

function UserReportRows({ userReport, reportView, workableHours, fromDate, toDate, showError }) {

    if (userReport == null)
        return null;

    var closeTextColor = "red";
    if (userReport.closed) {
        closeTextColor = "white";
    }

    var rows = [];
    var keyBase = userReport.userId + '_';
    var keyHeader = keyBase + '0';
    rows.push(<tr key={keyHeader}>
        <th className="fw-bold">{userReport.fullName}</th>
        <th className="fw-bold">Account</th>
        <th className="fw-bold">Task</th>
        <th className="fw-bold">Total Hours</th>
        <th></th>
    </tr>);

    userReport.taskReports.forEach(function (taskReport) {
        var accountName = taskReport.accountName;
        var accountShortName = accountName.substring(0, Math.min(20, accountName.length));
        var taskName = taskReport.taskName;
        var taskShortName = taskName.substring(0, Math.min(20, taskName.length));

        var key = keyBase + '_' + taskReport.idtask;
        rows.push(
            <tr key={key}>
                <td></td>
                <td className="text-nowrap" title={accountName}>{accountShortName}</td>
                <td className="text-nowrap" title={taskName}>{taskShortName}</td>
                <td>{taskReport.totalHours}</td>
                <td></td>
            </tr>);
    });

    var textColor = '';
    if (userReport.totalTime < workableHours) {
        textColor = 'text-danger';
    }

    var key = keyBase + "_footer";
    rows.push(<tr key={key} className="table-primary border-top border-2">
        <th className="text-muted"></th>
        <th className="fw-bold fs-6">📊 Total Time</th>
        <th></th>
        <th className={`fw-bold fs-6`}>{userReport.totalTime} hrs</th>
        <th>
            {reportView === Constants.MONTH_VIEW ? (
                <UserDetailReport userId={userReport.userId} fromDate={fromDate} toDate={toDate} showError={showError} />
            ) : ''}
        </th>
    </tr>);
    return (
        <tbody>
            {rows}
        </tbody>
    );
}

function TimeReportTableHeaderRow({ days }) {
    if (days == null)
        return null;
    var columns = [];
    if (days != null) {
        days.forEach(function (day) {
            var backGroundColor = '';
            if (day.weekend) {
                backGroundColor = Constants.WEEKEND_COLOR;
            } else if (day.majorHoliday) {
                backGroundColor = Constants.WEEKEND_COLOR;
            } else {
                backGroundColor = Constants.DAY_COLOR;
            }

            columns.push(
                <th><font color={backGroundColor}>{day.day}</font></th>);
        });
    }

    return (
        <tr key=''>
            <th><font color={Constants.DAY_COLOR}>Account</font></th>
            <th><font color={Constants.DAY_COLOR}>Task</font></th>
            <th><font color={Constants.DAY_COLOR}>Time</font></th>
            {columns}
        </tr>
    );
}

function UserDetailReport({ userId, fromDate, toDate, showError }) {
    const [timeReport, setTimeReport] = useState(null);
    const [isOpen, setIsOpen] = useState(false);

    const showDetails = useCallback(() => {
        loadFromServer();
    }, []);

    const closeDetails = useCallback(() => {
        setIsOpen(false);
    }, []);

    const loadFromServer = useCallback(() => {
        var service = new TimeService();
        service.getUserTimes(userId, fromDate)
            .then(response => {
                console.log('Server response:', response);
                setTimeReport(response.data);
                setIsOpen(true);
            })
            .catch(error => {
                console.error('Error loading user report:', error);
                showError('Failed to load user report: ' + (error.response?.data?.message || error.message));
            });
    }, [userId, fromDate, showError]);

    var headerRow = '';
    if (timeReport != null) {
        headerRow = <TimeReportTableHeaderRow days={timeReport.days} />;
    }

    console.log('Tr', timeReport);

    var rows = [];
    if (timeReport != null && timeReport.timeReportTasks != null) {
        timeReport.timeReportTasks.forEach(function (timeReportTask) {
            var totalTaskTime = 0;
            timeReportTask.timeEntries.forEach(function (timeEntry) {
                if (timeEntry.time != null) {
                    var time = parseFloat(timeEntry.time);
                    if (!isNaN(time)) {
                        totalTaskTime += time;
                    }
                }
            });

            rows.push(
                <TimeReportTableRow timeReportTask={timeReportTask} totalTaskTime={totalTaskTime} />);
        });
    }

    return (
        <div>
            <button className="btn btn-outline-success btn-sm" onClick={showDetails}>
                📋 View Details
            </button>
            {isOpen ? (
                <Modal
                    isOpen={isOpen}
                    onRequestClose={closeDetails}
                    style={{
                        content: {
                            top: '50%',
                            left: '50%',
                            right: 'auto',
                            bottom: 'auto',
                            marginRight: '-50%',
                            transform: 'translate(-50%, -50%)',
                            maxWidth: '90%',
                            maxHeight: '90%'
                        }
                    }}
                >
                    <div>
                        <div className="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom">
                            <h4 className="mb-0 text-success fw-bold">📊 Time Report Details</h4>
                            <button type="button" className="btn btn-outline-secondary btn-sm" onClick={closeDetails}>
                                ✕ Close
                            </button>
                        </div>
                        <div className="table-responsive">
                            <table className="table table-sm table-striped table-hover">
                                <thead className="bg-success text-white">
                                    {headerRow}
                                </thead>
                                <tbody>
                                    {rows}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </Modal>
            ) : ''}
        </div>
    )
}

function UserTaskReportTable({ report, reportView, fromDate }) {
    const { showError } = useToast();
    
    if (report == null)
        return null;

    var rows = [];
    var workableHours = report.workableHours;
    var fromDateValue = report.fromDate;
    var toDateValue = report.toDate;
    if (report.userReports != null) {
        report.userReports.forEach(function (userReport) {
            rows.push(<UserReportRows key={userReport.userId} userReport={userReport} workableHours={workableHours} reportView={reportView} fromDate={fromDateValue} toDate={toDateValue} showError={showError} />);
        });
    }

    return (
        <div className="col-12">
            <div className="card shadow-sm">
                <div className="card-header bg-success text-white">
                    <h5 className="mb-0 fw-bold text-white">👥 User Task Time Summary</h5>
                </div>
                <div className="card-body p-0">
                    <div className="table-responsive">
                        <table className="table table-hover table-striped mb-0">
                            {rows}
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default UserTaskReportTable;