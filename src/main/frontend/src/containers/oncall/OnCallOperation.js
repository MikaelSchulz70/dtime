import React from "react";
import OnCallService from '../../service/OnCallService';
import OnCallAlarmService from '../../service/OnCallAlarmService';

class SessionTable extends React.Component {
    constructor(props) {
        super(props);
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    componentDidMount() {
        this.loadFromServer();
    }

    loadFromServer() {
        const self = this;
        var service = new OnCallService();
        service.getOnCallSession()
            .then(response => {
                self.setState({ onCallSession: response.data });
            })
            .catch(error => {
                alert('Failed to fetch on call session');
            });
    }

    render() {
        if (this.state == null || this.state.onCallSession == null) return null;

        return (
            <div className="row">
                <table className="table">
                    <thead className="bg-success">
                        <tr className="text-white">
                            <th>Last poll</th>
                            <th>Read</th>
                            <th>Dispatched</th>
                            <th>Total read</th>
                            <th>Total dispatched</th>
                            <th>Mail in inbox</th>
                            <th>Message</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>{this.state.onCallSession.lastPollDateTime}</td>
                            <td>{this.state.onCallSession.totalEmails}</td>
                            <td>{this.state.onCallSession.dispatchedInLastPoll}</td>
                            <td>{this.state.onCallSession.mailInInboxInLastPoll}</td>
                            <td>{this.state.onCallSession.totalDispatched}</td>
                            <td>{this.state.onCallSession.readInLastPoll}</td>
                            <td>{this.state.onCallSession.message}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        );
    }
};


export default class OnCallOperation extends React.Component {
    constructor(props) {
        super(props);
        this.dispatchOnCallEmails = this.dispatchOnCallEmails.bind(this);
        this.truncateAlarms = this.truncateAlarms.bind(this);
    }

    componentDidMount() {

    }

    dispatchOnCallEmails() {
        var service = new OnCallService();
        service.dispatchOnCallEmails()
            .then(response => {
                alert('Email dispatched');
            })
            .catch(error => {
                alert('Failed to dispatch emails');
            });
    }

    truncateAlarms() {
        var service = new OnCallAlarmService();
        service.truncateAlarms()
            .then(response => {
                alert('Alarms truncated');
            })
            .catch(error => {
                alert('Failed to truncate alarms');
            });
    }

    render() {
        var truncateAlarms = this.truncateAlarms;
        var dispatchOnCallEmails = this.dispatchOnCallEmails;

        return (
            <div className="container">
                <SessionTable />
                <div className="row">
                    <table className="table">
                        <thead className="bg-success">
                            <tr className="text-white">
                                <th></th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>Dispatch on call emails</td>
                                <td>
                                    <button className="btn btn-success" onClick={() => dispatchOnCallEmails()}>Send</button>
                                </td>
                            </tr>
                            <tr>
                                <td>Truncate alarms older than 3 months</td>
                                <td>
                                    <button className="btn btn-success" onClick={() => truncateAlarms()}>Send</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }
};