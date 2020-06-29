import React from "react";

export default class ProjectReportTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { report: this.props.report };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null)
            return null;

        var rows = [];
        this.state.report.projectReports.forEach(function (projectReport) {
            rows.push(
                <tr>
                    <td>{projectReport.companyName}</td>
                    <td>{projectReport.projectName}</td>
                    <td>{projectReport.totalHours}</td>
                    <td>{projectReport.projectCategory}</td>
                    <td><input type="checkbox" readOnly="true" checked={projectReport.provision} /></td>
                    <td><input type="checkbox" readOnly="true" checked={projectReport.internal} /></td>
                    <td><input type="checkbox" readOnly="true" checked={projectReport.onCall} /></td>
                </tr>);
        });

        return (
            <div className="table-responsive">
                <table className="table">
                    <thead>
                        <tr className="bg-success text-white">
                            <th>Company</th>
                            <th>Project</th>
                            <th>Total</th>
                            <th>Category</th>
                            <th>Provision</th>
                            <th>Internal</th>
                            <th>On call</th>
                        </tr>
                    </thead>
                    <tbody>
                        {rows}
                    </tbody>
                </table>
            </div>
        );
    }
};