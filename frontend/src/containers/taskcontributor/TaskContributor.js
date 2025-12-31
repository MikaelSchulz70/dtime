import React, { useState, useEffect, useCallback } from "react";
import { Table } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import TaskContributorService from '../../service/TaskContributorService';
import UserService from '../../service/UserService';
import *  as Constants from '../../common/Constants';
import { useToast } from '../../components/Toast';
import { useTableSort } from '../../hooks/useTableSort';
import SortableTableHeader from '../../components/SortableTableHeader';

function TaskcontributorTableRow({ taskcontributor: initialTaskcontributor, activationStatusChanged }) {
    const { t } = useTranslation();
    const [taskcontributor, setTaskcontributor] = useState(initialTaskcontributor);
    const { showError } = useToast();

    useEffect(() => {
        setTaskcontributor(initialTaskcontributor);
    }, [initialTaskcontributor]);

    const handleChange = useCallback((event) => {
        let updatedTaskcontributor = JSON.parse(JSON.stringify(taskcontributor));
        let field = event.target.name;
        let value = event.target.value;
        updatedTaskcontributor[field] = value;

        const taskContributorService = new TaskContributorService();
        taskContributorService.udate(updatedTaskcontributor)
            .then(response => {
                setTaskcontributor(updatedTaskcontributor);
                activationStatusChanged(updatedTaskcontributor);
            })
            .catch(error => {
                console.error('Failed to update task contributor:', error);
                showError(t('taskContributors.messages.updateFailed') + ': ' + (error.response?.data?.error || error.message));
            });
    }, [taskcontributor, activationStatusChanged, showError]);

    if (taskcontributor == null) return null;

    return (
        <tr>
            <td>{taskcontributor.task.account.name}</td>
            <td>{taskcontributor.task.name}</td>
            <td>
                <select className="form-control input-sm" name="activationStatus" value={taskcontributor.activationStatus} onChange={handleChange}>
                    <option value={Constants.ACTIVE_STATUS}>{t('common.status.active')}</option>
                    <option value={Constants.INACTIVE_STATUS}>{t('common.status.inactive')}</option>
                </select>
            </td>
        </tr>
    );
}

function TaskContributorTable({ taskcontributors, accountNameFilter, taskNameFilter, statusFilter, activationStatusChanged }) {
    const { t } = useTranslation();
    const { sortedData: sortedTaskcontributors, requestSort, getSortIcon } = useTableSort(taskcontributors, 'task.account.name');
    
    if (taskcontributors == null) return null;

    var filteredTaskcontributors = (sortedTaskcontributors || []).filter(function (taskcontributor) {
        return (taskcontributor.activationStatus === statusFilter) &&
            (taskcontributor.task.account.name.toLowerCase().startsWith(accountNameFilter.toLowerCase())) &&
            (taskcontributor.task.name.toLowerCase().startsWith(taskNameFilter.toLowerCase()));
    });

    var rows = [];
    filteredTaskcontributors.forEach(function (taskcontributor) {
        var key = taskcontributor.task.account.id + '_' + taskcontributor.task.id;
        rows.push(
            <TaskcontributorTableRow taskcontributor={taskcontributor} key={key} activationStatusChanged={activationStatusChanged} />);
    });

    return (
        <Table striped bordered hover responsive>
            <thead className="bg-success">
                <tr>
                    <SortableTableHeader 
                        field="task.account.name" 
                        onSort={requestSort} 
                        getSortIcon={getSortIcon}
                        className="text-white"
                    >
                        {t('taskContributors.headers.account')}
                    </SortableTableHeader>
                    <SortableTableHeader 
                        field="task.name" 
                        onSort={requestSort} 
                        getSortIcon={getSortIcon}
                        className="text-white"
                    >
                        {t('taskContributors.headers.task')}
                    </SortableTableHeader>
                    <SortableTableHeader 
                        field="activationStatus" 
                        onSort={requestSort} 
                        getSortIcon={getSortIcon}
                        className="text-white"
                    >
                        {t('taskContributors.headers.status')}
                    </SortableTableHeader>
                </tr>
            </thead>
            <tbody>{rows}</tbody>
        </Table>
    );
}

