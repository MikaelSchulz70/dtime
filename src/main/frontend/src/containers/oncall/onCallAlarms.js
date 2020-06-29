import React from "react";
import OnCallAlarmService from '../../service/OnCallAlarmService';
import { Link } from 'react-router-dom';

class AlarmTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { alarm: this.props.alarm };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.props == null) return null;

        var companyName = this.state.alarm.companyName;
        var projectName = this.state.alarm.projectName;
        var userName = this.state.alarm.userName;

        var companyNameShort = companyName.substring(0, Math.min(20, companyName.length));
        var projectNameShort = projectName.substring(0, Math.min(20, projectName.length));
        var userNameShort = userName.substring(0, Math.min(20, userName.length));

        const editRoute = '/oncall/alarms/' + this.state.alarm.id;

        return (
            <tr>
                <td className="text-nowrap" title={companyName}>{companyNameShort}</td>
                <td className="text-nowrap" title={projectName}>{projectNameShort}</td>
                <td className="text-nowrap" title={userName}>{userNameShort}</td>
                <td className="text-nowrap" title={this.state.alarm.dateTime}>{this.state.alarm.dateTime}</td>
                <td>{this.state.alarm.status}</td>
                <td>{this.state.alarm.onCallSeverity}</td>
                <td>
                    <Link className="btn btn-success" to={editRoute}>Details</Link>
                </td >
                <td>
                    {this.props.isAdmin ? (
                        <button className="btn btn-danger" onClick={() => this.props.handleDelete(this.state.alarm.id)} >Delete</button>
                    ) : ''
                    }
                </td>
            </tr >
        );
    }
};

class AlarmTable extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.state = { alarms: this.props.alarms };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    handleDelete(id) {
        this.props.handleDelete(id);
    }

    render() {
        if (this.state == null || this.props.alarms == null ||
            this.props.alarms.onCallAlarms == null)
            return null;

        var companyNameFilter = this.props.companyFilter;
        var projectNameFilter = this.props.projectFilter;
        var userNameFilter = this.props.userFilter;

        var filteredAlarms = this.props.alarms.onCallAlarms.filter(function (alarm) {
            return (alarm.companyName.toLowerCase().startsWith(companyNameFilter.toLowerCase())) &&
                (alarm.projectName.toLowerCase().startsWith(projectNameFilter.toLowerCase())) &&
                (alarm.userName.toLowerCase().startsWith(userNameFilter.toLowerCase()));
        });

        var isAdmin = this.props.alarms.admin;
        var handleDelete = this.handleDelete;
        var rows = [];
        filteredAlarms.forEach(function (alarm) {
            rows.push(
                <AlarmTableRow key={alarm.id} alarm={alarm} handleDelete={handleDelete} isAdmin={isAdmin} />);
        });

        return (
            <table className="table">
                <thead className="thead-inverse bg-success">
                    <tr className="text-white">
                        <th>Company</th>
                        <th>Project</th>
                        <th>User</th>
                        <th>Received</th>
                        <th>Status</th>
                        <th>Severity</th>
                        <th align="right">Details</th>
                        {isAdmin ? (
                            <th align="right">Delete</th>
                        ) : ''
                        }
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </table>
        );
    }
};

export default class OnCallAlarms extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.filterChanged = this.filterChanged.bind(this);

        this.state = { companyFilter: '', projectFilter: '', userFilter: '' };
    }

    componentDidMount() {
        this.loadFromServer();
    }

    loadFromServer() {
        const self = this;
        var service = new OnCallAlarmService();
        service.getAll()
            .then(response => {
                self.setState({ alarms: response.data });
            })
            .catch(error => {
                alert('Failed to load alarms');
            });
    }

    filterChanged(event) {
        const value = event.target.value;
        const name = event.target.name;
        this.setState({ [name]: value });
    }

    handleDelete(id) {
        if (!confirm('Are you really sure you want to delete?')) {
            return;
        }

        const self = this;
        const service = new OnCallAlarmService();
        service.delete(id)
            .then(response => {
                self.loadFromServer();
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    render() {
        if (this.state == null || this.state.alarms == null) return null;

        return (
            <div className="container">
                <div className="row mb-3">
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Company" name="companyFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Project" name="projectFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="User" name="userFilter" onChange={this.filterChanged} />
                    </div>
                </div>
                <div className="row">
                    <AlarmTable alarms={this.state.alarms}
                        handleDelete={this.handleDelete.bind(this)}
                        companyFilter={this.state.companyFilter}
                        projectFilter={this.state.projectFilter}
                        userFilter={this.state.userFilter}
                    />
                </div>
            </div>
        );
    }
};
