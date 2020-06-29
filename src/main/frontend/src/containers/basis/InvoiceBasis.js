import React from "react";
import BasisService from '../../service/BasisService';
import { formatAmount } from '../../common/utils';

class InvoiceBasisCompanyRows extends React.Component {
    constructor(props) {
        super(props);
        this.handleInvoiceVerifiedSent = this.handleInvoiceVerifiedSent.bind(this);
        this.state = { invoiceCompanyBasis: this.props.invoiceCompanyBasis };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    handleInvoiceVerifiedSent(event) {
        let field = event.target.name;
        let value = event.target.checked;

        let basis = JSON.parse(JSON.stringify(this.state.invoiceCompanyBasis));
        basis.monthlyCheck[field] = value;

        const self = this;
        var service = new BasisService();
        service.addUpdateMonthlyCheck(basis.monthlyCheck)
            .then(response => {
                self.setState(() => ({ invoiceCompanyBasis: basis }));
            })
            .catch(error => {
                alert('Failed to set check');
            });
    }

    render() {
        if (this.state == null || this.state.invoiceCompanyBasis == null)
            return null;

        var rows = [];
        var companyName = this.state.invoiceCompanyBasis.companyName;
        var companyNameShort = companyName.substring(0, Math.min(20, companyName.length));

        var invoiceVerifiedColor = (this.state.invoiceCompanyBasis.monthlyCheck.invoiceVerified ? 'white' : 'red');
        var invoiceSentColor = (this.state.invoiceCompanyBasis.monthlyCheck.invoiceSent ? 'white' : 'red');

        rows.push(
            <tr className="bg-success text-white" key={1}>
                <th title={companyName}>{companyNameShort}</th>
                <th>Project</th>
                <th>User</th>
                <th>On call</th>
                <th>Fix rate</th>
                <th>Hours</th>
                <th>Customer rate</th>
                <th>Sum customer</th>
                <th>Subcontractor rate</th>
                <th>Sum subcontractor</th>
                <th>Fix rate</th>
                <th>Comment</th>
                <th>
                    <p>
                        <font color={invoiceVerifiedColor}>
                            <abbr className="mr-1" title="Invoice verified">V</abbr>
                        </font>
                        <input className="mr-1" type="checkbox" checked={this.state.invoiceCompanyBasis.monthlyCheck.invoiceVerified} name="invoiceVerified" onChange={this.handleInvoiceVerifiedSent.bind(this)} />
                        <font color={invoiceSentColor}>
                            <abbr className="mr-1" title="Invoice sent">S</abbr>
                        </font>
                        <input className="mr-1" type="checkbox" checked={this.state.invoiceCompanyBasis.monthlyCheck.invoiceSent} name="invoiceSent" onChange={this.handleInvoiceVerifiedSent.bind(this)} />
                    </p>
                </th>
            </tr>
        );

        var key = 1;
        this.state.invoiceCompanyBasis.invoiceFixRateBases.forEach(function (fixRateBasis) {
            var projectName = fixRateBasis.projectName;
            var projectNameShort = projectName.substring(0, Math.min(20, projectName.length));

            key++;
            rows.push(
                <tr key={key}>
                    <td></td>
                    <td title={projectName}>{projectNameShort}</td>
                    <td>Fix rate</td>
                    <td><input type="checkbox" readOnly={true} checked={fixRateBasis.onCall} /></td>
                    <td><input type="checkbox" readOnly={true} checked={true} /></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td>{fixRateBasis.fixRate}</td>
                    <td></td>
                    <td></td>
                </tr>);
        });

        key++;
        if (this.state.invoiceCompanyBasis.invoiceFixRateBases.length > 0) {
            rows.push(
                <tr className="bg-light" key={key}>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th>{this.state.invoiceCompanyBasis.sumFixRate}</th>
                    <th></th>
                    <th></th>
                </tr>);
        }
        key++;
        this.state.invoiceCompanyBasis.invoiceAssignmentBasisOnCall.forEach(function (assignmentBasis) {
            var projectName = assignmentBasis.projectName;
            var userName = assignmentBasis.userName;
            var projectNameShort = projectName.substring(0, Math.min(20, projectName.length));
            var userNameShort = userName.substring(0, Math.min(20, userName.length));
            var comment = assignmentBasis.comment != null ? assignmentBasis.comment : '';
            var commentShort = comment.substring(0, Math.min(20, comment.length));

            key++;
            rows.push(
                <tr key={key}>
                    <td></td>
                    <td title={projectName}>{projectNameShort}</td>
                    <td title={userName}>{userNameShort}</td>
                    <td><input type="checkbox" readOnly={true} checked={assignmentBasis.onCall} /></td>
                    <td><input type="checkbox" readOnly={true} checked={false} /></td>
                    <td>{assignmentBasis.hours}</td>
                    <td>{assignmentBasis.customerRate}</td>
                    <td>{assignmentBasis.sumCustomer}</td>
                    <td>{assignmentBasis.subContractorRate}</td>
                    <td>{assignmentBasis.sumSubcontractor}</td>
                    <td></td>
                    <td title={comment}>{commentShort}</td>
                    <td></td>
                </tr>);
        });

        if (this.state.invoiceCompanyBasis.invoiceAssignmentBasisOnCall.length > 0) {
            key++;
            rows.push(
                <tr className="bg-light" key={key}>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th>{this.state.invoiceCompanyBasis.hoursOnCall}</th>
                    <th></th>
                    <th>{this.state.invoiceCompanyBasis.sumOnCall}</th>
                    <th></th>
                    <th>{this.state.invoiceCompanyBasis.sumSubcontractorOnCall}</th>
                    <th></th>
                    <th></th>
                    <th></th>
                </tr>);
        }

        key++;
        this.state.invoiceCompanyBasis.invoiceAssignmentBasis.forEach(function (assignmentBasis) {
            var projectName = assignmentBasis.projectName;
            var userName = assignmentBasis.userName;
            var projectNameShort = projectName.substring(0, Math.min(20, projectName.length));
            var userNameShort = userName.substring(0, Math.min(20, userName.length));
            var comment = assignmentBasis.comment != null ? assignmentBasis.comment : '';
            var commentShort = comment.substring(0, Math.min(20, comment.length));

            key++;
            rows.push(
                <tr key={key}>
                    <td></td>
                    <td title={projectName}>{projectNameShort}</td>
                    <td title={userName}>{userNameShort}</td>
                    <td><input type="checkbox" readOnly={true} checked={assignmentBasis.onCall} /></td>
                    <td><input type="checkbox" readOnly={true} checked={false} /></td>
                    <td>{assignmentBasis.hours}</td>
                    <td>{assignmentBasis.customerRate}</td>
                    <td>{assignmentBasis.sumCustomer}</td>
                    <td>{assignmentBasis.subContractorRate}</td>
                    <td>{assignmentBasis.sumSubcontractor}</td>
                    <td></td>
                    <td title={comment}>{commentShort}</td>
                    <td></td>
                </tr>);
        });

        var sumCustomer = formatAmount(this.state.invoiceCompanyBasis.sumCustomer);
        var sumSubcontractor = formatAmount(this.state.invoiceCompanyBasis.sumSubcontractor);
        var sumFixRate = formatAmount(this.state.invoiceCompanyBasis.sumFixRate);

        key++;
        rows.push(
            <tr className="bg-light" key={key}>
                <th></th>
                <th></th>
                <th></th>
                <th></th>
                <th></th>
                <th>{this.state.invoiceCompanyBasis.hours}</th>
                <th></th>
                <th>{sumCustomer}</th>
                <th></th>
                <th>{sumSubcontractor}</th>
                <th>{sumFixRate}</th>
                <th></th>
                <th></th>
            </tr>);

        return (
            <tbody>
                {rows}
            </tbody>
        );
    }
};

export default class InvoiceBasis extends React.Component {
    constructor(props) {
        super(props);
        this.handlePreviousInvoiceBasis = this.handlePreviousInvoiceBasis.bind(this);
        this.handleNextInvoiceBasis = this.handleNextInvoiceBasis.bind(this);
    }

