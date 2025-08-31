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
                <td>
                    <Link className="btn btn-outline-primary btn-sm me-2" to={editRoute}>Edit</Link>
                    <button className="btn btn-outline-danger btn-sm" onClick={() => this.props.handleDelete(this.props.user.id)}>Delete</button>
                </td>
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

        var statusFilter = this.props.statusFilter;
        var emailFilter = this.props.emailFilter || '';
        var roleFilter = this.props.roleFilter;

        // Filter only by status, email, and role since firstName/lastName filtering is now done server-side
        var filteredUsers = this.props.users.filter(function (user) {
            return (user.activationStatus === statusFilter) &&
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
                        <th>Actions</th>
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
        this.debouncedSearch = this.debouncedSearch.bind(this);
        this.state = { firstNameFilter: '', lastNameFilter: '', emailFilter: '', statusFilter: Constants.ACTIVE_STATUS, categoryFilter: '', roleFilter: '' };
        this.searchTimeout = null;
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

    componentWillUnmount() {
        // Clean up timeout
        if (this.searchTimeout) {
            clearTimeout(this.searchTimeout);
        }
    }

    loadFromServer(firstName = '', lastName = '') {
        const self = this;
        const userService = new UserService();
        
        console.log('Loading users with filters:', { firstName, lastName });
        
        // Use paged API for search functionality
        if (firstName || lastName) {
            userService.getAllPaged(undefined, undefined, undefined, undefined, undefined, firstName, lastName)
                .then(response => {
                    console.log('Search API response:', response.data);
                    // Handle both paginated and non-paginated responses
                    const users = response.data.content || response.data;
                    console.log('Setting users:', users);
                    self.setState({ users: users });
                })
                .catch(error => {
                    console.error('Failed to load users with search:', error);
                    alert('Failed to load users: ' + (error.response?.data?.error || error.message));
                });
        } else {
            // Use regular getAll for initial load or when no search terms
            userService.getAll()
                .then(response => {
                    console.log('Regular API response:', response.data);
                    self.setState({ users: response.data });
                })
                .catch(error => {
                    console.error('Failed to load users:', error);
                    alert('Failed to load users');
                });
        }
    }

    debouncedSearch() {
        const { firstNameFilter, lastNameFilter } = this.state;
        this.loadFromServer(firstNameFilter, lastNameFilter);
    }

    filterChanged = (event) => {
        console.log('filterChanged called!', event.target.name, event.target.value);
        const value = event.target.value;
        const name = event.target.name;
        
        this.setState({ [name]: value }, () => {
            console.log('State updated:', this.state);
            // For first and last name, trigger real-time search
            if (name === 'firstNameFilter' || name === 'lastNameFilter') {
                console.log('Triggering search for name filter');
                // Clear existing timeout
                if (this.searchTimeout) {
                    clearTimeout(this.searchTimeout);
                }
                
                // Set new timeout for debounced search (300ms delay)
                this.searchTimeout = setTimeout(() => {
                    console.log('Executing debounced search');
                    this.debouncedSearch();
                }, 300);
            }
        });
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
                        <input className="form-control input-sm" type="text" placeholder="First Name" name="firstNameFilter" value={this.state.firstNameFilter} onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Last name" name="lastNameFilter" value={this.state.lastNameFilter} onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Email" name="emailFilter" value={this.state.emailFilter} onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" name="roleFilter" value={this.state.roleFilter} onChange={this.filterChanged}>
                            <option value=""></option>
                            <option value={Constants.USER_ROLE}>User</option>
                            <option value={Constants.ADMIN_ROLE}>Admin</option>
                        </select>
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" name="statusFilter" value={this.state.statusFilter} onChange={this.filterChanged}>
                            <option value={Constants.ACTIVE_STATUS}>Active</option>
                            <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                        </select>
                    </div>
                    <div className="col-sm-2 text-end">
                        <button className="btn btn-warning btn-sm me-2" onClick={() => console.log('Test button clicked', this.state)}>Test</button>
                        <Link className="btn btn-primary btn-sm" to='/users/0'>+ Add User</Link>
                    </div>
                </div>
                <div className="row">
                    <UserTable users={this.state.users}
                        handleDelete={this.handleDelete.bind(this)}
                        emailFilter={this.state.emailFilter}
                        roleFilter={this.state.roleFilter}
                        statusFilter={this.state.statusFilter}
                    />
                </div>
            </div>
        );
    }
};