import React from "react";
import { Link } from 'react-router-dom';
import TaskService from '../../service/TaskService';
import *  as Constants from '../../common/Constants';
import { TaskTypeLabels } from '../../common/TaskType';

class TaskTableRow extends React.Component {
    render() {
        if (this.props == null) return null;

        const editRoute = '/tasks/' + this.props.task.id;

        return (
            <tr>
                <td>{this.props.task.account.name}</td>
                <td>{this.props.task.name}</td>
                <td>{TaskTypeLabels[this.props.task.taskType] || this.props.task.taskType}</td>
                <td>{this.props.task.activationStatus}</td>
                <td><Link className="btn btn-success" to={editRoute}>Edit</Link></td>
                <td><button className="btn btn-success" onClick={() => this.props.handleDelete(this.props.task.id)} >Delete</button></td>
            </tr>
        );
    }
};

class TaskTable extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.state = { tasks: this.props.tasks };
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
        if (this.state == null || this.state.tasks == null) return null;

        var nameFilter = this.props.nameFilter;
        var accountNameFilter = this.props.accountNameFilter;
        var statusFilter = this.props.statusFilter;

        var filteredtasks = this.props.tasks.filter(function (task) {
            return (task.activationStatus === statusFilter) &&
                (task.name.toLowerCase().startsWith(nameFilter.toLowerCase())) &&
                (task.account.name.toLowerCase().startsWith(accountNameFilter.toLowerCase()));
        });

        var handleDelete = this.handleDelete;
        var rows = [];
        filteredtasks.forEach(function (task) {
            rows.push(
                <TaskTableRow task={task} key={task.id} handleDelete={handleDelete} />);
        });

        return (
            <table className="table table-striped">
                <thead className="thead-inverse bg-success">
                    <tr className="text-white">
                        <th>Account name</th>
                        <th>Name</th>
                        <th>Task Type</th>
                        <th>Status</th>
                        <th align="right">Edit</th>
                        <th align="right">Delete</th>
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </table>
        );
    }
};

export default class Task extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.filterChanged = this.filterChanged.bind(this);
        this.loadFromServer = this.loadFromServer.bind(this);

        this.state = { accountNameFilter: '', nameFilter: '', statusFilter: Constants.ACTIVE_STATUS };
    }

    componentDidMount() {
        this.loadFromServer();
    }

    componentDidUpdate(prevProps) {
        // Reload data when navigating back to this route or when refresh parameter changes
        if (this.props.location && prevProps.location) {
            const currentParams = new URLSearchParams(this.props.location.search);
            const prevParams = new URLSearchParams(prevProps.location.search);
            const currentRefresh = currentParams.get('refresh');
            const prevRefresh = prevParams.get('refresh');

            if (currentRefresh !== prevRefresh && currentRefresh) {
                this.loadFromServer();
                // Clean up the URL parameter
                this.props.history.replace('/task');
            }
        }
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    loadFromServer() {
        const self = this;
        var service = new TaskService();
        service.getAll()
            .then(response => {
                self.setState({ tasks: response.data });
            })
            .catch(error => {
                alert('Failed to load companies');
            });
    }

    filterChanged(event) {
        const value = event.target.value;
        const name = event.target.name;
        this.setState({ [name]: value });
    }

    handleDelete(id) {
        const shallDelete = confirm('Are you really sure you want to delete?');
        if (!shallDelete) {
            return;
        }

        var self = this;
        var service = new TaskService();
        service.delete(id)
            .then(response => {
                self.loadFromServer();
            })
            .catch(error => {
                alert('Failed to delete\n' + (error.response.data.error));
            });
    }

    render() {
        if (this.state == null || this.state.tasks == null) return null;

        return (
            <div className="container">
                <h2>Task</h2>
                <div className="row mb-3">
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="account name" name="accountNameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Name" name="nameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" name="statusFilter" onChange={this.filterChanged}>
                            <option value={Constants.ACTIVE_STATUS}>Active</option>
                            <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <Link className="btn btn-success float-sm-right" to='/tasks/0'>Add</Link>
                    </div>
                </div>
                <div className="row">
                    <TaskTable tasks={this.state.tasks}
                        handleDelete={this.handleDelete.bind(this)}
                        nameFilter={this.state.nameFilter}
                        accountNameFilter={this.state.accountNameFilter}
                        statusFilter={this.state.statusFilter} />
                </div>
            </div>
        );
    }
};