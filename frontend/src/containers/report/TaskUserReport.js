
import React from "react";
import *  as Constants from '../../common/Constants';

function TimeReportTableEntry({ timeReportDay }) {
    if (timeReportDay == null) return null;

    var backGroundColor = Constants.DAY_COLOR;
    if (timeReportDay.day.weekend) {
        backGroundColor = Constants.WEEKEND_COLOR;
    } else if (timeReportDay.day.majorHoliday) {
        backGroundColor = Constants.MAJOR_HOLIDAY_COLOR;
    } else {
        backGroundColor = Constants.DAY_COLOR;
    }

    var time = (timeReportDay.time == null || timeReportDay.time == 0 ? '' : timeReportDay.time);

    return (
        <td style={{ padding: "0px" }}>
            <input 
                style={{ backgroundColor: backGroundColor, width: '40px', fontSize: '0.75rem' }} 
                className="time text-center" 
                readOnly={true} 
                type="text" 
                value={time} 
            />
        </td>
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
