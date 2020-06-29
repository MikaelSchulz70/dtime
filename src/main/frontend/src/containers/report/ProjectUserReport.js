
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
};

class ProjectUserTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { timeReportProject: this.props.timeReportProject, totalProjectTime: this.props.totalProjectTime };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null) return null;
        var entries = [];
        if (this.state.timeReportProject != null) {
            this.state.timeReportProject.timeReportDays.forEach(function (timeReportDay) {
                entries.push(
                    <TimeReportTableEntry timeReportDay={timeReportDay} />);
            });
        }

        var companyName = this.state.timeReportProject.project.company.name;
        var companyShortName = companyName.substring(0, Math.min(20, companyName.length));
        var projectName = this.state.timeReportProject.project.name;
        var projectShortName = projectName.substring(0, Math.min(20, projectName.length));

        return (
            <tr>
                <th className="text-nowrap" title={companyName}>{companyShortName}</th>
                <th className="text-nowrap" title={projectName}>{projectShortName}</th>
                <th><input className="time" readOnly={true} name={this.state.timeReportProject.project.name} type="text" value={this.state.totalProjectTime} /></th>
                {entries}
            </tr>
        );
    }
};


class ProjectUserReportRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { projectUserReport: this.props.projectUserReport };
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

        var companyName = this.state.projectUserReport.companyName;
        var companyShortName = companyName.substring(0, Math.min(30, companyName.length));
        var projectName = this.state.projectUserReport.projectName;
        var projectShortName = projectName.substring(0, Math.min(30, projectName.length));
        var columnNameShortName = companyShortName + "/" + projectShortName;
        var columnName = companyName + "/" + projectName;

        rows.push(<tr className="bg-success text-white">
            <th className="w-25" title={columnName}>{columnNameShortName}</th>
            <th className="w-25">{this.state.projectUserReport.totalHours} (hours)</th>
            <th className="w-50">{this.state.projectUserReport.totalDaysScaled} (days)</th>
        </tr>);

        this.state.projectUserReport.projectUserUserReports.forEach(function (userReport) {
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
    };
};

export default class ProjectUserReportTable extends React.Component {
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

        if (this.state.report.projectUserReports != null) {
            this.state.report.projectUserReports.forEach(function (projectUserReport) {
                rows.push(<ProjectUserReportRow projectUserReport={projectUserReport} />);
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
};
