import React, { useState, useEffect, useCallback } from "react";
import Modal from 'react-modal';
import * as Constants from '../../common/Constants';
import TimeService from '../../service/TimeService';
import { useToast } from '../../components/Toast';

Modal.setAppElement('#root');

function TimeReportTableEntry({ timeReportDay }) {
    if (timeReportDay == null) return null;
    
    var cellClass = 'text-center py-1 px-1 border-end';
    var badgeClass = 'badge rounded px-1 py-0 fw-bold';
    var time = (timeReportDay.time == null || timeReportDay.time === 0 ? '' : timeReportDay.time);
    
    if (timeReportDay.day.weekend) {
        cellClass += ' bg-warning bg-opacity-10';
        badgeClass += time ? ' bg-warning text-dark' : ' bg-light text-muted';
    } else if (timeReportDay.day.majorHoliday) {
        cellClass += ' bg-danger bg-opacity-10';
        badgeClass += time ? ' bg-danger text-white' : ' bg-light text-muted';
    } else {
        cellClass += ' bg-light';
        badgeClass += time ? ' bg-primary text-white' : ' bg-light text-muted';
    }

    return (
        <td className={cellClass} style={{ width: '35px', minWidth: '35px', maxWidth: '35px', fontSize: '0.75rem' }}>
            {time ? (
                <span className={badgeClass} style={{ fontSize: '0.7rem' }}>
                    {time}h
                </span>
            ) : (
                <span className="text-muted" style={{ fontSize: '0.7rem' }}>—</span>
            )}
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
    var accountShortName = accountName.substring(0, Math.min(40, accountName.length));
    var taskName = timeReportTask.task.name;
    var taskShortName = taskName.substring(0, Math.min(40, taskName.length));

    return (
        <tr className="border-bottom">
            <td className="text-start py-2 px-3 border-end" title={accountName} style={{ width: '300px', minWidth: '300px' }}>
                <div className="d-flex align-items-center">
                    <span className="me-2 text-primary fs-6">🏢</span>
                    <span className="fw-bold text-dark" style={{ fontSize: '0.95rem' }}>
                        {accountShortName}{accountName.length > 40 ? '...' : ''}
                    </span>
                </div>
            </td>
            <td className="text-start py-2 px-3 border-end" title={taskName} style={{ width: '300px', minWidth: '300px' }}>
                <div className="d-flex align-items-center">
                    <span className="me-2 text-success fs-6">📋</span>
                    <span className="fw-bold text-dark" style={{ fontSize: '0.95rem' }}>
                        {taskShortName}{taskName.length > 40 ? '...' : ''}
                    </span>
                </div>
            </td>
            <td className="text-center py-2 px-2 border-end" style={{ width: '70px', minWidth: '70px' }}>
                <span className="badge bg-success rounded px-2 py-1 fw-bold small">
                    {totalTaskTime || 0}h
                </span>
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
        days.forEach(function (day, index) {
            var cellClass = 'text-center py-1 px-2 border-end';
            var badgeClass = 'badge rounded px-1 py-0 small';
            
            if (day.weekend) {
                badgeClass += ' bg-warning text-dark';
            } else if (day.majorHoliday) {
                badgeClass += ' bg-danger text-white';
            } else {
                badgeClass += ' bg-primary text-white';
            }

            columns.push(
                <th key={index} className={cellClass} style={{ width: '35px', minWidth: '35px', maxWidth: '35px' }}>
                    <span className={badgeClass}>{day.day}</span>
                </th>
            );
        });
    }

    return (
        <tr>
            <th className="text-start py-2 px-3 fw-bold text-dark border-end" style={{ width: '300px', minWidth: '300px' }}>
                <span className="d-flex align-items-center">
                    <span className="me-2">🏢</span>Account
                </span>
            </th>
            <th className="text-start py-2 px-3 fw-bold text-dark border-end" style={{ width: '300px', minWidth: '300px' }}>
                <span className="d-flex align-items-center">
                    <span className="me-2">📋</span>Task
                </span>
            </th>
            <th className="text-center py-2 px-2 fw-bold text-dark border-end" style={{ width: '70px', minWidth: '70px' }}>
                <span className="d-flex align-items-center justify-content-center">
                    <span className="me-1">⏱️</span>Total
                </span>
            </th>
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
                            maxWidth: '95%',
                            maxHeight: '95%',
                            padding: '0',
                            border: 'none',
                            borderRadius: '12px',
                            boxShadow: '0 10px 30px rgba(0, 0, 0, 0.3)'
                        },
                        overlay: {
                            backgroundColor: 'rgba(0, 0, 0, 0.6)',
                            zIndex: 1050
                        }
                    }}
                >
                    <div className="modal-content border-0" style={{ borderRadius: '12px', overflow: 'hidden' }}>
                        {/* Modal Header */}
                        <div className="modal-header bg-gradient" style={{
                            background: 'linear-gradient(135deg, #28a745 0%, #20c997 100%)',
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
                            <button 
                                type="button" 
                                className="btn btn-outline-light btn-sm px-3 py-2"
                                onClick={closeDetails}
                                style={{ borderRadius: '8px' }}
                            >
                                <span className="fw-bold">✕ Close</span>
                            </button>
                        </div>

                        {/* Modal Body */}
                        <div className="modal-body p-0" style={{ maxHeight: '75vh', overflowY: 'auto' }}>
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
                                                        <h6 className="card-title text-muted mb-1">Total Hours</h6>
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
                                        <h6 className="mb-0 fw-bold text-dark">📊 Detailed Time Entries</h6>
                                    </div>
                                    <div className="table-responsive">
                                        <table className="table table-hover table-sm mb-0" style={{ fontSize: '0.85rem' }}>
                                            <thead style={{ 
                                                background: 'linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)',
                                                borderBottom: '2px solid #dee2e6'
                                            }}>
                                                {headerRow}
                                            </thead>
                                            <tbody>
                                                {rows}
                                            </tbody>
                                        </table>
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