function TaskContributor(props) {
    const { t } = useTranslation();
    const [accountNameFilter, setAccountNameFilter] = useState('');
    const [taskNameFilter, setTaskNameFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState(Constants.ACTIVE_STATUS);
    const [userSearchTerm, setUserSearchTerm] = useState('');
    const [selectedUserId, setSelectedUserId] = useState(0);
    const [showUserDropdown, setShowUserDropdown] = useState(false);
    const [highlightedIndex, setHighlightedIndex] = useState(-1);
    const [users, setUsers] = useState(null);
    const [taskcontributors, setTaskcontributors] = useState(null);
    const { showError, showSuccess } = useToast();

    useEffect(() => {
        loadFromServer();
    }, []);

    const loadFromServer = useCallback(() => {
        console.log('TaskContributor loadFromServer called');
        const userService = new UserService();
        userService.getByStatus(true)
            .then(response => {
                setUsers(response.data);
            })
            .catch(error => {
                console.error('Failed to load users:', error);
                showError(t('taskContributors.messages.loadUsersFailed'));
            });
    }, [showError]);

    const activationStatusChanged = useCallback((updatedtaskcontributor) => {
        setTaskcontributors(prevTaskcontributors => {
            if (!prevTaskcontributors) return prevTaskcontributors;

            let updatedTaskcontributors = JSON.parse(JSON.stringify(prevTaskcontributors));
            for (var i in updatedTaskcontributors) {
                if (updatedTaskcontributors[i].task.id === updatedtaskcontributor.task.id &&
                    updatedTaskcontributors[i].task.account.id === updatedtaskcontributor.task.account.id) {
                    updatedTaskcontributors[i] = updatedtaskcontributor;
                    break;
                }
            }
            return updatedTaskcontributors;
        });
    }, []);

    const filterChanged = useCallback((event) => {
        var value = event.target.value;
        const name = event.target.name;

        if (name === 'accountNameFilter') {
            setAccountNameFilter(value);
        } else if (name === 'taskNameFilter') {
            setTaskNameFilter(value);
        } else if (name === 'statusFilter') {
            setStatusFilter(value);
        }
    }, []);

    const handleUserSearch = useCallback((event) => {
        const value = event.target.value;
        setUserSearchTerm(value);
        setShowUserDropdown(value.length > 0);
        setHighlightedIndex(-1);

        // If search is cleared, reset selection
        if (value === '') {
            handleUserSelection(0);
        }
    }, []);

    const handleUserSelect = useCallback((user) => {
        setUserSearchTerm(`${user.firstName} ${user.lastName} (${user.email})`);
        setShowUserDropdown(false);
        setHighlightedIndex(-1);
        handleUserSelection(user.id);
    }, []);

    const handleKeyDown = useCallback((event) => {
        if (!showUserDropdown || !users) return;

        const filteredUsers = users.filter(user => {
            if (!userSearchTerm) return true;
            const fullName = `${user.firstName} ${user.lastName} ${user.email}`.toLowerCase();
            return fullName.includes(userSearchTerm.toLowerCase());
        });

        const maxIndex = Math.min(filteredUsers.length - 1, 9); // Limit to 10 items

        switch (event.key) {
            case 'ArrowDown':
                event.preventDefault();
                setHighlightedIndex(prevIndex => prevIndex < maxIndex ? prevIndex + 1 : 0);
                break;

            case 'ArrowUp':
                event.preventDefault();
                setHighlightedIndex(prevIndex => prevIndex > 0 ? prevIndex - 1 : maxIndex);
                break;

            case 'Enter':
                event.preventDefault();
                if (highlightedIndex >= 0 && highlightedIndex <= maxIndex) {
                    const selectedUser = filteredUsers[highlightedIndex];
                    handleUserSelect(selectedUser);
                }
                break;

            case 'Escape':
                event.preventDefault();
                setShowUserDropdown(false);
                setHighlightedIndex(-1);
                break;

            default:
                break;
        }
    }, [showUserDropdown, users, userSearchTerm, highlightedIndex, handleUserSelect]);

    const handleUserSelection = useCallback((userId) => {
        console.log('User selected:', userId);
        setSelectedUserId(userId);

        if (userId === 0) {
            setAccountNameFilter('');
            setTaskNameFilter('');
            setStatusFilter(Constants.ACTIVE_STATUS);
            setTaskcontributors(null);
            return;
        }

        const taskContributorService = new TaskContributorService();
        taskContributorService.getTaskContributor(userId)
            .then(response => {
                setTaskcontributors(response.data);
            })
            .catch(error => {
                console.error('Failed to load taskcontributors:', error.response?.status, error.response?.data);
                if (error.response?.status === 403) {
                    showError(t('taskContributors.messages.accessDenied'));
                } else {
                    showError(t('taskContributors.messages.loadTaskContributorsFailed') + ': ' + (error.response?.data?.error || error.message));
                }
            });
    }, [showError]);

    const handleUserChange = useCallback((event) => {
        const userId = parseInt(event.target.value, 10);
        handleUserSelection(userId);
    }, [handleUserSelection]);

    // Update handleUserSearch to use the handleUserSelection function
    const handleUserSearchUpdated = useCallback((event) => {
        const value = event.target.value;
        setUserSearchTerm(value);
        setShowUserDropdown(value.length > 0);
        setHighlightedIndex(-1);

        // If search is cleared, reset selection
        if (value === '') {
            handleUserSelection(0);
        }
    }, [handleUserSelection]);

    if (users == null) {
        return (
            <div className="container-fluid mt-4">
                <div className="text-center">
                    <div className="spinner-border" role="status">
                        <span className="visually-hidden">{t('common.loading.default')}</span>
                    </div>
                </div>
            </div>
        );
    }

    // Filter users based on search term
    const filteredUsers = users.filter(user => {
        if (!userSearchTerm) return true;
        const fullName = `${user.firstName} ${user.lastName} ${user.email}`.toLowerCase();
        return fullName.includes(userSearchTerm.toLowerCase());
    });

    return (
        <div className="container-fluid ml-2 mr-2">
            <div className="card">
                <div className="card-header">
                    <h4>{t('taskContributors.title')}</h4>
                </div>
                <div className="card-body">
                    <div className="row mb-3">
                        <div className="col-sm-2" style={{ position: 'relative' }}>
                            <input
                                type="text"
                                className="form-control input-sm"
                                placeholder={t('taskContributors.placeholders.searchUser')}
                                value={userSearchTerm}
                                onChange={handleUserSearchUpdated}
                                onKeyDown={handleKeyDown}
                                onFocus={() => setShowUserDropdown(true)}
                                onBlur={() => setTimeout(() => setShowUserDropdown(false), 200)}
                            />
                            {showUserDropdown && filteredUsers.length > 0 && (
                                <div className="dropdown-menu show" style={{
                                    position: 'absolute',
                                    top: '100%',
                                    left: 0,
                                    right: 0,
                                    maxHeight: '300px',
                                    overflowY: 'auto',
                                    zIndex: 1000
                                }}>
                                    {filteredUsers.slice(0, 10).map((user, index) => (
                                        <button
                                            key={user.id}
                                            type="button"
                                            className={`dropdown-item ${index === highlightedIndex ? 'active' : ''}`}
                                            onClick={() => handleUserSelect(user)}
                                            style={{ textAlign: 'left', whiteSpace: 'nowrap' }}
                                        >
                                            <strong>{user.firstName} {user.lastName}</strong>
                                            <br />
                                            <small className="text-muted">{user.email}</small>
                                        </button>
                                    ))}
                                </div>
                            )}
                        </div>
                        <div className="col-sm-2">
                            <input
                                className="form-control input-sm"
                                type="text"
                                placeholder={t('taskContributors.placeholders.filterByAccount')}
                                name="accountNameFilter"
                                value={accountNameFilter}
                                onChange={filterChanged}
                            />
                        </div>
                        <div className="col-sm-2">
                            <input
                                className="form-control input-sm"
                                type="text"
                                placeholder={t('taskContributors.placeholders.filterByTask')}
                                name="taskNameFilter"
                                value={taskNameFilter}
                                onChange={filterChanged}
                            />
                        </div>
                        <div className="col-sm-2">
                            <select
                                className="form-control input-sm"
                                name="statusFilter"
                                value={statusFilter}
                                onChange={filterChanged}
                            >
                                <option value={Constants.ACTIVE_STATUS}>{t('common.status.active')}</option>
                                <option value={Constants.INACTIVE_STATUS}>{t('common.status.inactive')}</option>
                            </select>
                        </div>
                        <div className="col-sm-4 text-end">
                            {/* Add button can go here if needed */}
                        </div>
                    </div>

                    <div className="row">
                        <TaskContributorTable
                            taskcontributors={taskcontributors}
                            accountNameFilter={accountNameFilter}
                            taskNameFilter={taskNameFilter}
                            statusFilter={statusFilter}
                            activationStatusChanged={activationStatusChanged}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
}

export default TaskContributor;

