import React, { useState, useEffect, useCallback } from "react";
import ReportService from '../../service/ReportService';
import * as Constants from '../../common/Constants';
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

    useEffect(() => {
        loadFromServer(Constants.MONTH_VIEW, Constants.USER_TASK_REPORT);
    }, [loadFromServer]);

    const viewChange = useCallback((event) => {
        // Preserve the current date range when changing view type
        if (report && report.fromDate) {
            loadReportForDate(event.target.value, reportType, report.fromDate);
        } else {
            loadFromServer(event.target.value, reportType);
        }
    }, [report, reportType, loadFromServer, loadReportForDate]);

    const typeChange = useCallback((event) => {
        // Preserve the current date range when changing report type
        if (report && report.fromDate) {
            loadReportForDate(reportView, event.target.value, report.fromDate);
        } else {
            loadFromServer(reportView, event.target.value);
        }
    }, [report, reportView, loadFromServer, loadReportForDate]);

    const loadFromServer = useCallback((view, type) => {
        var service = new ReportService();
        service.getCurrentReport(view, type)
            .then(response => {
                setReport(response.data);
                setReportView(view);
                setReportType(type);
            })
            .catch(error => {
                showError('Failed to load report: ' + (error.response?.data?.message || error.message));
            });
    }, [showError]);

    const loadReportForDate = useCallback((view, type, date) => {
        var service = new ReportService();

        // First try to get the report by going to previous, then coming back with next
        // This helps ensure we get the exact same time period
        service.getPreviousReport(view, type, date)
            .then(response => {
                // Now get the next report from the previous period to get back to our target period
                const previousFromDate = response.data.fromDate;
                return service.getNextReport(view, type, previousFromDate);
            })
            .then(response => {
                setReport(response.data);
                setReportView(view);
                setReportType(type);
            })
            .catch(_error => {
                console.log('Failed to load report for specific date, trying direct approach');
                // If the roundtrip fails, try using the date directly with getNextReport
                service.getNextReport(view, type, date)
                    .then(response => {
                        setReport(response.data);
                        setReportView(view);
                        setReportType(type);
                    })
                    .catch(_secondError => {
                        console.log('All attempts failed, falling back to current report');
                        // If everything fails, fall back to current report
                        loadFromServer(view, type);
                    });
            });
    }, [loadFromServer]);

    const handlePreviousReport = useCallback((event) => {
        const date = event.target.name;

        var service = new ReportService();
        service.getPreviousReport(reportView, reportType, date)
            .then(response => {
                setReport(response.data);
            })
            .catch(error => {
                showError('Failed to load previous report: ' + (error.response?.data?.message || error.message));
            });
    }, [reportView, reportType, showError]);

    const handleNextReport = useCallback((event) => {
        const date = event.target.name;

        var service = new ReportService();
        service.getNextReport(reportView, reportType, date)
            .then(response => {
                setReport(response.data);
            })
            .catch(error => {
                showError('Failed to load next report: ' + (error.response?.data?.message || error.message));
            });
    }, [reportView, reportType, showError]);

    if (report == null)
        return null;

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>{t('reports.administrativeReports')}</h2>
            <div className="card shadow-sm mb-4">
                <div className="card-body">
                    <div className="row mb-3 align-items-center">
                        <div className="col-sm-2">
                            <div className="d-flex gap-2" role="group" aria-label="Navigation">
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
                                <option value={Constants.BILLABLE_TASK_TYPE_REPORT}>📊 Billable & Task Type Report</option>
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
                    <UserTaskReportTable report={report} reportView={reportView} fromDate={report.fromDate} />
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
