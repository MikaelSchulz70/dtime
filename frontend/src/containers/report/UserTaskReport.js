import React, { useState, useCallback } from "react";
import Modal from 'react-modal';
import * as Constants from '../../common/Constants';
import TimeService from '../../service/TimeService';
import { useToast } from '../../components/Toast';
import {
    UserTaskBarChart,
    TaskDistributionPieChart,
    AccountHoursChart,
    TimeTrendChart,
    ChartViewToggle
} from '../../components/Charts';

Modal.setAppElement('#root');

function TimeReportTableEntry({ timeReportDay }) {
    if (timeReportDay == null) return null;

    var backgroundColor = '';
    var time = (timeReportDay.time == null || timeReportDay.time === 0 ? '' : timeReportDay.time);

    if (timeReportDay.day.weekend) {
        backgroundColor = Constants.WEEKEND_COLOR;
    } else if (timeReportDay.day.majorHoliday) {
        backgroundColor = Constants.MAJOR_HOLIDAY_COLOR;
    } else if (timeReportDay.day.halfDay) {
        backgroundColor = Constants.HALF_DAY_COLOR;
    } else {
        backgroundColor = Constants.DAY_COLOR;
    }

    const inputStyle = {
        backgroundColor: backgroundColor,
        width: '40px',
        textAlign: 'center',
        border: '1px solid #dee2e6',
        fontSize: '0.875rem',
        fontWeight: time ? '600' : '400'
    }

    return (
        <td style={{ padding: "0px" }}>
            <input
                style={inputStyle}
                className="form-control form-control-sm"
                readOnly={true}
                type="text"
                value={time}
            />
        </td>
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
        timeReportTask.timeEntries.forEach(function (timeEntry, index) {
            entries.push(
                <TimeReportTableEntry key={index} timeReportDay={timeEntry} />);
        });
    }

    var accountName = timeReportTask.task.account.name;
    var accountShortName = accountName.substring(0, Math.min(15, accountName.length));
    var taskName = timeReportTask.task.name;
    var taskShortName = taskName.substring(0, Math.min(15, taskName.length));

    return (
        <tr className="border-bottom">
            <th className="text-nowrap" title={accountName} style={{ padding: '0px' }}>
                {accountShortName}
            </th>
            <th className="text-nowrap" title={taskName} style={{ padding: '0px' }}>
                {taskShortName}
            </th>
            <td style={{ padding: '0px' }}>
                <input
                    className="time form-control form-control-sm fw-bold text-center"
                    style={{
                        width: "40px",
                        backgroundColor: '#f8f9fa',
                        border: '2px solid #28a745',
                        color: '#28a745'
                    }}
                    readOnly={true}
                    type="text"
                    value={totalTaskTime || '0'}
                />
            </td>
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
        <th className="fw-bold">Hours</th>
        <th className="fw-bold">
            {reportView === Constants.MONTH_VIEW ? (
                <UserDetailReport userId={userReport.userId} fromDate={fromDate} toDate={toDate} showError={showError} />
            ) : ''}
        </th>
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
    rows.push(<tr key={key} className="bg-success text-white border-top border-2">
        <th className="text-muted"></th>
        <th className="fw-bold fs-6"></th>
        <th></th>
        <th className={`fw-bold fs-6`}>{userReport.totalTime} hrs</th>
        <th></th>
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
        var i = 3;
        days.forEach(function (day) {
            var textClass = 'fw-bold text-center';
            var dayStyle = {};
            
            // Use text colors that mirror the main grid color patterns but are visible on green background
            if (day.weekend) {
                dayStyle = { color: '#4a90e2', textShadow: '0 0 3px rgba(0,0,0,0.3)' }; // Blue for weekends (represents weekend color)
            } else if (day.majorHoliday) {
                dayStyle = { color: '#e74c3c', textShadow: '0 0 3px rgba(0,0,0,0.3)' }; // Red for major holidays  
            } else if (day.halfDay) {
                dayStyle = { color: '#f39c12', textShadow: '0 0 3px rgba(0,0,0,0.3)' }; // Orange/Gold for half days
            } else {
                dayStyle = { color: 'white', textShadow: '0 0 2px rgba(0,0,0,0.5)' }; // White with shadow for regular days
            }

            var key = 'header-' + i;
            columns.push(
                <th key={key} className={`${textClass} p-2`} style={dayStyle}>
                    {day.day}
                </th>
            );
            i++;
        });
    }

    return (
        <tr key="0" className="table-success">
            <th key="header-0"><font color={Constants.DAY_COLOR}>Account</font></th>
            <th key="header-1"><font color={Constants.DAY_COLOR}>Task</font></th>
            <th key="header-2"><font color={Constants.DAY_COLOR}>Total</font></th>
            {columns}
        </tr>
    );
}

function UserDetailReport({ userId, fromDate, toDate, showError }) {
    const [timeReport, setTimeReport] = useState(null);
    const [isOpen, setIsOpen] = useState(false);
    const [viewMode, setViewMode] = useState('table');

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
                            width: '95vw',
                            maxWidth: '95vw',
                            maxHeight: '95vh',
                            padding: '0',
                            border: 'none',
                            borderRadius: '12px',
                            boxShadow: '0 10px 30px rgba(0, 0, 0, 0.3)',
                            overflow: 'hidden'
                        },
                        overlay: {
                            backgroundColor: 'rgba(0, 0, 0, 0.6)',
                            zIndex: 1050
                        }
                    }}
                >
                    <div className="modal-content border-0" style={{ borderRadius: '12px', overflow: 'hidden', height: '100%', display: 'flex', flexDirection: 'column' }}>
                        {/* Modal Header */}
                        <div className="modal-header bg-success" style={{
                            borderBottom: 'none',
                            padding: '1.5rem 2rem'
                        }}>
                            <div className="d-flex align-items-center">
                                <div className="me-3 p-2 rounded-circle bg-white bg-opacity-20">
                                    <span className="fs-4">📊</span>
                                </div>
                                <div>
                                    <h4 className="mb-1 text-white fw-bold">Time Report Details</h4>
                                    <p className="mb-0 text-white-50 small">
                                        📅 {fromDate && fromDate} {toDate && ` - ${toDate}`}
                                    </p>
                                </div>
                            </div>
                            <div className="d-flex align-items-center gap-3">
                                <ChartViewToggle viewMode={viewMode} onViewChange={setViewMode} />
                                <button
                                    type="button"
                                    className="btn btn-outline-light btn-sm px-3 py-2"
                                    onClick={closeDetails}
                                    style={{ borderRadius: '8px' }}
                                >
                                    <span className="fw-bold">✕ Close</span>
                                </button>
                            </div>
                        </div>

                        {/* Modal Body */}
                        <div className="modal-body p-0" style={{ flex: '1', overflow: 'auto', minHeight: '0' }}>
                            <div style={{ padding: '1rem 1.5rem' }}>
                                {timeReport && (
                                    <div className="mb-3">
                                        <div className="row g-2 mb-3">
                                            <div className="col-md-3">
                                                <div className="card border-0 bg-light h-100">
                                                    <div className="card-body text-center p-2">
                                                        <div className="text-primary mb-2">
                                                            <span className="fs-4">📈</span>
                                                        </div>
                                                        <h6 className="card-title text-muted mb-1">Hours</h6>
                                                        <p className="card-text fs-5 fw-bold text-dark mb-0">
                                                            {(() => {
                                                                if (!timeReport.timeReportTasks) return '0h';
                                                                let total = 0;
                                                                timeReport.timeReportTasks.forEach(task => {
                                                                    task.timeEntries.forEach(entry => {
                                                                        if (entry.time) total += parseFloat(entry.time) || 0;
                                                                    });
                                                                });
                                                                return total.toFixed(1) + 'h';
                                                            })()}
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="col-md-3">
                                                <div className="card border-0 bg-light h-100">
                                                    <div className="card-body text-center p-2">
                                                        <div className="text-info mb-2">
                                                            <span className="fs-4">📋</span>
                                                        </div>
                                                        <h6 className="card-title text-muted mb-1">Tasks</h6>
                                                        <p className="card-text fs-5 fw-bold text-dark mb-0">
                                                            {timeReport.timeReportTasks ? timeReport.timeReportTasks.length : 0}
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="col-md-3">
                                                <div className="card border-0 bg-light h-100">
                                                    <div className="card-body text-center p-2">
                                                        <div className="text-warning mb-2">
                                                            <span className="fs-4">📅</span>
                                                        </div>
                                                        <h6 className="card-title text-muted mb-1">Days</h6>
                                                        <p className="card-text fs-5 fw-bold text-dark mb-0">
                                                            {timeReport.days ? timeReport.days.length : 0}
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="col-md-3">
                                                <div className="card border-0 bg-light h-100">
                                                    <div className="card-body text-center p-2">
                                                        <div className="text-success mb-2">
                                                            <span className="fs-4">⏱️</span>
                                                        </div>
                                                        <h6 className="card-title text-muted mb-1">Avg/Day</h6>
                                                        <p className="card-text fs-5 fw-bold text-dark mb-0">
                                                            {(() => {
                                                                if (!timeReport.timeReportTasks || !timeReport.days) return '0h';
                                                                let total = 0;
                                                                timeReport.timeReportTasks.forEach(task => {
                                                                    task.timeEntries.forEach(entry => {
                                                                        if (entry.time) total += parseFloat(entry.time) || 0;
                                                                    });
                                                                });
                                                                const workDays = timeReport.days.filter(day => !day.weekend && !day.majorHoliday).length;
                                                                return workDays > 0 ? (total / workDays).toFixed(1) + 'h' : '0h';
                                                            })()}
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                )}

                                <div className="card border-0 shadow-sm">
                                    <div className="card-header bg-white border-bottom py-2">
                                        <h6 className="mb-0 fw-bold text-dark">
                                            {viewMode === 'table' ? '📊 Detailed Time Entries' : '📈 Time Visualization'}
                                        </h6>
                                    </div>
                                    <div className="card-body p-3">
                                        {viewMode === 'table' ? (
                                            <div className="table-responsive" style={{ 
                                                overflowX: 'auto', 
                                                overflowY: 'auto',
                                                maxHeight: 'calc(75vh - 200px)',
                                                border: '1px solid #dee2e6',
                                                borderRadius: '8px'
                                            }}>
                                                <table className="table-sm time-report-table mb-0" style={{ 
                                                    fontSize: '0.9rem',
                                                    minWidth: 'max-content',
                                                    width: 'auto'
                                                }}>
                                                    <thead className="table-success sticky-top">
                                                        {headerRow}
                                                    </thead>
                                                    <tbody>
                                                        {rows}
                                                    </tbody>
                                                </table>
                                            </div>
                                        ) : (
                                            <div>
                                                <div className="mb-4">
                                                    <h6 className="mb-3 fw-bold text-primary">📊 Daily Time Distribution</h6>
                                                    <TimeTrendChart
                                                        timeReportTasks={timeReport?.timeReportTasks}
                                                        days={timeReport?.days}
                                                    />
                                                </div>

                                                {/* Transform data for other charts */}
                                                {(() => {
                                                    if (!timeReport?.timeReportTasks) return null;

                                                    // Create user reports format for chart components
                                                    const userReports = [{
                                                        fullName: "Current User",
                                                        totalTime: (() => {
                                                            let total = 0;
                                                            timeReport.timeReportTasks.forEach(task => {
                                                                task.timeEntries.forEach(entry => {
                                                                    if (entry.time) total += parseFloat(entry.time) || 0;
                                                                });
                                                            });
                                                            return total;
                                                        })(),
                                                        taskReports: timeReport.timeReportTasks.map(task => ({
                                                            accountName: task.task.account.name,
                                                            taskName: task.task.name,
                                                            totalHours: (() => {
                                                                let total = 0;
                                                                task.timeEntries.forEach(entry => {
                                                                    if (entry.time) total += parseFloat(entry.time) || 0;
                                                                });
                                                                return total;
                                                            })()
                                                        }))
                                                    }];

                                                    return (
                                                        <>
                                                            <div className="mb-4">
                                                                <h6 className="mb-3 fw-bold text-success">📋 Task Distribution</h6>
                                                                <TaskDistributionPieChart userReports={userReports} />
                                                            </div>

                                                            <div className="mb-4">
                                                                <h6 className="mb-3 fw-bold text-info">🏢 Hours by Account</h6>
                                                                <AccountHoursChart userReports={userReports} />
                                                            </div>
                                                        </>
                                                    );
                                                })()}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Modal Footer */}
                        <div className="modal-footer bg-light border-top-0 py-3">
                            <small className="text-muted me-auto">
                                💡 <strong>Tip:</strong> Hover over truncated names to see full text
                            </small>
                            <button
                                type="button"
                                className="btn btn-primary px-4"
                                onClick={closeDetails}
                                style={{ borderRadius: '8px' }}
                            >
                                Done
                            </button>
                        </div>
                    </div>
                </Modal>
            ) : ''}
        </div>
    )
}

