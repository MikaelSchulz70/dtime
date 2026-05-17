import React, { useState, useCallback } from "react";
import Modal from 'react-modal';
import * as Constants from '../../common/Constants';
import TimeService from '../../service/TimeService';
import { useToast } from '../../components/Toast';
import { useTranslation } from 'react-i18next';
import {
    UserTaskBarChart,
    TaskDistributionPieChart,
    AccountHoursChart,
    TimeTrendChart,
    ChartViewToggle
} from '../../components/Charts';
import TimeReportTable from '../../components/TimeReportGrid';

Modal.setAppElement('#root');

function UserReportSummaryTable({ report }) {
    const { t } = useTranslation();
    if (report == null)
        return null;

    var rows = [];
    rows.push(
        <tr key={'summary-header'} className="bg-success text-white">
            <th className="fw-bold fs-6">{t('reports.summary.title')}</th>
            <th className="fw-bold fs-6 col-num">{t('reports.summary.workableHoursMonth')}</th>
            <th className="fw-bold fs-6 col-num">{t('reports.summary.totalWorkableHours')}</th>
            <th className="fw-bold fs-6 col-num">{t('reports.summary.totalHoursWorked')}</th>
            <th></th>
        </tr>
    );

    rows.push(
        <tr key={'summary-info'} className="table-light">
            <td className="fw-bold text-muted">{t('reports.summary.totals')}</td>
            <td className="fw-bold col-num">{report.workableHours}</td>
            <td className="fw-bold col-num">{report.totalWorkableHours}</td>
            <td className="fw-bold col-num text-success">{report.totalHoursWorked}</td>
            <td></td>
        </tr>
    );

    return (
        <tbody>
            {rows}
        </tbody>
    );
}

function UserReportRows({ userReport, reportView, workableHours, fromDate, toDate, showError, variant = 'admin' }) {
    const { t } = useTranslation();
    const isSelf = variant === 'self';

    if (userReport == null)
        return null;

    var rows = [];
    var keyBase = userReport.userId + '_';
    var keyHeader = keyBase + '0';

    if (isSelf) {
        rows.push(<tr key={keyHeader}>
            <th className="fw-bold">{t('reports.tableAccount')}</th>
            <th className="fw-bold text-start">{t('reports.tableTask')}</th>
            <th className="fw-bold col-num">{t('reports.tableTotalHours')}</th>
            <th className="fw-bold">
                {reportView === Constants.MONTH_VIEW ? (
                    <UserDetailReport userId={userReport.userId} fromDate={fromDate} toDate={toDate} showError={showError} />
                ) : ''}
            </th>
        </tr>);
    } else {
        rows.push(<tr key={keyHeader}>
            <th className="fw-bold">{userReport.fullName}</th>
            <th className="fw-bold">{t('reports.tableAccount')}</th>
            <th className="fw-bold text-start">{t('reports.tableTask')}</th>
            <th className="fw-bold col-num">{t('reports.tableTotalHours')}</th>
            <th className="fw-bold">
                {reportView === Constants.MONTH_VIEW ? (
                    <UserDetailReport userId={userReport.userId} fromDate={fromDate} toDate={toDate} showError={showError} />
                ) : ''}
            </th>
        </tr>);
    }

    userReport.taskReports.forEach(function (taskReport) {
        var accountName = taskReport.accountName;
        var accountShortName = accountName.substring(0, Math.min(20, accountName.length));
        var taskName = taskReport.taskName;
        var taskShortName = taskName.substring(0, Math.min(20, taskName.length));

        var key = keyBase + '_' + taskReport.idtask;
        if (isSelf) {
            rows.push(
                <tr key={key}>
                    <td className="text-nowrap" title={accountName}>{accountShortName}</td>
                    <td className="text-nowrap text-start" title={taskName}>{taskShortName}</td>
                    <td className="col-num">{taskReport.totalHours}</td>
                    <td></td>
                </tr>);
        } else {
            rows.push(
                <tr key={key}>
                    <td></td>
                    <td className="text-nowrap" title={accountName}>{accountShortName}</td>
                    <td className="text-nowrap text-start" title={taskName}>{taskShortName}</td>
                    <td className="col-num">{taskReport.totalHours}</td>
                    <td></td>
                </tr>);
        }
    });

    var key = keyBase + "_footer";
    if (isSelf) {
        rows.push(<tr key={key} className="bg-success text-white border-top border-2">
            <th className="fw-bold fs-6" colSpan="2">{t('reports.totalTime')}</th>
            <th className="fw-bold fs-6 col-num">{userReport.totalTime}</th>
            <th></th>
        </tr>);
    } else {
        rows.push(<tr key={key} className="bg-success text-white border-top border-2">
            <th className="text-muted"></th>
            <th className="fw-bold fs-6"></th>
            <th></th>
            <th className="fw-bold fs-6 col-num">{userReport.totalTime}</th>
            <th></th>
        </tr>);
    }
    return (
        <tbody>
            {rows}
        </tbody>
    );
}

