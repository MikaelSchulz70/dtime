import React from "react";

export default class AccountReportTable extends React.Component {
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
        
        this.state.report.accountReports.forEach(function (accountReport) {
            const hours = parseFloat(accountReport.totalHours) || 0;
            totalSum += hours;
            
            rows.push(
                <tr key={accountReport.accountId}>
                    <td>{accountReport.accountName}</td>
                    <td>{accountReport.totalHours}</td>
                </tr>);
        });

        // Add summary row
        rows.push(
            <tr key="summary" className="bg-light font-weight-bold">
                <td><strong>Total Sum</strong></td>
                <td><strong>{totalSum.toFixed(2)}</strong></td>
            </tr>
        );

        return (
            <div className="table-responsive">
                <table className="table">
                    <thead>
                        <tr className="bg-success text-white">
                            <th>Account</th>
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