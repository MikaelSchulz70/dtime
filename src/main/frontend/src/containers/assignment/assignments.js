import React from "react";
import AssignmentService from '../../service/AssignmentService';
import UserService from '../../service/UserService';
import *  as Constants from '../../common/Constants';


class AssignmentTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.state = { assignment: this.props.assignment };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    handleChange(event) {
        let assignment = JSON.parse(JSON.stringify(this.state.assignment));
        let field = event.target.name;
        let value = event.target.value;
        assignment[field] = value;

        const self = this;
        const assignmentService = new AssignmentService();
        assignmentService.udate(assignment)
            .then(response => {
                self.props.activationStatusChanged(assignment);
            })
            .catch(error => {
                alert('Failed to update');
            });
    }

    render() {
        if (this.state == null || this.state.assignment == null) return null;

        return (
            <tr>
                <td>{this.state.assignment.project.company.name}</td>
                <td>{this.state.assignment.project.name}</td>
                <td><input type="checkbox" readOnly={true} checked={this.state.assignment.project.provision} /></td>
                <td><input type="checkbox" readOnly={true} checked={this.state.assignment.project.internal} /></td>
                <td><input type="checkbox" readOnly={true} checked={this.state.assignment.project.onCall} /></td>
                <td>{this.state.assignment.project.projectCategory}</td>
                <td>
                    <select className="form-control input-sm" name="activationStatus" value={this.state.assignment.activationStatus} onChange={this.handleChange}>
                        <option value={Constants.ACTIVE_STATUS}>Active</option>
                        <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                    </select>
                </td>
            </tr>
        );
    }
};

class AssignmentTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { assignments: this.props.assignments };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null) return null;

        var companyNameFilter = this.props.companyNameFilter;
        var projectNameFilter = this.props.projectNameFilter;
        var statusFilter = this.props.statusFilter;

        if (this.state.assignments != null) {
            var filteredAssignments = this.state.assignments.filter(function (assignment) {
                return (assignment.activationStatus === statusFilter) &&
                    (assignment.project.company.name.toLowerCase().startsWith(companyNameFilter.toLowerCase())) &&
                    (assignment.project.name.toLowerCase().startsWith(projectNameFilter.toLowerCase()));
            });

            var self = this;
            var rows = [];
            filteredAssignments.forEach(function (assignment) {
                var key = assignment.project.company.id + '_' + assignment.project.id;
                rows.push(
                    <AssignmentTableRow assignment={assignment} key={key} activationStatusChanged={self.props.activationStatusChanged} />);
            });
        }

        return (
            <table className="table table-striped">
                <thead className="thead-inverse bg-success">
                    <tr className="text-white">
                        <th>Company</th>
                        <th>Project</th>
                        <th>Provision</th>
                        <th>Internal</th>
                        <th>On call</th>
                        <th>Category</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </table>
        );
    }
};

export default class Assignment extends React.Component {
    constructor(props) {
        super(props);
        this.loadFromServer = this.loadFromServer.bind(this);
        this.filterChanged = this.filterChanged.bind(this);
        this.handleUserChange = this.handleUserChange.bind(this);
        this.activationStatusChanged = this.activationStatusChanged.bind(this);

        this.state = { companyNameFilter: '', projectNameFilter: '', statusFilter: Constants.ACTIVE_STATUS };
    }

    componentDidMount() {
        this.loadFromServer();
    }

    loadFromServer() {
        const self = this;
        const userService = new UserService();
        userService.getByStatus(true)
            .then(response => {
                self.setState({ users: response.data });
            })
            .catch(error => {
                alert('Failed to load users');
            });
    }

    activationStatusChanged(updatedAssignment) {
        let assignments = JSON.parse(JSON.stringify(this.state.assignments));

        for (var i in assignments) {
            if (assignments[i].project.id === updatedAssignment.project.id &&
                assignments[i].project.company.id === updatedAssignment.project.company.id) {
                assignments[i] = updatedAssignment;
                this.setState({ assignments: assignments });
                break;
            }
        }
    }

    filterChanged(event) {
        var value = event.target.value;
        const name = event.target.name;
        this.setState({ [name]: value });
    }

    handleUserChange(event) {
        const userId = parseInt(event.target.value, 10);
        if (userId === 0) {
            this.setState({ companyNameFilter: '', projectNameFilter: '', statusFilter: Constants.ACTIVE_STATUS });
            return;
        }

        const self = this;
        const assignmentService = new AssignmentService();
        assignmentService.getAssginmentForUser(userId)
            .then(response => {
                self.setState({ assignments: response.data });
            })
            .catch(error => {
                alert('Failed to load assignments');
            });
    }

    render() {
        if (this.state == null || this.state.users == null) return null;


        let userOptions = this.state.users.map(u => (
            <option value={u.id} key={u.id} >{u.firstName + ' ' + u.lastName}</option>
        ));

        userOptions.unshift(<option value={0} key={0}>{''}</option>);

        return (
            <div className="container">
                <div className="row mb-3">
                    <div className="col-sm-3">
                        <select className="form-control dataLiveSearch" value={this.state.userId} name="user.id" onChange={this.handleUserChange}>
                            {userOptions}
                        </select>
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Company" name="companyNameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Project" name="projectNameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <select className="form-control input-sm" name="statusFilter" onChange={this.filterChanged}>
                            <option value={Constants.ACTIVE_STATUS}>Active</option>
                            <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                        </select>
                    </div>
                </div>
                <div className="row">
                    <AssignmentTable
                        assignments={this.state.assignments}
                        companyNameFilter={this.state.companyNameFilter}
                        projectNameFilter={this.state.projectNameFilter}
                        statusFilter={this.state.statusFilter}
                        activationStatusChanged={this.activationStatusChanged} />
                </div>
            </div>
        );
    }
};

