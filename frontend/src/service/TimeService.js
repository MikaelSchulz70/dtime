
import axios from 'axios';
import { Headers } from './ServiceUtil';

const BASE_URL = "/api/timereport";

export default class TimeService {

    updateTime(time) {
        var payLoad = JSON.stringify(time);
        return axios.post(BASE_URL,
            payLoad,
            Headers());
    }

    getTimes(view) {
        return axios.get(BASE_URL + '?view=' + view);
    }

    getPreviousTimes(view, date) {
        return axios.get(BASE_URL + '/previous?view=' + view + '&date=' + date);
    }

    getNextTimes(view, date) {
        return axios.get(BASE_URL + '/next?view=' + view + '&date=' + date);
    }

    getUserTimes(userId, fromDate) {
        return axios.get(BASE_URL + '/user?view=MONTH&userId=' + userId + '&date=' + fromDate);
    }

    getVacations() {
        return axios.get(BASE_URL + '/vacations');
    }

    getPreviousVacations(date) {
        return axios.get(BASE_URL + '/vacations/previous?date=' + date);
    }

    getNextVacations(date) {
        return axios.get(BASE_URL + '/vacations/next?date=' + date);
    }
}