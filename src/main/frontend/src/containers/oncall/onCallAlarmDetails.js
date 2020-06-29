import React from "react";
import OnCallAlarmService from '../../service/OnCallAlarmService';
import BaseDetails from '../BaseDetails';

const STATUS_NEW = 'NEW';
const STATUS_IN_PROGRESS = 'IN_PROGRESS';
const STATUS_DONE = 'DONE';

const SEVERITY_INFO = 'INFO';
const SEVERITY_WARNING = 'WARNING';
const SEVERITY_ERROR = 'ERROR';

export default class OnCallAlarmDetails extends BaseDetails {

    constructor(props) {
        super(props);
        this.handleCreateUpdate = this.handleCreateUpdate.bind(this);
        this.cancelEdit = this.cancelEdit.bind(this);
        this.statusChanged = this.statusChanged.bind(this);

        this.state = { alarmId: this.props.match.params.alarmId };
    }

    componentDidMount() {
        const self = this;
        const service = new OnCallAlarmService();
        service.get(this.state.alarmId)
            .then(response => {
                self.setState({ alarm: response.data });
            })
            .catch(error => {
                alert('Failed to fetch alarms');
            });
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    statusChanged(event) {
        let alarm = this.state.alarm;
        let field = event.target.name;
        let value = event.target.value;
        alarm[field] = value;

        var self = this;
        const userService = new OnCallAlarmService();
        userService.addOrUdate(alarm)
            .then(response => {
                self.setState(() => ({ alarm: alarm }));
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    cancelEdit() {
        this.props.history.push('/oncall/alarms');
    }

    handleCreateUpdate() {
        this.clearErrors();
        const self = this;
        const service = new OnCallAlarmService();
        service.addOrUdate(this.state.alarm)
            .then(response => {
                self.props.history.push('/oncall/alarms');
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    handleChange(event) {
        let alarm = JSON.parse(JSON.stringify(this.state.alarm));
        let field = event.target.name;
        let value = event.target.value;
        alarm[field] = value;

        this.setState(() => ({ alarm: alarm }));
    }

    render() {
        if (this.state == null || this.state.alarm == null)
            return null;

        var handleCreateUpdate = this.handleCreateUpdate;
        var cancelEdit = this.cancelEdit;
        var id = this.state.alarm.id;
        var isAdd = (id === 0);
        var buttonText = (isAdd ? "Add" : "Update");

        return (
            <div className="container">
                <div className="table-responsive">
                    <table className="table-sm">
                        <tbody>
                            <tr>
                                <th>Company</th>
                                <td>
                                    <input className="form-control input-sm" readOnly="true" type="text" value={this.state.alarm.companyName} />
                                </td>
                            </tr>
                            <tr>
                                <th>Project</th>
                                <td>
                                    <input className="form-control input-sm" readOnly="true" type="text" value={this.state.alarm.projectName} />
                                </td>
                            </tr>
                            <tr>
                                <th>User</th>
                                <td>
                                    <input className="form-control input-sm" readOnly="true" type="text" value={this.state.alarm.userName} />
                                </td>
                            </tr>
                            <tr>
                                <th>Received</th>
                                <td>
                                    <input className="form-control input-sm" readOnly="true" type="text" value={this.state.alarm.dateTime} />
                                </td>
                            </tr>
                            <tr>
                                <th>Status</th>
                                <td>
                                    <select className="form-control input-sm" value={this.state.alarm.status} name="status" onChange={this.statusChanged}>
                                        <option value="NEW">New</option>
                                        <option value="IN_PROGRESS">In progress</option>
                                        <option value="DONE">Done</option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <th>Severity</th>
                                <td>
                                    <input className="form-control input-sm" readOnly="true" type="text" value={this.state.alarm.onCallSeverity} />
                                </td>
                            </tr>
                            <tr>
                                <th>Email sent</th>
                                <td>
                                    <input className="input-sm" type="checkbox" readOnly="true" checked={this.state.alarm.emailSent} />
                                </td>
                            </tr>
                            <tr>
                                <th>Sms sent</th>
                                <td>
                                    <input className="input-sm" type="checkbox" readOnly="true" checked={this.state.alarm.instantMsgSent} />
                                </td>
                            </tr>
                            <tr>
                                <th>From</th>
                                <td>
                                    <input className="form-control input-sm" readOnly="true" type="text" value={this.state.alarm.sender} />
                                </td>
                            </tr>
                            <tr>
                                <th>Subject</th>
                                <td>
                                    <input className="form-control input-sm" readOnly="true" type="text" value={this.state.alarm.subject} />
                                </td>
                            </tr>
                            <tr>
                                <th>Message</th>
                                <td>
                                    <textarea className="form-control input-sm" rows="4" cols="50" readOnly="true">
                                        {this.state.alarm.message}
                                    </textarea>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div className="form-group row">
                    <div className="col-sm-7">
                        <button className="btn btn-success float-sm-right" onClick={() => handleCreateUpdate(id)}>{buttonText}</button>
                    </div>
                    <div className="col-sm-5">
                        <button className="btn btn-success" onClick={() => cancelEdit()}>Cancel</button>
                    </div>
                </div>
            </div>
        );
    }
};