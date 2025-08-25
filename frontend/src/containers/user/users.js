import React from "react";
import { Link } from 'react-router-dom';
import UserService from '../../service/UserService';
import *  as Constants from '../../common/Constants';

class UserTableRow extends React.Component {

    render() {
        if (this.props == null) return null;

        const editRoute = '/users/' + this.props.user.id;

        return (
            <tr>
                <td>{this.props.user.firstName}</td>
                <td>{this.props.user.lastName}</td>
                <td>{this.props.user.email}</td>
                <td>{this.props.user.userRole}</td>
                <td>{this.props.user.activationStatus}</td>
                <td><Link className="btn btn-success" to={editRoute}>Edit</Link></td>
                <td><button className="btn btn-success" onClick={() => this.props.handleDelete(this.props.user.id)} >Delete</button></td>
            </tr>
        );
    }
};

class UserTable extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.state = { users: this.props.users };
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
        if (this.state == null) return null;

        var firstNameFilter = this.props.firstNameFilter;
        var lastNameFilter = this.props.lastNameFilter;
        var statusFilter = this.props.statusFilter;
        var emailFilter = this.props.emailFilter || '';
        var roleFilter = this.props.roleFilter;

        var filteredUsers = this.props.users.filter(function (user) {
            return (user.activationStatus === statusFilter) &&
                (user.firstName.toLowerCase().startsWith(firstNameFilter.toLowerCase())) &&
                (user.lastName.toLowerCase().startsWith(lastNameFilter.toLowerCase())) &&
                (user.email.toLowerCase().includes(emailFilter.toLowerCase())) &&
                (user.userRole === roleFilter || roleFilter === '');
        });

        var handleDelete = this.handleDelete;
        var rows = [];
        filteredUsers.forEach(function (user) {
            rows.push(
                <UserTableRow user={user} key={user.id} handleDelete={handleDelete} />);
        });

        return (
            <table className="table table-striped">
                <thead className="thead-inverse bg-success">
                    <tr className="text-white">
                        <th>First name</th>
                        <th>Last name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Edit</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </table>
        );
    }
};

export default class Users extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.loadFromServer = this.loadFromServer.bind(this);
        this.filterChanged = this.filterChanged.bind(this);
        this.state = { firstNameFilter: '', lastNameFilter: '', statusFilter: Constants.ACTIVE_STATUS, categoryFilter: '', roleFilter: '' };
    }

    componentDidMount() {
        this.loadFromServer();
    }

    componentDidUpdate(prevProps) {
        // Reload data when navigating back to this route
        if (this.props.location !== prevProps.location) {
            this.loadFromServer();
        }
    }

    loadFromServer() {
        const self = this;
        const userService = new UserService();
        userService.getAll()
            .then(response => {
                self.setState({ users: response.data });
            })
            .catch(error => {
                alert('Failed to load users');
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
        const userService = new UserService();
        userService.delete(id)
            .then(response => {
                self.loadFromServer();
            })
            .catch(error => {
                alert('Failed to delete\n' + (error.response.data.error));
            });
    }

    render() {
        if (this.state == null || this.state.users == null) return null;

        return (
            <div className="container">
                <h2>User</h2>
                <div className="row mb-3">
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="First Name" name="firstNameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Last name" name="lastNameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" name="roleFilter" onChange={this.filterChanged}>
                            <option value=""></option>
                            <option value={Constants.USER_ROLE}>User</option>
                            <option value={Constants.ADMIN_ROLE}>Admin</option>
                        </select>
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" name="statusFilter" onChange={this.filterChanged}>
                            <option value={Constants.ACTIVE_STATUS}>Active</option>
                            <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                        </select>
                    </div>
                    <div className="col-sm-2">
                        <Link className="btn btn-success float-sm-right" to='/users/0'>Add</Link>
                    </div>
                </div>
                <div className="row">
                    <UserTable users={this.state.users}
                        handleDelete={this.handleDelete.bind(this)}
                        firstNameFilter={this.state.firstNameFilter}
                        lastNameFilter={this.state.lastNameFilter}
                        categoryFilter={this.state.categoryFilter}
                        roleFilter={this.state.roleFilter}
                        statusFilter={this.state.statusFilter}
                    />
                </div>
            </div>
        );
    }
};