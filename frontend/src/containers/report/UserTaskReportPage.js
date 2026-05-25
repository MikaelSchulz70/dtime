import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router';
import ReportService from '../../service/ReportService';
import * as Constants from '../../common/Constants';
import {
    parsePeriodFromParams,
    shiftPeriodPath,
    buildPeriodUrl,
    currentMonthPeriod,
} from '../../util/periodNavigation';
import { useTranslation } from 'react-i18next';
import UserTaskReportTable from './UserTaskReport';
import PeriodLoadError from '../../components/PeriodLoadError';
import { getApiErrorMessage } from '../../util/apiError';

function UserTaskReportPage() {
    const params = useParams();
    const navigate = useNavigate();
    const period = parsePeriodFromParams(params);
    const [report, setReport] = useState(null);
    const [loadError, setLoadError] = useState(null);
    const reportView = period?.mode === 'year' ? Constants.YEAR_VIEW : Constants.MONTH_VIEW;
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
            ? service.getUserReportForYear(period.year)
            : service.getUserReportForMonth(period.year, period.month);
        request
            .then(response => {
                if (!cancelled) {
                    setReport(response.data);
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
    }, [period?.year, period?.month, period?.week, period?.mode]);

    const viewChange = useCallback((event) => {
        const view = event.target.value;
        if (view === Constants.YEAR_VIEW) {
            navigate(buildPeriodUrl('/userreport', { mode: 'year', year: period?.year || currentMonthPeriod().year }));
        } else {
            const monthPeriod = period?.mode === 'month'
                ? period
                : currentMonthPeriod();
            navigate(buildPeriodUrl('/userreport', monthPeriod));
        }
    }, [period, navigate]);

    const handlePreviousReport = useCallback(() => {
        if (!period) {
            return;
        }
        navigate(buildPeriodUrl('/userreport', shiftPeriodPath(period, -1)));
    }, [period, navigate]);

    const handleNextReport = useCallback(() => {
        if (!period) {
            return;
        }
        navigate(buildPeriodUrl('/userreport', shiftPeriodPath(period, 1)));
    }, [period, navigate]);

    if (loadError) {
        return (
            <div className="container-fluid ml-2 mr-2">
                <h2>{t('reports.myTimeReport')}</h2>
                <PeriodLoadError
                    message={loadError}
                    basePath="/userreport"
                    onGoToCurrent={(url) => navigate(url)}
                />
            </div>
        );
    }

    if (report == null) {
        return null;
    }

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>{t('reports.myTimeReport')}</h2>
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
                <UserTaskReportTable report={report} reportView={reportView} fromDate={report.fromDate} variant="self" />
            </div>
        </div>
    );
}

export default UserTaskReportPage;
