import React, { useState } from "react";
import { 
    TaskDistributionPieChart,
    AccountHoursChart,
    ChartViewToggle 
} from '../../components/Charts';

function TaskReportTable({ report }) {
    const [viewMode, setViewMode] = useState('table');
    
    if (report == null)
        return null;

    var rows = [];
    var totalSum = 0;

    report.taskReports.forEach(function (taskReport) {
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
        <tr key="summary" className="bg-success text-white border-top border-2">
            <td className="fw-bold fs-6">📊 Total Time</td>
            <td></td>
            <td className="text-end fw-bold fs-6">{totalSum.toFixed(2)} hrs</td>
        </tr>
    );

    // Transform data for chart components
    const userReports = [{
        fullName: "All Users",
        totalTime: totalSum,
        taskReports: report.taskReports.map(taskReport => ({
            accountName: taskReport.accountName,
            taskName: taskReport.taskName,
            totalHours: parseFloat(taskReport.totalHours) || 0
        }))
    }];

    return (
        <div className="col-12">
            <div className="card shadow-sm">
                <div className="card-header bg-success text-white">
                    <div className="d-flex justify-content-between align-items-center">
                        <h5 className="mb-0 fw-bold text-white">📋 Task Time Summary</h5>
                        <ChartViewToggle viewMode={viewMode} onViewChange={setViewMode} />
                    </div>
                </div>
                <div className="card-body p-0">
                    {viewMode === 'table' ? (
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
                    ) : (
                        <div className="p-3">
                            <div className="row">
                                <div className="col-lg-6 mb-4">
                                    <h6 className="mb-3 fw-bold text-success">📋 Task Distribution</h6>
                                    <TaskDistributionPieChart userReports={userReports} />
                                </div>
                                <div className="col-lg-6 mb-4">
                                    <h6 className="mb-3 fw-bold text-info">🏢 Hours by Account</h6>
                                    <AccountHoursChart userReports={userReports} />
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default TaskReportTable;