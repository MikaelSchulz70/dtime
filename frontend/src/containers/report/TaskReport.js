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
        var totalSum = 0;

        this.state.report.taskReports.forEach(function (taskReport) {
            const hours = parseFloat(taskReport.totalHours) || 0;
            totalSum += hours;

            rows.push(
                <tr key={taskReport.taskId}>
                    <td>{taskReport.accountName}</td>
                    <td>{taskReport.taskName}</td>
                    <td>{taskReport.totalHours}</td>
                </tr>);
        });

        // Add summary row
        rows.push(
            <tr key="summary" className="bg-light font-weight-bold">
                <td><strong>Total time</strong></td>
                <td></td>
                <td><strong>{totalSum.toFixed(2)}</strong></td>
            </tr>
        );

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