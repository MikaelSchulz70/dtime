import React from "react";
import ActivityService from '../../service/ActivityService';
import BaseDetails from '../BaseDetails';

export default class ActivityDetails extends BaseDetails {

    constructor(props) {
        super(props);
        this.handleCreateUpdate = this.handleCreateUpdate.bind(this);
        this.handleChange = this.handleChange.bind(this);

        const activity = { id: '0', description: '' };
        this.state = { activity: activity };
    }

    componentDidMount() {
    }

    handleCreateUpdate() {
        if (this.state.activity.description === null || this.state.activity.description === '') {
            return;
        }

        this.clearErrors();
        const self = this;
        const service = new ActivityService();
        service.addOrUdate(this.state.activity)
            .then(response => {
                self.props.history.push('/misc/activities');
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    canelAddEdit() {
        this.props.history.push('/misc/activities');
    }

    handleChange(event) {
        let activity = JSON.parse(JSON.stringify(this.state.activity));
        let field = event.target.name;
        let value = event.target.value;
        activity[field] = value;

        this.setState(() => ({ activity: activity }));
    }

    render() {
        if (this.state == null || this.state.activity == null)
            return null;

        var handleCreateUpdate = this.handleCreateUpdate;
        var canelAddEdit = this.canelAddEdit;

        return (
            <div className="container">
                <div className="form-group row">
                    <label className="col-sm-2 col-form-label">Description</label>
                    <div className="col-sm-8">
                        <input className="form-control" maxLength="100" type="text" value={this.state.activity.description} name="description" onChange={this.handleChange} />
                    </div>
                </div>
                <div className="form-group row">
                    <div className="col-sm-10">
                        <button className="btn btn-success float-sm-right" onClick={() => canelAddEdit()}>Cancel</button>
                        <button className="btn btn-success float-sm-right mr-5" onClick={() => handleCreateUpdate()}>Add</button>
                    </div>
                </div>
            </div>
        );
    }
};