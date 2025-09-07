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

        const timeService = new TimeService();
        timeService.updateTime(this.state.timeReportDay)
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
            if (this.state.timeReportDay.day.weekend) {
                backGroundColor = Constants.CLOSED_WEEK_END_COLOR;
            } else if (this.state.timeReportDay.day.majorHoliday) {
                backGroundColor = Constants.CLOSED_MAJOR_HOLIDAY_COLOR;
            } else if (this.state.timeReportDay.day.halfDay) {
                backGroundColor = Constants.CLOSED_HALF_DAY_COLOR;
            }
        } else if (this.state.timeReportDay.day.weekend) {
            backGroundColor = Constants.WEEKEND_COLOR;
        } else if (this.state.timeReportDay.day.majorHoliday) {
            backGroundColor = Constants.MAJOR_HOLIDAY_COLOR;
        } else if (this.state.timeReportDay.day.halfDay) {
            backGroundColor = Constants.HALF_DAY_COLOR;
        } else {
            backGroundColor = Constants.DAY_COLOR;
        }

        var time = (this.state.timeReportDay.time == null || this.state.timeReportDay.time === 0 ? '' : this.state.timeReportDay.time);
        var classes = "time " + (this.state.fieldError ? "border border-danger" : '');
        var inputName = JSON.stringify(this.state.timeReportDay.day.date);

        const inputStyle = {
            backgroundColor: backGroundColor,
            width: '40px'
        }

        return (
            <td key={this.props.id} style={{ padding: "0px" }}>
                <input style={inputStyle} className={classes} readOnly={isClosed} name={inputName} type="text" value={time} maxLength="5" onChange={this.handleChange} onBlur={this.addUpdate} />
            </td>
        );
    }
}

class TimeReportTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.timeChanged = this.timeChanged.bind(this);
        this.state = { timeReportTask: this.props.timeReportTask, totalTaskTime: this.props.totalTaskTime };
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

        var keyBase = this.state.timeReportTask.task.id;

        var timeChanged = this.timeChanged;
        var entries = [];
        if (this.state.timeReportTask != null) {
            var i = 3;
            this.state.timeReportTask.timeEntries.forEach(function (timeReportDay) {
                var key = keyBase + '-' + i;
                entries.push(
                    <TimeReportTableEntry key={key} timeReportDay={timeReportDay} timeChanged={timeChanged} />);
                i++;
            });
        }

        var accountName = this.state.timeReportTask.task.account.name;
        var accountShortName = accountName.substring(0, Math.min(8, accountName.length));
        var taskName = this.state.timeReportTask.task.name;
        var taskShortName = taskName.substring(0, Math.min(8, taskName.length));

        return (
            <tr key={keyBase}>
                <th key={keyBase + '-0'} className="text-nowrap" title={accountName}>{accountShortName}</th>
                <th key={keyBase + '-1'} className="text-nowrap" title={taskName}>{taskShortName}</th>
                <th key={keyBase + '-2  '}><input className="time" style={{ width: "50px" }} readOnly={true} name={this.state.timeReportTask.task.name} type="text" value={this.state.totalTaskTime} /></th>
                {entries}
            </tr>
        );
    }
}

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
                <th key="header-0"><font color={Constants.DAY_COLOR}>Account</font></th>
                <th key="header-1"><font color={Constants.DAY_COLOR}>Task</font></th>
                <th key="header-2"><font color={Constants.DAY_COLOR}>Time</font></th>
                {columns}
            </tr>
        );
    }
}

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
}

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

    updateTime(timeReportDayUpdated, timeReportTasks) {
        for (var timeReportTaskKey in timeReportTasks) {
            var timeReportTask = timeReportTasks[timeReportTaskKey];
            for (var timeReportEntryKey in timeReportTask.timeEntries) {
                var timeReportEntry = timeReportTask.timeEntries[timeReportEntryKey];
                if (timeReportEntry.taskContributorId === timeReportDayUpdated.taskContributorId &&
                    timeReportEntry.day.date === timeReportDayUpdated.day.date) {
                    timeReportEntry.time = timeReportDayUpdated.time;
                }
            }
        }
    }

    timeChanged(timeReportDayUpdated) {
        this.updateTime(timeReportDayUpdated, this.state.timeReport.timeReportTasks);
        this.setState({ timeReport: this.state.timeReport });
    }

    render() {
        if (this.state == null || this.state.timeReport == null) return null;

        var headerRow = <TimeReportTableHeaderRow days={this.state.timeReport.days} />;

        var rows = [];
        var timeChanged = this.timeChanged;
        var totalTime = 0;

        if (this.state.timeReport.timeReportTasks != null) {
            this.state.timeReport.timeReportTasks.forEach(function (timeReportTask) {
                var totalTaskTime = 0;
                timeReportTask.timeEntries.forEach(function (timeReportDay) {
                    if (timeReportDay.time != null) {
                        var time = parseFloat(timeReportDay.time);
                        if (!isNaN(time)) {
                            totalTaskTime += time;
                        }
                    }
                });

                totalTime += totalTaskTime;

                rows.push(
                    <TimeReportTableRow key={timeReportTask.task.id} timeReportTask={timeReportTask} totalTaskTime={totalTaskTime} timeChanged={timeChanged} />);
            });
        }

        rows.push(<TimeReportTableFooterRow key="footer3" days={this.state.timeReport.days} time={totalTime} label='Sum total' />);
        rows.push(<TimeReportTableFooterRow key="footer4" days={this.state.timeReport.days} time={this.state.timeReport.workableHours} label='Workable hours' />);

        return (
            <table className="table-sm time-report-table">
                <thead className="bg-success">
                    {headerRow}
                </thead>
                <tbody>
                    {rows}
                </tbody>
            </table>
        );
    }
}


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
        var timeService = new TimeService();
        timeService.getTimes(view)
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

        const timeService = new TimeService();
        timeService.getPreviousTimes(this.state.reportView, date)
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

        const timeService = new TimeService();
        timeService.getNextTimes(this.state.reportView, date)
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

        var payLoad = '{ "userId": "' + this.state.timeReport.user.id + '", "closeDate": "' + date + '"}';
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
            <div className="container-fluid ml-2 mr-2">
                <h2>Time</h2>
                <div className="row mb-2">
                    <div className="col-auto">
                        <button className="btn btn-success btn-sm" name={this.state.timeReport.firstDate} onClick={this.loadPreviousTimes}>&lt;&lt;</button>
                    </div>
                    <div className="col-auto">
                        <button className="btn btn-success btn-sm" name={this.state.timeReport.lastDate} onClick={this.loadNextTimes}>&gt;&gt;</button>
                    </div>
                    <div className="col-auto">
                        <select className="form-control form-control-sm" value={this.state.reportView} name="reportView" onChange={this.viewChange}>
                            <option value={Constants.WEEK_VIEW}>Week</option>
                            <option value={Constants.MONTH_VIEW}>Month</option>
                        </select>
                    </div>
                    <div className="col">
                        {this.state.reportView === Constants.MONTH_VIEW && !this.state.timeReport.closed ? (
                            <span className="float-right">
                                <button className="btn btn-success btn-sm" name={this.state.timeReport.firstDate} onClick={this.closeReport}>Close report</button>
                            </span>
                        ) : ''}
                    </div>
                    <div className="col-auto">
                        <span className="text-nowrap">
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
}