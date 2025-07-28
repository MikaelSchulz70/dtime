import React from "react";
import UserService from '../../service/UserService';
import *  as Constants from '../../common/Constants';
import BaseDetails from '../BaseDetails';

export default class UserDetails extends BaseDetails {

    constructor(props) {
        super(props);
        this.handleCreateUpdate = this.handleCreateUpdate.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.canelAddEdit = this.canelAddEdit.bind(this);
        this.validate = this.validate.bind(this);

        const id = this.props.match.params.userId;

        if (id === '0') {
            const user = { id: id, firstName: "", lastName: "", email: "", mobileNumber: "", activationStatus: Constants.ACTIVE_STATUS, userRole: Constants.USER_ROLE, password: "" };
            this.state = { user: user, id: id };
        } else {
            this.state = { id: id };
        }
    }

    componentDidMount() {
        if (this.state.id !== '0') {
            const self = this;
            const userService = new UserService();
            userService.get(this.state.id)
                .then(response => {
                    self.setState({ user: response.data });
                })
                .catch(error => {
                    alert('Failed to fetch user');
                });
        }
    }

    handleCreateUpdate(id) {
        this.clearErrors();
        const self = this;
        const userService = new UserService();
        userService.addOrUdate(this.state.user)
            .then(response => {
                self.props.history.push('/users');
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    canelAddEdit() {
        this.props.history.push('/users');
    }


    validate(event) {
        let field = event.target.name;
        let value = event.target.value;

        if (this.state.id !== '0' && field === 'password') {
            return;
        }

        var self = this;
        const userService = new UserService();
        userService.validate(this.state.id, field, value)
            .then(response => {
                self.clearError(field);
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    handleChange(event) {
        let user = JSON.parse(JSON.stringify(this.state.user));
        let field = event.target.name;
        let value = event.target.value;
        user[field] = value;

        this.setState(() => ({ user: user }));
    }

    render() {
        if (this.state == null || this.state.user == null)
            return null;

        var handleCreateUpdate = this.handleCreateUpdate;
        var canelAddEdit = this.canelAddEdit;
        var id = this.state.id;
        var isAdd = (id === '0');
        var buttonText = (isAdd ? "Add" : "Update");

        return (
            <div className="container">
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">First name</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={this.state.user.firstName} name="firstName" maxLength="30" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="firstNameErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Last name</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={this.state.user.lastName} name="lastName" maxLength="30" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="lastNameErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Email</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={this.state.user.email} name="email" minLength="6" maxLength="60" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="emailErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Mobile number</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={this.state.user.mobileNumber} name="mobileNumber" minLength="10" maxLength="20" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="mobileNumberErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Password</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="password" value={this.state.user.password} name="password" maxLength="80" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="passwordErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">User role</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={this.state.user.userRole} name="userRole" onChange={this.handleChange}>
                            <option value={Constants.USER_ROLE}>User</option>
                            <option value={Constants.ADMIN_ROLE}>Admin</option>
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="userRoleErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Status</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={this.state.user.activationStatus} name="activationStatus" onChange={this.handleChange}>
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