import React from "react";
import MappleToolTip from 'reactjs-mappletooltip'
import OnCallService from '../../service/OnCallService';

class OnCallDayConfigTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.update = this.update.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.isTimeFormatOk = this.isTimeFormatOk.bind(this);
        this.state = { onCallDayConfig: this.props.onCallDayConfig, startTimeError: false, endTimeError: false };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    isTimeFormatOk(text) {
        if (text == null || text === '') {
            return true;
        }

        return /^([0-1]?[0-9]|2[0-4]):([0-5][0-9])(:[0-5][0-9])?$/.test(text);
    }

    handleChange(event) {
        if (this.state.onCallDayConfig.readOnly) {
            return;
        }

        let field = event.target.name;
        var value = event.target.value;
        if (value != null) {
            value = value.replace(".", ":")
        }

        let onCallDayConfig = JSON.parse(JSON.stringify(this.state.onCallDayConfig));
        onCallDayConfig[field] = value;

        var startTimeError = this.state.startTimeError;
        var endTimeError = this.state.endTimeError;

        var isInputOk = this.isTimeFormatOk(value);
        if (!isInputOk) {
            if (field === 'startTime') {
                startTimeError = true;
            } else if (field === 'endTime') {
                endTimeError = true;
            }
        } else {
            if (field === 'startTime') {
                startTimeError = false;
            } else if (field == 'endTime') {
                endTimeError = false;
            }
        }

        this.setState({ onCallDayConfig: onCallDayConfig, startTimeError: startTimeError, endTimeError: endTimeError });
    }

    update(event) {
        if (this.state.onCallDayConfig.readOnly) {
            return;
        }

        var value = event.target.value;

        var isInputOk = this.isTimeFormatOk(value);
        if (!isInputOk) {
            return;
        }

        if (this.state.startTimeError || this.state.endTimeError) {
            return;
        }

        const self = this;
        var service = new OnCallService();
        service.udateOnCallConfig(this.state.onCallDayConfig)
            .then(response => {
                self.setState({ onCallDayConfig: this.state.onCallDayConfig });
            })
            .catch(error => {
                var errorMsg = error.response.data.error != null ? error.response.data.error : 'Failed to update';
                alert(errorMsg);
            });
    }

    render() {
        if (this.state == null) return null;

        var isReadOnly = this.state.onCallDayConfig.isReadOnly;

        var startTimeShort = '';
        if (this.state.onCallDayConfig.startTime != null) {
            var startTime = this.state.onCallDayConfig.startTime;
            startTimeShort = startTime.substring(0, Math.min(5, startTime.length));
        }

        var endTimeShort = '';
        if (this.state.onCallDayConfig.endTime != null) {
            var endTime = this.state.onCallDayConfig.endTime;
            endTimeShort = endTime.substring(0, Math.min(5, endTime.length));
        }

        var startTimeClass = '';
        if (this.state.startTimeError) {
            startTimeClass = 'border border-danger'
        }

        var endTimeClass = '';
        if (this.state.endTimeError) {
            endTimeClass = 'border border-danger'
        }

        return (
            <tr>
                <td></td>
                <td></td>
                <td>{this.state.onCallDayConfig.dayOfWeek}</td>
                <td><input className={startTimeClass} readOnly={isReadOnly} name="startTime" type="text" value={startTimeShort} maxLength="5" onChange={this.handleChange} onBlur={this.update} /></td>
                <td><input className={endTimeClass} readOnly={isReadOnly} name="endTime" type="text" value={endTimeShort} maxLength="5" onChange={this.handleChange} onBlur={this.update} /></td>
            </tr>
        );
    }
};

class OnCallPropertyTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { onCallPhoneNumber: this.props.onCallPhoneNumber };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null) return null;

        return (
            <div className="container">
                <div className="row">
                    <div className="table-responsive">
                        <table className="table table-bordered">
                            <tbody>
                                <tr>
                                    <td>OnCall phone number</td>
                                    <td><input className="form-control" readOnly={true} type="text" value={this.state.onCallPhoneNumber} name="value" maxLength="100" /></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        );
    }
};

class OnCallConfigTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { onCallProjectConfigs: this.props.onCallProjectConfigs };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null || this.state.onCallProjectConfigs == null) return null;

        var rows = [];

        this.state.onCallProjectConfigs.forEach(function (onCallProjectConfig) {
            var i = 0;
            var key = onCallProjectConfig.idProject + '_' + i;
            i++;
            rows.push(<tr key={key} className="bg-success text-white">
                <td>{onCallProjectConfig.companyName}</td>
                <td>{onCallProjectConfig.projectName}</td>
                <td>Day</td>
                <td>Start time</td>
                <td>End time</td>
            </tr>
            );

            onCallProjectConfig.onCallDayConfigs.forEach(function (onCallDayConfig) {
                var key1 = onCallProjectConfig.idProject + '_' + onCallDayConfig.dayOfWeek;
                rows.push(
                    <OnCallDayConfigTableRow key={key1} onCallDayConfig={onCallDayConfig} />);
            });
        });

        return (
            <div className="container">
                <div className="row">
                    <div className="col-sm-12">
                        <div className="float-right">
                            <MappleToolTip float={true} direction={'bottom'} mappleType={'info'} >
                                <div className="btn btn-primary">
                                    Info
                                </div>
                                <div>
                                    <ul>
                                        <li>No start time and no end time no on call this day</li>
                                        <li>No start time means from beginning of day</li>
                                        <li>No end time means until end of day</li>
                                        <li>Time format 10:00</li>
                                    </ul>
                                </div>
                            </MappleToolTip>
                        </div>
                    </div>
                </div>
                <div className="row">
                    <div className="table-responsive">
                        <table className="table table-bordered">
                            <tbody>
                                {rows}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        );
    }
};

export default class OnCallConfig extends React.Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.loadFromServer();
    }

    loadFromServer(view) {
        const self = this;
        var service = new OnCallService();
        service.getOnCallConfig()
            .then(response => {
                self.setState({ onCallConfig: response.data });
            })
            .catch(error => {
                alert('Failed to load on call config');
            });
    }

    render() {
        if (this.state == null || this.state.onCallConfig == null)
            return null;

        return (
            <div>
                <OnCallPropertyTable onCallPhoneNumber={this.state.onCallConfig.onCallPhoneNumber} readOnly={this.state.onCallConfig.readOnly} />
                <OnCallConfigTable onCallProjectConfigs={this.state.onCallConfig.onCallProjectConfigs} />
            </div>
        );
    }
};
