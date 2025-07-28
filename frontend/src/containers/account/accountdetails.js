import React from "react";
import AccountService from '../../service/AccountService';
import *  as Constants from '../../common/Constants';
import BaseDetails from '../BaseDetails';
import $ from 'jquery';

export default class AccountDetails extends BaseDetails {

    constructor(props) {
        super(props);
        this.handleCreateUpdate = this.handleCreateUpdate.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.canelAddEdit = this.canelAddEdit.bind(this);
        this.validate = this.validate.bind(this);

        const id = this.props.match.params.accountId;

        if (id === '0') {
            const account = { id: '0', name: '', activationStatus: Constants.ACTIVE_STATUS };
            this.state = { account: account, id: id };
        } else {
            this.state = { id: id };
        }
    }

    componentDidMount() {
        if (this.state.id !== '0') {
            const self = this;
            const service = new AccountService();
            service.get(this.state.id)
                .then(response => {
                    self.setState({ account: response.data });
                })
                .catch(error => {
                    alert('Failed to fetch account');
                });
        }
    }

    handleCreateUpdate(id) {
        this.clearErrors();
        const self = this;
        const service = new AccountService();
        service.addOrUdate(this.state.account)
            .then(response => {
                self.props.history.push('/account');
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    canelAddEdit() {
        this.props.history.push('/companies');
    }

    validate(event) {
        let field = event.target.name;
        let value = event.target.value;

        var self = this;
        const service = new AccountService();
        service.validate(this.state.id, field, value)
            .then(response => {
                self.clearError(field);
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    handleChange(event) {
        let account = JSON.parse(JSON.stringify(this.state.account));
        let field = event.target.name;
        let value = event.target.value;
        account[field] = value;

        this.setState(() => ({ account: account }));
    }

    render() {
        if (this.state == null || this.state.account == null)
            return null;

        var handleCreateUpdate = this.handleCreateUpdate;
        var canelAddEdit = this.canelAddEdit;
        var id = this.state.id;
        var isAdd = (id === '0');
        var buttonText = (isAdd ? "Add" : "Update");

        return (
            <div className="container">
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Name</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={this.state.account.name} name="name" maxLength="40" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="nameErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Status</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={this.state.account.activationStatus} name="activationStatus" onChange={this.handleChange}>
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
};