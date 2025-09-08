import React, { useState, useEffect, useCallback } from "react";
import TimeReportStatusService from '../../service/TimeReportStatusService';
import SystemService from '../../service/SystemService';
import { useToast } from '../../components/Toast';

function UnclosedUsersTable({ report: initialReport, onReportUpdate, showError }) {
    const [report, setReport] = useState(initialReport);

    useEffect(() => {
        setReport(initialReport);
    }, [initialReport]);

    const handleOpenCloseReport = useCallback((event) => {
        const userId = parseInt(event.target.id);
        const date = event.target.name;

        if (!report.unclosedUsers) return;

        const user = report.unclosedUsers.find(u => u.userId === userId);
        if (!user) return;

        // Show confirmation dialog when closing a time report
        if (!user.closed) {
            const confirmMessage = `Do you really want to close the time report for ${user.fullName || 'this user'}?`;
            if (!window.confirm(confirmMessage)) {
                // User cancelled, revert the checkbox
                event.target.checked = false;
                return;
            }
        }

        const service = user.closed ?
            TimeReportStatusService.openUserTimeReport(userId, date) :
            TimeReportStatusService.closeUserTimeReport(userId, date);

        service
            .then(response => {
                // Update the user's closed status
                const updatedUsers = report.unclosedUsers.map(u =>
                    u.userId === userId ? { ...u, closed: !u.closed } : u
                );

                // Filter out users who are now closed (since this is unclosed users view)
                const filteredUsers = updatedUsers.filter(u => !u.closed);

                const updatedReport = { ...report, unclosedUsers: filteredUsers };
                setReport(updatedReport);

                if (onReportUpdate) {
                    onReportUpdate(updatedReport);
                }
            })
            .catch(error => {
                console.error('Error toggling report status:', error);
                const errorMessage = error.response?.data?.message || error.message || 'Failed to open/close report';
                showError(errorMessage);

                // Revert the checkbox on error
                event.target.checked = user.closed;
            });
    }, [report, onReportUpdate, showError]);

    if (report == null) {
        return (
            <div className="text-center">
                <div className="spinner-border" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );
    }

    var rows = [];
    const workableHours = report.workableHours || 0;
    const fromDate = report.fromDate || '';

    rows.push(
        <tr key={0} className="bg-primary text-white">
            <th className="fw-bold">👤 User</th>
            <th className="fw-bold">📧 Email</th>
            <th className="fw-bold">⏱️ Total Hours</th>
            <th className="fw-bold">📊 Workable Hours</th>
            <th className="fw-bold">📋 Status</th>
            <th className="fw-bold">⚙️ Action</th>
        </tr>);

    var key = 1;
    if (report.unclosedUsers && Array.isArray(report.unclosedUsers) && report.unclosedUsers.length > 0) {
        report.unclosedUsers.forEach((user) => {
            if (!user) return; // Skip null/undefined users

            const totalTime = user.totalTime || 0;
            const textColor = totalTime < workableHours ? 'text-danger' : '';
            const closeTextColor = user.closed ? "white" : "red";

            rows.push(
                <tr key={key}>
                    <td className="fw-medium">{user.fullName || 'Unknown User'}</td>
                    <td className="text-muted">{user.email || 'No Email'}</td>
                    <td className={`fw-bold ${textColor}`}>{totalTime} hrs</td>
                    <td className="fw-bold text-primary">{workableHours} hrs</td>
                    <td>
                        <span className={`badge ${user.closed ? 'bg-primary' : 'bg-warning text-dark'} py-1 px-2`}>
                            {user.closed ? '✅ Closed' : '⏳ Open'}
                        </span>
                    </td>
                    <td>
                        <div className="form-check">
                            <input
                                className="form-check-input"
                                type="checkbox"
                                id={user.userId}
                                name={fromDate}
                                checked={user.closed || false}
                                onChange={handleOpenCloseReport}
                            />
                            <label className="form-check-label fw-medium" htmlFor={user.userId}>
                                {user.closed ? '🔓 Reopen' : '🔒 Close'}
                            </label>
                        </div>
                    </td>
                </tr>);
            key++;
        });
    } else {
        rows.push(
            <tr key={key}>
                <td colSpan="6" className="text-center text-muted">
                    {report.unclosedUsers === undefined ?
                        'Loading users...' :
                        'All users have closed their time reports for this month'
                    }
                </td>
            </tr>
        );
    }

    return (
        <div className="card shadow-sm">
            <div className="card-header bg-light">
                <h5 className="mb-0 fw-bold text-muted">👥 User Status Overview</h5>
            </div>
            <div className="card-body p-0">
                <div className="table-responsive">
                    <table className="table table-hover table-striped mb-0">
                        <tbody>
                            {rows}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}

function UnclosedUsersPage(props) {
    const [report, setReport] = useState(null);
    const [mailEnabled, setMailEnabled] = useState(false);
    const { showError, showSuccess, showWarning } = useToast();

    useEffect(() => {
        loadCurrentReport();
        checkMailEnabled();
    }, []);

    const loadCurrentReport = useCallback(() => {
        TimeReportStatusService.getCurrentUnclosedUsers()
            .then(response => {
                console.log('Unclosed users response:', response);
                setReport(response);
            })
            .catch(error => {
                console.error('Error loading unclosed users report:', error);
                const errorMessage = error.response?.data?.message || error.message || 'Failed to load unclosed users report';
                showError(errorMessage);
            });
    }, [showError]);

    const handlePreviousReport = useCallback((event) => {
        const date = event.target.name;

        TimeReportStatusService.getPreviousUnclosedUsers(date)
            .then(response => {
                console.log('Previous unclosed users response:', response);
                setReport(response);
            })
            .catch(error => {
                console.error('Error loading previous unclosed users report:', error);
                const errorMessage = error.response?.data?.message || error.message || 'Failed to load previous unclosed users report';
                showError(errorMessage);
            });
    }, [showError]);

    const handleNextReport = useCallback((event) => {
        const date = event.target.name;

        TimeReportStatusService.getNextUnclosedUsers(date)
            .then(response => {
                console.log('Next unclosed users response:', response);
                setReport(response);
            })
            .catch(error => {
                console.error('Error loading next unclosed users report:', error);
                const errorMessage = error.response?.data?.message || error.message || 'Failed to load next unclosed users report';
                showError(errorMessage);
            });
    }, [showError]);

    const checkMailEnabled = useCallback(() => {
        const systemService = new SystemService();
        systemService.isMailEnabled()
            .then(response => {
                setMailEnabled(response.data);
            })
            .catch(error => {
                console.error('Error checking mail enabled status:', error);
                // Default to false if we can't check
                setMailEnabled(false);
            });
    }, []);

    const handleReportUpdate = useCallback((updatedReport) => {
        setReport(updatedReport);
    }, []);

    const handleSendEmailReminder = useCallback(() => {
        const unclosedUsers = report.unclosedUsers || [];
        const unclosedCount = unclosedUsers.length;

        if (unclosedCount === 0) {
            showWarning('No users have unclosed time reports. No emails will be sent.');
            return;
        }

        const confirmMessage = `Send email reminders to ${unclosedCount} user${unclosedCount === 1 ? '' : 's'} who have unclosed time reports for this month?`;
        if (!window.confirm(confirmMessage)) {
            return;
        }

        const systemService = new SystemService();
        systemService.sendEmailReminderToUnclosedUsers()
            .then(response => {
                showSuccess(`Email reminders sent successfully to ${unclosedCount} user${unclosedCount === 1 ? '' : 's'} with unclosed time reports`);
            })
            .catch(error => {
                console.error('Error sending email reminders:', error);
                const errorMessage = error.response?.data?.message || error.message || 'Failed to send email reminders';
                showError('Failed to send email reminders: ' + errorMessage);
            });
    }, [report, showWarning, showSuccess, showError]);

    if (report == null)
        return (
            <div className="text-center">
                <div className="spinner-border" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );

    const fromDate = report.fromDate || '';
    const toDate = report.toDate || '';

    return (
        <div className="container-fluid p-4">
            <div className="card shadow-sm mb-4">
                <div className="card-header bg-warning text-dark">
                    <div className="row align-items-center">
                        <div className="col-sm-6">
                            <h2 className="mb-0 fw-bold">⚠️ Unclosed Time Reports</h2>
                        </div>
                        <div className="col-sm-6 text-end">
                            {mailEnabled && (
                                <button
                                    className="btn btn-primary btn-sm me-3"
                                    onClick={handleSendEmailReminder}
                                    title="Send email reminders to users with unclosed time reports"
                                >
                                    📧 Send Reminders
                                </button>
                            )}
                            <span className="badge bg-secondary fs-6 py-2 px-3">
                                📅 {fromDate} - {toDate}
                            </span>
                        </div>
                    </div>
                </div>
                <div className="card-body">
                    <div className="row mb-3">
                        <div className="col-sm-2">
                            <div className="d-flex gap-2" role="group" aria-label="Navigation">
                                <button className="btn btn-success btn-sm" name={fromDate} onClick={handlePreviousReport} title="Previous Period">
                                    &lt;&lt;
                                </button>
                                <button className="btn btn-success btn-sm" name={toDate} onClick={handleNextReport} title="Next Period">
                                    &gt;&gt;
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col-sm-12">
                    <UnclosedUsersTable report={report} onReportUpdate={handleReportUpdate} showError={showError} />
                </div>
            </div>
        </div>
    );
}

export default UnclosedUsersPage;