import React, { useState } from "react";
import { useTranslation } from 'react-i18next';
import { 
    AccountHoursChart,
    ChartViewToggle 
} from '../../components/Charts';
import { useTableSort } from '../../hooks/useTableSort';
import SortableTableHeader from '../../components/SortableTableHeader';

function AccountReportTable({ report }) {
    const { t } = useTranslation();
    const [viewMode, setViewMode] = useState('table');
    const { sortedData: sortedAccountReports, requestSort, getSortIcon } = useTableSort(
        report?.accountReports, 
        'accountName'
    );
    
    if (report == null)
        return null;

    var rows = [];
    var totalSum = 0;

    (sortedAccountReports || []).forEach(function (accountReport) {
        const hours = parseFloat(accountReport.totalHours) || 0;
        totalSum += hours;

        rows.push(
            <tr key={accountReport.accountId}>
                <td className="fw-medium">{accountReport.accountName}</td>
                <td className="col-num">{accountReport.totalHours}</td>
            </tr>);
    });

    // Add summary row
    rows.push(
        <tr key="summary" className="bg-success text-white border-top border-2">
            <td className="fw-bold fs-6">{t('reports.totalTime')}</td>
            <td className="col-num fw-bold fs-6">{totalSum.toFixed(2)}</td>
        </tr>
    );

    // Transform data for chart components
    const userReports = [{
        fullName: t('reports.allUsers'),
        totalTime: totalSum,
        taskReports: report.accountReports.map(accountReport => ({
            accountName: accountReport.accountName,
            taskName: t('reports.allTasks'),
            totalHours: parseFloat(accountReport.totalHours) || 0
        }))
    }];

    return (
        <div className="col-12">
            <div className="card shadow-sm">
                <div className="card-header bg-success text-white">
                    <div className="d-flex justify-content-between align-items-center">
                        <h5 className="mb-0 fw-bold text-white">{t('reports.accountTimeSummary')}</h5>
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
                                            {t('reports.accountNameColumn')}
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
                                <div className="col-12">
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

export default AccountReportTable;