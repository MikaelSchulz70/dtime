import React from "react";
import SystemService from '../../service/SystemService';

class PublicHolidayTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { publicHolidays: this.props.publicHolidays };
    }

    render() {
        if (this.state == null || this.state.publicHolidays == null) return null;

        var publicHolidaysRows = [];
        this.state.publicHolidays.forEach(function (publicHoliday) {
            publicHolidaysRows.push(
                <tr key={publicHoliday.id}>
                    <td>{publicHoliday.name}</td>
                    <td><input type="checkbox" checked={publicHoliday.workday} readOnly={true} /></td>
                </tr>
            );

        });

        return (
            <div className="row">
                <h5>Public holidays</h5>
                <table className="table table-striped">
                    <thead className="thead-inverse bg-success text-white">
                        <tr>
                            <th>Name</th>
                            <th>Workday</th>
                        </tr>
                    </thead>
                    <tbody>{publicHolidaysRows}</tbody>
                </table>
            </div>
        );
    }
};

class SystemPropertyRow extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.update = this.update.bind(this);
        this.state = { systemProperty: this.props.systemProperty };
    }

    handleChange(event) {
        let systemProperty = JSON.parse(JSON.stringify(this.state.systemProperty));
        let field = event.target.name;
        let value = event.target.value;
        systemProperty[field] = value;
        this.setState(() => ({ systemProperty: systemProperty }));
    }

    update(event) {
        var self = this;
        var service = new SystemService();
        service.udateProperty(this.state.systemProperty)
            .then(response => {
                self.setState({ systemConfig: response.data });
            })
            .catch(error => {
                alert(error.response.data.error);
            });
    }

    render() {
        if (this.state == null || this.state.systemProperty == null) return null;

        return (
            <tr>
                <td>{this.state.systemProperty.name}</td>
                <td><input className="form-control" type="text" value={this.state.systemProperty.value} name="value" maxLength="100" onChange={this.handleChange} onBlur={this.update} /></td>
                <td>{this.state.systemProperty.systemPropertyType}</td>
                <td>{this.state.systemProperty.description}</td>
            </tr>
        );
    }
}

class SystemPropertyTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { systemProperties: this.props.systemProperties };
    }

    render() {
        if (this.state == null || this.state.systemProperties == null) return null;

        var systemPropRows = [];
        this.state.systemProperties.forEach(function (systemProperty) {
            systemPropRows.push(
                <SystemPropertyRow key={systemProperty.id} systemProperty={systemProperty} />
            );
        });

        return (
            <div className="row">
                <h5>System properties</h5>
                <table className="table table-striped">
                    <thead className="thead-inverse bg-success text-white">
                        <tr>
                            <th>Name</th>
                            <th>Value</th>
                            <th>Type</th>
                            <th>Description</th>
                        </tr>
                    </thead>
                    <tbody>{systemPropRows}</tbody>
                </table>
            </div>
        );
    }
};


export default class SystemConfig extends React.Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.loadFromServer();
    }

    loadFromServer() {
        const self = this;
        var service = new SystemService();
        service.getSystemConfig()
            .then(response => {
                self.setState({ systemConfig: response.data });
            })
            .catch(error => {
                alert('Failed to load configuration');
            });
    }

    render() {
        if (this.state == null) return null;

        var tables = [];
        tables.push(<PublicHolidayTable key={'holydayTable'} publicHolidays={this.state.systemConfig.publicHolidays} />);
        tables.push(<SystemPropertyTable key={'propTable'} systemProperties={this.state.systemConfig.systemProperties} />);

        return (
            <div className="container">
                {tables}
            </div>
        );
    }
};