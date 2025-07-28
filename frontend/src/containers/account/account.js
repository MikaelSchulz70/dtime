import React from "react";
import { Link } from 'react-router-dom';
import AccountService from '../../service/AccountService';
import *  as Constants from '../../common/Constants';

class AccountTableRow extends React.Component {
    render() {
        if (this.props == null) return null;

        const editRoute = '/organizations/' + this.props.organization.id;

        return (
            <tr>
                <td>{this.props.organization.name}</td>
                <td>{this.props.organization.activationStatus}</td>
                <td><Link className="btn btn-success" to={editRoute}>Edit</Link></td>
                <td><button className="btn btn-success" onClick={() => this.props.handleDelete(this.props.organization.id)} >Delete</button></td>
            </tr>
        );
    }
};

class OrganizationTable extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.state = { organizations: this.props.organizations };
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
        if (this.state == null || this.state.organizations == null) return null;

        var nameFilter = this.props.nameFilter;
        var statusFilter = this.props.statusFilter;

        var filteredOrganizations = this.state.organizations.filter(function (organization) {
            return (organization.activationStatus === statusFilter) &&
                (organization.name.toLowerCase().startsWith(nameFilter.toLowerCase()));
        });

        var handleDelete = this.handleDelete;
        var rows = [];
        filteredOrganizations.forEach(function (organization) {
            rows.push(
                <AccountTableRow organization={organization} key={organization.id} handleDelete={handleDelete} />);
        });

        return (
            <table className="table table-striped">
                <thead className="thead-inverse bg-success">
                    <tr className="text-white">
                        <th>Name</th>
                        <th>Status</th>
                        <th align="right">Edit</th>
                        <th align="right">Delete</th>
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </table>
        );
    }
};

export default class Account extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.filterChanged = this.filterChanged.bind(this);
        this.loadFromServer = this.loadFromServer.bind(this);

        this.state = { nameFilter: '', statusFilter: Constants.ACTIVE_STATUS };
    }

    componentDidMount() {
        this.loadFromServer();
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    loadFromServer() {
        const self = this;
        var organizationService = new AccountService();
        organizationService.getAll()
            .then(response => {
                self.setState({ organizations: response.data });
            })
            .catch(error => {
                alert('Failed to load organizations');
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
        var organizationService = new AccountService();
        organizationService.delete(id)
            .then(response => {
                self.loadFromServer();
            })
            .catch(error => {
                alert('Failed to delete\n' + (error.response.data.error));
            });
    }

    render() {
        if (this.state == null || this.state.organizations == null) return null;

        return (
            <div className="container">
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
                    <div className="col-sm-8">
                        <Link className="btn btn-success float-sm-right" to='/organizations/0'>Add</Link>
                    </div>
                </div>
                <div className="row">
                    <OrganizationTable organizations={this.state.organizations}
                        handleDelete={this.handleDelete.bind(this)}
                        nameFilter={this.state.nameFilter}
                        statusFilter={this.state.statusFilter} />
                </div>
            </div>
        );
    }
};