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
                <th>Summary</th>
                <th>Total workable hours</th>
                <th>Workable hours</th>
                <th>Provision</th>
                <th>No provision</th>
                <th>Total</th>
                <th>On call</th>
                <th>Provision internal</th>
                <th>No provision internal</th>
                <th>Provision external</th>
            </tr>
        );

        rows.push(
            <tr key={'summary-info'}>
                <td></td>
                <td>{this.state.report.totalWorkableHours}</td>
                <td>{this.state.report.workableHours}</td>
                <td>{this.state.report.totalWorkedHoursProvision} ({this.state.report.totalWorkedHoursProvisionPcp}%)</td>
                <td>{this.state.report.totalWorkedHoursNoProvision} ({this.state.report.totalWorkedHoursNoProvisionPcp}%)</td>
                <td>{this.state.report.totalWorkedHours} ({this.state.report.totalWorkedHoursPcp}%)</td>
                <td>{this.state.report.totalWorkedHoursOnCall}</td>
                <td>{this.state.report.totalWorkedHoursInternalProvision} ({this.state.report.totalWorkedHoursInternalProvisionPcp}%)</td>
                <td>{this.state.report.totalWorkedHoursInternalNoProvision} ({this.state.report.totalWorkedHoursInternalNoProvisionPcp}%)</td>
                <td>{this.state.report.totalWorkedHoursExternalProvision} ({this.state.report.totalWorkedHoursExternalProvisionPcp}%)</td>
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
        this.state = { timeReportProject: this.props.timeReportProject, totalProjectTime: this.props.totalProjectTime };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null) return null;
        var entries = [];
        if (this.state.timeReportProject != null) {
            this.state.timeReportProject.timeReportDays.forEach(function (timeReportDay) {
                entries.push(
                    <TimeReportTableEntry timeReportDay={timeReportDay} />);
            });
        }

        var companyName = this.state.timeReportProject.project.company.name;
        var companyShortName = companyName.substring(0, Math.min(20, companyName.length));
        var projectName = this.state.timeReportProject.project.name;
        var projectShortName = projectName.substring(0, Math.min(20, projectName.length));

        return (
            <tr>
                <th className="text-nowrap" title={companyName}>{companyShortName}</th>
                <th className="text-nowrap" title={projectName}>{projectShortName}</th>
                <th><input style={{ width: '55px' }} readOnly={true} name={this.state.timeReportProject.project.name} type="text" value={this.state.totalProjectTime} /></th>
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
        var idUser = event.target.id;
        var date = event.target.name;

        var urlPath = '';
        if (this.state.userReport.closed) {
            urlPath = 'open';
        } else {
            urlPath = 'close';
        }

        var payLoad = '{ "idUser": "' + idUser + '", "closeDate": "' + date + '"}';
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
        var keyBase = this.state.userReport.idUser + '_';
        var keyHeader = keyBase + '0';
        rows.push(<tr key={keyHeader} className="bg-success text-white">
            <th>{this.state.userReport.userName}</th>
            <th>Company</th>
            <th>Project</th>
            <th>Provision</th>
            <th>No provision</th>
            <th>Total</th>
            <th>On call</th>
            <th>Provision hours</th>
            <th></th>
            <th>
                {this.state.reportView === Constants.MONTH_VIEW ? (
                    <label>
                        <font color={closeTextColor}>Closed</font>
                        <input className="ml-2" type="checkbox" id={this.state.userReport.idUser} name={this.state.fromDate} checked={this.state.userReport.closed} onClick={this.handleOpenCloseReport.bind(this)} />
                    </label>
                ) : ''}
            </th>
        </tr>);

        this.state.userReport.projectReports.forEach(function (projectReport) {
            var timeProvision = '';
            var timeNoProvision = '';
            var timeOnCall = '';
            if (projectReport.onCall) {
                timeOnCall = projectReport.totalHours;
            } else if (projectReport.provision === true) {
                timeProvision = projectReport.totalHours;
            } else {
                timeNoProvision = projectReport.totalHours;
            }

            var companyName = projectReport.companyName;
            var companyShortName = companyName.substring(0, Math.min(20, companyName.length));
            var projectName = projectReport.projectName;
            var projectShortName = projectName.substring(0, Math.min(20, projectName.length));

            var key = keyBase + '_' + projectReport.idProject;
            rows.push(
                <tr key={key}>
                    <td>{projectReport.userName}</td>
                    <td className="text-nowrap" title={companyName}>{companyShortName}</td>
                    <td className="text-nowrap" title={projectName}>{projectShortName}</td>
                    <td>{timeProvision}</td>
                    <td>{timeNoProvision}</td>
                    <td></td>
                    <td>{timeOnCall}</td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>);
        });

        var provisionHours = '';
        if (this.state.reportView === Constants.MONTH_VIEW) {
            provisionHours += this.state.userReport.provisionHours;
        }

        var textColor = '';
        if (this.state.userReport.totalTime < this.state.workableHours) {
            textColor = 'text-danger';
        }

        var key = keyBase + "_footer";
        rows.push(<tr key={key}>
            <th></th>
            <th>Total time</th>
            <th></th>
            <th className={textColor}>{this.state.userReport.totalTimeProvision}</th>
            <th className={textColor}>{this.state.userReport.totalTimeNoProvision}</th>
            <th className={textColor}>{this.state.userReport.totalTime}</th>
            <th>{this.state.userReport.totalTimeOnCall}</th>
            <th>{provisionHours}</th>
            <th></th>
            <th>
                {this.state.reportView === Constants.MONTH_VIEW ? (
                    <UserDetailReport idUser={this.state.userReport.idUser} fromDate={this.state.fromDate} toDate={this.state.toDate} />
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
            <tr>
                <th><font color={Constants.DAY_COLOR}>Company</font></th>
                <th><font color={Constants.DAY_COLOR}>Project</font></th>
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
        this.state = { idUser: this.props.idUser, fromDate: this.props.fromDate, toDate: this.props.toDate, isOpen: false };
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
        service.getUserTimes(this.state.idUser, this.state.fromDate)
            .then(response => {
                self.setState({ timeReport: response.data, isOpen: true });
            })
            .catch(error => {
                alert('Failed to load user report');
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

        var rows = [];
        if (this.state.timeReport != null && this.state.timeReport.timeReportProjectsExternal != null) {
            this.state.timeReport.timeReportProjectsExternal.forEach(function (timeReportProject) {
                var totalProjectTime = 0;
                timeReportProject.timeReportDays.forEach(function (timeReportDay) {
                    if (timeReportDay.time != null) {
                        var time = parseFloat(timeReportDay.time);
                        if (!isNaN(time)) {
                            totalProjectTime += time;
                        }
                    }
                });

                rows.push(
                    <TimeReportTableRow timeReportProject={timeReportProject} totalProjectTime={totalProjectTime} />);
            });
        }

        if (this.state.timeReport != null && this.state.timeReport.timeReportProjectsInternal != null) {
            this.state.timeReport.timeReportProjectsInternal.forEach(function (timeReportProject) {
                var totalProjectTime = 0;
                timeReportProject.timeReportDays.forEach(function (timeReportDay) {
                    if (timeReportDay.time != null) {
                        var time = parseFloat(timeReportDay.time);
                        if (!isNaN(time)) {
                            totalProjectTime += time;
                        }
                    }
                });

                rows.push(
                    <TimeReportTableRow timeReportProject={timeReportProject} totalProjectTime={totalProjectTime} />);
            });
        }

        return (
            <div>
                <button className="btn btn-primary" onClick={this.showDetails}>Details</button>
                {this.state.isOpen ? (
                    <Modal
                        isOpen={this.state.isOpen}
                        onRequestClose={this.closeDetails}
                    >
                        <div className="table-responsive">
                            <table className="table-sm">
                                <thead className="bg-success">
                                    {headerRow}
                                </thead>
                                <tbody>
                                    {rows}
                                </tbody>
                            </table>
                        </div>
                    </Modal>
                ) : ''}
            </div>
        )
    }
}

export default class UserProjectReportTable extends React.Component {
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
                rows.push(<UserReportRows key={userReport.idUser} userReport={userReport} workableHours={workableHours} reportView={reportView} fromDate={fromDate} />);
            });
        }

        return (
            <div className="table-responsive">
                <table className="table">
                    {rows}
                </table>
            </div>
        );
    }
};