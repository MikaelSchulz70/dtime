import React, { useState, useEffect, useCallback } from "react";
import TimeService from '../../service/TimeService';
import * as Constants from '../../common/Constants';
import { useToast } from '../../components/Toast';

function VacationTableEntry({ vacationsDay }) {
    if (vacationsDay == null) return null;

    var backGroundColor = '';
    if (vacationsDay.vacation) {
        backGroundColor = Constants.ALERT_COLOR;
    } else if (vacationsDay.day.weekend) {
        backGroundColor = Constants.WEEKEND_COLOR;
    } else if (vacationsDay.day.majorHoliday) {
        backGroundColor = Constants.WEEKEND_COLOR;
    } else {
        backGroundColor = Constants.DAY_COLOR;
    }

    const inputStyle = {
        backgroundColor: backGroundColor,
        minWidth: '30px'
    }

    return (
        <td style={inputStyle} name={vacationsDay.day.date} >
        </td>
    );
}

function VacationTableRow({ userVacation }) {
    if (userVacation == null) return null;

    var keyBase = userVacation.userId;

    var entries = [];
    var i = 3;
    userVacation.vacationsDays.forEach(function (vacationsDay) {
        var key = keyBase + '-' + i;
        entries.push(
            <VacationTableEntry key={key} vacationsDay={vacationsDay} />);
        i++;
    });

    var userName = userVacation.name;
    var userNameShort = userName.substring(0, Math.min(15, userName.length));

    return (
        <tr key={keyBase}>
            <th key={keyBase + '-0'} className="text-nowrap fw-medium" title={userName}>{userNameShort}</th>
            <td key={keyBase + '-1'} className="text-center fw-bold text-primary">{userVacation.noVacationDays}</td>
            {entries}
        </tr>
    );
}

function VacationTableHeaderRow({ days }) {
    if (days == null) return null;

    var columns = [];
    if (days != null) {
        var i = 2;
        days.forEach(function (day) {
            var backGroundColor = '';
            if (day.weekend) {
                backGroundColor = Constants.WEEKEND_COLOR;
            } else if (day.majorHoliday) {
                backGroundColor = Constants.WEEKEND_COLOR;
            } else {
                backGroundColor = Constants.DAY_COLOR;
            }

            var key = 'header-' + i;
            columns.push(
                <th key={key}><font color={backGroundColor}>{day.day}</font></th>);
            i++;
        });
    }

    return (
        <tr key="0">
            <th key="header-0" className="fw-bold">👤 Employee</th>
            <th key="header-1" className="fw-bold">📊 Vacation Days</th>
            {columns}
        </tr>
    );
}

function VacationTable({ vacations }) {
    if (vacations == null) return null;

    var rows = [];
    if (vacations.userVacations != null) {
        vacations.userVacations.forEach(function (userVacation) {
            rows.push(
                <VacationTableRow key={userVacation.userId} userVacation={userVacation} />);
        });
    }

    return (
        <table className="table table-sm table-bordered table-hover mb-0">
            <thead className="bg-primary text-white">
                <VacationTableHeaderRow days={vacations.days} />
            </thead>
            <tbody>
                {rows}
            </tbody>
        </table>
    );
}


export default function Vacations(props) {
    const [vacations, setVacations] = useState(null);
    const { showError } = useToast();

    const loadCurrentVacations = useCallback(() => {
        var timeService = new TimeService();
        timeService.getVacations()
            .then(response => {
                setVacations(response.data);
            })
            .catch(error => {
                showError?.('Failed to load vacations') || alert('Failed to load vacations');
            });
    }, [showError]);

    const loadPreviousVacations = useCallback((event) => {
        const date = event.target.name;
        var timeService = new TimeService();

        timeService.getPreviousVacations(date)
            .then(response => {
                setVacations(response.data);
            })
            .catch(error => {
                showError?.('Failed to load vacations') || alert('Failed to load vacations');
            });
    }, [showError]);

    const loadNextVacations = useCallback((event) => {
        const date = event.target.name;
        var timeService = new TimeService();

        timeService.getNextVacations(date)
            .then(response => {
                setVacations(response.data);
            })
            .catch(error => {
                showError?.('Failed to load time vacations') || alert('Failed to load time vacations');
            });
    }, [showError]);

    useEffect(() => {
        loadCurrentVacations();
    }, [loadCurrentVacations]);

    if (vacations == null) return null;

    return (
        <div className="container-fluid p-4">
            <div className="card shadow-sm mb-4">
                <div className="card-header bg-primary text-white">
                    <h2 className="mb-0 fw-bold">🏖️ Vacation Calendar</h2>
                </div>
                <div className="card-body">
                    <div className="row mb-3 align-items-center">
                        <div className="col-sm-2">
                            <div className="btn-group" role="group" aria-label="Navigation">
                                <button className="btn btn-outline-primary" name={vacations.firstDate} onClick={loadPreviousVacations} title="Previous Month">
                                    ← Previous
                                </button>
                                <button className="btn btn-outline-primary" name={vacations.lastDate} onClick={loadNextVacations} title="Next Month">
                                    Next →
                                </button>
                            </div>
                        </div>
                        <div className="col-sm-10">
                            <div className="text-end">
                                <span className="badge bg-secondary fs-6 py-2 px-3">
                                    📅 {vacations.firstDate} - {vacations.lastDate}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div className="row">
                <div className="col-12">
                    <div className="card shadow-sm">
                        <div className="card-body">
                            <div className="table-responsive">
                                <VacationTable vacations={vacations} />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div >
    );
}