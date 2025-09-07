import React from "react";
import { Link } from 'react-router-dom';
import AccountService from '../../service/AccountService';
import *  as Constants from '../../common/Constants';

class AccountTableRow extends React.Component {
    render() {
        if (this.props == null) return null;

        const editRoute = '/account/' + this.props.organization.id;

        return (
            <tr>
                <td>{this.props.organization.name}</td>
                <td>{this.props.organization.activationStatus}</td>
                <td>
                    <Link className="btn btn-outline-primary btn-sm me-2" to={editRoute}>Edit</Link>
                    <button className="btn btn-outline-danger btn-sm" onClick={() => this.props.handleDelete(this.props.organization.id)}>Delete</button>
                </td>
            </tr>
        );
    }
}

class AccountTable extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.state = { accounts: this.props.accounts };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    handleDelete(id) {
        this.props.handleDelete(id);
    }

    render() {
        if (this.state == null || this.state.accounts == null) return null;

        var nameFilter = this.props.nameFilter;
        var statusFilter = this.props.statusFilter;

        var filteredAccounts = this.props.accounts.filter(function (account) {
            return (account.activationStatus === statusFilter) &&
                (account.name.toLowerCase().startsWith(nameFilter.toLowerCase()));
        });

        var handleDelete = this.handleDelete;
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
}

export default class Account extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.filterChanged = this.filterChanged.bind(this);
        this.loadFromServer = this.loadFromServer.bind(this);

        this.state = { nameFilter: '', statusFilter: Constants.ACTIVE_STATUS, accounts: null };
    }

    componentDidMount() {
        console.log('Account componentDidMount called');
        this.loadFromServer();
        
        // Add event listener for when the window gains focus
        this.handleFocus = () => {
            console.log('Window gained focus, reloading account data');
            this.loadFromServer();
        };
        window.addEventListener('focus', this.handleFocus);
    }

    componentWillUnmount() {
        // Clean up event listener
        if (this.handleFocus) {
            window.removeEventListener('focus', this.handleFocus);
        }
    }

    componentDidUpdate(prevProps) {
        // Reload data when navigating back to this route or when refresh parameter changes
        console.log('Account componentDidUpdate called');
        if (this.props.location && prevProps.location) {
            const currentParams = new URLSearchParams(this.props.location.search);
            const prevParams = new URLSearchParams(prevProps.location.search);
            const currentRefresh = currentParams.get('refresh');
            const prevRefresh = prevParams.get('refresh');
            
            if (currentRefresh !== prevRefresh && currentRefresh) {
                console.log('Refresh parameter detected, reloading data');
                this.loadFromServer();
                // Clean up the URL parameter
                this.props.history.replace('/account');
            }
        }
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    loadFromServer() {
        console.log('loadFromServer called for accounts');
        const self = this;
        var accountService = new AccountService();
        console.log('Making request to get all accounts...');
        accountService.getAll()
            .then(response => {
                console.log('Accounts loaded successfully:', response.data);
                self.setState({ accounts: response.data });
            })
            .catch(error => {
                console.error('Failed to load accounts:', error);
                alert('Failed to load accounts: ' + (error.response?.data?.error || error.message));
            });
    }

    filterChanged(event) {
        const value = event.target.value;
        const name = event.target.name;
        this.setState({ [name]: value });
    }

    handleDelete(id) {
        const shallDelete = confirm('Are you really sure you want to delete?');
        if (!shallDelete) {
            return;
        }

        var self = this;
        var accountService = new AccountService();
        accountService.delete(id)
            .then(response => {
                self.loadFromServer();
            })
            .catch(error => {
                alert('Failed to delete\n' + (error.response.data.error));
            });
    }

    render() {
        if (this.state == null || this.state.accounts == null) return null;

        return (
            <div className="container">
                <h2>Account</h2>
                <div className="row mb-3">
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Name" name="nameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" name="statusFilter" onChange={this.filterChanged}>
                            <option value={Constants.ACTIVE_STATUS}>Active</option>
                            <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                        </select>
                    </div>
                    <div className="col-sm-8 text-end">
                        <Link className="btn btn-primary btn-sm" to='/account/0'>+ Add Account</Link>
                    </div>
                </div>
                <div className="row">
                    <AccountTable accounts={this.state.accounts}
                        handleDelete={this.handleDelete.bind(this)}
                        nameFilter={this.state.nameFilter}
                        statusFilter={this.state.statusFilter} />
                </div>
            </div>
        );
    }
}