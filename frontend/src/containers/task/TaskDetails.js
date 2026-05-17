import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from 'react-router';
import TaskService from '../../service/TaskService';
import AccountService from '../../service/AccountService';
import *  as Constants from '../../common/Constants';
import { TaskType, TaskTypeI18nKeys } from '../../common/TaskType';
import { useBaseDetails } from '../BaseDetails';
import { useToast } from '../../components/Toast';
import { useTranslation } from 'react-i18next';

export default function TaskDetails(props) {
    const { taskId } = useParams();
    const navigate = useNavigate();
    const { handleError, clearError, clearErrors } = useBaseDetails();
    const { showError } = useToast();
    const { t } = useTranslation();
    
    const [task, setTask] = useState(() => {
        if (taskId === '0') {
            return {
                id: '0', name: '', activationStatus: Constants.ACTIVE_STATUS,
                taskType: TaskType.NORMAL,
                account: { id: '0', name: '' }
            };
        }
        return null;
    });
    const [companies, setCompanies] = useState(null);

    useEffect(() => {
        if (taskId !== '0') {
            const service = new TaskService();
            service.get(taskId)
                .then(response => {
                    const fetchedTask = response.data;
                    if (!fetchedTask.taskType) {
                        fetchedTask.taskType = TaskType.NORMAL;
                    }
                    setTask(fetchedTask);
                })
                .catch(error => {
                    showError?.(t('tasks.messages.fetchFailed')) || alert(t('tasks.messages.fetchFailed'));
                });
        }

        const accountService = new AccountService();
        accountService.getByStatus(true)
            .then(response => {
                setCompanies(response.data);
            })
            .catch(error => {
                showError?.(t('tasks.messages.fetchAccountsFailed')) || alert(t('tasks.messages.fetchAccountsFailed'));
            });
    }, [taskId, showError, t]);

    const handleCreateUpdate = useCallback((id) => {
        clearErrors();
        const service = new TaskService();
        const isUpdate = task.id && task.id !== 0;
        const serviceCall = isUpdate
            ? service.update(task)
            : service.create(task);

        serviceCall
            .then(response => {
                navigate('/task?refresh=' + Date.now(), { replace: true });
            })
            .catch(error => {
                handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }, [task, navigate, clearErrors, handleError]);

    const canelAddEdit = useCallback(() => {
        navigate('/task');
    }, [navigate]);

    const validate = useCallback((event) => {
        let field = event.target.name;
        let value = event.target.value;

        const service = new TaskService();
        service.validate(taskId, field, value)
            .then(response => {
                clearError(field);
            })
            .catch(error => {
                handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }, [taskId, clearError, handleError]);

    const setValue = useCallback((fieldPath, value, taskObj) => {
        let properties = Array.isArray(fieldPath) ? fieldPath : fieldPath.split(".")

        if (properties.length > 1) {
            if (!taskObj.hasOwnProperty(properties[0]) || typeof taskObj[properties[0]] !== "object")
                taskObj[properties[0]] = {};
            return setValue(properties.slice(1), value, taskObj[properties[0]]);
        } else {
            taskObj[properties[0]] = value;
            return true;
        }
    }, []);

    const handleChange = useCallback((event) => {
        let field = event.target.name;
        let value = event.target.value;

        if (event.target.type === 'checkbox') {
            value = event.target.checked;
        }

        setTask(prevTask => {
            const updatedTask = JSON.parse(JSON.stringify(prevTask));
            setValue(field, value, updatedTask);
            return updatedTask;
        });
    }, [setValue]);

    if (task == null || companies == null) return null;

    var isAdd = (taskId === '0');
    var buttonText = (isAdd ? t('common.buttons.add') : t('common.buttons.update'));

    let accountOptions = companies.map(c => (
        <option key={c.id} value={c.id}>{c.name}</option>
    ));

    if (task.account.id === '0' && companies != null && companies.length >= 1) {
        task.account.id = companies[0].id;
    }

    if (!task.taskType) {
        task.taskType = TaskType.NORMAL;
    }

    return (

            <div className="container">
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">{t('common.labels.accountName')}</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={task.account.id} name="account.id" onChange={handleChange}>
                            {accountOptions}
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="account.idErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">{t('common.labels.name')}</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={task.name} name="name" maxLength="80" onChange={handleChange} onBlur={validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="nameErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">{t('common.labels.taskType')}</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={task.taskType} name="taskType" onChange={handleChange}>
                            {Object.keys(TaskType).map(key => (
                                <option key={TaskType[key]} value={TaskType[key]}>
                                    {t(TaskTypeI18nKeys[TaskType[key]])}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="taskTypeErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">{t('common.labels.status')}</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={task.activationStatus} name="activationStatus" onChange={handleChange}>
                            <option value={Constants.ACTIVE_STATUS}>{t('common.status.active')}</option>
                            <option value={Constants.INACTIVE_STATUS}>{t('common.status.inactive')}</option>
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="activationStatusErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <div className="col-sm-8">
                        <button className="btn btn-success float-sm-right" onClick={() => canelAddEdit()}>{t('common.buttons.cancel')}</button>
                        <button className="btn btn-success float-sm-right mr-5" onClick={() => handleCreateUpdate(taskId)}>{buttonText}</button>
                    </div>
                </div>
            </div>
    );
}