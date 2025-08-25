import React from "react";
import TimeService from '../../service/TimeService';
import * as Constants from '../../common/Constants';

class VacationTableEntry extends React.Component {
    constructor(props) {
        super(props);
        this.state = { vacationsDay: this.props.vacationsDay };
    }

    componentDidUpdate(prevProps) {
        if (this.props !== prevProps) {
            this.setState(this.props);
        }
    }

    render() {
        if (this.state == null) return null;

        var backGroundColor = '';
        if (this.state.vacationsDay.vacation) {
            backGroundColor = Constants.ALERT_COLOR;
        } else if (this.state.vacationsDay.day.weekend) {
            backGroundColor = Constants.WEEKEND_COLOR;
        } else if (this.state.vacationsDay.day.majorHoliday) {
            backGroundColor = Constants.WEEKEND_COLOR;
        } else {
            backGroundColor = Constants.DAY_COLOR;
        }

        const inputStyle = {
            backgroundColor: backGroundColor,
            minWidth: '30px'
        }

        return (
            <td style={inputStyle} name={this.state.vacationsDay.day.date} >
            </td>
        );
    }
};

class VacationTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { userVacation: this.props.userVacation };
    }

    componentDidUpdate(prevProps) {
        if (this.props !== prevProps) {
            this.setState(this.props);
        }
    }

    render() {
        if (this.state == null) return null;

        var keyBase = this.state.userVacation.userId;

        var entries = [];
        var i = 3;
        this.state.userVacation.vacationsDays.forEach(function (vacationsDay) {
            var key = keyBase + '-' + i;
            entries.push(
                <VacationTableEntry key={key} vacationsDay={vacationsDay} />);
            i++;
        });

        var userName = this.state.userVacation.name;
        var userNameShort = userName.substring(0, Math.min(15, userName.length));

        return (
            <tr key={keyBase}>
                <th key={keyBase + '-0'} className="text-nowrap" title={userName}>{userNameShort}</th>
                <td key={keyBase + '-1'} className="text-nowrap" >{this.state.userVacation.noVacationDays}</td>
                {entries}
            </tr>
        );
    }
};

class VacationTableHeaderRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { days: this.props.days };
    }

    componentDidUpdate(prevProps) {
        if (this.props !== prevProps) {
            this.setState(this.props);
        }
    }

    render() {
        if (this.state == null || this.state.days == null)
            return null;

        var columns = [];
        if (this.state.days != null) {
            var i = 2;
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
                <th key="header-0"><font color={Constants.DAY_COLOR}>Name</font></th>
                <th key="header-1"><font color={Constants.DAY_COLOR}>Days</font></th>
                {columns}
            </tr>
        );
    }
};

class VacationTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { vacations: this.props.vacations };
    }

    componentDidUpdate(prevProps) {
        if (this.props.vacations !== prevProps.vacations) {
            this.setState({ vacations: this.props.vacations });
        }
    }

    render() {
        if (this.state == null || this.state.vacations == null) return null;

        var rows = [];
        if (this.state.vacations.userVacations != null) {
            this.state.vacations.userVacations.forEach(function (userVacation) {
                rows.push(
                    <VacationTableRow key={userVacation.userId} userVacation={userVacation} />);
            });
        }

        return (
            <table className="table-sm table-bordered time-report-table mb5">
                <thead className="bg-success">
                    <VacationTableHeaderRow days={this.state.vacations.days} />
                </thead>
                <tbody>
                    {rows}
                </tbody>
            </table>
        );
    }
};


export default class Vacations extends React.Component {

    constructor(props) {
        super(props);
        this.state = { vacations: null };
        this.loadCurrentVacations = this.loadCurrentVacations.bind(this);
        this.loadPreviousVacations = this.loadPreviousVacations.bind(this);
        this.loadNextVacations = this.loadNextVacations.bind(this);
    }

    componentDidMount() {
        this.loadCurrentVacations();
    }

    loadCurrentVacations() {
        var self = this;
        var timeService = new TimeService();
        timeService.getVacations()
            .then(response => {
                self.setState({ vacations: response.data });
            })
            .catch(error => {
                alert('Failed to load vacations');
            });
    }

    loadPreviousVacations(event) {
        const date = event.target.name;
        var self = this;
        var timeService = new TimeService();

        timeService.getPreviousVacations(date)
            .then(response => {
                self.setState({ vacations: response.data });
            })
            .catch(error => {
                alert('Failed to load vacations');
            });
    }

    loadNextVacations(event) {
        const date = event.target.name;
        var self = this;
        var timeService = new TimeService();

        timeService.getNextVacations(date)
            .then(response => {
                self.setState({ vacations: response.data });
            })
            .catch(error => {
                alert('Failed to load time vacations');
            });
    }

    render() {
        if (this.state == null || this.state.vacations == null) return null;

        return (
            <div className="container-fluid ml-4 mr-4">
                <h2>Vacations</h2>
                <div className="row mb-3">
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.vacations.firstDate} onClick={this.loadPreviousVacations}>&lt;&lt;</button>
                    </div>
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.vacations.lastDate} onClick={this.loadNextVacations}>&gt;&gt;</button>
                    </div>
                    <div className="col-sm-10">
                        <span className="float-right">
                            <b>{this.state.vacations.firstDate} - {this.state.vacations.lastDate}</b>
                        </span>
                    </div>
                </div>
                <div className="row">
                    <div className="table-responsive">
                        <VacationTable vacations={this.state.vacations} />
                    </div>
                </div>
            </div >
        );
    }
};