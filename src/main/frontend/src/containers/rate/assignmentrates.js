import React from "react";
import RateService from '../../service/RateService';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import { toDateString } from '../../common/utils'

class AssignmentRateTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.handleRateChange = this.handleRateChange.bind(this);
        this.isRateOk = this.isRateOk.bind(this);
        this.validate = this.validate.bind(this);
        this.handleCreateUpdate = this.handleCreateUpdate.bind(this);
        this.delete = this.delete.bind(this);
        this.handleDateChange = this.handleDateChange.bind(this);
        this.handleError = this.handleError.bind(this);
        this.state = { rate: this.props.rate };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    handleError(status, error) {
        alert("Error:\n" + error);
    }

    delete() {
        const confirmDelete = confirm('Are you really sure you want to delete?');
        if (!confirmDelete) {
            return;
        }

        const self = this;
        const service = new RateService();
        service.delete(this.state.rate.id)
            .then(response => {
                self.props.refresh();
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    handleCreateUpdate() {
        const confirmAddUpdate = confirm('Are you really sure you want to update?');
        if (!confirmAddUpdate) {
            return;
        }

        var ok = this.validate();
        if (!ok) {
            return;
        }

        const self = this;
        const service = new RateService();
        service.addOrUdate(this.state.rate)
            .then(response => {
                self.props.refresh();
            })
            .catch(error => {
                self.handleError(error.response.status, error.response.data.error, error.response.data.fieldErrors);
            });
    }

    validate() {
        var rate = this.state.rate.customerRate;
        var rateStr = (rate != null ? rate.toString() : null);
        var rateOk = this.isRateOk(rateStr);
        if (rateStr == null || !rateOk) {
            alert("Invalid rate");
            return false;
        }

        if (this.state.rate.subcontractor) {
            var subcontractorRate = this.state.rate.subcontractorRate;
            rateStr = (subcontractorRate != null ? subcontractorRate.toString() : null);
            rateOk = this.isRateOk(rateStr);
            if (rateStr == null || !rateOk) {
                alert("Invalid subcontractor rate");
                return false;
            }
        }

        var fromDate = this.state.rate.fromDate;
        if (fromDate == null || fromDate === "") {
            alert("From date not specified");
            return false;
        }

        var toDate = this.state.rate.toDate;
        if (toDate != null && toDate.length > 0) {
            var fDate = new Date(fromDate);
            var tDate = new Date(toDate);

            if (fDate > tDate) {
                alert('From date is after to date');
                return false;
            }
        }

        return true;
    }

    isRateOk(rate) {
        if (rate == null) {
            return true;
        }

        var number = parseFloat(rate);

        if (isNaN(number) && !isFinite(rate)) {
            return false;
        }

        if (number < 0) {
            return false;
        }

        var index = rate.indexOf('.');
        if (index !== -1) {
            var decimals = rate.substring(index, rate.length - 1);
            return decimals.length === 0 || decimals.length === 1 || decimals.length === 2;
        }

        return true;
    }

    handleRateChange(event) {
        var value = event.target.value;
        let field = event.target.name;
        if (value != null) {
            value = value.replace(",", ".")
        }

        var isInputOk = this.isRateOk(value);
        if (!isInputOk) {
            return;
        }

        let rate = JSON.parse(JSON.stringify(this.state.rate));
        rate[field] = value;
        this.setState(() => ({ rate: rate }));
    }

    handleChange(event) {
        let field = event.target.name;
        let value = event.target.value;

        let rate = JSON.parse(JSON.stringify(this.state.rate));

        if (event.target.type === 'checkbox') {
            value = event.target.checked;
        }

        rate[field] = value;
        this.setState(() => ({ rate: rate }));
    }

    handleDateChange(fieldName, date) {
        var dateStr = toDateString(date);
        let rate = JSON.parse(JSON.stringify(this.state.rate));
        rate[fieldName] = dateStr;
        this.setState(() => ({ rate: rate }));
    }

    render() {
        if (this.state == null || this.state.rate == null) return null;

        var handleCreateUpdate = this.handleCreateUpdate;
        var deleteRate = this.delete;
        var customerRate = (this.state.rate.customerRate == null ? '' : this.state.rate.customerRate);
        var subcontractorRate = (this.state.rate.subcontractorRate == null ? '' : this.state.rate.subcontractorRate);
        var fromDate = this.state.rate.fromDate == null || this.state.rate.fromDate === "" ? null : new Date(this.state.rate.fromDate);
        var toDate = this.state.rate.toDate == null || this.state.rate.toDate === "" ? null : new Date(this.state.rate.toDate);
        var comment = this.state.rate.comment == null ? '' : this.state.rate.comment;
        var isNew = (this.state.rate.id === 0 || this.state.rate.id == null);
        var buttonText = (isNew ? 'New' : 'Change');

        return (
            <tr>
                <td>
                    <input name="customerRate" type="text" value={customerRate} maxLength="10" onChange={this.handleRateChange} />
                </td>
                {this.state.rate.subcontractor &&
                    <td>
                        <input name="subcontractorRate" type="text" value={subcontractorRate} maxLength="10" onChange={this.handleRateChange} />
                    </td>
                }
                <td>
                    <DatePicker
                        type="date"
                        dateFormat="yyyy-MM-dd"
                        onChange={date => this.handleDateChange('fromDate', date)}
                        selected={fromDate}
                    />
                </td>
                <td>
                    <DatePicker
                        type="date"
                        dateFormat="yyyy-MM-dd"
                        onChange={date => this.handleDateChange('toDate', date)}
                        selected={toDate}
                    />
                </td>
                <td>
                    <input type="text" value={comment} maxLength="250" name="comment" onChange={this.handleChange} />
                </td>
                <td>
                    <button className="btn btn-success float-sm-right" onClick={() => handleCreateUpdate()}>{buttonText}</button>
                </td>
                <td>
                    {!isNew &&
                        < button className="btn btn-success float-sm-right" onClick={() => deleteRate()}>Delete</button>
                    }
                </td>
            </tr >
        );
    }
};

class AssignmentRatesTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { rates: this.props.rates };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null || this.state.rates == null) return null;

        var isSubContractor = (this.state.rates.length > 0 ? this.state.rates[0].subcontractor : false);

        var self = this;
        var rows = [];
        this.state.rates.forEach(function (rate) {
            rows.push(
                <AssignmentRateTableRow rate={rate} key={rate.id} refresh={self.props.refresh} />);
        });

        return (
            <table className="table table-striped">
                <thead className="thead-inverse bg-success">
                    <tr className="text-white">
                        <th>Customer rate</th>
                        {isSubContractor &&
                            <th>Sub contractor rate</th>
                        }
                        <th>From date</th>
                        <th>To date</th>
                        <th>Comment</th>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </table>
        );
    }
};

