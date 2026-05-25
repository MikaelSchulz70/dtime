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

    getReportForMonth(year, month, type) {
        return axios.get(`${BASE_URL}/year/${year}/month/${month}` + buildQuery({ type }));
    }

    getReportForYear(year, type) {
        return axios.get(`${BASE_URL}/year/${year}` + buildQuery({ type }));
    }

    getUserReportForMonth(year, month) {
        return axios.get(`${BASE_URL}/user/year/${year}/month/${month}`);
    }

    getUserReportForYear(year) {
        return axios.get(`${BASE_URL}/user/year/${year}`);
    }

    updateOpenCloseReport(payLoad, path) {
        return axios.post(BASE_URL + '/' + path,
            payLoad,
            Headers());
    }

}
