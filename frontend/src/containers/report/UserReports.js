import React from "react";
import ReportService from '../../service/ReportService';
import * as Constants from '../../common/Constants';



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
        rows.push(<tr key={0} className="bg-light">
            <th className="fw-bold text-success">🏢 Account</th>
            <th className="fw-bold text-success">📋 Task</th>
            <th className="fw-bold text-success text-end">⏱️ Total Hours</th>
        </tr>);

        var i = 0;
        this.state.userReport.taskReports.forEach(function (taskReport) {
            var time = '';

            var key = 'row-' + i;
            rows.push(
                <tr key={key}>
                    <td className="fw-medium">{taskReport.accountName}</td>
                    <td className="fw-medium">{taskReport.taskName}</td>
                    <td className="text-end fw-bold text-success">{taskReport.totalHours} hrs</td>
                </tr>);
            i++;
        });

        var key = 'row-' + i;
        rows.push(<tr key={key} className="table-primary border-top border-2">
            <th></th>
            <th className="fw-bold fs-6">📊 Total Time</th>
            <th className="text-end fw-bold fs-6 text-success">{this.state.userReport.totalTime} hrs</th>
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
                rows.push(<UserReportRows key={i} userReport={userReport} />);
                i++;
            });
        }

        return (
            <div className="card shadow-sm">
                <div className="card-header bg-primary text-white">
                    <h5 className="mb-0 fw-bold">📋 Task Breakdown</h5>
                </div>
                <div className="card-body p-0">
                    <div className="table-responsive">
                        <table className="table table-hover table-striped mb-0">
                            {rows}
                        </table>
                    </div>
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
            <div className="container-fluid p-4">
                <div className="card shadow-sm mb-4">
                    <div className="card-header bg-success text-white">
                        <h2 className="mb-0 fw-bold">👤 My Time Report</h2>
                    </div>
                    <div className="card-body">
                        <div className="row mb-3 align-items-center">
                            <div className="col-sm-2">
                                <div className="btn-group" role="group" aria-label="Navigation">
                                    <button className="btn btn-outline-success" name={this.state.report.fromDate} onClick={this.handlePreviousReport} title="Previous Period">
                                        ← Previous
                                    </button>
                                    <button className="btn btn-outline-success" name={this.state.report.toDate} onClick={this.handleNextReport} title="Next Period">
                                        Next →
                                    </button>
                                </div>
                            </div>
                            <div className="col-sm-2">
                                <label className="form-label fw-bold text-muted small">Period Type</label>
                                <select className="form-select form-select-sm" value={this.state.reportView} name="reportView" onChange={this.viewChange}>
                                    <option value={Constants.MONTH_VIEW}>📅 Monthly</option>
                                    <option value={Constants.YEAR_VIEW}>📆 Yearly</option>
                                </select>
                            </div>
                            <div className="col-sm-4">
                                <span className="badge bg-secondary fs-6 py-2 px-3">
                                    📅 {this.state.report.fromDate} - {this.state.report.toDate}
                                </span>
                            </div>
                            <div className="col-sm-4 text-end">
                                <div className="alert alert-info py-2 px-3 mb-0 d-inline-block">
                                    <strong>🕰️ Workable Hours: {this.state.report.workableHours}</strong>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <UserReportTable report={this.state.report} />
            </div>
        );
    }
};