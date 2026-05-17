import React, { useState, useEffect, useCallback } from "react";
import TimeService from '../../service/TimeService';
import * as Constants from '../../common/Constants';
import { shiftPeriodDate } from '../../util/periodNavigation';
import ReportService from '../../service/ReportService';
import { useToast } from '../../components/Toast';
import { useTranslation } from 'react-i18next';
import { useConfirm } from '../../components/ConfirmProvider';
import TimeReportTable from '../../components/TimeReportGrid';

function Times(props) {
    const [timeReport, setTimeReport] = useState(null);
    const [reportView, setReportView] = useState(Constants.MONTH_VIEW);
    const { showError, showSuccess } = useToast();
    const { t } = useTranslation();
    const confirm = useConfirm();

    const loadTimeReport = useCallback((view, date) => {
        const timeService = new TimeService();
        timeService.getTimeReport(view, date)
            .then(response => {
                setTimeReport(response.data);
                setReportView(view);
            })
            .catch(() => {
                showError(t('time.messages.loadFailed'));
            });
    }, [showError, t]);

    useEffect(() => {
        loadTimeReport(Constants.MONTH_VIEW);
    }, [loadTimeReport]);

    const loadPreviousTimes = useCallback(() => {
        if (!timeReport?.firstDate) {
            return;
        }
        const date = shiftPeriodDate(timeReport.firstDate, reportView, -1);
        loadTimeReport(reportView, date);
    }, [timeReport, reportView, loadTimeReport]);

    const loadNextTimes = useCallback(() => {
        if (!timeReport?.firstDate) {
            return;
        }
        const date = shiftPeriodDate(timeReport.firstDate, reportView, 1);
        loadTimeReport(reportView, date);
    }, [timeReport, reportView, loadTimeReport]);

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
                loadTimeReport(Constants.MONTH_VIEW);
                showSuccess(t('time.messages.reportClosed'));
            })
            .catch(() => {
                showError(t('time.messages.closeFailed'));
            });
    }, [timeReport, loadTimeReport, showSuccess, showError, confirm, t]);

    const viewChange = useCallback((event) => {
        const view = event.target.value;
        if (timeReport?.firstDate) {
            loadTimeReport(view, timeReport.firstDate);
        } else {
            loadTimeReport(view);
        }
    }, [timeReport, loadTimeReport]);

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
