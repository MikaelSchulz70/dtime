import React, { useState, useEffect, useCallback } from 'react';
import ReportService from '../../service/ReportService';
import * as Constants from '../../common/Constants';
import { useToast } from '../../components/Toast';
import { useTranslation } from 'react-i18next';
import UserTaskReportTable from './UserTaskReport';

/**
 * Standalone User Task report page for the logged-in user (USER role).
 * Admins use AdminReports with report type USER_TASK and the same table component.
 */
function UserTaskReportPage() {
    const [report, setReport] = useState(null);
    const [reportView, setReportView] = useState(Constants.MONTH_VIEW);
    const { showError } = useToast();
    const { t } = useTranslation();

    const loadFromServer = useCallback((view) => {
        const service = new ReportService();
        service.getCurrentUserReport(view)
            .then(response => {
                setReport(response.data);
                setReportView(view);
            })
            .catch(error => {
                showError(t('reports.messages.loadFailed', { message: error.response?.data?.message || error.message }));
            });
    }, [showError, t]);

    useEffect(() => {
        loadFromServer(Constants.MONTH_VIEW);
    }, [loadFromServer]);

    const viewChange = useCallback((event) => {
        loadFromServer(event.target.value);
    }, [loadFromServer]);

    const handlePreviousReport = useCallback((event) => {
        const date = event.target.name;
        const service = new ReportService();
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
        const service = new ReportService();
        service.getNextUserReport(reportView, date)
            .then(response => {
                setReport(response.data);
            })
            .catch(error => {
                showError(t('reports.messages.loadFailed', { message: error.response?.data?.message || error.message }));
            });
    }, [reportView, showError, t]);

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
                                <button
                                    type="button"
                                    className="btn btn-success btn-sm"
                                    name={report.fromDate}
                                    onClick={handlePreviousReport}
                                    title={t('reports.previousPeriod')}
                                >
                                    &lt;&lt;
                                </button>
                                <button
                                    type="button"
                                    className="btn btn-success btn-sm"
                                    name={report.toDate}
                                    onClick={handleNextReport}
                                    title={t('reports.nextPeriod')}
                                >
                                    &gt;&gt;
                                </button>
                            </div>
                        </div>
                        <div className="col-sm-2">
                            <label className="form-label fw-bold text-muted small">{t('reports.periodType')}</label>
                            <select
                                className="form-select form-select-sm"
                                value={reportView}
                                name="reportView"
                                onChange={viewChange}
                            >
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
                <UserTaskReportTable
                    report={report}
                    reportView={reportView}
                    fromDate={report.fromDate}
                    variant="self"
                />
            </div>
        </div>
    );
}

export default UserTaskReportPage;
