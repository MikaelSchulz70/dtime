import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate, useSearchParams } from 'react-router';
import ReportService from '../../service/ReportService';
import * as Constants from '../../common/Constants';
import {
    parsePeriodFromParams,
    shiftPeriodPath,
    buildPeriodUrl,
    currentMonthPeriod,
} from '../../util/periodNavigation';
import TaskReportTable from './TaskReport';
import AccountReportTable from './AccountReport';
import UserReportTable from './UserReport';
import UserTaskReportTable from './UserTaskReport';
import BillableTaskTypeReportTable from './BillableTaskTypeReportTable';
import { useTranslation } from 'react-i18next';
import PeriodLoadError from '../../components/PeriodLoadError';
import { getApiErrorMessage } from '../../util/apiError';

function AdminReports() {
    const params = useParams();
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const period = parsePeriodFromParams(params);
    const [report, setReport] = useState(null);
    const [loadError, setLoadError] = useState(null);
    const reportView = period?.mode === 'year' ? Constants.YEAR_VIEW : Constants.MONTH_VIEW;
    const [reportType, setReportType] = useState(
        searchParams.get('type') || Constants.USER_TASK_REPORT
    );
    const { t } = useTranslation();

    useEffect(() => {
        if (!period) {
            return undefined;
        }
        let cancelled = false;
        setLoadError(null);
        setReport(null);
        const service = new ReportService();
        const request = period.mode === 'year'
            ? service.getReportForYear(period.year, reportType)
            : service.getReportForMonth(period.year, period.month, reportType);
        request
            .then(response => {
                if (!cancelled) {
                    setReport(response.data);
                    setReportType(reportType);
                    setLoadError(null);
                }
            })
            .catch(error => {
                if (!cancelled) {
                    setReport(null);
                    setLoadError(getApiErrorMessage(error, 'errors.period.invalidPeriod'));
                }
            });
        return () => {
            cancelled = true;
        };
    }, [period?.year, period?.month, period?.week, period?.mode, reportType]);

    const viewChange = useCallback((event) => {
        const view = event.target.value;
        if (view === Constants.YEAR_VIEW) {
            navigate({
                pathname: buildPeriodUrl('/reports', { mode: 'year', year: period?.year || currentMonthPeriod().year }),
                search: searchParams.toString(),
            });
        } else {
            const monthPeriod = period?.mode === 'month'
                ? period
                : currentMonthPeriod();
            navigate({
                pathname: buildPeriodUrl('/reports', monthPeriod),
                search: searchParams.toString(),
            });
        }
    }, [period, navigate, searchParams]);

    const typeChange = useCallback((event) => {
        const type = event.target.value;
        setReport(null);
        setSearchParams({ type });
        setReportType(type);
    }, [setSearchParams]);

    const handlePreviousReport = useCallback(() => {
        if (!period) {
            return;
        }
        navigate({
            pathname: buildPeriodUrl('/reports', shiftPeriodPath(period, -1)),
            search: searchParams.toString(),
        });
    }, [period, navigate, searchParams]);

    const handleNextReport = useCallback(() => {
        if (!period) {
            return;
        }
        navigate({
            pathname: buildPeriodUrl('/reports', shiftPeriodPath(period, 1)),
            search: searchParams.toString(),
        });
    }, [period, navigate, searchParams]);

    if (loadError) {
        return (
            <div className="container-fluid ml-2 mr-2">
                <h2>{t('reports.administrativeReports')}</h2>
                <PeriodLoadError
                    message={loadError}
                    basePath="/reports"
                    search={searchParams.toString() || 'type=USER_TASK'}
                    onGoToCurrent={(url) => navigate(url)}
                />
            </div>
        );
    }

    if (report == null)
        return null;

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>{t('reports.administrativeReports')}</h2>
            <div className="card shadow-sm mb-4">
                <div className="card-body">
                    <div className="row mb-3 align-items-center">
                        <div className="col-sm-2">
                            <div className="d-flex gap-2" role="group" aria-label={t('accessibility.navigation')}>
                                <button type="button" className="btn btn-success btn-sm" onClick={handlePreviousReport} title={t('reports.previousPeriod')}>
                                    &lt;&lt;
                                </button>
                                <button type="button" className="btn btn-success btn-sm" onClick={handleNextReport} title={t('reports.nextPeriod')}>
                                    &gt;&gt;
                                </button>
                            </div>
                        </div>
                        <div className="col-sm-2">
                            <label className="form-label fw-bold text-muted small">{t('reports.periodType')}</label>
                            <select className="form-select form-select-sm" value={reportView} name="reportView" onChange={viewChange}>
                                <option value="MONTH">📅 {t('reports.monthly')}</option>
                                <option value="YEAR">📆 {t('reports.yearly')}</option>
                            </select>
                        </div>
                        <div className="col-sm-3">
                            <label className="form-label fw-bold text-muted small">{t('reports.reportType')}</label>
                            <select className="form-select form-select-sm" value={reportType} name="reportType" onChange={typeChange}>
                                <option value={Constants.USER_TASK_REPORT}>👥 {t('reports.userTaskReport')}</option>
                                <option value={Constants.ACCOUNT_REPORT}>🏢 {t('reports.accountReport')}</option>
                                <option value={Constants.TASK_REPORT}>📋 {t('reports.taskReport')}</option>
                                <option value={Constants.USER_REPORT}>👤 {t('reports.userReport')}</option>
                                <option value={Constants.BILLABLE_TASK_TYPE_REPORT}>📊 {t('reports.billableTaskTypeReport')}</option>
                            </select>
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
                {reportType === Constants.USER_TASK_REPORT ? (
                    <UserTaskReportTable report={report} reportView={reportView} fromDate={report.fromDate} variant="admin" />
                ) : reportType === Constants.TASK_REPORT ? (
                    <TaskReportTable report={report} />
                ) : reportType === Constants.ACCOUNT_REPORT ? (
                    <AccountReportTable report={report} />
                ) : reportType === Constants.USER_REPORT ? (
                    <UserReportTable report={report} />
                ) : (
                    <BillableTaskTypeReportTable report={report} />
                )}
            </div>
        </div>
    );
}

export default AdminReports;
