import React from "react";
import TimeService from '../../service/TimeService';
import * as Constants from '../../common/Constants';
import ReportService from '../../service/ReportService';

class TimeReportTableEntry extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.addUpdate = this.addUpdate.bind(this);
        this.isTextAllowed = this.isTextAllowed.bind(this);
        this.handleError = this.handleError.bind(this);
        this.timeChanged = this.timeChanged.bind(this);
        this.state = { timeReportDay: this.props.timeReportDay, fieldError: false };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    timeChanged(timeReportDay, id) {
        timeReportDay.id = id;
        this.props.timeChanged(timeReportDay);
    }

    isTextAllowed(text) {
        if (text == null) {
            return true;
        }

        var number = parseFloat(text);

        if (isNaN(number) && !isFinite(text)) {
            return false;
        }

        if (number < 0 || number > 24) {
            return false;
        }

        var index = text.indexOf('.');
        if (index !== -1) {
            var decimals = text.substring(index, text.length - 1);
            return decimals.length === 0 || decimals.length === 1 || decimals.length === 2;
        }

        return true;
    }

    addUpdate(event) {
        if (this.state.timeReportDay.closed) {
            return;
        }

        var value = event.target.value;

        var isInputOk = this.isTextAllowed(value);
        if (!isInputOk) {
            return;
        }

        var handleError = this.handleError;
        var timeChanged = this.timeChanged;
        var timeReportDay = this.state.timeReportDay;


        TimeService.updateTime(this.state.timeReportDay)
            .then(response => {
                timeChanged(timeReportDay, response.data);
            })
            .catch(error => {
                handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    handleError(status, error) {
        if (status === 400 && error != null) {
            alert(error);
        } else if (status === 500) {
            alert("Internal server error:\n" + error);
        } else {
            alert("Error:\n" + error);
        }
    }

    handleChange(event) {
        if (this.state.timeReportDay.closed) {
            return;
        }

        var value = event.target.value;
        if (value != null) {
            value = value.replace(",", ".")
        }

        var fieldError = !this.isTextAllowed(value);

        let timeReportDay = JSON.parse(JSON.stringify(this.state.timeReportDay));
        timeReportDay['time'] = value;
        this.setState(() => ({ timeReportDay: timeReportDay, fieldError: fieldError }));
    }

    render() {
        if (this.state == null) return null;

        var isClosed = this.state.timeReportDay.closed;
        var backGroundColor = '';
        if (isClosed) {
            backGroundColor = Constants.CLOSED_COLOR;
            if (this.state.timeReportDay.day.weekend || this.state.timeReportDay.day.majorHoliday) {
                backGroundColor = Constants.CLOSED_WEEK_END_COLOR;
            }
        } else if (this.state.timeReportDay.day.weekend) {
            backGroundColor = Constants.WEEKEND_COLOR;
        } else if (this.state.timeReportDay.day.majorHoliday) {
            backGroundColor = Constants.WEEKEND_COLOR;
        } else {
            backGroundColor = Constants.DAY_COLOR;
        }

        var time = (this.state.timeReportDay.time == null || this.state.timeReportDay.time === 0 ? '' : this.state.timeReportDay.time);
        var classes = "time " + (this.state.fieldError ? "border border-danger" : '');
        var inputName = JSON.stringify(this.state.timeReportDay.day.date);

        const inputStyle = {
            backgroundColor: backGroundColor,
            width: '55px'
        }

        return (
            <td key={this.props.id} style={{ padding: "0px" }}>
                <input style={inputStyle} className={classes} readOnly={isClosed} name={inputName} type="text" value={time} maxLength="5" onChange={this.handleChange} onBlur={this.addUpdate} />
            </td>
        );
    }
};

class TimeReportTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.timeChanged = this.timeChanged.bind(this);
        this.state = { timeReportProject: this.props.timeReportProject, totalProjectTime: this.props.totalProjectTime };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    timeChanged(timeReportDay) {
        this.props.timeChanged(timeReportDay);
    }

    render() {
        if (this.state == null) return null;

        var keyBase = this.state.timeReportProject.project.id;

        var timeChanged = this.timeChanged;
        var entries = [];
        if (this.state.timeReportProject != null) {
            var i = 3;
            this.state.timeReportProject.timeReportDays.forEach(function (timeReportDay) {
                var key = keyBase + '-' + i;
                entries.push(
                    <TimeReportTableEntry key={key} timeReportDay={timeReportDay} timeChanged={timeChanged} />);
                i++;
            });
        }

        var companyName = this.state.timeReportProject.project.company.name;
        var companyShortName = companyName.substring(0, Math.min(10, companyName.length));
        var projectName = this.state.timeReportProject.project.name;
        var projectShortName = projectName.substring(0, Math.min(10, projectName.length));

        return (
            <tr key={keyBase}>
                <th key={keyBase + '-0'} className="text-nowrap" title={companyName}>{companyShortName}</th>
                <th key={keyBase + '-1'} className="text-nowrap" title={projectName}>{projectShortName}</th>
                <th key={keyBase + '-2  '}><input className="time" style={{ width: "65px" }} readOnly={true} name={this.state.timeReportProject.project.name} type="text" value={this.state.totalProjectTime} /></th>
                {entries}
            </tr>
        );
    }
};

class TimeReportTableHeaderRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { days: this.props.days };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null || this.state.days == null)
            return null;

        var columns = [];
        if (this.state.days != null) {
            var i = 3;
            this.state.days.forEach(function (day) {
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
                <th key="header-0"><font color={Constants.DAY_COLOR}>Company</font></th>
                <th key="header-1"><font color={Constants.DAY_COLOR}>Project</font></th>
                <th key="header-2"><font color={Constants.DAY_COLOR}>Time</font></th>
                {columns}
            </tr>
        );
    }
};

class TimeReportTableFooterRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { days: this.props.days, time: this.props.time, label: this.props.label };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null || this.state.days == null)
            return null;

        var columns = [];
        if (this.state.days != null) {
            var i = 3;
            var baseKey = this.props.id + '-';
            this.state.days.forEach(function (day) {
                var key = baseKey + i;
                columns.push(<th key={key}></th>);
                i++;
            });
        }

        return (
            <tr key={this.props.id} className="table-secondary">
                <th key={this.props.id + '-0'}></th>
                <th key={this.props.id + '-1'} className="text-nowrap">{this.state.label}</th>
                <th key={this.props.id + '-2'}>{this.state.time}</th>
                {columns}
            </tr>
        );
    }
};

class TimeReportTable extends React.Component {
    constructor(props) {
        super(props);
        this.timeChanged = this.timeChanged.bind(this);
        this.updateTime = this.updateTime.bind(this);
        this.state = { timeReport: this.props.timeReport };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    updateTime(timeReportDayUpdated, timeReportProjects) {
        for (var timeReportProjectKey in timeReportProjects) {
            var timeReportProject = timeReportProjects[timeReportProjectKey];
            for (var timeReportDayKey in timeReportProject.timeReportDays) {
                var timeReportDay = timeReportProject.timeReportDays[timeReportDayKey];
                if (timeReportDay.idAssignment === timeReportDayUpdated.idAssignment &&
                    timeReportDay.day.date === timeReportDayUpdated.day.date) {
                    timeReportDay.time = timeReportDayUpdated.time;
                }
            }
        }
    }

    timeChanged(timeReportDayUpdated) {
        this.updateTime(timeReportDayUpdated, this.state.timeReport.timeReportProjectsInternal);
        this.updateTime(timeReportDayUpdated, this.state.timeReport.timeReportProjectsExternal);
        this.setState({ timeReport: this.state.timeReport });
    }

    render() {
        if (this.state == null || this.state.timeReport == null) return null;

        var headerRow = <TimeReportTableHeaderRow days={this.state.timeReport.days} />;

        var rows = [];
        var timeChanged = this.timeChanged;
        var totalTimeProvision = 0;
        var totalTimeNoProvision = 0;

        if (this.state.timeReport.timeReportProjectsExternal != null) {
            this.state.timeReport.timeReportProjectsExternal.forEach(function (timeReportProject) {
                var totalProjectTime = 0;
                var reportProject = timeReportProject;
                timeReportProject.timeReportDays.forEach(function (timeReportDay) {
                    if (timeReportDay.time != null) {
                        var time = parseFloat(timeReportDay.time);
                        if (!isNaN(time)) {
                            totalProjectTime += time;

                            if (reportProject.project.provision) {
                                totalTimeProvision += time;
                            } else {
                                totalTimeNoProvision += time;
                            }
                        }
                    }
                });

                rows.push(
                    <TimeReportTableRow key={timeReportProject.project.id} timeReportProject={timeReportProject} totalProjectTime={totalProjectTime} timeChanged={timeChanged} />);
            });
        }

        if (this.state.timeReport.timeReportProjectsInternal != null) {
            this.state.timeReport.timeReportProjectsInternal.forEach(function (timeReportProject) {
                var totalProjectTime = 0;
                var reportProject = timeReportProject;
                timeReportProject.timeReportDays.forEach(function (timeReportDay) {
                    if (timeReportDay.time != null) {
                        var time = parseFloat(timeReportDay.time);
                        if (!isNaN(time)) {
                            totalProjectTime += time;
                            if (reportProject.project.provision) {
                                totalTimeProvision += time;
                            } else {
                                totalTimeNoProvision += time;
                            }
                        }
                    }
                });

                rows.push(
                    <TimeReportTableRow key={timeReportProject.project.id} timeReportProject={timeReportProject} totalProjectTime={totalProjectTime} timeChanged={timeChanged} />);
            });
        }

        var totalTime = totalTimeProvision + totalTimeNoProvision;

        rows.push(<TimeReportTableFooterRow key="footer1" days={this.state.timeReport.days} time={totalTimeProvision} label='Sum provision' />);
        rows.push(<TimeReportTableFooterRow key="footer2" days={this.state.timeReport.days} time={totalTimeNoProvision} label='Sum no provision' />);
        rows.push(<TimeReportTableFooterRow key="footer3" days={this.state.timeReport.days} time={totalTime} label='Sum total' />);
        rows.push(<TimeReportTableFooterRow key="footer4" days={this.state.timeReport.days} time={this.state.timeReport.workableHours} label='Workable hours' />);

        if (this.state.reportView === Constants.MONTH_VIEW && this.state.timeReport.user.userCategory === Constants.EMPLOYEE_CATEGORY) {
            var provisionTime = Math.ceil(totalTimeProvision - this.state.timeReport.workableHours * 0.9);
            if (provisionTime < 0) {
                provisionTime = 0;
            }
            rows.push(<TimeReportTableFooterRow key="footer5" days={this.state.timeReport.days} time={provisionTime} label='Provision hours' />);
        }

        return (
            <table className="table-sm">
                <thead className="bg-success">
                    {headerRow}
                </thead>
                <tbody>
                    {rows}
                </tbody>
            </table>
        );
    }
};


export default class Times extends React.Component {

