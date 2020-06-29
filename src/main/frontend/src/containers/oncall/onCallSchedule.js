import React from "react";
import OnCallService from '../../service/OnCallService';
import * as Constants from '../../common/Constants';


class OnCallTableEntry extends React.Component {
    constructor(props) {
        super(props);
        this.update = this.update.bind(this);
        this.state = { onCallDay: this.props.onCallDay };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    update(event) {
        if (this.state.onCallDay.readOnly) {
            return;
        }

        let onCallDay = JSON.parse(JSON.stringify(this.state.onCallDay));
        onCallDay['onCall'] = !this.state.onCallDay.onCall;

        const self = this;
        var service = new OnCallService();
        service.udateOnCallDay(onCallDay)
            .then(response => {
                self.setState({ onCallDay: onCallDay });
            })
            .catch(error => {
                var errorMsg = error.response.data.error != null ? error.response.data.error : 'Failed to update';
                alert(errorMsg);
            });
    }

    render() {
        if (this.state == null) return null;

        var backGroundColor = '';

        if (this.state.onCallDay.onCall) {
            backGroundColor = Constants.ALERT_COLOR;
        } else if (this.state.onCallDay.day.weekend) {
            backGroundColor = Constants.WEEKEND_COLOR;
        } else if (this.state.onCallDay.day.majorHoliday) {
            backGroundColor = Constants.WEEKEND_COLOR;
        } else {
            backGroundColor = Constants.DAY_COLOR;
        }

        const inputStyle = {
            backgroundColor: backGroundColor,
            minWidth: '30px'
        }

        return (
            <td style={inputStyle} name={this.state.onCallDay.day.date} id={this.state.onCallDay.idAssignment} onClick={this.update}>
            </td>
        );
    }
};

class OnCallReportTableHeaderRow extends React.Component {
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
            this.state.days.forEach(function (day) {
                var backGroundColor = '';
                if (day.weekend) {
                    backGroundColor = Constants.WEEKEND_COLOR;
                } else if (day.majorHoliday) {
                    backGroundColor = Constants.WEEKEND_COLOR;
                } else {
                    backGroundColor = Constants.DAY_COLOR;
                }

                columns.push(
                    <th key={day.day}><font color={backGroundColor}>{day.day}</font></th>);
            });
        }

        return (
            <tr>
                <th key='hr-0'><font color={Constants.DAY_COLOR}>Company</font></th>
                <th key='hr-1'><font color={Constants.DAY_COLOR}>Project</font></th>
                <th key='hr-2'><font color={Constants.DAY_COLOR}>Name</font></th>
                {columns}
            </tr>
        );
    }
};

class OnCallUserRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { onCallUser: this.props.onCallUser, companyName: this.props.companyName, projectName: this.props.projectName };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null || this.state.onCallUser == null)
            return null;

        var columns = [];
        var baseKey = this.state.onCallUser.idUser;
        var i = 4;
        if (this.state.onCallUser != null) {
            this.state.onCallUser.onCallDays.forEach(function (onCallDay) {
                var key = baseKey + '_' + i;
                i++;
                columns.push(<OnCallTableEntry key={key} onCallDay={onCallDay} />);
            });
        }

        var companyName = this.state.companyName;
        var companyShortName = companyName.substring(0, Math.min(10, companyName.length));
        var projectName = this.state.projectName;
        var projectShortName = projectName.substring(0, Math.min(10, projectName.length));

        return (
            <tr>
                <th key={this.state.onCallUser.idUser + '_1'} className="text-nowrap" title={companyName}>{companyShortName}</th>
                <th key={this.state.onCallUser.idUser + '_2'} className="text-nowrap" title={projectName}>{projectShortName}</th>
                <th key={this.state.onCallUser.idUser + '_3'} className="text-nowrap">{this.state.onCallUser.userName}</th>
                {columns}
            </tr>
        );
    }
};

class OnCallReportTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { onCallReport: this.props.onCallReport };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null) return null;

        var headerRow = <OnCallReportTableHeaderRow key={0} days={this.state.onCallReport.days} />;

        var rows = [];

        if (this.state.onCallReport.onCallProjects != null) {
            this.state.onCallReport.onCallProjects.forEach(function (onCallProject) {
                var companyName = onCallProject.companyName;
                var projectName = onCallProject.projectName;
                var firstUser = true;
                if (onCallProject.onCallUsers != null) {
                    onCallProject.onCallUsers.forEach(function (onCallUser) {
                        if (!firstUser) {
                            companyName = '';
                            projectName = '';
                        }

                        var key = onCallUser.idUser + '_' + onCallProject.idProject;

                        rows.push(
                            <OnCallUserRow key={key} onCallUser={onCallUser} companyName={companyName} projectName={projectName} />);

                        firstUser = false;
                    });
                }
            });
        }

        return (
            <table className="table-sm table-bordered">
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

export default class OnCallSchedule extends React.Component {
    constructor(props) {
        super(props);
        this.loadFromServer = this.loadFromServer.bind(this);
        this.handlePreviousReport = this.handlePreviousReport.bind(this);
        this.handleNextReport = this.handleNextReport.bind(this);
    }

    componentDidMount() {
        this.loadFromServer();
    }

    loadFromServer() {
        const self = this;
        var service = new OnCallService();
        service.getCurrentSchedule()
            .then(response => {
                self.setState({ onCallReport: response.data });
            })
            .catch(error => {
                alert('Failed to load on call schedule');
            });
    }

    handlePreviousReport(event) {
        const date = event.target.name;
        var self = this;

        var service = new OnCallService();
        service.getPreviousSchedule(date)
            .then(response => {
                self.setState({ onCallReport: response.data });
            })
            .catch(error => {
                alert('Failed to load on call schedule');
            });
    }

    handleNextReport(event) {
        const date = event.target.name;
        var self = this;

        var service = new OnCallService();
        service.getNextSchedule(date)
            .then(response => {
                self.setState({ onCallReport: response.data });
            })
            .catch(error => {
                alert('Failed to load on call schedule');
            });
    }

    render() {
        if (this.state == null || this.state.onCallReport == null)
            return null;

        return (
            <div className="container-fluid ml-4">
                <div className="row mb-3">
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.onCallReport.firstDate} onClick={this.handlePreviousReport}>&lt;&lt;</button>
                    </div>
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.onCallReport.lastDate} onClick={this.handleNextReport}>&gt;&gt;</button>
                    </div>
                    <div className="col-sm-10">
                        <span className=" float-right">
                            <b>{this.state.onCallReport.firstDate} - {this.state.onCallReport.lastDate}</b>
                        </span>
                    </div>
                </div>
                <div className="row">
                    <div className="table-responsive">
                        <OnCallReportTable onCallReport={this.state.onCallReport} />
                    </div>
                </div>
            </div>
        );
    }
};