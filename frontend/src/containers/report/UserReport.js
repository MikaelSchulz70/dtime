import React, { useState } from "react";
import { 
    UserTaskBarChart,
    ChartViewToggle 
} from '../../components/Charts';
import { useTranslation } from 'react-i18next';
import { useTableSort } from '../../hooks/useTableSort';
import SortableTableHeader from '../../components/SortableTableHeader';

function UserReportTable({ report }) {
    const [viewMode, setViewMode] = useState('table');
    const { t } = useTranslation();
    const { sortedData: sortedUserReports, requestSort, getSortIcon } = useTableSort(
        report?.userReports, 
        'fullName'
    );
    
    if (report == null)
        return null;

    var rows = [];
    var totalSum = 0;

    sortedUserReports.forEach(function (userReport) {
        const hours = parseFloat(userReport.totalTime) || 0;
        totalSum += hours;

        rows.push(
            <tr key={userReport.userId}>
                <td className="fw-medium">{userReport.fullName}</td>
                <td className="text-start fw-bold">{userReport.totalTime}</td>
            </tr>);
    });

    // Add summary row
    rows.push(
        <tr key="summary" className="bg-success text-white border-top border-2">
            <td className="fw-bold fs-6">📊 {t('common.labels.totalTime')}</td>
            <td className="text-start fw-bold fs-6">{totalSum.toFixed(2)}</td>
        </tr>
    );

    return (
        <div className="col-12">
            <div className="card shadow-sm">
                <div className="card-header bg-success text-white">
                    <div className="d-flex justify-content-between align-items-center">
                        <h5 className="mb-0 fw-bold text-white">👤 {t('reports.userTimeSummary')}</h5>
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
                                            field="fullName" 
                                            onSort={requestSort} 
                                            getSortIcon={getSortIcon}
                                            className="fw-bold"
                                        >
                                            {t('common.labels.userName')}
                                        </SortableTableHeader>
                                        <SortableTableHeader 
                                            field="totalTime" 
                                            onSort={requestSort} 
                                            getSortIcon={getSortIcon}
                                            className="fw-bold text-start"
                                        >
                                            {t('common.labels.totalHours')}
                                        </SortableTableHeader>
                                    </tr>
                                    {rows}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <div className="p-3">
                            <div className="row">
                                <div className="col-12">
                                    <h6 className="mb-3 fw-bold text-primary">👤 {t('reports.userHoursOverview')}</h6>
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