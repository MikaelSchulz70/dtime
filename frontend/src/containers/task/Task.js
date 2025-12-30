import React, { useState, useEffect, useCallback } from "react";
import { Link } from 'react-router';
import { useTranslation } from 'react-i18next';
import TaskService from '../../service/TaskService';
import *  as Constants from '../../common/Constants';
import { TaskTypeLabels } from '../../common/TaskType';
import { useToast } from '../../components/Toast';

function TaskTableRow({ task, handleDelete }) {
    const { t } = useTranslation();
    if (task == null) return null;

    const editRoute = '/tasks/' + task.id;

    return (
        <tr>
            <td>{task.account.name}</td>
            <td>{task.name}</td>
            <td>{TaskTypeLabels[task.taskType] || task.taskType}</td>
            <td>{task.activationStatus}</td>
            <td>
                <Link className="btn btn-outline-primary btn-sm me-2" to={editRoute}>{t('common.buttons.edit')}</Link>
                <button className="btn btn-outline-danger btn-sm" onClick={() => handleDelete(task.id)}>{t('common.buttons.delete')}</button>
            </td>
        </tr>
    );
}

function TaskTable({ tasks, handleDelete, nameFilter, accountNameFilter, statusFilter }) {
    const { t } = useTranslation();
    if (tasks == null) return null;

    var filteredtasks = tasks.filter(function (task) {
        return (task.activationStatus === statusFilter) &&
            (task.name.toLowerCase().startsWith(nameFilter.toLowerCase())) &&
            (task.account.name.toLowerCase().startsWith(accountNameFilter.toLowerCase()));
    });

    var rows = [];
    filteredtasks.forEach(function (task) {
        rows.push(
            <TaskTableRow task={task} key={task.id} handleDelete={handleDelete} />);
    });

    return (
        <table className="table table-striped">
            <thead className="thead-inverse bg-success">
                <tr className="text-white">
                    <th>{t('common.labels.accountName')}</th>
                    <th>{t('common.labels.name')}</th>
                    <th>{t('common.labels.taskType')}</th>
                    <th>{t('common.labels.status')}</th>
                    <th>{t('common.labels.actions')}</th>
                </tr>
            </thead>
            <tbody>{rows}</tbody>
        </table>
    );
}

function Task(props) {
    const { t } = useTranslation();
    const [accountNameFilter, setAccountNameFilter] = useState('');
    const [nameFilter, setNameFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState(Constants.ACTIVE_STATUS);
    const [tasks, setTasks] = useState(null);
    const { showError, showSuccess } = useToast();

    const loadFromServer = useCallback(() => {
        var service = new TaskService();
        service.getAll()
            .then(response => {
                setTasks(response.data);
            })
            .catch(error => {
                showError(t('tasks.messages.loadTasksFailed') + ': ' + (error.response?.data?.error || error.message));
            });
    }, [showError]);

    useEffect(() => {
        loadFromServer();
    }, [loadFromServer]);

    useEffect(() => {
        // Reload data when navigating back to this route or when refresh parameter changes
        if (props.location) {
            const currentParams = new URLSearchParams(props.location.search);
            const currentRefresh = currentParams.get('refresh');

            if (currentRefresh) {
                loadFromServer();
                // Clean up the URL parameter
                props.history.replace('/task');
            }
        }
    }, [props.location, props.history, loadFromServer]);


    const filterChanged = useCallback((event) => {
        const value = event.target.value;
        const name = event.target.name;
        
        if (name === 'accountNameFilter') {
            setAccountNameFilter(value);
        } else if (name === 'nameFilter') {
            setNameFilter(value);
        } else if (name === 'statusFilter') {
            setStatusFilter(value);
        }
    }, []);

    const handleDelete = useCallback((id) => {
        const shallDelete = confirm(t('tasks.messages.taskDeleteConfirm'));
        if (!shallDelete) {
            return;
        }

        var service = new TaskService();
        service.delete(id)
            .then(response => {
                showSuccess(t('tasks.messages.taskDeleted'));
                loadFromServer();
            })
            .catch(error => {
                showError(t('tasks.messages.deleteTaskFailed') + ': ' + (error.response.data.error));
            });
    }, [loadFromServer, showSuccess, showError, t]);

    if (tasks == null) return null;

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>{t('tasks.title')}</h2>
            <div className="mt-0">
                <div className="row mb-3">
                <div className="col-sm-2">
                    <input className="form-control input-sm" type="text" placeholder={t('tasks.placeholders.accountName')} name="accountNameFilter" onChange={filterChanged} />
                </div>
                <div className="col-sm-2">
                    <input className="form-control input-sm" type="text" placeholder={t('tasks.placeholders.filterByName')} name="nameFilter" onChange={filterChanged} />
                </div>
                <div className="col-sm-2">
                    <select className="form-control input-sm" name="statusFilter" onChange={filterChanged}>
                        <option value={Constants.ACTIVE_STATUS}>{t('common.status.active')}</option>
                        <option value={Constants.INACTIVE_STATUS}>{t('common.status.inactive')}</option>
                    </select>
                </div>
                <div className="col-sm-6 text-end">
                    <Link className="btn btn-primary btn-sm" to='/tasks/0'>{t('tasks.addTask')}</Link>
                </div>
            </div>
            <div className="row">
                <TaskTable tasks={tasks}
                    handleDelete={handleDelete}
                    nameFilter={nameFilter}
                    accountNameFilter={accountNameFilter}
                    statusFilter={statusFilter} />
            </div>
            </div>
        </div>
    );
}

export default Task;