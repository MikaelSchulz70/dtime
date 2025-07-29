import React from "react";
import FollowUpService from '../../service/FollowUpService';
import * as Constants from '../../common/Constants';
import { formatAmount } from '../../common/utils';

class FollowUpUserReportTable extends React.Component {
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
        if (this.state == null || this.state.report == null)
            return null;

        var rows = [];
        rows.push(
            <tr key={0} className="bg-success text-white">
                <th>User</th>
                <th>Hours</th>
                <th>Total amount</th>
                <th>Amount subcontractor</th>
                <th>Amount</th>
                <th>Comment</th>
            </tr>);

        var key = 1;
        this.state.report.followUpUserReports.forEach(function (followUpUserReport) {
            rows.push(
                <tr key={key}>
                    <td>{followUpUserReport.userName}</td>
                    <td>{followUpUserReport.totalHours}</td>
                    <td>{followUpUserReport.totalAmount}</td>
                    <td>{followUpUserReport.amountSubcontractor}</td>
                    <td>{followUpUserReport.amount}</td>
                    <td>{followUpUserReport.comment}</td>
                </tr>);
            key++;
        });

        const totalAmount = formatAmount(this.state.report.totalAmount);
        const subContractorAmount = formatAmount(this.state.report.amountSubcontractor);
        const amount = formatAmount(this.state.report.amount);

        rows.push(
            <tr key={key} className="bg-light">
                <th></th>
                <th>{this.state.report.totalHours}</th>
                <th>{totalAmount}</th>
                <th>{subContractorAmount}</th>
                <th>{amount}</th>
                <th></th>
            </tr>);

        return (
            <div className="table-responsive" >
                <table className="table">
                    <tbody>
                        {rows}
                    </tbody>
                </table>
            </div>
        );
    }
};

class FollowUpCategoryReportTable extends React.Component {
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
        if (this.state == null || this.state.report == null)
            return null;

        var rows = [];
        var key = 1;
        this.state.report.followUpCategoryReports.forEach(function (followUpCategoryReport) {
            rows.push(
                <tr key={key} className="bg-success text-white">
                    <th>{followUpCategoryReport.taskCategory}</th>
                    <th>User</th>
                    <th>Hours</th>
                    <th>Total amount</th>
                    <th>Subcontractor amount</th>
                    <th>Amount</th>
                    <th>Comment</th>
                </tr>);

            key++;
            followUpCategoryReport.followUpUserReports.forEach(function (followUpUserReport) {
                rows.push(
                    <tr key={key}>
                        <td></td>
                        <td>{followUpUserReport.userName}</td>
                        <td>{followUpUserReport.totalHours}</td>
                        <td>{followUpUserReport.totalAmount}</td>
                        <td>{followUpUserReport.amountSubcontractor}</td>
                        <td>{followUpUserReport.amount}</td>
                        <td>{followUpUserReport.comment}</td>
                    </tr>);
                key++;
            });

            const totalAmount = formatAmount(followUpCategoryReport.totalAmount);
            const subContractorAmount = formatAmount(followUpCategoryReport.amountSubcontractor);
            const amount = formatAmount(followUpCategoryReport.amount);

            rows.push(
                <tr key={key} className="bg-light">
                    <th></th>
                    <th></th>
                    <th>{followUpCategoryReport.totalHours}</th>
                    <th>{totalAmount}</th>
                    <th>{subContractorAmount}</th>
                    <th>{amount}</th>
                    <th></th>
                </tr>);
            key++;
        });

        return (
            <div className="table-responsive" >
                <table className="table">
                    <tbody>
                        {rows}
                    </tbody>
                </table>
            </div>
        );
    }
};

class FollowUpAccountReportTable extends React.Component {
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
        if (this.state == null || this.state.report == null)
            return null;

        var rows = [];
        rows.push(
            <tr key={0} className="bg-success text-white">
                <th>Account</th>
                <th>Hours</th>
                <th>Comment</th>
            </tr>);

        var key = 1;
        this.state.report.followUpAccountReports.forEach(function (followUpAccountReport) {
            rows.push(
                <tr key={key}>
                    <td>{followUpAccountReport.accountName}</td>
                    <td>{followUpAccountReport.totalHours}</td>
                    <td>{followUpAccountReport.totalAmount}</td>
                    <td>{followUpAccountReport.amountSubcontractor}</td>
                    <td>{followUpAccountReport.amount}</td>
                    <td>{followUpAccountReport.comment}</td>
                </tr>);
            key++;
        });

        const totalAmount = formatAmount(this.state.report.totalAmount);
        const subContractorAmount = formatAmount(this.state.report.amountSubcontractor);
        const amount = formatAmount(this.state.report.amount);

        rows.push(
            <tr key={key} className="bg-light">
                <th></th>
                <th>{this.state.report.totalHours}</th>
                <th>{totalAmount}</th>
                <th>{subContractorAmount}</th>
                <th>{amount}</th>
                <th></th>
            </tr>);

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
};

export default class FollowUp extends React.Component {
    constructor(props) {
        super(props);
        this.handlePreviousReport = this.handlePreviousReport.bind(this);
        this.handleNextReport = this.handleNextReport.bind(this);
        this.viewChange = this.viewChange.bind(this);
        this.typeChange = this.typeChange.bind(this);
        this.state = { reportView: Constants.MONTH_VIEW, reportType: Constants.FOLLOW_UP_CATEGORY };
    }

    componentDidMount() {
        this.loadFromServer(Constants.MONTH_VIEW, Constants.FOLLOW_UP_CATEGORY);
    }

    viewChange(event) {
        this.loadFromServer(event.target.value, this.state.reportType);
    }

    typeChange(event) {
        this.loadFromServer(this.state.reportView, event.target.value);
    }

    loadFromServer(view, type) {
        const self = this;
        var service = new FollowUpService();
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
        var service = new FollowUpService();
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
        var service = new FollowUpService();
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
                            <option value={Constants.FOLLOW_UP_CATEGORY}>task category</option>
                            <option value={Constants.FOLLOW_UP_USER}>User</option>
                            <option value={Constants.FOLLOW_UP_ACCOUNT}>account</option>
                        </select>
                    </div>
                    <div className="col-sm-5">
                        <span className=" float-right">
                            <b>{this.state.report.fromDate} - {this.state.report.toDate}</b>
                        </span>
                    </div>

                </div>
                <div className="row">
                    {this.state.reportType === Constants.FOLLOW_UP_CATEGORY ? (
                        <FollowUpCategoryReportTable report={this.state.report} reportView={this.state.reportView} fromDate={this.state.report.fromDate} />
                    ) : this.state.reportType === Constants.FOLLOW_UP_USER ? (
                        <FollowUpUserReportTable report={this.state.report} reportView={this.state.reportView} fromDate={this.state.report.fromDate} />
                    ) : this.state.reportType === Constants.FOLLOW_UP_ACCOUNT ? (
                        <FollowUpAccountReportTable report={this.state.report} reportView={this.state.reportView} fromDate={this.state.report.fromDate} />
                    ) : ''}
                </div>
            </div>
        );
    }
};