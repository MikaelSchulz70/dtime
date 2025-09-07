import React from "react";
import TimeReportStatusService from '../../service/TimeReportStatusService';
import SystemService from '../../service/SystemService';
import { useToast } from '../../components/Toast';

class UnclosedUsersTable extends React.Component {
    constructor(props) {
        super(props);
        this.handleOpenCloseReport = this.handleOpenCloseReport.bind(this);
        this.state = { report: this.props.report };
    }

    componentDidUpdate(prevProps) {
        if (this.props.report !== prevProps.report) {
            this.setState({ report: this.props.report });
        }
    }

    handleOpenCloseReport(event) {
        const userId = parseInt(event.target.id);
        const date = event.target.name;

        if (!this.state.report.unclosedUsers) return;

        const user = this.state.report.unclosedUsers.find(u => u.userId === userId);
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
                const updatedUsers = this.state.report.unclosedUsers.map(u =>
                    u.userId === userId ? { ...u, closed: !u.closed } : u
                );

                // Filter out users who are now closed (since this is unclosed users view)
                const filteredUsers = updatedUsers.filter(u => !u.closed);

                const updatedReport = { ...this.state.report, unclosedUsers: filteredUsers };
                this.setState({ report: updatedReport });

                if (this.props.onReportUpdate) {
                    this.props.onReportUpdate(updatedReport);
                }
            })
            .catch(error => {
                console.error('Error toggling report status:', error);
                const errorMessage = error.response?.data?.message || error.message || 'Failed to open/close report';
                this.props.showError(errorMessage);

                // Revert the checkbox on error
                event.target.checked = user.closed;
            });
    }

    render() {
        if (this.state == null || this.state.report == null) {
            return (
                <div className="text-center">
                    <div className="spinner-border" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            );
        }

        var rows = [];
        const workableHours = this.state.report.workableHours || 0;
        const fromDate = this.state.report.fromDate || '';

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
        if (this.state.report.unclosedUsers && Array.isArray(this.state.report.unclosedUsers) && this.state.report.unclosedUsers.length > 0) {
            this.state.report.unclosedUsers.forEach((user) => {
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
                                    onChange={this.handleOpenCloseReport}
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
                        {this.state.report.unclosedUsers === undefined ?
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
}

class UnclosedUsersPage extends React.Component {
    constructor(props) {
        super(props);
        this.handlePreviousReport = this.handlePreviousReport.bind(this);
        this.handleNextReport = this.handleNextReport.bind(this);
        this.handleReportUpdate = this.handleReportUpdate.bind(this);
        this.handleSendEmailReminder = this.handleSendEmailReminder.bind(this);
        this.state = { report: null, mailEnabled: false };
    }

    componentDidMount() {
        this.loadCurrentReport();
        this.checkMailEnabled();
    }

    loadCurrentReport() {
        const self = this;

        TimeReportStatusService.getCurrentUnclosedUsers()
            .then(response => {
                console.log('Unclosed users response:', response);
                self.setState({ report: response });
            })
            .catch(error => {
                console.error('Error loading unclosed users report:', error);
                const errorMessage = error.response?.data?.message || error.message || 'Failed to load unclosed users report';
                this.props.showError(errorMessage);
            });
    }

    handlePreviousReport(event) {
        const date = event.target.name;
        const self = this;

        TimeReportStatusService.getPreviousUnclosedUsers(date)
            .then(response => {
                console.log('Previous unclosed users response:', response);
                self.setState({ report: response });
            })
            .catch(error => {
                console.error('Error loading previous unclosed users report:', error);
                const errorMessage = error.response?.data?.message || error.message || 'Failed to load previous unclosed users report';
                this.props.showError(errorMessage);
            });
    }

    handleNextReport(event) {
        const date = event.target.name;
        const self = this;

        TimeReportStatusService.getNextUnclosedUsers(date)
            .then(response => {
                console.log('Next unclosed users response:', response);
                self.setState({ report: response });
            })
            .catch(error => {
                console.error('Error loading next unclosed users report:', error);
                const errorMessage = error.response?.data?.message || error.message || 'Failed to load next unclosed users report';
                this.props.showError(errorMessage);
            });
    }

    checkMailEnabled() {
        const systemService = new SystemService();
        systemService.isMailEnabled()
            .then(response => {
                this.setState({ mailEnabled: response.data });
            })
            .catch(error => {
                console.error('Error checking mail enabled status:', error);
                // Default to false if we can't check
                this.setState({ mailEnabled: false });
            });
    }

    handleReportUpdate(updatedReport) {
        this.setState({ report: updatedReport });
    }

    handleSendEmailReminder() {
        const unclosedUsers = this.state.report.unclosedUsers || [];
        const unclosedCount = unclosedUsers.length;

        if (unclosedCount === 0) {
            this.props.showWarning('No users have unclosed time reports. No emails will be sent.');
            return;
        }

        const confirmMessage = `Send email reminders to ${unclosedCount} user${unclosedCount === 1 ? '' : 's'} who have unclosed time reports for this month?`;
        if (!window.confirm(confirmMessage)) {
            return;
        }

        const systemService = new SystemService();
        systemService.sendEmailReminderToUnclosedUsers()
            .then(response => {
                this.props.showSuccess(`Email reminders sent successfully to ${unclosedCount} user${unclosedCount === 1 ? '' : 's'} with unclosed time reports`);
            })
            .catch(error => {
                console.error('Error sending email reminders:', error);
                const errorMessage = error.response?.data?.message || error.message || 'Failed to send email reminders';
                alert('Failed to send email reminders: ' + errorMessage);
            });
    }

    render() {
        if (this.state == null || this.state.report == null)
            return (
                <div className="text-center">
                    <div className="spinner-border" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            );

        const fromDate = this.state.report.fromDate || '';
        const toDate = this.state.report.toDate || '';

        return (
            <div className="container-fluid p-4">
                <div className="card shadow-sm mb-4">
                    <div className="card-header bg-warning text-dark">
                        <div className="row align-items-center">
                            <div className="col-sm-6">
                                <h2 className="mb-0 fw-bold">⚠️ Unclosed Time Reports</h2>
                            </div>
                            <div className="col-sm-6 text-end">
                                {this.state.mailEnabled && (
                                    <button
                                        className="btn btn-primary btn-sm me-3"
                                        onClick={this.handleSendEmailReminder}
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
                                    <button className="btn btn-success btn-sm" name={fromDate} onClick={this.handlePreviousReport} title="Previous Period">
                                        &lt;&lt;
                                    </button>
                                    <button className="btn btn-success btn-sm" name={toDate} onClick={this.handleNextReport} title="Next Period">
                                        &gt;&gt;
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="row">
                    <div className="col-sm-12">
                        <UnclosedUsersTable report={this.state.report} onReportUpdate={this.handleReportUpdate} showError={this.props.showError} />
                    </div>
                </div>
            </div>
        );
    }
}

export default function UnclosedUsersPageWithToast(props) {
    const { showError, showSuccess, showWarning } = useToast();
    return <UnclosedUsersPage {...props} showError={showError} showSuccess={showSuccess} showWarning={showWarning} />;
}