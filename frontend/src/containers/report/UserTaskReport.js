import React from "react";
import Modal from 'react-modal';
import * as Constants from '../../common/Constants';
import ReportService from '../../service/ReportService';
import TimeService from '../../service/TimeService';

Modal.setAppElement('#root');

class TimeReportTableEntry extends React.Component {
    constructor(props) {
        super(props);
        this.state = { timeReportDay: this.props.timeReportDay };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null) return null;
        var backGroundColor = Constants.CLOSED_COLOR;
        if (this.state.timeReportDay.day.weekend) {
            backGroundColor = Constants.WEEKEND_COLOR;
        } else if (this.state.timeReportDay.day.majorHoliday) {
            backGroundColor = Constants.WEEKEND_COLOR;
        } else {
            backGroundColor = Constants.DAY_COLOR;
        }

        var time = (this.state.timeReportDay.time == null || this.state.timeReportDay.time === 0 ? '' : this.state.timeReportDay.time);
        const inputStyle = {
            backgroundColor: backGroundColor,
            width: '55px'
        }

        return (
            <td style={{ padding: "0px" }}><input style={inputStyle} readOnly={true} type="text" value={time} /></td>
        );
    }
};

class UserReportSummaryTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { report: this.props.report };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null)
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
                <td className="fw-bold">{this.state.report.workableHours}</td>
                <td className="fw-bold">{this.state.report.totalWorkableHours}</td>
                <td className="fw-bold text-success">{this.state.report.totalHoursWorked}</td>
                <td></td>
            </tr>
        );

        return (
            <tbody>
                {rows}
            </tbody>
        );
    }
}

class TimeReportTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { timeReportTask: this.props.timeReportTask, totalTaskTime: this.props.totalTaskTime };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null) return null;
        var entries = [];
        if (this.state.timeReportTask != null) {
            this.state.timeReportTask.timeEntries.forEach(function (timeEntry) {
                entries.push(
                    <TimeReportTableEntry timeReportDay={timeEntry} />);
            });
        }

        var accountName = this.state.timeReportTask.task.account.name;
        var accountShortName = accountName.substring(0, Math.min(20, accountName.length));
        var taskName = this.state.timeReportTask.task.name;
        var taskShortName = taskName.substring(0, Math.min(20, taskName.length));

        return (
            <tr>
                <th className="text-nowrap" title={accountName}>{accountShortName}</th>
                <th className="text-nowrap" title={taskName}>{taskShortName}</th>
                <th><input style={{ width: '55px' }} readOnly={true} name={this.state.timeReportTask.task.name} type="text" value={this.state.totalTaskTime} /></th>
                {entries}
            </tr>
        );
    }
};

class UserReportRows extends React.Component {
    constructor(props) {
        super(props);
        this.handleOpenCloseReport = this.handleOpenCloseReport.bind(this);
        this.state = { userReport: this.props.userReport, reportView: this.props.reportView, workableHours: this.props.workableHours, fromDate: this.props.fromDate };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    handleOpenCloseReport(event) {
        var userId = event.target.id;
        var date = event.target.name;

        var urlPath = '';
        if (this.state.userReport.closed) {
            urlPath = 'open';
        } else {
            urlPath = 'close';
        }

        var payLoad = '{ "userId": "' + userId + '", "closeDate": "' + date + '"}';
        var self = this;
        var service = new ReportService();
        service.updateOpenCloseReport(payLoad, urlPath)
            .then(response => {
                if (self.state.userReport.closed) {
                    self.state.userReport.closed = false;
                } else {
                    self.state.userReport.closed = true;
                }
                self.setState({ userReport: self.state.userReport });
            })
            .catch(error => {
                alert('Failed to open/close report');
            });
    }

    render() {
        if (this.state == null || this.state.userReport == null)
            return null;

        var closeTextColor = "red";
        if (this.state.userReport.closed) {
            closeTextColor = "white";
        }

        var rows = [];
        var keyBase = this.state.userReport.userId + '_';
        var keyHeader = keyBase + '0';
        rows.push(<tr key={keyHeader} className="bg-success text-white">)
            <th className="fw-bold">{this.state.userReport.fullName}</th>
            <th className="fw-bold">Account</th>
            <th className="fw-bold">Task</th>
            <th className="fw-bold">Total Hours</th>
            <th></th>
            <th></th>
        </tr>);

        this.state.userReport.taskReports.forEach(function (taskReport) {
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
                    <td></td>
                    <td></td>
                </tr>);
        });

