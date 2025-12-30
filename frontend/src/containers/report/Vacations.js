import React, { useState, useEffect, useCallback } from "react";
import { useTranslation } from 'react-i18next';
import TimeService from '../../service/TimeService';
import * as Constants from '../../common/Constants';
import { useToast } from '../../components/Toast';

function VacationTableEntry({ vacationsDay }) {
    if (vacationsDay == null) return null;

    var backgroundColor = '';
    var displayValue = '';

    if (vacationsDay.vacation) {
        backgroundColor = Constants.BRAND_SUCCESS;
        displayValue = 'V';
    } else if (vacationsDay.day.weekend) {
        backgroundColor = Constants.WEEKEND_COLOR;
        displayValue = '';
    } else if (vacationsDay.day.majorHoliday) {
        backgroundColor = Constants.MAJOR_HOLIDAY_COLOR;
        displayValue = '';
    } else {
        backgroundColor = Constants.DAY_COLOR;
        displayValue = '';
    }

    const inputStyle = {
        backgroundColor: backgroundColor,
        width: '40px'
    }

    return (
        <td style={{ padding: "0px" }}>
            <input
                style={inputStyle}
                className="time"
                readOnly={true}
                type="text"
                value={displayValue}
                title={`${vacationsDay.day.date}`}
            />
        </td>
    );
}

function VacationTableRow({ userVacation }) {
    const { t } = useTranslation();
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
    var userNameShort = userName.length > 25
        ? userName.substring(0, 22) + '...'
        : userName;

    const vacationCount = userVacation.noVacationDays;

    return (
        <tr key={keyBase}>
            <th key={keyBase + '-0'} className="text-nowrap" title={userName}>{userNameShort}</th>
            <th key={keyBase + '-1'}>{t('vacations.labels.vacation')}</th>
            <th key={keyBase + '-2'}><input className="time" style={{ width: "50px" }} readOnly={true} type="text" value={vacationCount} /></th>
            {entries}
        </tr>
    );
}

function VacationTableHeaderRow({ days }) {
    const { t } = useTranslation();
    if (days == null) return null;

    var columns = [];
    if (days != null) {
        var i = 3;
        days.forEach(function (day) {
            var backGroundColor = '';
            if (day.weekend) {
                backGroundColor = Constants.WEEKEND_COLOR;
            } else if (day.majorHoliday) {
                backGroundColor = Constants.MAJOR_HOLIDAY_COLOR;
            } else if (day.halfDay) {
                backGroundColor = Constants.HALF_DAY_COLOR;
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
            <th key="header-0"><font color={Constants.DAY_COLOR}>{t('vacations.labels.employee')}</font></th>
            <th key="header-1"><font color={Constants.DAY_COLOR}>{t('vacations.labels.type')}</font></th>
            <th key="header-2"><font color={Constants.DAY_COLOR}>{t('vacations.labels.days')}</font></th>
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
        <table className="table-sm vacation-table">
            <thead className="bg-success">
                <VacationTableHeaderRow days={vacations.days} />
            </thead>
            <tbody>
                {rows}
            </tbody>
        </table>
    );
}


// Add custom CSS styles
const vacationStyles = `
.vacation-table-container {
    position: relative;
}

.vacation-row:hover {
    background-color: #f8f9fa;
}

.vacation-day-cell {
    border: 1px solid #e9ecef;
    transition: background-color 0.2s ease;
}

.vacation-day-cell:hover {
    opacity: 0.8;
}
`;

// Inject styles
if (typeof document !== 'undefined') {
    const styleSheet = document.createElement('style');
    styleSheet.type = 'text/css';
    styleSheet.innerText = vacationStyles;
    document.head.appendChild(styleSheet);
}

export default function Vacations(props) {
    const { t } = useTranslation();
    const [vacations, setVacations] = useState(null);
    const { showError } = useToast();

    const loadCurrentVacations = useCallback(() => {
        var timeService = new TimeService();
        timeService.getVacations()
            .then(response => {
                setVacations(response.data);
            })
            .catch(error => {
                showError?.(t('vacations.messages.loadFailed')) || alert(t('vacations.messages.loadFailed'));
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
                showError?.(t('vacations.messages.loadFailed')) || alert(t('vacations.messages.loadFailed'));
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
                showError?.(t('vacations.messages.loadFailed')) || alert(t('vacations.messages.loadFailed'));
            });
    }, [showError]);

    useEffect(() => {
        loadCurrentVacations();
    }, [loadCurrentVacations]);

    if (vacations == null) return null;

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>{t('vacations.title')}</h2>
            <div className="row mb-4">
                <div className="col-12">
                    <div className="card shadow-sm">
                        <div className="card-body">
                            <div className="row align-items-center mb-3">
                                <div className="col-md-6">
                                    <div className="d-flex gap-2">
                                        <button
                                            className="btn btn-success btn-sm"
                                            name={vacations.firstDate}
                                            onClick={loadPreviousVacations}
                                            title={t('vacations.navigation.previousMonth')}
                                        >
                                            &lt;&lt;
                                        </button>
                                        <button
                                            className="btn btn-success btn-sm"
                                            name={vacations.lastDate}
                                            onClick={loadNextVacations}
                                            title={t('vacations.navigation.nextMonth')}
                                        >
                                            &gt;&gt;
                                        </button>
                                    </div>
                                </div>
                                <div className="col-md-6">
                                    <div className="d-flex justify-content-end gap-3 small">
                                        <span className="badge bg-secondary fs-6 py-2 px-3">
                                            📅 {vacations.firstDate} - {vacations.lastDate}
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div className="row align-items-center">
                                <div className="col-md-12">
                                    <div className="d-flex justify-content-end gap-3 small">
                                        <span>
                                            <span className="badge me-1" style={{ backgroundColor: Constants.BRAND_SUCCESS, color: 'white' }}>V</span>
                                            {t('vacations.labels.vacation')}
                                        </span>
                                        <span>
                                            <span className="badge me-1" style={{ backgroundColor: Constants.MAJOR_HOLIDAY_COLOR, color: '#0c5460' }}>H</span>
                                            {t('vacations.labels.holiday')}
                                        </span>
                                        <span>
                                            <span className="badge me-1" style={{ backgroundColor: Constants.WEEKEND_COLOR, color: '#856404' }}>W</span>
                                            {t('vacations.labels.weekend')}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Vacation Table Section */}
            <div className="row">
                <div className="col-12">
                    <div className="card shadow-sm">
                        <div className="card-body p-0">
                            <div className="table-responsive">
                                <VacationTable vacations={vacations} />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}