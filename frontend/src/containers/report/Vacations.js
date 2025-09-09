import React, { useState, useEffect, useCallback } from "react";
import TimeService from '../../service/TimeService';
import * as Constants from '../../common/Constants';
import { useToast } from '../../components/Toast';

function VacationTableEntry({ vacationsDay }) {
    if (vacationsDay == null) return null;

    let cellClasses = "text-center vacation-day-cell";
    let cellStyle = { 
        minWidth: '30px', 
        width: '30px',
        height: '35px', 
        padding: '4px',
        position: 'relative'
    };
    let tooltipText = '';
    let displayContent = '';

    if (vacationsDay.vacation) {
        cellClasses += " bg-success text-white fw-bold";
        displayContent = 'V';
        tooltipText = `Vacation: ${vacationsDay.day.date}`;
    } else if (vacationsDay.day.weekend) {
        cellClasses += " bg-secondary text-white";
        displayContent = 'W';
        tooltipText = `Weekend: ${vacationsDay.day.date}`;
    } else if (vacationsDay.day.majorHoliday) {
        cellClasses += " bg-warning text-dark fw-bold";
        displayContent = 'H';
        tooltipText = `Holiday: ${vacationsDay.day.date}`;
    } else {
        cellClasses += " bg-light border";
        displayContent = '';
        tooltipText = `Work day: ${vacationsDay.day.date}`;
    }

    return (
        <td 
            className={cellClasses}
            style={cellStyle}
            name={vacationsDay.day.date}
            title={tooltipText}
        >
            <div className="d-flex align-items-center justify-content-center h-100">
                <small className="fw-bold" style={{ fontSize: '11px' }}>
                    {displayContent}
                </small>
            </div>
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
    var userNameShort = userName.length > 25 
        ? userName.substring(0, 22) + '...' 
        : userName;

    const vacationCount = userVacation.noVacationDays;

    return (
        <tr key={keyBase} className="vacation-row">
            <td 
                key={keyBase + '-0'} 
                className="text-start fw-medium align-middle ps-3"
                title={userName}
                style={{ 
                    minWidth: '200px',
                    width: '200px',
                    maxWidth: '200px'
                }}
            >
                {userNameShort}
            </td>
            <td 
                key={keyBase + '-1'} 
                className="text-center align-middle fw-bold text-primary"
                style={{
                    minWidth: '80px',
                    width: '80px'
                }}
            >
                {vacationCount}
            </td>
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
            let headerClass = "text-center fw-bold text-white";
            let dayNumber = day.day;
            
            var key = 'header-' + i;
            columns.push(
                <th key={key} className={headerClass} style={{ 
                    minWidth: '30px', 
                    width: '30px',
                    padding: '8px 4px',
                    fontSize: '12px'
                }}>
                    {dayNumber}
                </th>);
            i++;
        });
    }

    return (
        <tr key="0" className="vacation-header">
            <th 
                key="header-0" 
                className="fw-bold text-white text-start ps-3" 
                style={{ 
                    minWidth: '200px',
                    width: '200px',
                    maxWidth: '200px'
                }}
            >
                Employee
            </th>
            <th 
                key="header-1" 
                className="fw-bold text-white text-center" 
                style={{
                    minWidth: '80px',
                    width: '80px'
                }}
            >
                Days
            </th>
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
        <div className="vacation-table-container" style={{ width: '100%' }}>
            <table className="table table-sm table-bordered table-hover mb-0 w-100" style={{ tableLayout: 'fixed' }}>
                <thead className="bg-primary text-white sticky-top">
                    <VacationTableHeaderRow days={vacations.days} />
                </thead>
                <tbody>
                    {rows}
                </tbody>
            </table>
        </div>
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
            {/* Header Section */}
            <div className="row mb-4">
                <div className="col-12">
                    <div className="card shadow-sm">
                        <div className="card-header bg-primary text-white">
                            <div className="row align-items-center">
                                <div className="col-md-6">
                                    <h2 className="mb-0 fw-bold">
                                        🏖️ Vacation Calendar
                                    </h2>
                                </div>
                                <div className="col-md-6">
                                    <div className="d-flex justify-content-end">
                                        <div className="btn-group" role="group" aria-label="Navigation">
                                            <button 
                                                className="btn btn-outline-light" 
                                                name={vacations.firstDate} 
                                                onClick={loadPreviousVacations} 
                                                title="Previous Month"
                                            >
                                                ← Previous
                                            </button>
                                            <button 
                                                className="btn btn-outline-light" 
                                                name={vacations.lastDate} 
                                                onClick={loadNextVacations} 
                                                title="Next Month"
                                            >
                                                Next →
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div className="card-body">
                            <div className="row align-items-center">
                                <div className="col-md-6">
                                    <span className="badge bg-secondary fs-6 py-2 px-3">
                                        📅 {vacations.firstDate} - {vacations.lastDate}
                                    </span>
                                </div>
                                <div className="col-md-6">
                                    <div className="d-flex justify-content-end gap-3 small">
                                        <span><span className="badge bg-success me-1">V</span> Vacation</span>
                                        <span><span className="badge bg-warning text-dark me-1">H</span> Holiday</span>
                                        <span><span className="badge bg-secondary me-1">W</span> Weekend</span>
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