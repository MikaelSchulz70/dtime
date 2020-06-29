import React from "react";
import UserService from '../../service/UserService';
import BaseDetails from '../BaseDetails';

export default class PasswordChanger extends BaseDetails {
    constructor(props) {
        super(props);
        this.handleError = this.handleError.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.changePassword = this.changePassword.bind(this);
        this.clearErrors = this.clearErrors.bind(this);

        var userPwd = { currentPassword: "", newPassword1: "", newPassword2: "" };
        this.state = { userPwd: userPwd };

    }

    handleChange(event) {
        let userPwd = JSON.parse(JSON.stringify(this.state.userPwd));
        let field = event.target.name;
        let value = event.target.value;
        userPwd[field] = value;

        this.setState(() => ({ userPwd: userPwd }));
    }

    changePassword() {
        this.clearErrors();

        var self = this;
        var service = new UserService();
        service.changePwd(this.state.userPwd)
            .then(response => {
                alert('Password changed successfully!');
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    render() {
        var changePassword = this.changePassword;

        return (
            <div className="container">
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Current password</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="password" value={this.state.userPwd.currentPassword} name="currentPassword" maxLength="80" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="currentPasswordErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">New password</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="password" value={this.state.userPwd.newPassword1} name="newPassword1" maxLength="80" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="newPassword1ErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">New password</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="password" value={this.state.userPwd.newPassword2} name="newPassword2" maxLength="80" onChange={this.handleChange} onBlur={this.validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="newPassword2ErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <div className="col-sm-8">
                        <button className="btn btn-success float-sm-right" onClick={() => changePassword()}>Change password</button>
                    </div>
                </div>
            </div>
        );
    }
};
