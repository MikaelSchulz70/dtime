import React from "react";
import SystemService from '../../service/SystemService';

class SpecialDaysTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { specialDays: this.props.specialDays };
    }

    render() {
        if (this.state == null || this.state.specialDays == null) return null;

        var specialDaysRows = [];
        this.state.specialDays.forEach(function (specialDay) {
            specialDaysRows.push(
                <tr key={specialDay.id}>
                    <td>{specialDay.name}</td>
                    <td>{specialDay.dayType}</td>
                    <td>{specialDay.date}</td>
                </tr>
            );

        });

        return (
            <div className="row">
                <h5>Special days</h5>
                <table className="table table-striped">
                    <thead className="thead-inverse bg-success text-white">
                        <tr>
                            <th>Name</th>
                            <th>Day type</th>
                            <th>Date</th>
                        </tr>
                    </thead>
                    <tbody>{specialDaysRows}</tbody>
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
        service.updateProperty(this.state.systemProperty)
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
        tables.push(<SpecialDaysTable key={'specialDayTable'} specialDays={this.state.systemConfig.specialDays} />);
        tables.push(<SystemPropertyTable key={'propTable'} systemProperties={this.state.systemConfig.systemProperties} />);

        return (
            <div className="container">
                {tables}
            </div>
        );
    }
};