        var textColor = '';
        if (this.state.userReport.totalTime < this.state.workableHours) {
            textColor = 'text-danger';
        }

        var key = keyBase + "_footer";
        rows.push(<tr key={key} className="table-light border-top border-2">
            <th className="text-muted"></th>
            <th className="fw-bold text-dark">Total Time</th>
            <th></th>
            <th className={`fw-bold fs-6 ${textColor}`}>{this.state.userReport.totalTime} hrs</th>
            <th></th>
            <th>
                {this.state.reportView === Constants.MONTH_VIEW ? (
                    <UserDetailReport userId={this.state.userReport.userId} fromDate={this.state.fromDate} toDate={this.state.toDate} />
                ) : ''}
            </th>
        </tr>);
        return (
            <tbody>
                {rows}
            </tbody>
        );
    };
};

class TimeReportTableHeaderRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { days: this.props.days };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null || this.state.days == null)
            return null;
        var columns = [];
        if (this.state.days != null) {
            this.state.days.forEach(function (day) {
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
};

class UserDetailReport extends React.Component {
    constructor(props) {
        super(props);
        this.loadFromServer = this.loadFromServer.bind(this);
        this.showDetails = this.showDetails.bind(this);
        this.closeDetails = this.closeDetails.bind(this);
        this.state = { userId: this.props.userId, fromDate: this.props.fromDate, toDate: this.props.toDate, isOpen: false };
    }

    componentDidMount() {

    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    showDetails() {
        this.loadFromServer();
    }

    closeDetails() {
        this.setState({ isOpen: false });
    }

    loadFromServer() {
        var self = this;
        var service = new TimeService();
        service.getUserTimes(this.state.userId, this.state.fromDate)
            .then(response => {
                console.log('Server response:', response);
                self.setState({ timeReport: response.data, isOpen: true });
            })
            .catch(error => {
                console.error('Error loading user report:', error);
                alert('Failed to load user report: ' + (error.response?.data?.message || error.message));
            });
    }

    render() {
        if (this.state == null) {
            return null;
        }

        var headerRow = '';
        if (this.state.timeReport != null) {
            headerRow = <TimeReportTableHeaderRow days={this.state.timeReport.days} />;
        }

        console.log('Tr', this.state.timeReport);

        var rows = [];
        if (this.state.timeReport != null && this.state.timeReport.timeReportTasks != null) {
            this.state.timeReport.timeReportTasks.forEach(function (timeReportTask) {
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
                <button className="btn btn-outline-success btn-sm" onClick={this.showDetails}>
                    📋 View Details
                </button>
                {this.state.isOpen ? (
                    <Modal
                        isOpen={this.state.isOpen}
                        onRequestClose={this.closeDetails}
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
                                <button type="button" className="btn btn-outline-secondary btn-sm" onClick={this.closeDetails}>
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
}

export default class UserTaskReportTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { report: this.props.report, reportView: this.props.reportView, fromDate: this.props.fromDate };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null)
            return null;

        var rows = [];
        rows.push(
            <UserReportSummaryTable key={'summary-table'} report={this.state.report} />
        );

        var workableHours = this.state.report.workableHours;
        var reportView = this.state.reportView;
        var fromDate = this.state.report.fromDate;
        if (this.state.report.userReports != null) {
            this.state.report.userReports.forEach(function (userReport) {
                rows.push(<UserReportRows key={userReport.userId} userReport={userReport} workableHours={workableHours} reportView={reportView} fromDate={fromDate} />);
            });
        }

        return (
            <div className="col-12">
                <div className="card shadow-sm">
                    <div className="card-header bg-success text-white">
                        <h5 className="mb-0 fw-bold">👥 User Task Time Summary</h5>
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
};