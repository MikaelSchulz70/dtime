import React, { useState } from "react";
import { 
    UserTaskBarChart,
    ChartViewToggle 
} from '../../components/Charts';

function UserReportTable({ report }) {
    const [viewMode, setViewMode] = useState('table');
    
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
        <tr key="summary" className="bg-success text-white border-top border-2">
            <td className="fw-bold fs-6">📊 Total Time</td>
            <td className="text-end fw-bold fs-6">{totalSum.toFixed(2)} hrs</td>
        </tr>
    );

    return (
        <div className="col-12">
            <div className="card shadow-sm">
                <div className="card-header bg-success text-white">
                    <div className="d-flex justify-content-between align-items-center">
                        <h5 className="mb-0 fw-bold text-white">👤 User Time Summary</h5>
                        <ChartViewToggle viewMode={viewMode} onViewChange={setViewMode} />
                    </div>
                </div>
                <div className="card-body p-0">
                    {viewMode === 'table' ? (
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
                    ) : (
                        <div className="p-3">
                            <div className="row">
                                <div className="col-12">
                                    <h6 className="mb-3 fw-bold text-primary">👤 User Hours Overview</h6>
                                    <UserTaskBarChart userReports={report.userReports} />
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default UserReportTable;