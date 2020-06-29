import React from "react";
import { Link } from 'react-router-dom';
import FixRateService from '../../service/FixRateService';

class FixRateTableRow extends React.Component {
    render() {
        if (this.props == null) return null;

        const changeRoute = '/fixrates/' + this.props.rate.idProject;

        var comment = (this.props.rate.comment != null ?
            this.props.rate.comment.substring(0, Math.min(20, this.props.rate.comment.length)) : '');

        return (
            <tr>
                <td>{this.props.rate.companyName}</td>
                <td>{this.props.rate.projectName}</td>
                <td>{this.props.rate.rate}</td>
                <td>{this.props.rate.fromDate}</td>
                <td>{this.props.rate.toDate}</td>
                <td>{comment}</td>
                <td><Link className="btn btn-success" to={changeRoute}>Change</Link></td>
            </tr>
        );
    }
};

class FixRateTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { rates: this.props.rates };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null || this.state.rates == null) return null;

        var companyNameFilter = this.props.companyNameFilter;
        var projectNameFilter = this.props.projectNameFilter;

        var filteredRates = this.props.rates.filter(function (rate) {
            return (rate.companyName.toLowerCase().startsWith(companyNameFilter.toLowerCase())) &&
                (rate.projectName.toLowerCase().startsWith(projectNameFilter.toLowerCase()));
        });

        var rows = [];
        var key = 0;
        filteredRates.forEach(function (rate) {
            rows.push(
                <FixRateTableRow rate={rate} key={key} />);
            key++;
        });

        return (
            <table className="table table-striped">
                <thead className="thead-inverse bg-success">
                    <tr className="text-white">
                        <th>Company name</th>
                        <th>Project name</th>
                        <th>Monthly fix rate</th>
                        <th>From date</th>
                        <th>To date</th>
                        <th>Comment</th>
                        <th align="right">Change</th>
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </table>
        );
    }
};

export default class FixRates extends React.Component {
    constructor(props) {
        super(props);
        this.filterChanged = this.filterChanged.bind(this);
        this.loadFromServer = this.loadFromServer.bind(this);

        this.state = { companyNameFilter: '', projectNameFilter: '', userNameFilter: '' };
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
        var service = new FixRateService();
        service.getCurrentFixRates()
            .then(response => {
                self.setState({ rates: response.data });
            })
            .catch(error => {
                alert('Failed to load fix rates');
            });
    }

    filterChanged(event) {
        const value = event.target.value;
        const name = event.target.name;
        this.setState({ [name]: value });
    }

    render() {
        if (this.state == null || this.state.rates == null) return null;

        return (
            <div className="container-fluid ml-4">
                <div className="row mb-3">
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Company name" name="companyNameFilter" onChange={this.filterChanged} />
                    </div>
                    <div className="col-sm-2">
                        <input className="form-control input-sm" type="text" placeholder="Project name" name="projectNameFilter" onChange={this.filterChanged} />
                    </div>
                </div>
                <div className="row">
                    <FixRateTable rates={this.state.rates}
                        companyNameFilter={this.state.companyNameFilter}
                        projectNameFilter={this.state.projectNameFilter} />
                </div>
            </div>
        );
    }
};