import React from "react";
import { Link } from 'react-router-dom';
import OnCallRuleDetailsService from '../../service/OnCallRuleDetailsService';


class OnCallRuleTableRow extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        if (this.props == null) return null;

        const editRoute = '/oncall/rules/' + this.props.rule.project.id;

        return (
            <tr>
                <td>{this.props.rule.project.company.name}</td>
                <td>{this.props.rule.project.name}</td>
                <td>{this.props.rule.fromMail}</td>
                <td><Link className="btn btn-success" to={editRoute}>Edit</Link></td>
            </tr>
        );
    }
}

class OnCallRuleTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { projectNameFilter: "", companyNameFilter: "", rules: this.props.rules };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null || this.state.rules == null) return null;

        var projectNameFilter = this.props.projectNameFilter;
        var companyNameFilter = this.props.companyNameFilter;

        var filteredRules = this.state.rules.filter(function (rule) {
            return (rule.project.name.toLowerCase().startsWith(projectNameFilter.toLowerCase())) &&
                (rule.project.company.name.toLowerCase().startsWith(companyNameFilter.toLowerCase()));
        });

        var rows = [];
        var i = 0;
        filteredRules.forEach(function (rule) {
            i++;
            rows.push(
                <OnCallRuleTableRow key={i} rule={rule} />);
        });

        return (
            <table className="table table-striped">
                <thead className="thead-inverse bg-success">
                    <tr className="text-white">
                        <th>Company name</th>
                        <th>Project name</th>
                        <th>Mapping</th>
                        <th align="right">Edit</th>
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </table>
        );
    }
};

export default class OnCallRules extends React.Component {
    constructor(props) {
        super(props);
        this.filterChanged = this.filterChanged.bind(this);
        this.state = { companyNameFilter: '', projectNameFilter: '' };
    }

    componentDidMount() {
        this.loadFromServer();
    }

    loadFromServer() {
        const self = this;
        var service = new OnCallRuleDetailsService();
        service.getAll()
            .then(response => {
                self.setState({ rules: response.data });
            })
            .catch(error => {
                alert('Failed to load on call rules');
            });
    }

    filterChanged(event) {
        const value = event.target.value;
        const name = event.target.name;
        this.setState({ [name]: value });
    }

    render() {
        if (this.state == null || this.state.rules == null) return null;

        return (
            <div className="container">
                <div className="row mb-3">
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Company" name="companyNameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Project" name="projectNameFilter" onChange={this.filterChanged} />
                    </div>
                </div>
                <div className="row">
                    <OnCallRuleTable rules={this.state.rules}
                        companyNameFilter={this.state.companyNameFilter}
                        projectNameFilter={this.state.projectNameFilter}
                    />
                </div>
            </div>
        );
    }
};

