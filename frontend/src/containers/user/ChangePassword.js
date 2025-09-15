import React, { useState, useCallback } from "react";
import UserService from '../../service/UserService';
import { useBaseDetails } from '../BaseDetails';
import { useToast } from '../../components/Toast';

function PasswordChanger(props) {
    const [userPwd, setUserPwd] = useState({ currentPassword: "", newPassword1: "", newPassword2: "" });
    const { showSuccess } = useToast();
    const { handleError, clearErrors } = useBaseDetails();

    const handleChange = useCallback((event) => {
        let updatedUserPwd = JSON.parse(JSON.stringify(userPwd));
        let field = event.target.name;
        let value = event.target.value;
        updatedUserPwd[field] = value;

        setUserPwd(updatedUserPwd);
    }, [userPwd]);

    const changePassword = useCallback(() => {
        clearErrors();

        var service = new UserService();
        service.changePwd(userPwd)
            .then(response => {
                showSuccess('Password changed successfully!');
            })
            .catch(error => {
                handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }, [userPwd, clearErrors, showSuccess, handleError]);

    return (
        <div className="container">
            <h2>Change Password</h2>
            <div className="form-group row">
                <label className="col-sm-2 col-form-label">Current password</label>
                <div className="col-sm-6">
                    <input className="form-control" type="password" value={userPwd.currentPassword} name="currentPassword" maxLength="80" onChange={handleChange} />
                </div>
                <div className="col-sm-4">
                    <small className="text-danger" id="currentPasswordErrorMsg"></small>
                </div>
            </div>
            <div className="form-group row">
                <label className="col-sm-2 col-form-label">New password</label>
                <div className="col-sm-6">
                    <input className="form-control" type="password" value={userPwd.newPassword1} name="newPassword1" maxLength="80" onChange={handleChange} />
                </div>
                <div className="col-sm-4">
                    <small className="text-danger" id="newPassword1ErrorMsg"></small>
                </div>
            </div>
            <div className="form-group row">
                <label className="col-sm-2 col-form-label">New password</label>
                <div className="col-sm-6">
                    <input className="form-control" type="password" value={userPwd.newPassword2} name="newPassword2" maxLength="80" onChange={handleChange} />
                </div>
                <div className="col-sm-4">
                    <small className="text-danger" id="newPassword2ErrorMsg"></small>
                </div>
            </div>
            <div className="form-group row">
                <div className="col-sm-8">
                    <button className="btn btn-success float-sm-right" onClick={changePassword}>Change password</button>
                </div>
            </div>
        </div>
    );
}

export default PasswordChanger;
