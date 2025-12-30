import React, { useState, useEffect, useCallback } from "react";
import { Link } from 'react-router';
import AccountService from '../../service/AccountService';
import *  as Constants from '../../common/Constants';
import { useToast } from '../../components/Toast';

function AccountTableRow({ organization, handleDelete }) {
    if (organization == null) return null;

    const editRoute = '/account/' + organization.id;

    return (
        <tr>
            <td>{organization.name}</td>
            <td>{organization.activationStatus}</td>
            <td>
                <Link className="btn btn-outline-primary btn-sm me-2" to={editRoute}>Edit</Link>
                <button className="btn btn-outline-danger btn-sm" onClick={() => handleDelete(organization.id)}>Delete</button>
            </td>
        </tr>
    );
}

function AccountTable({ accounts, handleDelete, nameFilter, statusFilter }) {
    if (accounts == null) return null;

    var filteredAccounts = accounts.filter(function (account) {
        return (account.activationStatus === statusFilter) &&
            (account.name.toLowerCase().startsWith(nameFilter.toLowerCase()));
    });

    var rows = [];
    filteredAccounts.forEach(function (organization) {
        rows.push(
            <AccountTableRow organization={organization} key={organization.id} handleDelete={handleDelete} />);
    });

    return (
        <table className="table table-striped">
            <thead className="thead-inverse bg-success">
                <tr className="text-white">
                    <th>Name</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>{rows}</tbody>
        </table>
    );
}

function Account(props) {
    const [nameFilter, setNameFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState(Constants.ACTIVE_STATUS);
    const [accounts, setAccounts] = useState(null);
    const { showError, showSuccess } = useToast();

    const loadFromServer = useCallback(() => {
        console.log('loadFromServer called for accounts');
        var accountService = new AccountService();
        console.log('Making request to get all accounts...');
        accountService.getAll()
            .then(response => {
                console.log('Accounts loaded successfully:', response.data);
                setAccounts(response.data);
            })
            .catch(error => {
                console.error('Failed to load accounts:', error);
                showError('Failed to load accounts: ' + (error.response?.data?.error || error.message));
            });
    }, [showError]);

    useEffect(() => {
        console.log('Account componentDidMount called');
        loadFromServer();
        
        // Add event listener for when the window gains focus
        const handleFocus = () => {
            console.log('Window gained focus, reloading account data');
            loadFromServer();
        };
        window.addEventListener('focus', handleFocus);

        return () => {
            // Clean up event listener
            window.removeEventListener('focus', handleFocus);
        };
    }, [loadFromServer]);

    useEffect(() => {
        // Reload data when navigating back to this route or when refresh parameter changes
        console.log('Account componentDidUpdate called');
        if (props.location) {
            const currentParams = new URLSearchParams(props.location.search);
            const currentRefresh = currentParams.get('refresh');
            
            if (currentRefresh) {
                console.log('Refresh parameter detected, reloading data');
                loadFromServer();
                // Clean up the URL parameter
                props.history.replace('/account');
            }
        }
    }, [props.location, props.history, loadFromServer]);


    const filterChanged = useCallback((event) => {
        const value = event.target.value;
        const name = event.target.name;
        if (name === 'nameFilter') {
            setNameFilter(value);
        } else if (name === 'statusFilter') {
            setStatusFilter(value);
        }
    }, []);

    const handleDelete = useCallback((id) => {
        const shallDelete = confirm('Are you really sure you want to delete?');
        if (!shallDelete) {
            return;
        }

        var accountService = new AccountService();
        accountService.delete(id)
            .then(_response => {
                showSuccess('Account deleted successfully');
                loadFromServer();
            })
            .catch(error => {
                showError('Failed to delete: ' + (error.response.data.error));
            });
    }, [loadFromServer, showSuccess, showError]);

    if (accounts == null) return null;

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>Accounts</h2>
            <div className="mt-0">
                <div className="row mb-3">
                <div className="col-sm-2">
                    <input className="form-control input-sm" type="text" placeholder="Name" name="nameFilter" onChange={filterChanged} />
                </div>
                <div className="col-sm-2">
                    <select className="form-control input-sm" name="statusFilter" onChange={filterChanged}>
                        <option value={Constants.ACTIVE_STATUS}>Active</option>
                        <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                    </select>
                </div>
                <div className="col-sm-8 text-end">
                    <Link className="btn btn-primary btn-sm" to='/account/0'>+ Add Account</Link>
                </div>
            </div>
            <div className="row">
                <AccountTable accounts={accounts}
                    handleDelete={handleDelete}
                    nameFilter={nameFilter}
                    statusFilter={statusFilter} />
            </div>
            </div>
        </div>
    );
}

export default Account;