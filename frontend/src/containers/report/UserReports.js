import React, { useState, useEffect, useCallback } from "react";
import ReportService from '../../service/ReportService';
import * as Constants from '../../common/Constants';
import { useToast } from '../../components/Toast';
import { useTranslation } from 'react-i18next';
import { 
    TaskDistributionPieChart, 
    AccountHoursChart,
    ChartViewToggle 
} from '../../components/Charts';

function UserReportRows({ userReport, t }) {
    if (userReport == null)
        return null;

    var rows = [];
    var i = 0;
    userReport.taskReports.forEach(function (taskReport) {
        var key = 'row-' + i;
        rows.push(
            <tr key={key}>
                <td className="fw-medium">{taskReport.accountName}</td>
                <td className="fw-medium text-start">{taskReport.taskName}</td>
                <td className="col-num">{taskReport.totalHours}</td>
            </tr>);
        i++;
    });

    var key = 'row-' + i;
    rows.push(<tr key={key} className="bg-success text-white border-top border-2">
        <td className="fw-bold fs-6" colSpan="2">{t('reports.totalTime')}</td>
        <td className="col-num fw-bold fs-6">{userReport.totalTime}</td>
    </tr>);

    return (
        <tbody>
            {rows}
        </tbody>
    );
}

function UserReportTable({ report, viewMode, setViewMode }) {
    const { t } = useTranslation();
    if (report == null)
        return null;

    var rows = [];
    var i = 0;
    if (report.userReports != null) {
        report.userReports.forEach(function (userReport) {
            rows.push(<UserReportRows key={i} userReport={userReport} t={t} />);
            i++;
        });
    }

    return (
        <div className="col-12">
            <div className="card shadow-sm">
                <div className="card-header bg-success">
                    <div className="d-flex justify-content-between align-items-center">
                        <h5 className="mb-0 fw-bold text-white">{t('reports.taskTimeBreakdown')}</h5>
                        <ChartViewToggle viewMode={viewMode} onViewChange={setViewMode} />
                    </div>
                </div>
                <div className="card-body p-0">
                    {viewMode === 'table' ? (
                        <div className="table-responsive">
                            <table className="table table-hover mb-0">
                                <tr>
                                    <th className="fw-bold">{t('reports.tableAccount')}</th>
                                    <th className="fw-bold text-start">{t('reports.tableTask')}</th>
                                    <th className="fw-bold col-num">{t('reports.tableTotalHours')}</th>
                                </tr>
                                {rows}
                            </table>
                        </div>
                    ) : (
                        <div className="p-3">
                            <div className="row">
                                <div className="col-lg-6 mb-4">
                                    <h6 className="mb-3 fw-bold text-success">{t('reports.taskDistribution')}</h6>
                                    <TaskDistributionPieChart userReports={report.userReports} />
                                </div>
                                <div className="col-lg-6 mb-4">
                                    <h6 className="mb-3 fw-bold text-info">{t('reports.hoursByAccount')}</h6>
                                    <AccountHoursChart userReports={report.userReports} />
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

function UserReports(props) {
    const [report, setReport] = useState(null);
    const [reportView, setReportView] = useState(Constants.MONTH_VIEW);
    const [viewMode, setViewMode] = useState('table');
    const { showError } = useToast();
    const { t } = useTranslation();

    useEffect(() => {
        loadFromServer(Constants.MONTH_VIEW);
    }, []);

    const viewChange = useCallback((event) => {
        loadFromServer(event.target.value);
    }, []);

    const loadFromServer = useCallback((view) => {
        var service = new ReportService();
        service.getCurrentUserReport(view)
            .then(response => {
                setReport(response.data);
                setReportView(view);
            })
            .catch(error => {
                showError(t('reports.messages.loadFailed', { message: error.response?.data?.message || error.message }));
            });
    }, [showError, t]);

    const handlePreviousReport = useCallback((event) => {
        const date = event.target.name;

        var service = new ReportService();
        service.getPreviousUserReport(reportView, date)
            .then(response => {
                setReport(response.data);
            })
            .catch(error => {
                showError(t('reports.messages.loadFailed', { message: error.response?.data?.message || error.message }));
            });
    }, [reportView, showError, t]);

    const handleNextReport = useCallback((event) => {
        const date = event.target.name;
        var service = new ReportService();
        service.getNextUserReport(reportView, date)
            .then(response => {
                setReport(response.data);
            })
            .catch(error => {
                showError(t('reports.messages.loadFailed', { message: error.response?.data?.message || error.message }));
            });
    }, [reportView, showError, t]);

    if (report == null)
        return null;

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>{t('reports.myTimeReport')}</h2>
            <div className="card shadow-sm mb-4">
                <div className="card-body">
                    <div className="row mb-3 align-items-center">
                        <div className="col-sm-2">
                            <div className="d-flex gap-2" role="group" aria-label={t('accessibility.navigation')}>
                                <button className="btn btn-success btn-sm" name={report.fromDate} onClick={handlePreviousReport} title={t('reports.previousPeriod')}>
                                    &lt;&lt;
                                </button>
                                <button className="btn btn-success btn-sm" name={report.toDate} onClick={handleNextReport} title={t('reports.nextPeriod')}>
                                    &gt;&gt;
                                </button>
                            </div>
                        </div>
                        <div className="col-sm-2">
                            <label className="form-label fw-bold text-muted small">{t('reports.periodType')}</label>
                            <select className="form-select form-select-sm" value={reportView} name="reportView" onChange={viewChange}>
                                <option value={Constants.MONTH_VIEW}>📅 {t('reports.monthly')}</option>
                                <option value={Constants.YEAR_VIEW}>📆 {t('reports.yearly')}</option>
                            </select>
                        </div>
                        <div className="col-sm-3">
                            <label className="form-label fw-bold text-muted small">{t('common.labels.workableHours')}</label>
                            <div className="badge bg-info fs-6 py-2 px-3">
                                🕰️ {report.workableHours}
                            </div>
                        </div>
                        <div className="col-sm-5">
                            <div className="text-end">
                                <span className="badge bg-secondary fs-6 py-2 px-3">
                                    📅 {report.fromDate} - {report.toDate}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div className="row">
                <UserReportTable report={report} viewMode={viewMode} setViewMode={setViewMode} />
            </div>
        </div>
    );
}

export default UserReports;