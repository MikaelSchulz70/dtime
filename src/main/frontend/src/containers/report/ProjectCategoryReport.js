import React from "react";

export default class ProjectCategoryReportTable extends React.Component {
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
        this.state.report.projectCategoryReports.forEach(function (projectCategoryReport) {
            rows.push(
                <tr className="bg-success text-white">
                    <th>{projectCategoryReport.projectCategory}</th>
                    <th>Provision</th>
                    <th>Internal</th>
                    <th>On call</th>
                    <th>Total</th>
                </tr>);

            projectCategoryReport.projectSubCategoryReports.forEach(function (projectSubCategoryReport) {
                rows.push(
                    <tr>
                        <td></td>
                        <td><input type="checkbox" readOnly="true" checked={projectSubCategoryReport.provision} /></td>
                        <td><input type="checkbox" readOnly="true" checked={projectSubCategoryReport.internal} /></td>
                        <td><input type="checkbox" readOnly="true" checked={projectSubCategoryReport.onCall} /></td>
                        <td>{projectSubCategoryReport.totalHours}</td>
                    </tr>);
            });

            rows.push(
                <tr>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th>{projectCategoryReport.totalHours}</th>
                </tr>);
        });

        return (
            <div className="table-responsive">
                <table className="table">
                    <tbody>
                        {rows}
                    </tbody>
                </table>
            </div>
        );
    }
};