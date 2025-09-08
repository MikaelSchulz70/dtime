import React, { useState, useEffect, useCallback, useRef } from "react";
import { Link } from 'react-router-dom';
import UserService from '../../service/UserService';
import *  as Constants from '../../common/Constants';
import { useToast } from '../../components/Toast';

function UserTableRow({ user, handleDelete }) {
    if (user == null) return null;

    const editRoute = '/users/' + user.id;

    return (
        <tr>
            <td>{user.firstName}</td>
            <td>{user.lastName}</td>
            <td>{user.email}</td>
            <td>{user.userRole}</td>
            <td>{user.activationStatus}</td>
            <td>
                <Link className="btn btn-outline-primary btn-sm me-2" to={editRoute}>Edit</Link>
                <button className="btn btn-outline-danger btn-sm" onClick={() => handleDelete(user.id)}>Delete</button>
            </td>
        </tr>
    );
}

function UserTable({ users, handleDelete, statusFilter, emailFilter, roleFilter }) {
    if (users == null) return null;

    var emailFilterValue = emailFilter || '';

    // Filter only by status, email, and role since firstName/lastName filtering is now done server-side
    var filteredUsers = users.filter(function (user) {
        return (user.activationStatus === statusFilter) &&
            (user.email.toLowerCase().includes(emailFilterValue.toLowerCase())) &&
            (user.userRole === roleFilter || roleFilter === '');
    });

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

function Users(props) {
    const [firstNameFilter, setFirstNameFilter] = useState('');
    const [lastNameFilter, setLastNameFilter] = useState('');
    const [emailFilter, setEmailFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState(Constants.ACTIVE_STATUS);
    const [categoryFilter, setCategoryFilter] = useState('');
    const [roleFilter, setRoleFilter] = useState('');
    const [users, setUsers] = useState(null);
    const searchTimeout = useRef(null);
    const { showError, showSuccess } = useToast();

    const loadFromServer = useCallback((firstName = '', lastName = '') => {
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
                    setUsers(users);
                })
                .catch(error => {
                    console.error('Failed to load users with search:', error);
                    showError('Failed to load users: ' + (error.response?.data?.error || error.message));
                });
        } else {
            // Use regular getAll for initial load or when no search terms
            userService.getAll()
                .then(response => {
                    console.log('Regular API response:', response.data);
                    setUsers(response.data);
                })
                .catch(error => {
                    console.error('Failed to load users:', error);
                    showError('Failed to load users');
                });
        }
    }, [showError]);

    useEffect(() => {
        loadFromServer();
    }, [loadFromServer]);

    useEffect(() => {
        // Reload data when navigating back to this route
        loadFromServer();
    }, [props.location, loadFromServer]);

    useEffect(() => {
        // Clean up timeout on unmount
        return () => {
            if (searchTimeout.current) {
                clearTimeout(searchTimeout.current);
            }
        };
    }, []);


    const debouncedSearch = useCallback(() => {
        loadFromServer(firstNameFilter, lastNameFilter);
    }, [firstNameFilter, lastNameFilter, loadFromServer]);

    const filterChanged = useCallback((event) => {
        console.log('filterChanged called!', event.target.name, event.target.value);
        const value = event.target.value;
        const name = event.target.name;
        
        // Update the appropriate state
        if (name === 'firstNameFilter') {
            setFirstNameFilter(value);
        } else if (name === 'lastNameFilter') {
            setLastNameFilter(value);
        } else if (name === 'emailFilter') {
            setEmailFilter(value);
        } else if (name === 'statusFilter') {
            setStatusFilter(value);
        } else if (name === 'categoryFilter') {
            setCategoryFilter(value);
        } else if (name === 'roleFilter') {
            setRoleFilter(value);
        }
        
        // For first and last name, trigger real-time search
        if (name === 'firstNameFilter' || name === 'lastNameFilter') {
            console.log('Triggering search for name filter');
            // Clear existing timeout
            if (searchTimeout.current) {
                clearTimeout(searchTimeout.current);
            }
            
            // Set new timeout for debounced search (300ms delay)
            searchTimeout.current = setTimeout(() => {
                console.log('Executing debounced search');
                loadFromServer(
                    name === 'firstNameFilter' ? value : firstNameFilter,
                    name === 'lastNameFilter' ? value : lastNameFilter
                );
            }, 300);
        }
    }, [firstNameFilter, lastNameFilter, loadFromServer]);

    const handleDelete = useCallback((id) => {
        const shallDelete = confirm('Are you really sure you want to delete?');
        if (!shallDelete) {
            return;
        }

        const userService = new UserService();
        userService.delete(id)
            .then(response => {
                showSuccess('User deleted successfully');
                loadFromServer();
            })
            .catch(error => {
                showError('Failed to delete: ' + (error.response.data.error));
            });
    }, [loadFromServer, showSuccess, showError]);

    if (users == null) return null;

    return (
        <div className="container">
            <h2>User</h2>
            <div className="row mb-3">
                <div className="col-sm-2">
                    <input className="form-control input-sm" type="text" placeholder="First Name" name="firstNameFilter" value={firstNameFilter} onChange={filterChanged} />
                </div>
                <div className="col-sm-2">
                    <input className="form-control input-sm" type="text" placeholder="Last name" name="lastNameFilter" value={lastNameFilter} onChange={filterChanged} />
                </div>
                <div className="col-sm-2">
                    <input className="form-control input-sm" type="text" placeholder="Email" name="emailFilter" value={emailFilter} onChange={filterChanged} />
                </div>
                <div className="col-sm-2">
                    <select className="form-control input-sm" name="roleFilter" value={roleFilter} onChange={filterChanged}>
                        <option value=""></option>
                        <option value={Constants.USER_ROLE}>User</option>
                        <option value={Constants.ADMIN_ROLE}>Admin</option>
                    </select>
                </div>
                <div className="col-sm-2">
                    <select className="form-control input-sm" name="statusFilter" value={statusFilter} onChange={filterChanged}>
                        <option value={Constants.ACTIVE_STATUS}>Active</option>
                        <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                    </select>
                </div>
                <div className="col-sm-2 text-end">
                    <button className="btn btn-warning btn-sm me-2" onClick={() => console.log('Test button clicked', { firstNameFilter, lastNameFilter, emailFilter, statusFilter, roleFilter })}>Test</button>
                    <Link className="btn btn-primary btn-sm" to='/users/0'>+ Add User</Link>
                </div>
            </div>
            <div className="row">
                <UserTable users={users}
                    handleDelete={handleDelete}
                    emailFilter={emailFilter}
                    roleFilter={roleFilter}
                    statusFilter={statusFilter}
                />
            </div>
        </div>
    );
}

export default Users;