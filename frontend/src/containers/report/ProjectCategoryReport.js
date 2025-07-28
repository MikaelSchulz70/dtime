import React from "react";

export default class TaskCategoryReportTable extends React.Component {
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
        this.state.report.taskCategoryReports.forEach(function (taskCategoryReport) {
            rows.push(
                <tr className="bg-success text-white">
                    <th>{taskCategoryReport.taskCategory}</th>
                    <th>Total</th>
                </tr>);

            taskCategoryReport.taskSubCategoryReports.forEach(function (taskSubCategoryReport) {
                rows.push(
                    <tr>
                        <td></td>
                        <td>{taskSubCategoryReport.totalHours}</td>
                    </tr>);
            });

            rows.push(
                <tr>
                    <th></th>
                    <th>{taskCategoryReport.totalHours}</th>
                </tr>);
        });

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