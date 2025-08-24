import React from "react";
import TimeReportStatusService from '../../service/TimeReportStatusService';

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
                alert(errorMessage);
                
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
            <tr key={0} className="bg-success text-white">
                <th>User</th>
                <th>Email</th>
                <th>Total Hours</th>
                <th>Workable Hours</th>
                <th>Report Status</th>
                <th>Action</th>
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
                        <td>{user.fullName || 'Unknown User'}</td>
                        <td>{user.email || 'No Email'}</td>
                        <td className={textColor}>{totalTime}</td>
                        <td>{workableHours}</td>
                        <td>
                            <span style={{color: closeTextColor}}>
                                {user.closed ? 'Closed' : 'Open'}
                            </span>
                        </td>
                        <td>
                            <label>
                                <input 
                                    type="checkbox" 
                                    id={user.userId} 
                                    name={fromDate} 
                                    checked={user.closed || false} 
                                    onChange={this.handleOpenCloseReport}
                                />
                                <span className="ml-2">{user.closed ? 'Open' : 'Close'}</span>
                            </label>
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
            <div className="table-responsive">
                <table className="table">
                    <tbody>
                        {rows}
                    </tbody>
                </table>
            </div>
        );
    }
}

export default class UnclosedUsersPage extends React.Component {
    constructor(props) {
        super(props);
        this.handlePreviousReport = this.handlePreviousReport.bind(this);
        this.handleNextReport = this.handleNextReport.bind(this);
        this.handleReportUpdate = this.handleReportUpdate.bind(this);
        this.state = { report: null };
    }

    componentDidMount() {
        this.loadCurrentReport();
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
                alert(errorMessage);
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
                alert(errorMessage);
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
                alert(errorMessage);
            });
    }

    handleReportUpdate(updatedReport) {
        this.setState({ report: updatedReport });
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
            <div className="container-fluid ml-4">
                <div className="row mb-3">
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={fromDate} onClick={this.handlePreviousReport}>&lt;&lt;</button>
                    </div>
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={toDate} onClick={this.handleNextReport}>&gt;&gt;</button>
                    </div>
                    <div className="col-sm-8">
                        <h2>Users with Unclosed Time Reports</h2>
                    </div>
                    <div className="col-sm-2">
                        <span className="float-right">
                            <b>{fromDate} - {toDate}</b>
                        </span>
                    </div>
                </div>
                <div className="row">
                    <div className="col-sm-12">
                        <UnclosedUsersTable report={this.state.report} onReportUpdate={this.handleReportUpdate} />
                    </div>
                </div>
            </div>
        );
    }
};