function UserDetailReport({ userId, fromDate, toDate, showError }) {
    const { t } = useTranslation();
    const [timeReport, setTimeReport] = useState(null);
    const [isOpen, setIsOpen] = useState(false);
    const [viewMode, setViewMode] = useState('table');

    const showDetails = useCallback(() => {
        loadFromServer();
    }, []);

    const closeDetails = useCallback(() => {
        setIsOpen(false);
    }, []);

    const loadFromServer = useCallback(() => {
        var service = new TimeService();
        service.getUserTimes(userId, fromDate)
            .then(response => {
                setTimeReport(response.data);
                setIsOpen(true);
            })
            .catch(error => {
                showError(t('reports.messages.loadUserReportFailed', { message: error.response?.data?.message || error.message }));
            });
    }, [userId, fromDate, showError, t]);

    return (
        <div>
            <button className="btn btn-outline-success btn-sm" onClick={showDetails}>
                {t('reports.viewDetails')}
            </button>
            {isOpen ? (
                <Modal
                    isOpen={isOpen}
                    onRequestClose={closeDetails}
                    style={{
                        content: {
                            top: '50%',
                            left: '50%',
                            right: 'auto',
                            bottom: 'auto',
                            marginRight: '-50%',
                            transform: 'translate(-50%, -50%)',
                            width: '95vw',
                            maxWidth: '95vw',
                            maxHeight: '95vh',
                            padding: '0',
                            border: 'none',
                            borderRadius: '12px',
                            boxShadow: '0 10px 30px rgba(0, 0, 0, 0.3)',
                            overflow: 'hidden'
                        },
                        overlay: {
                            backgroundColor: 'rgba(0, 0, 0, 0.6)',
                            zIndex: 1050
                        }
                    }}
                >
                    <div className="modal-content border-0" style={{ borderRadius: '12px', overflow: 'hidden', height: '100%', display: 'flex', flexDirection: 'column' }}>
                        {/* Modal Header */}
                        <div className="modal-header bg-success" style={{
                            borderBottom: 'none',
                            padding: '1.5rem 2rem'
                        }}>
                            <div className="d-flex align-items-center">
                                <div className="me-3 p-2 rounded-circle bg-white bg-opacity-20">
                                    <span className="fs-4">📊</span>
                                </div>
                                <div>
                                    <h4 className="mb-1 text-white fw-bold">{t('reports.timeReportDetails')}</h4>
                                    <p className="mb-0 text-white-50 small">
                                        📅 {fromDate && fromDate} {toDate && ` - ${toDate}`}
                                    </p>
                                </div>
                            </div>
                            <div className="d-flex align-items-center gap-3">
                                <ChartViewToggle viewMode={viewMode} onViewChange={setViewMode} />
                                <button
                                    type="button"
                                    className="btn btn-outline-light btn-sm px-3 py-2"
                                    onClick={closeDetails}
                                    style={{ borderRadius: '8px' }}
                                >
                                    <span className="fw-bold">{t('reports.closeModal')}</span>
                                </button>
                            </div>
                        </div>

                        {/* Modal Body */}
                        <div className="modal-body p-0" style={{ flex: '1', overflow: 'auto', minHeight: '0' }}>
                            <div style={{ padding: '1rem 1.5rem' }}>
                                {timeReport && (
                                    <div className="mb-3">
                                        <div className="row g-2 mb-3">
                                            <div className="col-md-3">
                                                <div className="card border-0 bg-light h-100">
                                                    <div className="card-body text-center p-2">
                                                        <div className="text-primary mb-2">
                                                            <span className="fs-4">📈</span>
                                                        </div>
                                                        <h6 className="card-title text-muted mb-1">{t('reports.cardHours')}</h6>
                                                        <p className="card-text fs-5 fw-bold text-dark mb-0">
                                                            {(() => {
                                                                if (!timeReport.timeReportTasks) return '0h';
                                                                let total = 0;
                                                                timeReport.timeReportTasks.forEach(task => {
                                                                    task.timeEntries.forEach(entry => {
                                                                        if (entry.time) total += parseFloat(entry.time) || 0;
                                                                    });
                                                                });
                                                                return total.toFixed(1) + 'h';
                                                            })()}
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="col-md-3">
                                                <div className="card border-0 bg-light h-100">
                                                    <div className="card-body text-center p-2">
                                                        <div className="text-info mb-2">
                                                            <span className="fs-4">📋</span>
                                                        </div>
                                                        <h6 className="card-title text-muted mb-1">{t('reports.cardTasks')}</h6>
                                                        <p className="card-text fs-5 fw-bold text-dark mb-0">
                                                            {timeReport.timeReportTasks ? timeReport.timeReportTasks.length : 0}
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="col-md-3">
                                                <div className="card border-0 bg-light h-100">
                                                    <div className="card-body text-center p-2">
                                                        <div className="text-warning mb-2">
                                                            <span className="fs-4">📅</span>
                                                        </div>
                                                        <h6 className="card-title text-muted mb-1">{t('reports.cardDays')}</h6>
                                                        <p className="card-text fs-5 fw-bold text-dark mb-0">
                                                            {timeReport.days ? timeReport.days.length : 0}
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="col-md-3">
                                                <div className="card border-0 bg-light h-100">
                                                    <div className="card-body text-center p-2">
                                                        <div className="text-success mb-2">
                                                            <span className="fs-4">⏱️</span>
                                                        </div>
                                                        <h6 className="card-title text-muted mb-1">{t('reports.cardAvgPerDay')}</h6>
                                                        <p className="card-text fs-5 fw-bold text-dark mb-0">
                                                            {(() => {
                                                                if (!timeReport.timeReportTasks || !timeReport.days) return '0h';
                                                                let total = 0;
                                                                timeReport.timeReportTasks.forEach(task => {
                                                                    task.timeEntries.forEach(entry => {
                                                                        if (entry.time) total += parseFloat(entry.time) || 0;
                                                                    });
                                                                });
                                                                const workDays = timeReport.days.filter(day => !day.weekend && !day.majorHoliday).length;
                                                                return workDays > 0 ? (total / workDays).toFixed(1) + 'h' : '0h';
                                                            })()}
                                                        </p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                )}

                                <div className="card border-0 shadow-sm">
                                    <div className="card-header bg-white border-bottom py-2">
                                        <h6 className="mb-0 fw-bold text-dark">
                                            {viewMode === 'table' ? t('reports.detailedTimeEntries') : t('reports.timeVisualization')}
                                        </h6>
                                    </div>
                                    <div className="card-body p-3">
                                        {viewMode === 'table' ? (
                                            <div className="table-responsive" style={{
                                                overflowX: 'auto',
                                                overflowY: 'auto',
                                                maxHeight: 'calc(75vh - 200px)',
                                                border: '1px solid #dee2e6',
                                                borderRadius: '8px'
                                            }}>
                                                <TimeReportTable
                                                    timeReport={timeReport}
                                                    readOnly={true}
                                                    className="table-sm time-report-table mb-0"
                                                    tableStyle={{
                                                        fontSize: '0.9rem',
                                                        minWidth: 'max-content',
                                                        width: 'auto',
                                                    }}
                                                    theadClassName="table-success sticky-top"
                                                    headerVariant="modal"
                                                />
                                            </div>
                                        ) : (
                                            <div>
                                                <div className="mb-4">
                                                    <h6 className="mb-3 fw-bold text-primary">{t('reports.dailyTimeDistribution')}</h6>
                                                    <TimeTrendChart
                                                        timeReportTasks={timeReport?.timeReportTasks}
                                                        days={timeReport?.days}
                                                    />
                                                </div>

                                                {/* Transform data for other charts */}
                                                {(() => {
                                                    if (!timeReport?.timeReportTasks) return null;

                                                    // Create user reports format for chart components
                                                    const userReports = [{
                                                        fullName: t('reports.currentUser'),
                                                        totalTime: (() => {
                                                            let total = 0;
                                                            timeReport.timeReportTasks.forEach(task => {
                                                                task.timeEntries.forEach(entry => {
                                                                    if (entry.time) total += parseFloat(entry.time) || 0;
                                                                });
                                                            });
                                                            return total;
                                                        })(),
                                                        taskReports: timeReport.timeReportTasks.map(task => ({
                                                            accountName: task.task.account.name,
                                                            taskName: task.task.name,
                                                            totalHours: (() => {
                                                                let total = 0;
                                                                task.timeEntries.forEach(entry => {
                                                                    if (entry.time) total += parseFloat(entry.time) || 0;
                                                                });
                                                                return total;
                                                            })()
                                                        }))
                                                    }];

                                                    return (
                                                        <>
                                                            <div className="mb-4">
                                                                <h6 className="mb-3 fw-bold text-success">{t('reports.taskDistribution')}</h6>
                                                                <TaskDistributionPieChart userReports={userReports} />
                                                            </div>

                                                            <div className="mb-4">
                                                                <h6 className="mb-3 fw-bold text-info">{t('reports.hoursByAccount')}</h6>
                                                                <AccountHoursChart userReports={userReports} />
                                                            </div>
                                                        </>
                                                    );
                                                })()}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Modal Footer */}
                        <div className="modal-footer bg-light border-top-0 py-3">
                            <small className="text-muted me-auto">
                                💡 {t('reports.truncatedNamesTip')}
                            </small>
                            <button
                                type="button"
                                className="btn btn-primary px-4"
                                onClick={closeDetails}
                                style={{ borderRadius: '8px' }}
                            >
                                {t('common.buttons.done')}
                            </button>
                        </div>
                    </div>
                </Modal>
            ) : ''}
        </div>
    )
}

function UserTaskReportTable({ report, reportView, fromDate, variant = 'admin' }) {
    const { t } = useTranslation();
    const { showError } = useToast();
    const [viewMode, setViewMode] = useState('table');
    const isSelf = variant === 'self';
    const titleKey = isSelf ? 'reports.taskTimeBreakdown' : 'reports.userTaskTimeSummary';

    if (report == null)
        return null;

    var rows = [];
    var workableHours = report.workableHours;
    var fromDateValue = report.fromDate;
    var toDateValue = report.toDate;
    if (report.userReports != null) {
        report.userReports.forEach(function (userReport) {
            rows.push(
                <UserReportRows
                    key={userReport.userId}
                    userReport={userReport}
                    workableHours={workableHours}
                    reportView={reportView}
                    fromDate={fromDateValue}
                    toDate={toDateValue}
                    showError={showError}
                    variant={variant}
                />
            );
        });
    }

    return (
        <div className="col-12">
            <div className="card shadow-sm">
                <div className="card-header bg-success text-white">
                    <div className="d-flex justify-content-between align-items-center">
                        <h5 className="mb-0 fw-bold text-white">{t(titleKey)}</h5>
                        <ChartViewToggle viewMode={viewMode} onViewChange={setViewMode} />
                    </div>
                </div>
                <div className="card-body p-0">
                    {viewMode === 'table' ? (
                        <div className="table-responsive">
                            <table className="table table-hover mb-0">
                                {isSelf ? <UserReportSummaryTable report={report} /> : null}
                                {rows}
                            </table>
                        </div>
                    ) : (
                        <div className="p-3">
                            <div className="row">
                                {!isSelf ? (
                                    <div className="col-lg-6 mb-4">
                                        <h6 className="mb-3 fw-bold text-primary">{t('reports.userHoursOverview')}</h6>
                                        <UserTaskBarChart userReports={report.userReports} />
                                    </div>
                                ) : null}
                                <div className="col-lg-6 mb-4">
                                    <h6 className="mb-3 fw-bold text-success">{t('reports.taskDistribution')}</h6>
                                    <TaskDistributionPieChart userReports={report.userReports} />
                                </div>
                                {isSelf ? (
                                    <div className="col-lg-6 mb-4">
                                        <h6 className="mb-3 fw-bold text-info">{t('reports.hoursByAccount')}</h6>
                                        <AccountHoursChart userReports={report.userReports} />
                                    </div>
                                ) : null}
                            </div>
                            {!isSelf ? (
                                <div className="row">
                                    <div className="col-12">
                                        <h6 className="mb-3 fw-bold text-info">{t('reports.hoursByAccount')}</h6>
                                        <AccountHoursChart userReports={report.userReports} />
                                    </div>
                                </div>
                            ) : null}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default UserTaskReportTable;