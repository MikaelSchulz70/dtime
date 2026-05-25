import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from 'react-router';
import TimeReportStatusService from '../../service/TimeReportStatusService';
import {
    parsePeriodFromParams,
    shiftPeriodPath,
    buildPeriodUrl,
} from '../../util/periodNavigation';
import SystemService from '../../service/SystemService';
import { useToast } from '../../components/Toast';
import { useTranslation } from 'react-i18next';
import { useTableSort } from '../../hooks/useTableSort';
import SortableTableHeader from '../../components/SortableTableHeader';
import { useConfirm } from '../../components/ConfirmProvider';
import PeriodLoadError from '../../components/PeriodLoadError';
import { getApiErrorMessage } from '../../util/apiError';
import { normalizeTimeReportStatusReport } from '../../util/timeReportStatus';

function UnclosedUsersTable({ report: initialReport, showClosedOnly, onReportUpdate, showError }) {
    const [report, setReport] = useState(initialReport);
    const { t } = useTranslation();
    const confirm = useConfirm();
    const { sortedData: sortedUsers, requestSort, getSortIcon } = useTableSort(
        report?.unclosedUsers, 
        'fullName'
    );

    useEffect(() => {
        setReport(initialReport);
    }, [initialReport]);

    const handleOpenCloseReport = useCallback(async (event) => {
        const userId = parseInt(event.target.id);
        const date = event.target.name;

        if (!report.unclosedUsers) return;

        const user = report.unclosedUsers.find(u => u.userId === userId);
        if (!user) return;

        const isClosing = !user.closed;
        const userName = user.fullName || t('common.labels.user');
        const confirmMessage = isClosing
            ? t('timeReports.messages.confirmCloseReport', { userName })
            : t('timeReports.messages.confirmOpenReport', { userName });
        const confirmed = await confirm({
            message: confirmMessage,
            confirmLabel: isClosing ? t('timeReports.actions.close') : t('timeReports.actions.reopen'),
            variant: isClosing ? 'danger' : 'primary',
        });
        if (!confirmed) {
            event.target.checked = user.closed;
            return;
        }

        const service = user.closed ?
            TimeReportStatusService.openUserTimeReport(userId, date) :
            TimeReportStatusService.closeUserTimeReport(userId, date);

        service
            .then(response => {
                const toggledClosed = !user.closed;
                const updatedUsers = report.unclosedUsers
                    .map(u => (u.userId === userId ? { ...u, closed: toggledClosed } : u))
                    .filter(u => (showClosedOnly ? u.closed : !u.closed));

                const updatedReport = { ...report, unclosedUsers: updatedUsers };
                setReport(updatedReport);

                if (onReportUpdate) {
                    onReportUpdate(updatedReport);
                }
            })
            .catch(error => {
                console.error('Error toggling report status:', error);
                const errorMessage = error.response?.data?.message || error.message || t('timeReports.messages.failedToggleReport');
                showError(errorMessage);

                // Revert the checkbox on error
                event.target.checked = user.closed;
            });
    }, [report, showClosedOnly, onReportUpdate, showError, confirm, t]);

    if (report == null) {
        return (
            <div className="text-center">
                <div className="spinner-border" role="status">
                    <span className="visually-hidden">{t('common.loading.default')}</span>
                </div>
            </div>
        );
    }

    var rows = [];
    const workableHours = report.workableHours || 0;
    const fromDate = report.fromDate || '';

    rows.push(
        <tr key={0} className="bg-success text-white">
            <SortableTableHeader 
                field="fullName" 
                onSort={requestSort} 
                getSortIcon={getSortIcon}
                className="text-white fw-bold"
            >
                👤 {t('common.labels.user')}
            </SortableTableHeader>
            <SortableTableHeader 
                field="email" 
                onSort={requestSort} 
                getSortIcon={getSortIcon}
                className="text-white fw-bold"
            >
                📧 {t('common.labels.email')}
            </SortableTableHeader>
            <SortableTableHeader 
                field="totalTime" 
                onSort={requestSort} 
                getSortIcon={getSortIcon}
                className="text-white fw-bold col-num"
            >
                ⏱️ {t('timeReports.labels.totalHours')}
            </SortableTableHeader>
            <th className="fw-bold col-num">📊 {t('timeReports.labels.workableHours')}</th>
            <SortableTableHeader 
                field="closed" 
                onSort={requestSort} 
                getSortIcon={getSortIcon}
                className="text-white fw-bold"
            >
                📋 {t('common.labels.status')}
            </SortableTableHeader>
            <th className="fw-bold">⚙️ {t('common.labels.actions')}</th>
        </tr>);

    var key = 1;
    if (sortedUsers && Array.isArray(sortedUsers) && sortedUsers.length > 0) {
        sortedUsers.forEach((user) => {
            if (!user) return; // Skip null/undefined users

            const totalTime = user.totalTime || 0;
            const textColor = totalTime < workableHours ? 'text-danger' : '';
            const closeTextColor = user.closed ? "white" : "red";

            rows.push(
                <tr key={key}>
                    <td className="fw-medium">{user.fullName || t('common.labels.unknownUser')}</td>
                    <td className="text-muted">{user.email || t('common.labels.noEmail')}</td>
                    <td className={`fw-bold col-num ${textColor}`}>{totalTime}</td>
                    <td className="fw-bold col-num text-primary">{workableHours}</td>
                    <td>
                        <span className={`badge ${user.closed ? 'bg-success' : 'bg-warning text-dark'} py-1 px-2`}>
                            {user.closed ? `✅ ${t('common.status.closed')}` : `⏳ ${t('common.status.open')}`}
                        </span>
                    </td>
                    <td>
                        <div className="form-check">
                            <input
                                className="form-check-input"
                                type="checkbox"
                                id={user.userId}
                                name={fromDate}
                                checked={user.closed || false}
                                onChange={handleOpenCloseReport}
                            />
                            <label className="form-check-label fw-medium" htmlFor={user.userId}>
                                {user.closed ? `🔓 ${t('timeReports.actions.reopen')}` : `🔒 ${t('timeReports.actions.close')}`}
                            </label>
                        </div>
                    </td>
                </tr>);
            key++;
        });
    } else {
        rows.push(
            <tr key={key}>
                <td colSpan="6" className="text-center text-muted">
                    {report?.unclosedUsers === undefined ?
                        t('common.loading.users') :
                        (showClosedOnly
                            ? t('timeReports.messages.noClosedReports')
                            : t('timeReports.messages.allUsersClosed'))
                    }
                </td>
            </tr>
        );
    }

    return (
        <div className="card shadow-sm">
            <div className="card-header bg-light">
                <h5 className="mb-0 fw-bold text-muted">👥 {t('timeReports.userStatusOverview')}</h5>
            </div>
            <div className="card-body p-0">
                <div className="table-responsive">
                    <table className="table table-hover table-striped mb-0">
                        <tbody>
                            {rows}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}

function UnclosedUsersPage(props) {
    const params = useParams();
    const navigate = useNavigate();
    const period = parsePeriodFromParams(params);
    const [report, setReport] = useState(null);
    const [loadError, setLoadError] = useState(null);
    const [showClosedOnly, setShowClosedOnly] = useState(false);
    const [mailEnabled, setMailEnabled] = useState(false);
    const { showError, showSuccess, showWarning } = useToast();
    const confirm = useConfirm();
    const { t } = useTranslation();

    const checkMailEnabled = useCallback(() => {
        const systemService = new SystemService();
        systemService.isMailEnabled()
            .then(response => {
                setMailEnabled(response.data);
            })
            .catch(error => {
                console.error('Error checking mail enabled status:', error);
                // Default to false if we can't check
                setMailEnabled(false);
            });
    }, []);

    useEffect(() => {
        checkMailEnabled();
    }, [checkMailEnabled]);

    useEffect(() => {
        if (period?.mode !== 'month') {
            return undefined;
        }
        let cancelled = false;
        setLoadError(null);
        setReport(null);
        const request = showClosedOnly
            ? TimeReportStatusService.getClosedUsersForMonth(period.year, period.month)
            : TimeReportStatusService.getUnclosedUsersForMonth(period.year, period.month);
        request
            .then(response => {
                if (!cancelled) {
                    setReport(normalizeTimeReportStatusReport(response));
                    setLoadError(null);
                }
            })
            .catch(error => {
                if (!cancelled) {
                    setReport(null);
                    setLoadError(getApiErrorMessage(error, 'timeReports.messages.failedToLoad'));
                }
            });
        return () => {
            cancelled = true;
        };
    }, [period?.year, period?.month, period?.mode, showClosedOnly]);

    const handleStatusFilterChange = useCallback((event) => {
        setShowClosedOnly(event.target.value === 'closed');
    }, []);

    const handlePreviousReport = useCallback(() => {
        if (!period) {
            return;
        }
        navigate(buildPeriodUrl('/timereportstatus', shiftPeriodPath(period, -1)));
    }, [period, navigate]);

    const handleNextReport = useCallback(() => {
        if (!period) {
            return;
        }
        navigate(buildPeriodUrl('/timereportstatus', shiftPeriodPath(period, 1)));
    }, [period, navigate]);

    const handleReportUpdate = useCallback((updatedReport) => {
        setReport(updatedReport);
    }, []);

    const handleSendEmailReminder = useCallback(async () => {
        const unclosedCount = (report?.unclosedUsers || []).length;

        if (unclosedCount === 0) {
            showWarning(t('timeReports.messages.noUnclosedUsers'));
            return;
        }

        const confirmMessage = t('timeReports.messages.confirmSendReminders', { count: unclosedCount, plural: unclosedCount === 1 ? '' : 's' });
        const confirmed = await confirm({
            message: confirmMessage,
            confirmLabel: t('timeReports.sendReminders'),
            variant: 'primary',
        });
        if (!confirmed) {
            return;
        }

        const systemService = new SystemService();
        systemService.sendEmailReminderToUnclosedUsers()
            .then(response => {
                showSuccess(t('timeReports.messages.remindersSent', { count: unclosedCount, plural: unclosedCount === 1 ? '' : 's' }));
            })
            .catch(error => {
                console.error('Error sending email reminders:', error);
                const errorMessage = error.response?.data?.message || error.message || t('timeReports.messages.failedToSendReminders');
                showError(t('timeReports.messages.failedToSendReminders') + ': ' + errorMessage);
            });
    }, [report, showWarning, showSuccess, showError, confirm, t]);

    if (loadError) {
        return (
            <div className="container-fluid ml-2 mr-2">
                <h2>{t('timeReports.title')}</h2>
                <PeriodLoadError
                    message={loadError}
                    basePath="/timereportstatus"
                    onGoToCurrent={(url) => navigate(url)}
                />
            </div>
        );
    }

    if (report == null)
        return (
            <div className="text-center">
                <div className="spinner-border" role="status">
                    <span className="visually-hidden">{t('common.loading.default')}</span>
                </div>
            </div>
        );

    const fromDate = report.fromDate || '';
    const toDate = report.toDate || '';

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>{t('timeReports.title')}</h2>
            <div className="card shadow-sm mb-4">
                <div className="card-body">
                    <div className="row align-items-center mb-3">
                        <div className="col-sm-4">
                            <div className="d-flex gap-2" role="group" aria-label={t('accessibility.navigation')}>
                                <button className="btn btn-success btn-sm" name={fromDate} onClick={handlePreviousReport} title={t('timeReports.previousPeriod')}>
                                    &lt;&lt;
                                </button>
                                <button className="btn btn-success btn-sm" name={toDate} onClick={handleNextReport} title={t('timeReports.nextPeriod')}>
                                    &gt;&gt;
                                </button>
                            </div>
                        </div>
                        <div className="col-sm-3">
                            <label className="form-label fw-bold text-muted small mb-1">{t('timeReports.reportStatus')}</label>
                            <select
                                className="form-select form-select-sm"
                                value={showClosedOnly ? 'closed' : 'open'}
                                onChange={handleStatusFilterChange}
                            >
                                <option value="open">{t('timeReports.filters.unclosed')}</option>
                                <option value="closed">{t('timeReports.filters.closed')}</option>
                            </select>
                        </div>
                        <div className="col-sm-5 text-end">
                            <span className="badge bg-secondary fs-6 py-2 px-3 me-3">
                                📅 {fromDate} - {toDate}
                            </span>
                            {mailEnabled && !showClosedOnly && (
                                <button
                                    className="btn btn-primary btn-sm"
                                    onClick={handleSendEmailReminder}
                                    title={t('timeReports.sendRemindersTooltip')}
                                >
                                    📧 {t('timeReports.sendReminders')}
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col-sm-12">
                    <UnclosedUsersTable
                        report={report}
                        showClosedOnly={showClosedOnly}
                        onReportUpdate={handleReportUpdate}
                        showError={showError}
                    />
                </div>
            </div>
        </div>
    );
}

export default UnclosedUsersPage;