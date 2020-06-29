import React from "react";
import { Link } from 'react-router-dom';
import CompanyService from '../../service/CompanyService';
import *  as Constants from '../../common/Constants';

class CompanyTableRow extends React.Component {
    render() {
        if (this.props == null) return null;

        const editRoute = '/companies/' + this.props.company.id;

        return (
            <tr>
                <td>{this.props.company.name}</td>
                <td>{this.props.company.activationStatus}</td>
                <td><Link className="btn btn-success" to={editRoute}>Edit</Link></td>
                <td><button className="btn btn-success" onClick={() => this.props.handleDelete(this.props.company.id)} >Delete</button></td>
            </tr>
        );
    }
};

class CompanyTable extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.state = { companies: this.props.companies };
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
        if (this.state == null || this.state.companies == null) return null;

        var nameFilter = this.props.nameFilter;
        var statusFilter = this.props.statusFilter;

        var filteredCompanies = this.state.companies.filter(function (company) {
            return (company.activationStatus === statusFilter) &&
                (company.name.toLowerCase().startsWith(nameFilter.toLowerCase()));
        });

        var handleDelete = this.handleDelete;
        var rows = [];
        filteredCompanies.forEach(function (company) {
            rows.push(
                <CompanyTableRow company={company} key={company.id} handleDelete={handleDelete} />);
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

export default class Companies extends React.Component {
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
        var companyService = new CompanyService();
        companyService.getAll()
            .then(response => {
                self.setState({ companies: response.data });
            })
            .catch(error => {
                alert('Failed to load companies');
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
        var companyService = new CompanyService();
        companyService.delete(id)
            .then(response => {
                self.loadFromServer();
            })
            .catch(error => {
                alert('Failed to delete\n' + (error.response.data.error));
            });
    }

    render() {
        if (this.state == null || this.state.companies == null) return null;

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
                        <Link className="btn btn-success float-sm-right" to='/companies/0'>Add</Link>
                    </div>
                </div>
                <div className="row">
                    <CompanyTable companies={this.state.companies}
                        handleDelete={this.handleDelete.bind(this)}
                        nameFilter={this.state.nameFilter}
                        statusFilter={this.state.statusFilter} />
                </div>
            </div>
        );
    }
};