export default class AssignmentRates extends React.Component {
    constructor(props) {
        super(props);
        this.loadFromServer = this.loadFromServer.bind(this);
        this.backToOverview = this.backToOverview.bind(this);
        this.refresh = this.refresh.bind(this);

        const idAssignment = this.props.match.params.assignmentId;
        this.state = { idAssignment: idAssignment };
    }

    componentDidMount() {
        this.loadFromServer();
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    refresh() {
        this.loadFromServer();
    }

    backToOverview() {
        this.props.history.push('/rates');
    }

    loadFromServer() {
        const self = this;
        var service = new RateService();
        service.getRatesForAssignment(this.state.idAssignment)
            .then(response => {
                self.setState({ rates: response.data });
            })
            .catch(error => {
                alert('Failed to load rates');
            });
    }

    render() {
        if (this.state == null || this.state.rates == null) return null;

        var header = '';
        if (this.state.rates != null && this.state.rates.length > 0) {
            header = this.state.rates[0].companyName + ', ' + this.state.rates[0].projectName + ', ' + this.state.rates[0].userName;
        }

        var backToOverview = this.backToOverview;
        var refresh = this.refresh;
        return (
            <div className="container">
                <div className="row mb-3">
                    <div className="col-sm-12">
                        <h4>{header}</h4>
                    </div>
                </div>
                <div className="row">
                    <AssignmentRatesTable rates={this.state.rates} refresh={refresh} />
                </div>
                <div className="row">
                    <button className="btn btn-success float-sm-right" onClick={() => backToOverview()}>Back</button>
                </div>
            </div>
        );
    }
};