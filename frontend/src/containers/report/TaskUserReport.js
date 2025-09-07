
import React from "react";
import *  as Constants from '../../common/Constants';

class TimeReportTableEntry extends React.Component {
    constructor(props) {
        super(props);
        this.state = { timeReportDay: this.props.timeReportDay };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null) return null;

        var backGroundColor = Constants.CLOSED_COLOR;
        if (this.state.timeReportDay.day.weekend) {
            backGroundColor = Constants.WEEKEND_COLOR;
        } else if (this.state.timeReportDay.day.majorHoliday) {
            backGroundColor = Constants.WEEKEND_COLOR;
        } else {
            backGroundColor = Constants.DAY_COLOR;
        }

        var time = (this.state.timeReportDay.time == null || this.state.timeReportDay.time == 0 ? '' : this.state.timeReportDay.time);

        return (
            <td><input style={{ backgroundColor: backGroundColor }} className="time" readOnly={true} type="text" value={time} /></td>
        );
    }
}

class TaskUserTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { timeReporttask: this.props.timeReporttask, totaltaskTime: this.props.totaltaskTime };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null) return null;
        var entries = [];
        if (this.state.timeReporttask != null) {
            this.state.timeReporttask.timeReportDays.forEach(function (timeReportDay) {
                entries.push(
                    <TimeReportTableEntry timeReportDay={timeReportDay} />);
            });
        }

        var accountName = this.state.timeReporttask.task.account.name;
        var accountShortName = accountName.substring(0, Math.min(20, accountName.length));
        var taskName = this.state.timeReporttask.task.name;
        var taskShortName = taskName.substring(0, Math.min(20, taskName.length));

        return (
            <tr>
                <th className="text-nowrap" title={accountName}>{accountShortName}</th>
                <th className="text-nowrap" title={taskName}>{taskShortName}</th>
                <th><input className="time" readOnly={true} name={this.state.timeReporttask.task.name} type="text" value={this.state.totaltaskTime} /></th>
                {entries}
            </tr>
        );
    }
}


class TaskUserReportRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { taskUserReport: this.props.taskUserReport };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null)
            return null;

        var rows = [];

        var accountName = this.state.taskUserReport.accountName;
        var accountShortName = accountName.substring(0, Math.min(30, accountName.length));
        var taskName = this.state.taskUserReport.taskName;
        var taskShortName = taskName.substring(0, Math.min(30, taskName.length));
        var columnNameShortName = accountShortName + "/" + taskShortName;
        var columnName = accountName + "/" + taskName;

        rows.push(<tr className="bg-success text-white">
            <th className="w-25" title={columnName}>{columnNameShortName}</th>
            <th className="w-25">{this.state.taskUserReport.totalHours} (hours)</th>
            <th className="w-50">{this.state.taskUserReport.totalDaysScaled} (days)</th>
        </tr>);

        this.state.taskUserReport.taskUserUserReports.forEach(function (userReport) {
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
}

export default class TaskUserReportTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { report: this.props.report };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null)
            return null;

        var rows = [];

        if (this.state.report.taskUserReports != null) {
            this.state.report.taskUserReports.forEach(function (taskUserReport) {
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
}
