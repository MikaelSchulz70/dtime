import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from 'react-router';
import TimeService from '../../service/TimeService';
import * as Constants from '../../common/Constants';
import {
    parsePeriodFromParams,
    shiftPeriodPath,
    buildPeriodUrl,
    currentMonthPeriod,
    currentWeekPeriod,
} from '../../util/periodNavigation';
import ReportService from '../../service/ReportService';
import { useToast } from '../../components/Toast';
import { useTranslation } from 'react-i18next';
import { useConfirm } from '../../components/ConfirmProvider';
import TimeReportTable from '../../components/TimeReportGrid';
import PeriodLoadError from '../../components/PeriodLoadError';
import { getApiErrorMessage } from '../../util/apiError';

function Times() {
    const params = useParams();
    const navigate = useNavigate();
    const period = parsePeriodFromParams(params);
    const [timeReport, setTimeReport] = useState(null);
    const [loadError, setLoadError] = useState(null);
    const reportView = period?.mode === 'week' ? Constants.WEEK_VIEW : Constants.MONTH_VIEW;
    const { showError, showSuccess } = useToast();
    const { t } = useTranslation();
    const confirm = useConfirm();

    const loadTimeReport = useCallback((p) => {
        const timeService = new TimeService();
        return (p.mode === 'week'
            ? timeService.getTimeReportForWeek(p.year, p.week)
            : timeService.getTimeReportForMonth(p.year, p.month))
            .then(response => {
                setTimeReport(response.data);
                setLoadError(null);
            })
            .catch((error) => {
                setTimeReport(null);
                setLoadError(getApiErrorMessage(error, 'time.messages.loadFailed'));
            });
    }, []);

    useEffect(() => {
        if (!period) {
            return undefined;
        }
        let cancelled = false;
        setLoadError(null);
        setTimeReport(null);
        const timeService = new TimeService();
        const request = period.mode === 'week'
            ? timeService.getTimeReportForWeek(period.year, period.week)
            : timeService.getTimeReportForMonth(period.year, period.month);
        request
            .then(response => {
                if (!cancelled) {
                    setTimeReport(response.data);
                    setLoadError(null);
                }
            })
            .catch((error) => {
                if (!cancelled) {
                    setTimeReport(null);
                    setLoadError(getApiErrorMessage(error, 'time.messages.loadFailed'));
                }
            });
        return () => {
            cancelled = true;
        };
    }, [period?.year, period?.month, period?.week, period?.mode]);

    const loadPreviousTimes = useCallback(() => {
        if (!period) {
            return;
        }
        navigate(buildPeriodUrl('/time', shiftPeriodPath(period, -1)));
    }, [period, navigate]);

    const loadNextTimes = useCallback(() => {
        if (!period) {
            return;
        }
        navigate(buildPeriodUrl('/time', shiftPeriodPath(period, 1)));
    }, [period, navigate]);

    const closeReport = useCallback(async (event) => {
        const confirmed = await confirm({
            message: t('time.messages.closeConfirm'),
            confirmLabel: t('common.buttons.closeReport'),
            variant: 'danger',
        });
        if (!confirmed) {
            return;
        }
        const date = event.target.name;

        var payLoad = '{ "userId": "' + timeReport.user.id + '", "closeDate": "' + date + '"}';
        var service = new ReportService();
        service.updateOpenCloseReport(payLoad, 'close')
            .then(() => {
                if (period) {
                    loadTimeReport(period);
                }
                showSuccess(t('time.messages.reportClosed'));
            })
            .catch(() => {
                showError(t('time.messages.closeFailed'));
            });
    }, [timeReport, period, loadTimeReport, showSuccess, showError, confirm, t]);

    const viewChange = useCallback((event) => {
        const view = event.target.value;
        if (view === Constants.WEEK_VIEW) {
            const weekPeriod = period?.mode === 'week'
                ? period
                : currentWeekPeriod();
            navigate(buildPeriodUrl('/time', weekPeriod));
        } else {
            const monthPeriod = period?.mode === 'month'
                ? period
                : currentMonthPeriod();
            navigate(buildPeriodUrl('/time', monthPeriod));
        }
    }, [period, navigate]);

    if (loadError) {
        return (
            <div className="container-fluid ml-2 mr-2">
                <h2>{t('time.title')}</h2>
                <PeriodLoadError
                    message={loadError}
                    basePath="/time"
                    onGoToCurrent={(url) => navigate(url)}
                />
            </div>
        );
    }

    if (timeReport == null) return null;

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>{t('time.title')}</h2>
            <div className="row mb-2">
                <div className="col-auto">
                    <button type="button" className="btn btn-success btn-sm" onClick={loadPreviousTimes}>&lt;&lt;</button>
                </div>
                <div className="col-auto">
                    <button type="button" className="btn btn-success btn-sm" onClick={loadNextTimes}>&gt;&gt;</button>
                </div>
                <div className="col-auto">
                    <select className="form-control form-control-sm" value={reportView} name="reportView" onChange={viewChange}>
                        <option value={Constants.WEEK_VIEW}>{t('common.timeView.week')}</option>
                        <option value={Constants.MONTH_VIEW}>{t('common.timeView.month')}</option>
                    </select>
                </div>
                <div className="col text-end">
                    {reportView === Constants.MONTH_VIEW && !timeReport.closed ? (
                        <button className="btn btn-success btn-sm" name={timeReport.firstDate} onClick={closeReport}>{t('common.buttons.closeReport')}</button>
                    ) : ''}
                </div>
                <div className="col-auto">
                    <span className="badge bg-secondary fs-6 py-2 px-3 text-nowrap">
                        📅 {timeReport.firstDate} - {timeReport.lastDate}
                    </span>
                </div>
            </div>
            <div className="row">
                <div className="table-responsive">
                    <TimeReportTable timeReport={timeReport} />
                </div>
            </div>
        </div>
    );
}

export default Times;
