
import React from "react";
import *  as Constants from '../../common/Constants';

function TimeReportTableEntry({ timeReportDay }) {
    if (timeReportDay == null) return null;

    var backGroundColor = Constants.CLOSED_COLOR;
    if (timeReportDay.day.weekend) {
        backGroundColor = Constants.WEEKEND_COLOR;
    } else if (timeReportDay.day.majorHoliday) {
        backGroundColor = Constants.WEEKEND_COLOR;
    } else {
        backGroundColor = Constants.DAY_COLOR;
    }

    var time = (timeReportDay.time == null || timeReportDay.time == 0 ? '' : timeReportDay.time);

    return (
        <td><input style={{ backgroundColor: backGroundColor }} className="time" readOnly={true} type="text" value={time} /></td>
    );
}

function TaskUserTableRow({ timeReporttask, totaltaskTime }) {
    if (timeReporttask == null) return null;
    var entries = [];
    if (timeReporttask != null) {
        timeReporttask.timeReportDays.forEach(function (timeReportDay) {
            entries.push(
                <TimeReportTableEntry timeReportDay={timeReportDay} />);
        });
    }

    var accountName = timeReporttask.task.account.name;
    var accountShortName = accountName.substring(0, Math.min(20, accountName.length));
    var taskName = timeReporttask.task.name;
    var taskShortName = taskName.substring(0, Math.min(20, taskName.length));

    return (
        <tr>
            <th className="text-nowrap" title={accountName}>{accountShortName}</th>
            <th className="text-nowrap" title={taskName}>{taskShortName}</th>
            <th><input className="time" readOnly={true} name={timeReporttask.task.name} type="text" value={totaltaskTime} /></th>
            {entries}
        </tr>
    );
}


function TaskUserReportRow({ taskUserReport }) {
    if (taskUserReport == null)
        return null;

    var rows = [];

    var accountName = taskUserReport.accountName;
    var accountShortName = accountName.substring(0, Math.min(30, accountName.length));
    var taskName = taskUserReport.taskName;
    var taskShortName = taskName.substring(0, Math.min(30, taskName.length));
    var columnNameShortName = accountShortName + "/" + taskShortName;
    var columnName = accountName + "/" + taskName;

    rows.push(<tr className="bg-success text-white">
        <th className="w-25" title={columnName}>{columnNameShortName}</th>
        <th className="w-25">{taskUserReport.totalHours} (hours)</th>
        <th className="w-50">{taskUserReport.totalDaysScaled} (days)</th>
    </tr>);

    taskUserReport.taskUserUserReports.forEach(function (userReport) {
        var userName = userReport.userName;
        var userShortName = userName.substring(0, Math.min(50, userName.length));

        rows.push(
            <tr>
                <td className="w-25" title={userName}>{userShortName}</td>
                <td className="w-25">{userReport.totalHours}</td>
                <td className="w-50">{userReport.totalDaysScaled}</td>
            </tr>);
    });

    return (
        <tbody>
            {rows}
        </tbody>
    );
}

function TaskUserReportTable({ report }) {
    if (report == null)
        return null;

    var rows = [];

    if (report.taskUserReports != null) {
        report.taskUserReports.forEach(function (taskUserReport) {
            rows.push(<TaskUserReportRow taskUserReport={taskUserReport} />);
        });
    }

    return (
        <div className="table-responsive">
            <table className="table">
                {rows}
            </table>
        </div>
    );
}

export default TaskUserReportTable;
