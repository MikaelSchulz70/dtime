
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

    getTimeReportForMonth(year, month) {
        return axios.get(`${BASE_URL}/year/${year}/month/${month}`);
    }

    getTimeReportForWeek(year, week) {
        return axios.get(`${BASE_URL}/year/${year}/week/${week}`);
    }

    getUserTimeReportForMonth(userId, year, month) {
        return axios.get(`${BASE_URL}/user/${userId}/year/${year}/month/${month}`);
    }

    getUserTimeReportForWeek(userId, year, week) {
        return axios.get(`${BASE_URL}/user/${userId}/year/${year}/week/${week}`);
    }

    getVacationReportForMonth(year, month) {
        return axios.get(`${BASE_URL}/vacations/year/${year}/month/${month}`);
    }

}