    componentDidMount() {
        this.loadFromServer();
    }


    loadFromServer() {
        const self = this;
        var service = new BasisService();
        service.getCurrentInvoiceBasis()
            .then(response => {
                self.setState({ invoicebasis: response.data });
            })
            .catch(error => {
                alert('Failed to load invoice basis');
            });
    }

    handlePreviousInvoiceBasis(event) {
        const date = event.target.name;

        const self = this;
        var service = new BasisService();
        service.getPreviousInvoiceBasis(date)
            .then(response => {
                self.setState({ invoicebasis: response.data });
            })
            .catch(error => {
                alert('Failed to load previous invoice basis');
            });
    }

    handleNextInvoiceBasis(event) {
        const date = event.target.name;

        const self = this;
        var service = new BasisService();
        service.getNextInvoiceBasis(date)
            .then(response => {
                self.setState({ invoicebasis: response.data });
            })
            .catch(error => {
                alert('Failed to load next invoice basis');
            });
    }

    render() {
        if (this.state == null || this.state.invoicebasis == null)
            return null;

        var hours = this.state.invoicebasis.hours;
        var sumCustomer = formatAmount(this.state.invoicebasis.sumCustomer);
        var sumSubcontractor = formatAmount(this.state.invoicebasis.sumSubcontractor);
        var sumFixRate = formatAmount(this.state.invoicebasis.sumFixRate);

        var headerRows = [];
        headerRows.push(
            <tr className="bg-success text-white" key={'totHdr'}>
                <th>Total</th>
                <th></th>
                <th></th>
                <th></th>
                <th></th>
                <th>Hours</th>
                <th></th>
                <th>Sum customer</th>
                <th></th>
                <th>Sum subcontractor</th>
                <th>Fix rate</th>
                <th></th>
                <th></th>
            </tr>
        );

        headerRows.push(
            <tr key={'totFig'}>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td>{hours}</td>
                <td></td>
                <td>{sumCustomer}</td>
                <td></td>
                <td>{sumSubcontractor}</td>
                <td>{sumFixRate}</td>
                <td></td>
                <td></td>
            </tr>
        );

        var rows = [];
        this.state.invoicebasis.invoiceCompanyBases.forEach(function (invoiceCompanyBasis) {
            rows.push(<InvoiceBasisCompanyRows key={invoiceCompanyBasis.idCompany} invoiceCompanyBasis={invoiceCompanyBasis} />);
        });

        return (
            <div className="container-fluid ml-4">
                <div className="row mb-3">
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.invoicebasis.fromDate} onClick={this.handlePreviousInvoiceBasis}>&lt;&lt;</button>
                    </div>
                    <div className="col-sm-1">
                        <button className="btn btn-success" name={this.state.invoicebasis.toDate} onClick={this.handleNextInvoiceBasis}>&gt;&gt;</button>
                    </div>
                    <div className="col-sm-10">
                        <span className=" float-right">
                            <b>{this.state.invoicebasis.fromDate} - {this.state.invoicebasis.toDate}</b>
                        </span>
                    </div>

                </div>
                <div className="row">
                    <div className="table-responsive">
                        <table className="table">
                            <thead>
                                {headerRows}
                            </thead>
                            {rows}
                        </table>
                    </div>
                </div>
            </div>
        );
    }
};
