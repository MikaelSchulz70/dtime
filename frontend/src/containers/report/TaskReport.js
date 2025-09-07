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
                    <td className="fw-medium">{taskReport.accountName}</td>
                    <td className="fw-medium">{taskReport.taskName}</td>
                    <td className="text-end">{taskReport.totalHours} hrs</td>
                </tr>);
        });

        // Add summary row
        rows.push(
            <tr key="summary" className="table-primary border-top border-2">
                <td className="fw-bold fs-6">📊 Total Time</td>
                <td></td>
                <td className="text-end fw-bold fs-6">{totalSum.toFixed(2)} hrs</td>
            </tr>
        );

        return (
            <div className="col-12">
                <div className="card shadow-sm">
                    <div className="card-header bg-success text-white">
                        <h5 className="mb-0 fw-bold text-white">📋 Task Time Summary</h5>
                    </div>
                    <div className="card-body p-0">
                        <div className="table-responsive">
                            <table className="table table-hover table-striped mb-0">
                                <tbody>
                                    <tr>
                                        <th className="fw-bold">Account</th>
                                        <th className="fw-bold">Task</th>
                                        <th className="fw-bold text-end">Total Hours</th>
                                    </tr>
                                    {rows}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}