import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from 'react-router-dom';
import UserService from '../../service/UserService';
import *  as Constants from '../../common/Constants';
import { useBaseDetails } from '../BaseDetails';
import { useToast } from '../../components/Toast';

export default function UserDetails(props) {
    const { userId } = useParams();
    const navigate = useNavigate();
    const { handleError, clearError, clearErrors } = useBaseDetails();
    const { showError } = useToast();
    
    const [user, setUser] = useState(() => {
        if (userId === '0') {
            return { id: userId, firstName: "", lastName: "", email: "", mobileNumber: "", activationStatus: Constants.ACTIVE_STATUS, userRole: Constants.USER_ROLE, password: "" };
        }
        return null;
    });

    useEffect(() => {
        if (userId !== '0') {
            const userService = new UserService();
            userService.get(userId)
                .then(response => {
                    setUser(response.data);
                })
                .catch(error => {
                    showError?.('Failed to fetch user') || alert('Failed to fetch user');
                });
        }
    }, [userId, showError]);

    const handleCreateUpdate = useCallback((id) => {
        clearErrors();
        const userService = new UserService();
        const isUpdate = user.id && user.id !== 0;
        const serviceCall = isUpdate
            ? userService.update(user)
            : userService.create(user);

        serviceCall
            .then(response => {
                navigate('/users');
            })
            .catch(error => {
                handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }, [user, navigate, clearErrors, handleError]);

    const canelAddEdit = useCallback(() => {
        navigate('/users');
    }, [navigate]);


    const validate = useCallback((event) => {
        let field = event.target.name;
        let value = event.target.value;

        if (userId !== '0' && field === 'password') {
            return;
        }

        const userService = new UserService();
        userService.validate(userId, field, value)
            .then(response => {
                clearError(field);
            })
            .catch(error => {
                handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }, [userId, clearError, handleError]);

    const handleChange = useCallback((event) => {
        let field = event.target.name;
        let value = event.target.value;
        
        setUser(prevUser => {
            const updatedUser = JSON.parse(JSON.stringify(prevUser));
            updatedUser[field] = value;
            return updatedUser;
        });
    }, []);

    if (user == null) return null;

    var isAdd = (userId === '0');
    var buttonText = (isAdd ? "Add" : "Update");

    return (

            <div className="container">
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">First name</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={user.firstName} name="firstName" maxLength="30" onChange={handleChange} onBlur={validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="firstNameErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Last name</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={user.lastName} name="lastName" maxLength="30" onChange={handleChange} onBlur={validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="lastNameErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Email</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={user.email} name="email" minLength="6" maxLength="60" onChange={handleChange} onBlur={validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="emailErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Mobile number</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={user.mobileNumber} name="mobileNumber" minLength="10" maxLength="20" onChange={handleChange} onBlur={validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="mobileNumberErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Password</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="password" value={user.password} name="password" maxLength="80" onChange={handleChange} onBlur={validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="passwordErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">User role</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={user.userRole} name="userRole" onChange={handleChange}>
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
                        <select className="form-control" value={user.activationStatus} name="activationStatus" onChange={handleChange}>
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
                        <button className="btn btn-success float-sm-right mr-5" onClick={() => handleCreateUpdate(userId)}>{buttonText}</button>
                    </div>
                </div>
            </div>
    );
}