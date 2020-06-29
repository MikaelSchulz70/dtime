import React from "react";
import { Link } from 'react-router-dom';
import ActivityService from '../../service/ActivityService';

class ActivityTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = { activity: this.props.activity };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null || this.state.activity == null) return null;

        const voteText = (this.state.activity.voted ? 'Unvote' : 'Vote');

        return (
            <tr>
                <td>{this.state.activity.description}</td>
                <td>{this.state.activity.noOfVotes}</td>
                <td>{this.state.activity.addedBy}</td>
                <td><button className="btn btn-success" onClick={() => this.props.handleVote(this.state.activity.id)} >{voteText}</button></td>
                <td><button className="btn btn-success" onClick={() => this.props.handleDelete(this.state.activity.id)} >Delete</button></td>
            </tr>
        );
    }
};

class ActivityTable extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.handleVote = this.handleVote.bind(this);
        this.state = { activities: this.props.activities };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    handleDelete(id) {
        this.props.handleDelete(id);
    }

    handleVote(id) {
        this.props.handleVote(id);
    }
    render() {
        if (this.state == null || this.state.activities == null) return null;

        var handleDelete = this.handleDelete;
        var handleVote = this.handleVote;
        var rows = [];
        this.state.activities.forEach(function (activity) {
            rows.push(
                <ActivityTableRow activity={activity} key={activity.id}
                    handleDelete={handleDelete} handleVote={handleVote} />);
        });

        return (
            <table className="table table-striped">
                <thead className="thead-inverse bg-success">
                    <tr className="text-white">
                        <th>Description</th>
                        <th>Number of votes</th>
                        <th>Added by</th>
                        <th align="right">Vote</th>
                        <th align="right">Delete</th>
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </table>
        );
    }
};

export default class Activities extends React.Component {
    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
        this.handleVote = this.handleVote.bind(this);
        this.loadFromServer = this.loadFromServer.bind(this);
        this.resetVotes = this.resetVotes.bind(this);
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
        var activityService = new ActivityService();
        activityService.getAll()
            .then(response => {
                self.setState({ activities: response.data });
            })
            .catch(error => {
                alert('Failed to load activities');
            });
    }

    resetVotes() {
        const shallReset = confirm('Are you really sure you want reset votes?');
        if (!shallReset) {
            return;
        }

        const self = this;
        var activityService = new ActivityService();
        activityService.resetVotes()
            .then(response => {
                self.loadFromServer();
            })
            .catch(error => {
                alert('Failed to reset votes');
            });
    }

    handleVote(id) {
        var self = this;
        var activityService = new ActivityService();
        activityService.vote(id)
            .then(response => {
                self.loadFromServer();
            })
            .catch(error => {
                alert('Failed to vote\n' + (error.response.data.error));
            });
    }

    handleDelete(id) {
        const shallDelete = confirm('Are you really sure you want to delete?');
        if (!shallDelete) {
            return;
        }

        var self = this;
        var activityService = new ActivityService();
        activityService.delete(id)
            .then(response => {
                self.loadFromServer();
            })
            .catch(error => {
                alert('Failed to delete\n' + (error.response.data.error));
            });
    }

    render() {
        if (this.state == null || this.state.activities == null) return null;

        return (
            <div className="container">
                <div className="row">
                    <div className="col-sm-12">
                        <button className="btn btn-success float-sm-right" onClick={() => this.resetVotes()}>Reset votes</button>
                        <Link className="btn btn-success float-sm-right mr-4" to='/misc/activities/add'>Add</Link>
                    </div>
                </div>
                <div className="row">
                    <ActivityTable activities={this.state.activities}
                        handleDelete={this.handleDelete.bind(this)}
                        handleVote={this.handleVote.bind(this)} />
                </div>
            </div>
        );
    }
};