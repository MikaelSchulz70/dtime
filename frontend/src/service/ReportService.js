import axios from 'axios';
import { Headers } from './ServiceUtil';

const BASE_URL = "/api/report";

function buildQuery(params) {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            search.append(key, value);
        }
    });
    const qs = search.toString();
    return qs ? `?${qs}` : '';
}

export default class ReportService {

    /**
     * Admin report for the period containing date (omit date for current period).
     */
    getReport(view, type, date) {
        return axios.get(BASE_URL + buildQuery({ view, type, date }));
    }

    /**
     * Logged-in user's report for the period containing date (omit date for current period).
     */
    getUserReport(view, date) {
        return axios.get(BASE_URL + '/user' + buildQuery({ view, date }));
    }

    updateOpenCloseReport(payLoad, path) {
        return axios.post(BASE_URL + '/' + path,
            payLoad,
            Headers());
    }

}
