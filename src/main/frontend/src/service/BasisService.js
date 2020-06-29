import axios from 'axios';
import { Headers } from './ServiceUtil';

const BASE_URL = "/api/basis";

export default class BasisService {

    getCurrentInvoiceBasis() {
        return axios.get(BASE_URL + '/invoice/current');
    };

    getPreviousInvoiceBasis(date) {
        return axios.get(BASE_URL + '/invoice/previous?date=' + date);
    };

    getNextInvoiceBasis(date) {
        return axios.get(BASE_URL + '/invoice/next?date=' + date);
    };

    addUpdateMonthlyCheck(monthlyCheck) {
        const payLoad = JSON.stringify(monthlyCheck);
        return axios.post(BASE_URL + '/invoice/monthlycheck',
            payLoad,
            Headers());
    }
} 