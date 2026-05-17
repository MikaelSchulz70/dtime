
import axios from 'axios';
import { Headers } from './ServiceUtil';

const BASE_URL = "/api/timereport";

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

export default class TimeService {

    updateTime(time) {
        var payLoad = JSON.stringify(time);
        return axios.post(BASE_URL,
            payLoad,
            Headers());
    }

    /**
     * Time sheet for the period containing date (omit date for current period).
     */
    getTimeReport(view, date) {
        return axios.get(BASE_URL + buildQuery({ view, date }));
    }

    getUserTimes(userId, fromDate) {
        return axios.get(BASE_URL + '/user?view=MONTH&userId=' + userId + '&date=' + fromDate);
    }

    /**
     * Vacation report for the month containing date (omit date for current month).
     */
    getVacationReport(date) {
        return axios.get(BASE_URL + '/vacations' + buildQuery({ date }));
    }

}
