import React from "react";

export default class UserReportTable extends React.Component {
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

        this.state.report.userReports.forEach(function (userReport) {
            const hours = parseFloat(userReport.totalTime) || 0;
            totalSum += hours;

            rows.push(
                <tr key={userReport.userId}>
                    <td>{userReport.fullName}</td>
                    <td>{userReport.totalTime}</td>
                </tr>);
        });

        // Add summary row
        rows.push(
            <tr key="summary" className="bg-light font-weight-bold">
                <td><strong>Total time</strong></td>
                <td><strong>{totalSum.toFixed(2)}</strong></td>
            </tr>
        );

        return (
            <div className="table-responsive">
                <table className="table">
                    <thead>
                        <tr className="bg-success text-white">
                            <th>Name</th>
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