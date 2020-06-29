import React from "react";
import OnCallRuleDetailsService from '../../service/OnCallRuleDetailsService';
import BaseDetails from '../BaseDetails';
import MappleToolTip from 'reactjs-mappletooltip'

export default class OnCallRuleDetails extends BaseDetails {

    constructor(props) {
        super(props);
        this.handleCreateUpdate = this.handleCreateUpdate.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.cancelEdit = this.cancelEdit.bind(this);
        this.validate = this.validate.bind(this);

        this.state = { projectId: this.props.match.params.projectId };
    }

    componentDidMount() {
        const self = this;
        const service = new OnCallRuleDetailsService();
        service.getByProjectId(this.state.projectId)
            .then(response => {
                self.setState({ rule: response.data });
            })
            .catch(error => {
                alert('Failed to fetch rule');
            });
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    cancelEdit() {
        this.props.history.push('/oncall/rules');
    }

    handleCreateUpdate() {
        this.clearErrors();
        const self = this;
        const service = new OnCallRuleDetailsService();
        service.addOrUdate(this.state.rule)
            .then(response => {
                self.props.history.push('/oncall/rules');
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    validate(event) {
        let field = event.target.name;
        let value = event.target.value;

        var self = this;
        const service = new OnCallRuleDetailsService();
        service.validate(this.state.id, field, value)
            .then(response => {
                self.clearError(field);
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    handleChange(event) {
        let rule = JSON.parse(JSON.stringify(this.state.rule));
        let field = event.target.name;
        let value = event.target.value;
        rule[field] = value;

        this.setState(() => ({ rule: rule }));
    }

    render() {
        if (this.state == null || this.state.rule == null)
            return null;

        var handleCreateUpdate = this.handleCreateUpdate;
        var cancelEdit = this.cancelEdit;
        var id = this.state.rule.id;
        var isAdd = (id === 0);
        var buttonText = (isAdd ? "Add" : "Update");

        return (
            <div className="container">
                <div className="row mb-3">
                    <div className="col-sm-8">
                        <div className="float-right">
                            <MappleToolTip float={true} direction={'bottom'} mappleType={'info'}>
                                <div className="btn btn-primary">
                                    Info
                                </div>
                                <div>
                                    <ul>
                                        <li>From mail: Last part of sender of the email will be matched against this value to find the project.</li>
                                        <li>Subject: A comma separated list of values. E.g. ERROR,CRITICAL </li>
                                        <li>Body: A comma separated list of values. E.g. Emergency,system down,system crash</li>
                                        <li>If any of the values are found either in the subject or body then message will be sent</li>
                                    </ul>
                                </div>
                            </MappleToolTip>
                        </div>
                    </div>
                </div>

                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Company name</label>
                    <div className="col-sm-6">
                        {this.state.rule.project.company.name}
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Project name</label>
                    <div className="col-sm-6">
                        {this.state.rule.project.name}
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">From mail</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={this.state.rule.fromMail} name="fromMail" maxLength="60" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="fromMailErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Subject</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={this.state.rule.subjectCSV} maxLength="100" name="subjectCSV" onChange={this.handleChange} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="subjectCSVErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Body</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={this.state.rule.bodyCSV} maxLength="100" name="bodyCSV" onChange={this.handleChange} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="bodyCSVErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <div className="col-sm-7">
                        <button className="btn btn-success float-sm-right" onClick={() => handleCreateUpdate()}>{buttonText}</button>
                    </div>
                    <div className="col-sm-5">
                        <button className="btn btn-success" onClick={() => cancelEdit()}>Cancel</button>
                    </div>
                </div>
            </div>
        );
    }
};