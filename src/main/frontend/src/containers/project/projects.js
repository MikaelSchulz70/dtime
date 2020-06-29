import React from "react";
import { Link } from 'react-router-dom';
import ProjectService from '../../service/ProjectService';
import *  as Constants from '../../common/Constants';

class ProjectTableRow extends React.Component {
    render() {
        if (this.props == null) return null;

        const editRoute = '/projects/' + this.props.project.id;

        return (
            <tr>
                <td>{this.props.project.company.name}</td>
                <td>{this.props.project.name}</td>
                <td><input type="checkbox" readOnly={true} checked={this.props.project.internal} /></td>
                <td><input type="checkbox" readOnly={true} checked={this.props.project.provision} /></td>
                <td><input type="checkbox" readOnly={true} checked={this.props.project.onCall} /></td>
                <td><input type="checkbox" readOnly={true} checked={this.props.project.fixRate} /></td>
                <td>{this.props.project.projectCategory}</td>
                <td>{this.props.project.activationStatus}</td>
                <td><Link className="btn btn-success" to={editRoute}>Edit</Link></td>
                <td><button className="btn btn-success" onClick={() => this.props.handleDelete(this.props.project.id)} >Delete</button></td>
            </tr>
        );
    }
};

class ProjectTable extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.state = { projects: this.props.projects };
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
        if (this.state == null || this.state.projects == null) return null;

        var nameFilter = this.props.nameFilter;
        var companyNameFilter = this.props.companyNameFilter;
        var statusFilter = this.props.statusFilter;
        var categoryFilter = this.props.categoryFilter;

        var filteredProjects = this.props.projects.filter(function (project) {
            return (project.activationStatus === statusFilter) &&
                (project.name.toLowerCase().startsWith(nameFilter.toLowerCase())) &&
                (project.projectCategory === categoryFilter || '' === categoryFilter) &&
                (project.company.name.toLowerCase().startsWith(companyNameFilter.toLowerCase()));
        });

        var handleDelete = this.handleDelete;
        var rows = [];
        filteredProjects.forEach(function (project) {
            rows.push(
                <ProjectTableRow project={project} key={project.id} handleDelete={handleDelete} />);
        });

        return (
            <table className="table table-striped">
                <thead className="thead-inverse bg-success">
                    <tr className="text-white">
                        <th>Company name</th>
                        <th>Name</th>
                        <th>Internal</th>
                        <th>Provision</th>
                        <th>On call</th>
                        <th>Fix rate</th>
                        <th>Category</th>
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

export default class Projects extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.filterChanged = this.filterChanged.bind(this);
        this.loadFromServer = this.loadFromServer.bind(this);

        this.state = { companyNameFilter: '', nameFilter: '', statusFilter: Constants.ACTIVE_STATUS, categoryFilter: '' };
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
        var service = new ProjectService();
        service.getAll()
            .then(response => {
                self.setState({ projects: response.data });
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
        var service = new ProjectService();
        service.delete(id)
            .then(response => {
                self.loadFromServer();
            })
            .catch(error => {
                alert('Failed to delete\n' + (error.response.data.error));
            });
    }

    render() {
        if (this.state == null || this.state.projects == null) return null;

        return (
            <div className="container">
                <div className="row mb-3">
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Company name" name="companyNameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Name" name="nameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" name="statusFilter" onChange={this.filterChanged}>
                            <option value={Constants.ACTIVE_STATUS}>Active</option>
                            <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                        </select>
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" name="categoryFilter" onChange={this.filterChanged}>
                            <option value=""></option>
                            <option value={Constants.DEVELOPMENT_CATEGORY}>Development</option>
                            <option value={Constants.INTEGRATION_CATEGORY}>Integration</option>
                        </select>
                    </div>
                    <div className="col-sm-4">
                        <Link className="btn btn-success float-sm-right" to='/projects/0'>Add</Link>
                    </div>
                </div>
                <div className="row">
                    <ProjectTable projects={this.state.projects}
                        handleDelete={this.handleDelete.bind(this)}
                        nameFilter={this.state.nameFilter}
                        companyNameFilter={this.state.companyNameFilter}
                        statusFilter={this.state.statusFilter}
                        categoryFilter={this.state.categoryFilter} />
                </div>
            </div>
        );
    }
};