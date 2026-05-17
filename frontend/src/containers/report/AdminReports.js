import React, { useState, useEffect, useCallback } from "react";
import ReportService from '../../service/ReportService';
import * as Constants from '../../common/Constants';
import { shiftPeriodDate } from '../../util/periodNavigation';
import TaskReportTable from './TaskReport';
import AccountReportTable from './AccountReport';
import UserReportTable from './UserReport';
import UserTaskReportTable from './UserTaskReport';
import BillableTaskTypeReportTable from './BillableTaskTypeReportTable';
import { useToast } from '../../components/Toast';
import { useTranslation } from 'react-i18next';

function AdminReports() {
    const [report, setReport] = useState(null);
    const [reportView, setReportView] = useState(Constants.MONTH_VIEW);
    const [reportType, setReportType] = useState(Constants.USER_TASK_REPORT);
    const { showError } = useToast();
    const { t } = useTranslation();

    const loadReport = useCallback((view, type, date) => {
        const service = new ReportService();
        service.getReport(view, type, date)
            .then(response => {
                setReport(response.data);
                setReportView(view);
                setReportType(type);
            })
            .catch(error => {
                showError(t('reports.messages.loadFailed', { message: error.response?.data?.message || error.message }));
            });
    }, [showError, t]);

    useEffect(() => {
        loadReport(Constants.MONTH_VIEW, Constants.USER_TASK_REPORT);
    }, [loadReport]);

    const viewChange = useCallback((event) => {
        const view = event.target.value;
        if (report && report.fromDate) {
            loadReport(view, reportType, report.fromDate);
        } else {
            loadReport(view, reportType);
        }
    }, [report, reportType, loadReport]);

    const typeChange = useCallback((event) => {
        const type = event.target.value;
        if (report && report.fromDate) {
            loadReport(reportView, type, report.fromDate);
        } else {
            loadReport(reportView, type);
        }
    }, [report, reportView, loadReport]);

    const handlePreviousReport = useCallback(() => {
        if (!report?.fromDate) {
            return;
        }
        const date = shiftPeriodDate(report.fromDate, reportView, -1);
        loadReport(reportView, reportType, date);
    }, [report, reportView, reportType, loadReport]);

    const handleNextReport = useCallback(() => {
        if (!report?.fromDate) {
            return;
        }
        const date = shiftPeriodDate(report.fromDate, reportView, 1);
        loadReport(reportView, reportType, date);
    }, [report, reportView, reportType, loadReport]);

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
                ) : reportType === Constants.USER_REPORT ? (
                    <UserReportTable report={report} />
                ) : reportType === Constants.ACCOUNT_REPORT ? (
                    <AccountReportTable report={report} />
                ) : reportType === Constants.BILLABLE_TASK_TYPE_REPORT ? (
                    <BillableTaskTypeReportTable report={report} />
                ) : ''}
            </div>
        </div>
    );
}

export default AdminReports;
