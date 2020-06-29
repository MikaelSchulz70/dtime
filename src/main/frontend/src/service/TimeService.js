
import axios from 'axios';
import { Headers } from './ServiceUtil';

const BASE_URL = "/api/timereport";

export default class TimeService {

    static updateTime(time) {
        var payLoad = JSON.stringify(time);
        return axios.post(BASE_URL,
            payLoad,
            Headers());
    };

    static getTimes(view) {
        return axios.get(BASE_URL + '?view=' + view);
    }

    static getPreviousTimes(view, date) {
        return axios.get(BASE_URL + '/previous?view=' + view + '&date=' + date);
    }

    static getNextTimes(view, date) {
        return axios.get(BASE_URL + '/next?view=' + view + '&date=' + date);
    }

    getUserTimes(idUser, fromDate) {
        return axios.get(BASE_URL + '/user?view=MONTH&idUser=' + idUser + '&date=' + fromDate);
    }

    static getVacations() {
        return axios.get(BASE_URL + '/vacations');
    }

    static getPreviousVacations(date) {
        return axios.get(BASE_URL + '/vacations/previous?date=' + date);
    }

    static getNextVacations(date) {
        return axios.get(BASE_URL + '/vacations/next?date=' + date);
    }
}