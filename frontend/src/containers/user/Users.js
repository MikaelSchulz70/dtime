import React, { useState, useEffect, useCallback, useRef } from "react";
import { Link } from 'react-router';
import { useTranslation } from 'react-i18next';
import UserService from '../../service/UserService';
import *  as Constants from '../../common/Constants';
import { useToast } from '../../components/Toast';
import { useTableSort } from '../../hooks/useTableSort';
import SortableTableHeader from '../../components/SortableTableHeader';

function UserTableRow({ user, handleDelete }) {
    const { t } = useTranslation();
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
                <Link className="btn btn-outline-primary btn-sm me-2" to={editRoute}>{t('common.buttons.edit')}</Link>
                <button className="btn btn-outline-danger btn-sm" onClick={() => handleDelete(user.id)}>{t('common.buttons.delete')}</button>
            </td>
        </tr>
    );
}

function UserTable({ users, handleDelete, statusFilter, emailFilter, roleFilter }) {
    const { t } = useTranslation();
    const { sortedData: sortedUsers, requestSort, getSortIcon } = useTableSort(users, 'firstName');
    
    if (users == null) return null;

    var emailFilterValue = emailFilter || '';

    // Filter only by status, email, and role since firstName/lastName filtering is now done server-side
    var filteredUsers = (sortedUsers || []).filter(function (user) {
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
                    <SortableTableHeader 
                        field="firstName" 
                        onSort={requestSort} 
                        getSortIcon={getSortIcon}
                        className="text-white"
                    >
                        {t('users.headers.firstName')}
                    </SortableTableHeader>
                    <SortableTableHeader 
                        field="lastName" 
                        onSort={requestSort} 
                        getSortIcon={getSortIcon}
                        className="text-white"
                    >
                        {t('users.headers.lastName')}
                    </SortableTableHeader>
                    <SortableTableHeader 
                        field="email" 
                        onSort={requestSort} 
                        getSortIcon={getSortIcon}
                        className="text-white"
                    >
                        {t('users.headers.email')}
                    </SortableTableHeader>
                    <SortableTableHeader 
                        field="userRole" 
                        onSort={requestSort} 
                        getSortIcon={getSortIcon}
                        className="text-white"
                    >
                        {t('users.headers.role')}
                    </SortableTableHeader>
                    <SortableTableHeader 
                        field="activationStatus" 
                        onSort={requestSort} 
                        getSortIcon={getSortIcon}
                        className="text-white"
                    >
                        {t('users.headers.status')}
                    </SortableTableHeader>
                    <th className="text-white">{t('common.labels.actions')}</th>
                </tr>
            </thead>
            <tbody>{rows}</tbody>
        </table>
    );
}

function Users(props) {
    const { t } = useTranslation();
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
                    showError(t('users.messages.loadUsersFailed') + ': ' + (error.response?.data?.error || error.message));
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
                    showError(t('users.messages.loadUsersFailed'));
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
        const shallDelete = confirm(t('users.messages.userDeleteConfirm'));
        if (!shallDelete) {
            return;
        }

        const userService = new UserService();
        userService.delete(id)
            .then(response => {
                showSuccess(t('users.messages.userDeleted'));
                loadFromServer();
            })
            .catch(error => {
                showError(t('users.messages.deleteUserFailed') + ': ' + (error.response.data.error));
            });
    }, [loadFromServer, showSuccess, showError, t]);

    if (users == null) return null;

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>{t('users.title')}</h2>
            <div className="row mb-3">
                <div className="col-sm-2">
                    <input className="form-control input-sm" type="text" placeholder={t('users.placeholders.filterByFirstName')} name="firstNameFilter" value={firstNameFilter} onChange={filterChanged} />
                </div>
                <div className="col-sm-2">
                    <input className="form-control input-sm" type="text" placeholder={t('users.placeholders.filterByLastName')} name="lastNameFilter" value={lastNameFilter} onChange={filterChanged} />
                </div>
                <div className="col-sm-2">
                    <input className="form-control input-sm" type="text" placeholder={t('common.placeholders.email')} name="emailFilter" value={emailFilter} onChange={filterChanged} />
                </div>
                <div className="col-sm-2">
                    <select className="form-control input-sm" name="roleFilter" value={roleFilter} onChange={filterChanged}>
                        <option value="">{t('common.labels.role')}</option>
                        <option value={Constants.USER_ROLE}>{t('users.roles.user')}</option>
                        <option value={Constants.ADMIN_ROLE}>{t('users.roles.admin')}</option>
                    </select>
                </div>
                <div className="col-sm-2">
                    <select className="form-control input-sm" name="statusFilter" value={statusFilter} onChange={filterChanged}>
                        <option value={Constants.ACTIVE_STATUS}>{t('common.status.active')}</option>
                        <option value={Constants.INACTIVE_STATUS}>{t('common.status.inactive')}</option>
                    </select>
                </div>
                <div className="col-sm-2 text-end">
                    <button className="btn btn-warning btn-sm me-2" onClick={() => console.log('Test button clicked', { firstNameFilter, lastNameFilter, emailFilter, statusFilter, roleFilter })}>Test</button>
                    <Link className="btn btn-primary btn-sm" to='/users/0'>{t('users.addUser')}</Link>
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