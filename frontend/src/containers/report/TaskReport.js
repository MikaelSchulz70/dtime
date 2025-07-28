import React from "react";

export default class TaskReportTable extends React.Component {
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
        this.state.report.taskReports.forEach(function (taskReport) {
            rows.push(
                <tr>
                    <td>{taskReport.accountName}</td>
                    <td>{taskReport.taskName}</td>
                    <td>{taskReport.totalHours}</td>
                </tr>);
        });

        return (
            <div className="table-responsive">
                <table className="table">
                    <thead>
                        <tr className="bg-success text-white">
                            <th>Account</th>
                            <th>Task</th>
                            <th>Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        {rows}
                    </tbody>
                </table>
            </div>
        );
    }
};