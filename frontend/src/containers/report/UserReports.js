import React, { useState, useEffect, useCallback } from "react";
import ReportService from '../../service/ReportService';
import * as Constants from '../../common/Constants';
import { useToast } from '../../components/Toast';

function UserReportRows({ userReport }) {
    if (userReport == null)
        return null;

    var rows = [];
    var i = 0;
    userReport.taskReports.forEach(function (taskReport) {
        var key = 'row-' + i;
        rows.push(
            <tr key={key}>
                <td className="fw-medium">{taskReport.accountName}</td>
                <td className="fw-medium">{taskReport.taskName}</td>
                <td className="text-end">{taskReport.totalHours} hrs</td>
            </tr>);
        i++;
    });

    var key = 'row-' + i;
    rows.push(<tr key={key} className="table-primary border-top border-2">
        <td className="fw-bold fs-6" colSpan="2">📊 Total Time</td>
        <td className="text-end fw-bold fs-6">{userReport.totalTime} hrs</td>
    </tr>);

    return (
        <tbody>
            {rows}
        </tbody>
    );
}

function UserReportTable({ report }) {
    if (report == null)
        return null;

    var rows = [];
    var i = 0;
    if (report.userReports != null) {
        report.userReports.forEach(function (userReport) {
            rows.push(<UserReportRows key={i} userReport={userReport} />);
            i++;
        });
    }

    return (
        <div className="col-12">
            <div className="card shadow-sm">
                <div className="card-header bg-success">
                    <h5 className="mb-0 fw-bold text-white">📋 Task Time Breakdown</h5>
                </div>
                <div className="card-body p-0">
                    <div className="table-responsive">
                        <table className="table table-hover table-striped mb-0">
                            <tr>
                                <th className="fw-bold">🏢 Account</th>
                                <th className="fw-bold">📋 Task</th>
                                <th className="fw-bold text-end">⏱️ Total Hours</th>
                            </tr>
                            {rows}
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}

function UserReports(props) {
    const [report, setReport] = useState(null);
    const [reportView, setReportView] = useState(Constants.MONTH_VIEW);
    const { showError } = useToast();

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
                showError('Failed to load report: ' + (error.response?.data?.message || error.message));
            });
    }, [showError]);

    const handlePreviousReport = useCallback((event) => {
        const date = event.target.name;

        var service = new ReportService();
        service.getPreviousUserReport(reportView, date)
            .then(response => {
                setReport(response.data);
            })
            .catch(error => {
                showError('Failed to load report: ' + (error.response?.data?.message || error.message));
            });
    }, [reportView, showError]);

    const handleNextReport = useCallback((event) => {
        const date = event.target.name;
        var service = new ReportService();
        service.getNextUserReport(reportView, date)
            .then(response => {
                setReport(response.data);
            })
            .catch(error => {
                showError('Failed to load report: ' + (error.response?.data?.message || error.message));
            });
    }, [reportView, showError]);

    if (report == null)
        return null;

    return (
        <div className="container-fluid p-4">
            <div className="card shadow-sm mb-4">
                <div className="card-header">
                    <h2 className="mb-0 fw-bold">👤 My Time Report</h2>
                </div>
                <div className="card-body">
                    <div className="row mb-3 align-items-center">
                        <div className="col-sm-2">
                            <div className="d-flex gap-2" role="group" aria-label="Navigation">
                                <button className="btn btn-success btn-sm" name={report.fromDate} onClick={handlePreviousReport} title="Previous Period">
                                    &lt;&lt;
                                </button>
                                <button className="btn btn-success btn-sm" name={report.toDate} onClick={handleNextReport} title="Next Period">
                                    &gt;&gt;
                                </button>
                            </div>
                        </div>
                        <div className="col-sm-2">
                            <label className="form-label fw-bold text-muted small">Period Type</label>
                            <select className="form-select form-select-sm" value={reportView} name="reportView" onChange={viewChange}>
                                <option value={Constants.MONTH_VIEW}>📅 Monthly</option>
                                <option value={Constants.YEAR_VIEW}>📆 Yearly</option>
                            </select>
                        </div>
                        <div className="col-sm-3">
                            <label className="form-label fw-bold text-muted small">Workable Hours</label>
                            <div className="badge bg-info fs-6 py-2 px-3">
                                🕰️ {report.workableHours} hours
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
                <UserReportTable report={report} />
            </div>
        </div>
    );
}

export default UserReports;