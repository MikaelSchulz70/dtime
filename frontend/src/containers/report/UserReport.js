import React from "react";

function UserReportTable({ report }) {
    if (report == null)
        return null;

    var rows = [];
    var totalSum = 0;

    report.userReports.forEach(function (userReport) {
        const hours = parseFloat(userReport.totalTime) || 0;
        totalSum += hours;

        rows.push(
            <tr key={userReport.userId}>
                <td className="fw-medium">{userReport.fullName}</td>
                <td className="text-end fw-bold">{userReport.totalTime} hrs</td>
            </tr>);
    });

    // Add summary row
    rows.push(
        <tr key="summary" className="table-primary border-top border-2">
            <td className="fw-bold fs-6">📊 Total Time</td>
            <td className="text-end fw-bold fs-6">{totalSum.toFixed(2)} hrs</td>
        </tr>
    );

    return (
        <div className="col-12">
            <div className="card shadow-sm">
                <div className="card-header bg-success text-white">
                    <h5 className="mb-0 fw-bold text-white">👤 User Time Summary</h5>
                </div>
                <div className="card-body p-0">
                    <div className="table-responsive">
                        <table className="table table-hover table-striped mb-0">
                            <tbody>
                                <tr>
                                    <th className="fw-bold">User Name</th>
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

export default UserReportTable;