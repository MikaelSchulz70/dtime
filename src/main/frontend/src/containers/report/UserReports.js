import React from "react";
import ReportService from '../../service/ReportService';
import * as Constants from '../../common/Constants';

class SubContractorReportRows extends React.Component {
    constructor(props) {
        super(props);
        this.state = { userReport: this.props.userReport };
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
        rows.push(<tr key={0} className="bg-success text-white">
            <th>Company</th>
            <th>Project</th>
            <th>On call hours</th>
            <th>Hours</th>
        </tr>);

        var i = 0;
        var totalTime = 0;
        var totalOnCall = 0;
        this.state.userReport.projectReports.forEach(function (projectReport) {
            var onCallTime = '';
            var time = '';
            var key = 'row-' + i;
            if (!projectReport.onCall) {
                totalTime += projectReport.totalHours;
                time = projectReport.totalHours;
            } else {
                onCallTime = projectReport.totalHours;
                totalOnCall += projectReport.totalHours;
            }

            rows.push(
                <tr key={key}>
                    <td>{projectReport.companyName}</td>
                    <td>{projectReport.projectName}</td>
                    <td>{onCallTime}</td>
                    <td>{time}</td>
                </tr>);
            i++;
        });

        var key = 'row-' + i;
        rows.push(<tr key={key}>
            <th></th>
            <th></th>
            <th>{totalOnCall}</th>
            <th>{totalTime}</th>
        </tr>);

        return (
            <tbody>
                {rows}
            </tbody>
        );
    };
};


class UserReportRows extends React.Component {
    constructor(props) {
        super(props);
        this.state = { userReport: this.props.userReport };
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
        rows.push(<tr key={0} className="bg-success text-white">
            <th>Company</th>
            <th>Project</th>
            <th>Time provision</th>
            <th>Time no provision</th>
            <th>On call hours</th>
            <th>Total hours</th>
            <th>Provision hours</th>
        </tr>);

        var i = 0;
        var totalOnCallHours = 0;
        this.state.userReport.projectReports.forEach(function (projectReport) {
            var time = '';
            var timeProvision = '';
            var timeNoProvision = '';
            var onCallHours = '';
            if (projectReport.onCall) {
                onCallHours = projectReport.totalHours;
                totalOnCallHours += projectReport.totalHours;
            } else if (projectReport.provision === true) {
                timeProvision = projectReport.totalHours;
                time = timeProvision;
            } else {
                timeNoProvision = projectReport.totalHours;
                time = timeNoProvision;
            }

            var key = 'row-' + i;
            rows.push(
                <tr key={key}>
                    <td>{projectReport.companyName}</td>
                    <td>{projectReport.projectName}</td>
                    <td>{timeProvision}</td>
                    <td>{timeNoProvision}</td>
                    <td>{onCallHours}</td>
                    <td>{time}</td>
                    <td></td>
                </tr>);
            i++;
        });

        var totalTime = this.state.userReport.totalTimeProvision + this.state.userReport.totalTimeNoProvision;
        var key = 'row-' + i;
        rows.push(<tr key={key}>
            <th></th>
            <th>Total time</th>
            <th>{this.state.userReport.totalTimeProvision}</th>
            <th>{this.state.userReport.totalTimeNoProvision}</th>
            <th>{totalOnCallHours}</th>
            <th>{totalTime}</th>
            <th>{this.state.userReport.provisionHours}</th>
        </tr>);

        return (
            <tbody>
                {rows}
            </tbody>
        );
    };
};

class UserReportTable extends React.Component {
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
        var i = 0;
        if (this.state.report.userReports != null) {
            this.state.report.userReports.forEach(function (userReport) {
                if (userReport.userCategory === Constants.EMPLOYEE_CATEGORY) {
                    rows.push(<UserReportRows key={i} userReport={userReport} />);
                } else {
                    rows.push(<SubContractorReportRows key={i} userReport={userReport} />);
                }
                i++;
            });
        }

        return (
            <div className="row">
                <div className="table-responsive">
                    <table className="table">
                        {rows}
                    </table>
                </div>
            </div>
        );
    }
};

export default class UserReports extends React.Component {
    constructor(props) {
        super(props);
        this.handlePreviousReport = this.handlePreviousReport.bind(this);
        this.handleNextReport = this.handleNextReport.bind(this);
        this.viewChange = this.viewChange.bind(this);
        this.state = { reportView: Constants.MONTH_VIEW };
    }

    componentDidMount() {
        this.loadFromServer(Constants.MONTH_VIEW);
    }

    viewChange(event) {
        this.loadFromServer(event.target.value);
    }

    loadFromServer(view) {
        const self = this;
        var service = new ReportService();
        service.getCurrentUserReport(view)
            .then(response => {
                self.setState({ report: response.data, reportView: view });
            })
            .catch(error => {
                alert('Failed to load report');
            });
    }

    handlePreviousReport(event) {
        const date = event.target.name;

        const self = this;
        var service = new ReportService();
        service.getPreviousUserReport(this.state.reportView, date)
            .then(response => {
                self.setState({ report: response.data });
            })
            .catch(error => {
                alert('Failed to load report');
            });
    }

    handleNextReport(event) {
        const date = event.target.name;
        const self = this;
        var service = new ReportService();
        service.getNextUserReport(this.state.reportView, date)
            .then(response => {
                self.setState({ report: response.data });
            })
            .catch(error => {
                alert('Failed to load report');
            });
    }

    render() {
        if (this.state == null || this.state.report == null)
            return null;

        return (
            <div>
                <div className="row mb-3">
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.report.fromDate} onClick={this.handlePreviousReport}>&lt;&lt;</button>
                    </div>
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.report.toDate} onClick={this.handleNextReport}>&gt;&gt;</button>
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" value={this.state.reportView} name="reportView" onChange={this.viewChange}>
                            <option value={Constants.MONTH_VIEW}>Month</option>
                            <option value={Constants.YEAR_VIEW}>Year</option>
                        </select>
                    </div>
                    <div className="col-sm-3">
                        {this.state.report.fromDate} - {this.state.report.toDate}
                    </div>
                    <div className="col-sm-5">
                        <span className=" float-right">
                            <b>Workable hours: {this.state.report.workableHours} </b>
                        </span>
                    </div>
                </div>
                <UserReportTable report={this.state.report} />
            </div>
        );
    }
};