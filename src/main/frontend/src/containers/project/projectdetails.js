import React from "react";
import ProjectService from '../../service/ProjectService';
import CompanyService from '../../service/CompanyService';
import *  as Constants from '../../common/Constants';
import BaseDetails from '../BaseDetails';

export default class ProjectDetails extends BaseDetails {

    constructor(props) {
        super(props);
        this.handleCreateUpdate = this.handleCreateUpdate.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.canelAddEdit = this.canelAddEdit.bind(this);
        this.validate = this.validate.bind(this);
        this.setValue = this.setValue.bind(this);

        const id = this.props.match.params.projectId;

        if (id === '0') {
            const project = {
                id: '0', name: '', activationStatus: Constants.ACTIVE_STATUS,
                projectCategory: Constants.DEVELOPMENT_CATEGORY,
                internal: false, provision: true, onCall: false,
                company: { id: '0', name: '' }
            };
            this.state = { project: project, id: id };
        } else {
            this.state = { id: id };
        }
    }

    componentDidMount() {
        const self = this;
        if (this.state.id !== '0') {
            const service = new ProjectService();
            service.get(this.state.id)
                .then(response => {
                    self.setState({ project: response.data });
                })
                .catch(error => {
                    alert('Failed to fetch project');
                });
        }

        const projectService = new CompanyService();
        projectService.getByStatus(true)
            .then(response => {
                self.setState({ companies: response.data });
            })
            .catch(error => {
                alert('Failed to fetch companies');
            });
    }

    handleCreateUpdate(id) {
        this.clearErrors();
        const self = this;
        const service = new ProjectService();
        service.addOrUdate(this.state.project)
            .then(response => {
                self.props.history.push('/projects');
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    canelAddEdit() {
        this.props.history.push('/projects');
    }

    validate(event) {
        let field = event.target.name;
        let value = event.target.value;

        const self = this;
        const service = new ProjectService();
        service.validate(this.state.id, field, value)
            .then(response => {
                self.clearError(field);
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    setValue(fieldPath, value, project) {
        let properties = Array.isArray(fieldPath) ? fieldPath : fieldPath.split(".")

        if (properties.length > 1) {
            if (!project.hasOwnProperty(properties[0]) || typeof project[properties[0]] !== "object")
                project[properties[0]] = {};
            return this.setValue(properties.slice(1), value, project[properties[0]]);
        } else {
            project[properties[0]] = value;
            return true;
        }
    }

    handleChange(event) {
        let field = event.target.name;
        let value = event.target.value;

        if (field === 'onCall' && !event.target.checked) {
            if (!confirm("Are you really sure you want to remove on call?\nAll on call configuration will be removed!")) {
                return;
            }
        }

        let project = JSON.parse(JSON.stringify(this.state.project));

        if (event.target.type === 'checkbox') {
            value = event.target.checked;
        }

        this.setValue(field, value, project);
        this.setState(() => ({ project: project }));
    }

    render() {
        if (this.state == null || this.state.project == null || this.state.companies == null)
            return null;

        var handleCreateUpdate = this.handleCreateUpdate;
        var canelAddEdit = this.canelAddEdit;
        var id = this.state.id;
        var isAdd = (id === '0');
        var buttonText = (isAdd ? "Add" : "Update");

        let companyOptions = this.state.companies.map(c => (
            <option key={c.id} value={c.id}>{c.name}</option>
        ));

        if (this.state.project.company.id === '0' && this.state.companies != null && this.state.companies.length >= 1) {
            this.state.project.company.id = this.state.companies[0].id;
        }

        return (
            <div className="container">
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Company name</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={this.state.project.company.id} name="company.id" onChange={this.handleChange}>
                            {companyOptions}
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="company.idErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Name</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={this.state.project.name} name="name" maxLength="80" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="nameErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Internal</label>
                    <div className="col-sm-6">
                        <input type="checkbox" checked={this.state.project.internal} name="internal" onChange={this.handleChange} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="internalErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Provision</label>
                    <div className="col-sm-6">
                        <input type="checkbox" checked={this.state.project.provision} name="provision" onChange={this.handleChange} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="provisionErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">On call</label>
                    <div className="col-sm-6">
                        <input type="checkbox" checked={this.state.project.onCall} name="onCall" onChange={this.handleChange} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="provisionErrorMsg"></small>
                    </div>
                </div>
                {!isAdd && this.state.project.onCall ? (
                    <div className="form-group row">
                        <label className="col-sm-2 col-form-label">On call id</label>
                        <div className="col-sm-6">
                            <input className="form-control" value={this.state.project.id} type="text" readOnly={true} />
                        </div>
                    </div>
                ) : ''}
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Fix rate</label>
                    <div className="col-sm-6">
                        <input type="checkbox" checked={this.state.project.fixRate} name="fixRate" onChange={this.handleChange} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="fixRateErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Status</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={this.state.project.activationStatus} name="activationStatus" onChange={this.handleChange}>
                            <option value={Constants.ACTIVE_STATUS}>Active</option>
                            <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="activationStatusErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Category</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={this.state.project.projectCategory} name="projectCategory" onChange={this.handleChange}>
                            <option value={Constants.DEVELOPMENT_CATEGORY}>Developement</option>
                            <option value={Constants.INTEGRATION_CATEGORY}>Integration</option>
                            <option value={Constants.NONE_CATEGORY}></option>
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="projectCategoryErrorMsg"></small>
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
};