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
        this.state.report.userReports.forEach(function (userReport) {
            rows.push(
                <tr>
                    <td>{userReport.userName}</td>
                    <td>{userReport.totalTime}</td>
                    <td>{userReport.totalTimeExternalProvision}</td>
                    <td>{userReport.totalTimeInternalProvision}</td>
                    <td>{userReport.totalTimeExternalNoProvision}</td>
                    <td>{userReport.totalTimeInternalNoProvision}</td>
                </tr>);
        });

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