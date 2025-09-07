import axios from 'axios';
import { Headers } from './ServiceUtil';

const BASE_URL = "/api/report";

export default class ReportService {

    getCurrentReport(view, type) {
        return axios.get(BASE_URL + '?view=' + view + '&type=' + type);
    }

    getPreviousReport(view, type, date) {
        return axios.get(BASE_URL + '/previous?view=' + view + '&type=' + type + '&date=' + date);
    }

    getNextReport(view, type, date) {
        return axios.get(BASE_URL + '/next?view=' + view + '&type=' + type + '&date=' + date);
    }

    getCurrentUserReport(view) {
        return axios.get(BASE_URL + '/user?view=' + view);
    }

    getPreviousUserReport(view, date) {
        return axios.get(BASE_URL + '/user/previous?view=' + view + '&date=' + date);
    }

    getNextUserReport(view, date) {
        return axios.get(BASE_URL + '/user/next?view=' + view + '&date=' + date);
    }

    updateOpenCloseReport(payLoad, path) {
        return axios.post(BASE_URL + '/' + path,
            payLoad,
            Headers());
    }

} 