    constructor(props) {
        super(props);
        this.loadCurrentTimes = this.loadCurrentTimes.bind(this);
        this.loadPreviousTimes = this.loadPreviousTimes.bind(this);
        this.loadNextTimes = this.loadNextTimes.bind(this);
        this.closeReport = this.closeReport.bind(this);
        this.viewChange = this.viewChange.bind(this);

        this.state = { reportView: Constants.MONTH_VIEW };
    }

    componentDidMount() {
        this.loadCurrentTimes(Constants.MONTH_VIEW);
    }

    loadCurrentTimes(view) {
        var self = this;
        TimeService.getTimes(view)
            .then(response => {
                self.setState({ timeReport: response.data, reportView: view });
            })
            .catch(error => {
                alert('Failed to load time report');
            });
    }

    loadPreviousTimes(event) {
        const date = event.target.name;
        var self = this;

        TimeService.getPreviousTimes(this.state.reportView, date)
            .then(response => {
                self.setState({ timeReport: response.data, reportView: self.state.reportView });
            })
            .catch(error => {
                alert('Failed to load time report');
            });
    }

    loadNextTimes(event) {
        const date = event.target.name;
        var self = this;

        TimeService.getNextTimes(this.state.reportView, date)
            .then(response => {
                self.setState({ timeReport: response.data, reportView: self.state.reportView });
            })
            .catch(error => {
                alert('Failed to load time report');
            });
    }

    closeReport(event) {
        const shallClose = confirm('Are you really sure you want close this month?\nYou will not be able to open it again.');
        if (!shallClose) {
            return;
        }
        const date = event.target.name;
        var self = this;

        var payLoad = '{ "idUser": "' + this.state.timeReport.user.id + '", "closeDate": "' + date + '"}';
        var service = new ReportService();
        service.updateOpenCloseReport(payLoad, 'close')
            .then(response => {
                self.loadCurrentTimes(Constants.MONTH_VIEW);
            })
            .catch(error => {
                alert('Failed to close report');
            });
    }

    viewChange(event) {
        const view = event.target.value;
        this.loadCurrentTimes(view);
    }

    render() {
        if (this.state == null || this.state.timeReport == null) return null;

        return (
            <div className="container-fluid ml-4 mr-4">
                <div className="row mb-3">
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.timeReport.firstDate} onClick={this.loadPreviousTimes}>&lt;&lt;</button>
                    </div>
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.timeReport.lastDate} onClick={this.loadNextTimes}>&gt;&gt;</button>
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" value={this.state.reportView} name="reportView" onChange={this.viewChange}>
                            <option value={Constants.DAY_VIEW}>Day</option>
                            <option value={Constants.WEEK_VIEW}>Week</option>
                            <option value={Constants.MONTH_VIEW}>Month</option>
                        </select>
                    </div>
                    <div className="col-sm-6">
                        {this.state.reportView === Constants.MONTH_VIEW && !this.state.timeReport.closed ? (
                            <span className="float-right">
                                <button className="btn btn-success" name={this.state.timeReport.firstDate} onClick={this.closeReport}>Close report</button>
                            </span>
                        ) : ''}
                    </div>
                    <div className="col-sm-2">
                        <span className="float-right">
                            <b>{this.state.timeReport.firstDate} - {this.state.timeReport.lastDate}</b>
                        </span>
                    </div>
                </div>
                <div className="row">
                    <div className="table-responsive">
                        <TimeReportTable timeReport={this.state.timeReport} />
                    </div>
                </div>
            </div >
        );
    }
};