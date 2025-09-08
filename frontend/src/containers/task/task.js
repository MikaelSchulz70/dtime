import React, { useState, useEffect, useCallback } from "react";
import { Link } from 'react-router-dom';
import TaskService from '../../service/TaskService';
import *  as Constants from '../../common/Constants';
import { TaskTypeLabels } from '../../common/TaskType';
import { useToast } from '../../components/Toast';

function TaskTableRow({ task, handleDelete }) {
    if (task == null) return null;

    const editRoute = '/tasks/' + task.id;

    return (
        <tr>
            <td>{task.account.name}</td>
            <td>{task.name}</td>
            <td>{TaskTypeLabels[task.taskType] || task.taskType}</td>
            <td>{task.activationStatus}</td>
            <td>
                <Link className="btn btn-outline-primary btn-sm me-2" to={editRoute}>Edit</Link>
                <button className="btn btn-outline-danger btn-sm" onClick={() => handleDelete(task.id)}>Delete</button>
            </td>
        </tr>
    );
}

function TaskTable({ tasks, handleDelete, nameFilter, accountNameFilter, statusFilter }) {
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
                    <th>Account name</th>
                    <th>Name</th>
                    <th>Task Type</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>{rows}</tbody>
        </table>
    );
}

function Task(props) {
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
                showError('Failed to load tasks: ' + (error.response?.data?.error || error.message));
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
        const shallDelete = confirm('Are you really sure you want to delete?');
        if (!shallDelete) {
            return;
        }

        var service = new TaskService();
        service.delete(id)
            .then(response => {
                showSuccess('Task deleted successfully');
                loadFromServer();
            })
            .catch(error => {
                showError('Failed to delete task: ' + (error.response.data.error));
            });
    }, [loadFromServer, showSuccess, showError]);

    if (tasks == null) return null;

    return (
        <div className="container">
            <h2>Task</h2>
            <div className="row mb-3">
                <div className="col-sm-2">
                    <input className="form-control input-sm" type="text" placeholder="account name" name="accountNameFilter" onChange={filterChanged} />
                </div>
                <div className="col-sm-2">
                    <input className="form-control input-sm" type="text" placeholder="Name" name="nameFilter" onChange={filterChanged} />
                </div>
                <div className="col-sm-2">
                    <select className="form-control input-sm" name="statusFilter" onChange={filterChanged}>
                        <option value={Constants.ACTIVE_STATUS}>Active</option>
                        <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                    </select>
                </div>
                <div className="col-sm-6 text-end">
                    <Link className="btn btn-primary btn-sm" to='/tasks/0'>+ Add Task</Link>
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
    );
}

export default Task;