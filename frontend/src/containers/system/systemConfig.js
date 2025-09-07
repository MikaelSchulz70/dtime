import React from "react";
import SystemService from '../../service/SystemService';
import { useToast } from '../../components/Toast';

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
                this.props.showError('Failed to update configuration: ' + (error.response?.data?.error || error.message));
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
        var showError = this.props.showError;
        this.state.systemProperties.forEach(function (systemProperty) {
            systemPropRows.push(
                <SystemPropertyRow key={systemProperty.id} systemProperty={systemProperty} showError={showError} />
            );
        });

        return (
            <div className="row">
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
}


class SystemConfig extends React.Component {
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
                this.props.showError('Failed to load configuration: ' + (error.response?.data?.message || error.message));
            });
    }

    render() {
        if (this.state == null) return null;

        return (
            <div className="container">
                <h2>System Properties</h2>
                <SystemPropertyTable systemProperties={this.state.systemConfig.systemProperties} showError={this.props.showError} />
            </div>
        );
    }
}

export default function SystemConfigWithToast(props) {
    const { showError } = useToast();
    return <SystemConfig {...props} showError={showError} />;
}