function UserTaskReportTable({ report, reportView, fromDate }) {
    const { showError } = useToast();
    const [viewMode, setViewMode] = useState('table');

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
                    <div className="d-flex justify-content-between align-items-center">
                        <h5 className="mb-0 fw-bold text-white">👥 User Task Time Summary</h5>
                        <ChartViewToggle viewMode={viewMode} onViewChange={setViewMode} />
                    </div>
                </div>
                <div className="card-body p-0">
                    {viewMode === 'table' ? (
                        <div className="table-responsive">
                            <table className="table table-hover mb-0">
                                {rows}
                            </table>
                        </div>
                    ) : (
                        <div className="p-3">
                            <div className="row">
                                <div className="col-lg-6 mb-4">
                                    <h6 className="mb-3 fw-bold text-primary">👤 User Hours Overview</h6>
                                    <UserTaskBarChart userReports={report.userReports} />
                                </div>
                                <div className="col-lg-6 mb-4">
                                    <h6 className="mb-3 fw-bold text-success">📋 Task Distribution</h6>
                                    <TaskDistributionPieChart userReports={report.userReports} />
                                </div>
                            </div>
                            <div className="row">
                                <div className="col-12">
                                    <h6 className="mb-3 fw-bold text-info">🏢 Hours by Account</h6>
                                    <AccountHoursChart userReports={report.userReports} />
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default UserTaskReportTable;