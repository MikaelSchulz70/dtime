import React from "react";
import TaskContributorService from '../../service/TaskContributorService';
import UserService from '../../service/UserService';
import *  as Constants from '../../common/Constants';


class TaskcontributorTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.state = { taskcontributor: this.props.taskcontributor };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    handleChange(event) {
        let taskcontributor = JSON.parse(JSON.stringify(this.state.taskcontributor));
        let field = event.target.name;
        let value = event.target.value;
        taskcontributor[field] = value;

        const self = this;
        const taskcontributorService = new TaskContributorService();
        taskcontributorService.update(taskcontributor)
            .then(response => {
                self.props.activationStatusChanged(taskcontributor);
            })
            .catch(error => {
                alert('Failed to update');
            });
    }

    render() {
        if (this.state == null || this.state.taskcontributor == null) return null;

        return (
            <tr>
                <td>{this.state.taskcontributor.task.account.name}</td>
                <td>{this.state.taskcontributor.task.name}</td>
                <td><input type="checkbox" readOnly={true} checked={this.state.taskcontributor.task.provision} /></td>
                <td><input type="checkbox" readOnly={true} checked={this.state.taskcontributor.task.internal} /></td>
                <td>{this.state.taskcontributor.task.taskCategory}</td>
                <td>
                    <select className="form-control input-sm" name="activationStatus" value={this.state.taskcontributor.activationStatus} onChange={this.handleChange}>
                        <option value={Constants.ACTIVE_STATUS}>Active</option>
                        <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                    </select>
                </td>
            </tr>
        );
    }
};

class TaskContributorTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { taskcontributors: this.props.taskcontributors };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null) return null;

        var accountNameFilter = this.props.accountNameFilter;
        var taskNameFilter = this.props.taskNameFilter;
        var statusFilter = this.props.statusFilter;

        if (this.state.taskcontributors != null) {
            var filteredtaskcontributors = this.state.taskcontributors.filter(function (taskcontributor) {
                return (taskcontributor.activationStatus === statusFilter) &&
                    (taskcontributor.task.account.name.toLowerCase().startsWith(accountNameFilter.toLowerCase())) &&
                    (taskcontributor.task.name.toLowerCase().startsWith(taskNameFilter.toLowerCase()));
            });

            var self = this;
            var rows = [];
            filteredtaskcontributors.forEach(function (taskcontributor) {
                var key = taskcontributor.task.account.id + '_' + taskcontributor.task.id;
                rows.push(
                    <TaskcontributorTableRow taskcontributor={taskcontributor} key={key} activationStatusChanged={self.props.activationStatusChanged} />);
            });
        }

        return (
            <table className="table table-striped">
                <thead className="thead-inverse bg-success">
                    <tr className="text-white">
                        <th>account</th>
                        <th>task</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </table>
        );
    }
};

export default class TaskContributor extends React.Component {
    constructor(props) {
        super(props);
        this.loadFromServer = this.loadFromServer.bind(this);
        this.filterChanged = this.filterChanged.bind(this);
        this.handleUserChange = this.handleUserChange.bind(this);
        this.activationStatusChanged = this.activationStatusChanged.bind(this);

        this.state = { accountNameFilter: '', taskNameFilter: '', statusFilter: Constants.ACTIVE_STATUS };
    }

    componentDidMount() {
        this.loadFromServer();
    }

    loadFromServer() {
        const self = this;
        const userService = new UserService();
        userService.getByStatus(true)
            .then(response => {
                self.setState({ users: response.data });
            })
            .catch(error => {
                alert('Failed to load users');
            });
    }

    activationStatusChanged(updatedtaskcontributor) {
        let taskcontributors = JSON.parse(JSON.stringify(this.state.taskcontributors));

        for (var i in taskcontributors) {
            if (taskcontributors[i].task.id === updatedtaskcontributor.task.id &&
                taskcontributors[i].task.account.id === updatedtaskcontributor.task.account.id) {
                taskcontributors[i] = updatedtaskcontributor;
                this.setState({ taskcontributors: taskcontributors });
                break;
            }
        }
    }

    filterChanged(event) {
        var value = event.target.value;
        const name = event.target.name;
        this.setState({ [name]: value });
    }

    handleUserChange(event) {
        const userId = parseInt(event.target.value, 10);
        if (userId === 0) {
            this.setState({ accountNameFilter: '', taskNameFilter: '', statusFilter: Constants.ACTIVE_STATUS });
            return;
        }

        const self = this;
        const taskcontributorService = new TaskContributorService();
        taskcontributorService.getAssginmentForUser(userId)
            .then(response => {
                self.setState({ taskcontributors: response.data });
            })
            .catch(error => {
                alert('Failed to load taskcontributors');
            });
    }

    render() {
        if (this.state == null || this.state.users == null) return null;


        let userOptions = this.state.users.map(u => (
            <option value={u.id} key={u.id} >{u.firstName + ' ' + u.lastName}</option>
        ));

        userOptions.unshift(<option value={0} key={0}>{''}</option>);

        return (
            <div className="container">
                <div className="row mb-3">
                    <div className="col-sm-3">
                        <select className="form-control dataLiveSearch" value={this.state.userId} name="user.id" onChange={this.handleUserChange}>
                            {userOptions}
                        </select>
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="account" name="accountNameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="task" name="taskNameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" name="statusFilter" onChange={this.filterChanged}>
                            <option value={Constants.ACTIVE_STATUS}>Active</option>
                            <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                        </select>
                    </div>
                </div>
                <div className="row">
                    <TaskContributorTable
                        taskcontributors={this.state.taskcontributors}
                        accountNameFilter={this.state.accountNameFilter}
                        taskNameFilter={this.state.taskNameFilter}
                        statusFilter={this.state.statusFilter}
                        activationStatusChanged={this.activationStatusChanged} />
                </div>
            </div>
        );
    }
};

