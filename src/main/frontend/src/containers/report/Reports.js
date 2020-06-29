import React from "react";
import ReportService from '../../service/ReportService';
import * as Constants from '../../common/Constants';
import UserProjectReportTable from './UserProjectReport'
import ProjectReportTable from './ProjectReport'
import ProjectCategoryReportTable from './ProjectCategoryReport'
import UserReportTable from './UserReport'
import ProjectUserReportTable from './ProjectUserReport'

export default class Reports extends React.Component {
    constructor(props) {
        super(props);
        this.handlePreviousReport = this.handlePreviousReport.bind(this);
        this.handleNextReport = this.handleNextReport.bind(this);
        this.viewChange = this.viewChange.bind(this);
        this.typeChange = this.typeChange.bind(this);
        this.state = { reportView: Constants.MONTH_VIEW, reportType: Constants.USER_PROJECT_REPORT };
    }

    componentDidMount() {
        this.loadFromServer(Constants.MONTH_VIEW, Constants.USER_PROJECT_REPORT);
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
            <div className="container-fluid ml-4">
                <div className="row mb-3">
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.report.fromDate} onClick={this.handlePreviousReport}>&lt;&lt;</button>
                    </div>
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.report.toDate} onClick={this.handleNextReport}>&gt;&gt;</button>
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" value={this.state.reportView} name="reportView" onChange={this.viewChange}>
                            <option value="MONTH">Month</option>
                            <option value="YEAR">Year</option>
                        </select>
                    </div>
                    <div className="col-sm-3">
                        <select className="form-control input-sm" value={this.state.reportType} name="reportType" onChange={this.typeChange}>
                            <option value={Constants.USER_PROJECT_REPORT}>User project report</option>
                            <option value={Constants.PROJECT_REPORT}>Project report</option>
                            <option value={Constants.USER_REPORT}>User report</option>
                            <option value={Constants.PROJECT_CATEGORY_REPORT}>Project category report</option>
                            <option value={Constants.PROJECT_USER_REPORT}>Internal project user report</option>
                        </select>
                    </div>
                    <div className="col-sm-5">
                        <span className=" float-right">
                            <b>{this.state.report.fromDate} - {this.state.report.toDate}</b>
                        </span>
                    </div>

                </div>
                <div className="row">
                    {this.state.reportType === Constants.USER_PROJECT_REPORT ? (
                        <UserProjectReportTable report={this.state.report} reportView={this.state.reportView} fromDate={this.state.report.fromDate} />
                    ) : this.state.reportType === Constants.PROJECT_REPORT ? (
                        <ProjectReportTable report={this.state.report} />
                    ) : this.state.reportType === Constants.PROJECT_CATEGORY_REPORT ? (
                        <ProjectCategoryReportTable report={this.state.report} />
                    ) : this.state.reportType === Constants.USER_REPORT ? (
                        <UserReportTable report={this.state.report} />
                    ) : this.state.reportType === Constants.PROJECT_USER_REPORT ? (
                        <ProjectUserReportTable report={this.state.report} />
                    ) : ''}
                </div>
            </div>
        );
    }
};
