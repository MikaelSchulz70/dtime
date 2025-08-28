import React from "react";
import ReportService from '../../service/ReportService';
import * as Constants from '../../common/Constants';
import TaskReportTable from './TaskReport';
import AccountReportTable from './AccountReport';
import UserReportTable from './UserReport';
import UserTaskReportTable from './UserTaskReport';

export default class AdminReports extends React.Component {
    constructor(props) {
        super(props);
        this.handlePreviousReport = this.handlePreviousReport.bind(this);
        this.handleNextReport = this.handleNextReport.bind(this);
        this.viewChange = this.viewChange.bind(this);
        this.typeChange = this.typeChange.bind(this);
        this.state = { reportView: Constants.MONTH_VIEW, reportType: Constants.USER_TASK_REPORT };
    }

    componentDidMount() {
        this.loadFromServer(Constants.MONTH_VIEW, Constants.USER_TASK_REPORT);
    }

    viewChange(event) {
        this.loadFromServer(event.target.value, this.state.reportType);
    }

    typeChange(event) {
        this.loadFromServer(this.state.reportView, event.target.value);
    }

    loadFromServer(view, type) {
        const self = this;
        var service = new ReportService();
        service.getCurrentReport(view, type)
            .then(response => {
                self.setState({ report: response.data, reportView: view, reportType: type });
            })
            .catch(error => {
                alert('Failed to load report');
            });
    }

    handlePreviousReport(event) {
        const date = event.target.name;

        const self = this;
        var service = new ReportService();
        service.getPreviousReport(this.state.reportView, this.state.reportType, date)
            .then(response => {
                self.setState({ report: response.data });
            })
            .catch(error => {
                alert('Failed to load previous report');
            });
    }

    handleNextReport(event) {
        const date = event.target.name;

        const self = this;
        var service = new ReportService();
        service.getNextReport(this.state.reportView, this.state.reportType, date)
            .then(response => {
                self.setState({ report: response.data });
            })
            .catch(error => {
                alert('Failed to load next report');
            });
    }

    render() {
        if (this.state == null || this.state.report == null)
            return null;

        return (
            <div className="container-fluid p-4">
                <div className="card shadow-sm mb-4">
                    <div className="card-header bg-success text-white">
                        <h2 className="mb-0 fw-bold">📊 Administrative Reports</h2>
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
                                    <option value="MONTH">📅 Monthly</option>
                                    <option value="YEAR">📆 Yearly</option>
                                </select>
                            </div>
                            <div className="col-sm-3">
                                <label className="form-label fw-bold text-muted small">Report Type</label>
                                <select className="form-select form-select-sm" value={this.state.reportType} name="reportType" onChange={this.typeChange}>
                                    <option value={Constants.USER_TASK_REPORT}>👥 User Task Report</option>
                                    <option value={Constants.ACCOUNT_REPORT}>🏢 Account Report</option>
                                    <option value={Constants.TASK_REPORT}>📋 Task Report</option>
                                    <option value={Constants.USER_REPORT}>👤 User Report</option>
                                </select>
                            </div>
                            <div className="col-sm-5">
                                <div className="text-end">
                                    <span className="badge bg-secondary fs-6 py-2 px-3">
                                        📅 {this.state.report.fromDate} - {this.state.report.toDate}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="row">
                    {this.state.reportType === Constants.USER_TASK_REPORT ? (
                        <UserTaskReportTable report={this.state.report} reportView={this.state.reportView} fromDate={this.state.report.fromDate} />
                    ) : this.state.reportType === Constants.TASK_REPORT ? (
                        <TaskReportTable report={this.state.report} />
                    ) : this.state.reportType === Constants.USER_REPORT ? (
                        <UserReportTable report={this.state.report} />
                    ) : this.state.reportType === Constants.ACCOUNT_REPORT ? (
                        <AccountReportTable report={this.state.report} />
                    ) : ''}
                </div>
            </div>
        );
    }
};
