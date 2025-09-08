import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from 'react-router-dom';
import AccountService from '../../service/AccountService';
import *  as Constants from '../../common/Constants';
import { useBaseDetails } from '../BaseDetails';
import { useToast } from '../../components/Toast';

export default function AccountDetails(props) {
    const { accountId } = useParams();
    const navigate = useNavigate();
    const { handleError, clearError, clearErrors } = useBaseDetails();
    const { showError } = useToast();
    
    const [account, setAccount] = useState(() => {
        if (accountId === '0') {
            return { id: '0', name: '', activationStatus: Constants.ACTIVE_STATUS };
        }
        return null;
    });

    useEffect(() => {
        if (accountId !== '0') {
            const service = new AccountService();
            service.get(accountId)
                .then(response => {
                    setAccount(response.data);
                })
                .catch(error => {
                    showError?.('Failed to fetch account') || alert('Failed to fetch account');
                });
        }
    }, [accountId, showError]);

    const handleCreateUpdate = useCallback((id) => {
        clearErrors();
        const service = new AccountService();
        const isUpdate = account.id && account.id !== 0;
        const serviceCall = isUpdate
            ? service.update(account)
            : service.create(account);

        serviceCall
            .then(response => {
                navigate('/account?refresh=' + Date.now(), { replace: true });
            })
            .catch(error => {
                handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }, [account, navigate, clearErrors, handleError]);

    const canelAddEdit = useCallback(() => {
        navigate('/account');
    }, [navigate]);

    const validate = useCallback((event) => {
        let field = event.target.name;
        let value = event.target.value;

        const service = new AccountService();
        service.validate(accountId, field, value)
            .then(response => {
                clearError(field);
            })
            .catch(error => {
                handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }, [accountId, clearError, handleError]);

    const handleChange = useCallback((event) => {
        let field = event.target.name;
        let value = event.target.value;
        
        setAccount(prevAccount => {
            const updatedAccount = JSON.parse(JSON.stringify(prevAccount));
            updatedAccount[field] = value;
            return updatedAccount;
        });
    }, []);

    if (account == null) return null;

    var isAdd = (accountId === '0');
    var buttonText = (isAdd ? "Add" : "Update");

    return (

            <div className="container">
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Name</label>
                    <div className="col-sm-6">
                        <input className="form-control" type="text" value={account.name} name="name" maxLength="40" onChange={handleChange} onBlur={validate} />
                    </div>
                    <div className="col-sm-4">
                        <small className="text-danger" id="nameErrorMsg"></small>
                    </div>
                </div>
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Status</label>
                    <div className="col-sm-6">
                        <select className="form-control" value={account.activationStatus} name="activationStatus" onChange={handleChange}>
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
                        <button className="btn btn-success float-sm-right mr-5" onClick={() => handleCreateUpdate(accountId)}>{buttonText}</button>
                    </div>
                </div>
            </div>
    );
}