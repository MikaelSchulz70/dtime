import React from "react";
import TaskService from '../../service/TaskService';
import AccountService from '../../service/AccountService';
import *  as Constants from '../../common/Constants';
import { TaskType, TaskTypeLabels } from '../../common/TaskType';
import BaseDetails from '../BaseDetails';

export default class TaskDetails extends BaseDetails {

    constructor(props) {
        super(props);
        this.handleCreateUpdate = this.handleCreateUpdate.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.canelAddEdit = this.canelAddEdit.bind(this);
        this.validate = this.validate.bind(this);
        this.setValue = this.setValue.bind(this);

        const id = this.props.match.params.taskId;

        if (id === '0') {
            const task = {
                id: '0', name: '', activationStatus: Constants.ACTIVE_STATUS,
                taskType: TaskType.NORMAL,
                account: { id: '0', name: '' }
            };
            this.state = { task: task, id: id };
        } else {
            this.state = { id: id };
        }
    }

    componentDidMount() {
        const self = this;
        if (this.state.id !== '0') {
            const service = new TaskService();
            service.get(this.state.id)
                .then(response => {
                    // Ensure taskType is set for existing tasks
                    const task = response.data;
                    if (!task.taskType) {
                        task.taskType = TaskType.NORMAL;
                    }
                    self.setState({ task: task });
                })
                .catch(error => {
                    alert('Failed to fetch task');
                });
        }

        const accountService = new AccountService();
        accountService.getByStatus(true)
            .then(response => {
                self.setState({ companies: response.data });
            })
            .catch(error => {
                alert('Failed to fetch accounts');
            });
    }

    handleCreateUpdate(id) {
        this.clearErrors();
        const self = this;
        const service = new TaskService();
        const isUpdate = this.state.task.id && this.state.task.id !== 0;
        const serviceCall = isUpdate
            ? service.update(this.state.task)
            : service.create(this.state.task);

        serviceCall
            .then(response => {
                // Force a full reload by using replace with a timestamp
                self.props.history.replace('/task?refresh=' + Date.now());
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    canelAddEdit() {
        this.props.history.push('/task');
    }

    validate(event) {
        let field = event.target.name;
        let value = event.target.value;

        const self = this;
        const service = new TaskService();
        service.validate(this.state.id, field, value)
            .then(response => {
                self.clearError(field);
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    setValue(fieldPath, value, task) {
        let properties = Array.isArray(fieldPath) ? fieldPath : fieldPath.split(".")

        if (properties.length > 1) {
            if (!task.hasOwnProperty(properties[0]) || typeof task[properties[0]] !== "object")
                task[properties[0]] = {};
            return this.setValue(properties.slice(1), value, task[properties[0]]);
        } else {
            task[properties[0]] = value;
            return true;
        }
    }

    handleChange(event) {
        let field = event.target.name;
        let value = event.target.value;

        let task = JSON.parse(JSON.stringify(this.state.task));

        if (event.target.type === 'checkbox') {
            value = event.target.checked;
        }

        this.setValue(field, value, task);
        this.setState(() => ({ task: task }));
    }

    render() {
        if (this.state == null || this.state.task == null || this.state.companies == null)
            return null;

        var handleCreateUpdate = this.handleCreateUpdate;
        var canelAddEdit = this.canelAddEdit;
        var id = this.state.id;
        var isAdd = (id === '0');
        var buttonText = (isAdd ? "Add" : "Update");

        let accountOptions = this.state.companies.map(c => (
            <option key={c.id} value={c.id}>{c.name}</option>
        ));

        if (this.state.task.account.id === '0' && this.state.companies != null && this.state.companies.length >= 1) {
            this.state.task.account.id = this.state.companies[0].id;
        }

        // Ensure taskType is always set
        if (!this.state.task.taskType) {
            this.state.task.taskType = TaskType.NORMAL;
        }

        return (
            <div className="container">
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Account name</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={this.state.task.account.id} name="account.id" onChange={this.handleChange}>
                            {accountOptions}
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="account.idErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Name</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={this.state.task.name} name="name" maxLength="80" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="nameErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Task Type</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={this.state.task.taskType} name="taskType" onChange={this.handleChange}>
                            {Object.keys(TaskType).map(key => (
                                <option key={TaskType[key]} value={TaskType[key]}>
                                    {TaskTypeLabels[TaskType[key]]}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="taskTypeErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Status</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={this.state.task.activationStatus} name="activationStatus" onChange={this.handleChange}>
                            <option value={Constants.ACTIVE_STATUS}>Active</option>
                            <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="activationStatusErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <div className="col-sm-8">
                        <button className="btn btn-success float-sm-right" onClick={() => canelAddEdit()}>Cancel</button>
                        <button className="btn btn-success float-sm-right mr-5" onClick={() => handleCreateUpdate(id)}>{buttonText}</button>
                    </div>
                </div>
            </div>
        );
    }
}