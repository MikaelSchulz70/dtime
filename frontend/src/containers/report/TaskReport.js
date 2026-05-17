import React, { useState } from "react";
import { useTranslation } from 'react-i18next';
import { 
    TaskDistributionPieChart,
    AccountHoursChart,
    ChartViewToggle 
} from '../../components/Charts';
import { useTableSort } from '../../hooks/useTableSort';
import SortableTableHeader from '../../components/SortableTableHeader';

function TaskReportTable({ report }) {
    const { t } = useTranslation();
    const [viewMode, setViewMode] = useState('table');
    const { sortedData: sortedTaskReports, requestSort, getSortIcon } = useTableSort(
        report?.taskReports, 
        'accountName'
    );
    
    if (report == null)
        return null;

    var rows = [];
    var totalSum = 0;

    (sortedTaskReports || []).forEach(function (taskReport) {
        const hours = parseFloat(taskReport.totalHours) || 0;
        totalSum += hours;

        rows.push(
            <tr key={taskReport.taskId}>
                <td className="fw-medium">{taskReport.accountName}</td>
                <td className="fw-medium">{taskReport.taskName}</td>
                <td className="col-num">{taskReport.totalHours}</td>
            </tr>);
    });

    // Add summary row
    rows.push(
        <tr key="summary" className="bg-success text-white border-top border-2">
            <td className="fw-bold fs-6">{t('reports.totalTime')}</td>
            <td></td>
            <td className="col-num fw-bold fs-6">{totalSum.toFixed(2)}</td>
        </tr>
    );

    // Transform data for chart components
    const userReports = [{
        fullName: t('reports.allUsers'),
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
                        <h5 className="mb-0 fw-bold text-white">{t('reports.taskTimeSummary')}</h5>
                        <ChartViewToggle viewMode={viewMode} onViewChange={setViewMode} />
                    </div>
                </div>
                <div className="card-body p-0">
                    {viewMode === 'table' ? (
                        <div className="table-responsive">
                            <table className="table table-hover mb-0">
                                <tbody>
                                    <tr>
                                        <SortableTableHeader 
                                            field="accountName" 
                                            onSort={requestSort} 
                                            getSortIcon={getSortIcon}
                                            className="fw-bold"
                                        >
                                            {t('reports.tableAccount')}
                                        </SortableTableHeader>
                                        <SortableTableHeader 
                                            field="taskName" 
                                            onSort={requestSort} 
                                            getSortIcon={getSortIcon}
                                            className="fw-bold"
                                        >
                                            {t('reports.tableTask')}
                                        </SortableTableHeader>
                                        <SortableTableHeader 
                                            field="totalHours" 
                                            onSort={requestSort} 
                                            getSortIcon={getSortIcon}
                                            className="fw-bold col-num"
                                        >
                                            {t('reports.tableTotalHours')}
                                        </SortableTableHeader>
                                    </tr>
                                    {rows}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <div className="p-3">
                            <div className="row">
                                <div className="col-lg-6 mb-4">
                                    <h6 className="mb-3 fw-bold text-success">{t('reports.taskDistribution')}</h6>
                                    <TaskDistributionPieChart userReports={userReports} />
                                </div>
                                <div className="col-lg-6 mb-4">
                                    <h6 className="mb-3 fw-bold text-info">{t('reports.hoursByAccount')}</h6>
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