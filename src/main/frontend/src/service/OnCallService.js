import axios from 'axios';
import { Headers } from './ServiceUtil';


const BASE_URL = "/api/oncall";

export default class OnCallService {

    getCurrentSchedule() {
        return axios.get(BASE_URL);
    }

    getNextSchedule(date) {
        return axios.get(BASE_URL + '/next?date=' + date);
    }

    getPreviousSchedule(date) {
        return axios.get(BASE_URL + '/previous?date=' + date);
    }

    getOnCallConfig() {
        return axios.get(BASE_URL + '/config');
    }

    getOnCallSession() {
        return axios.get(BASE_URL + '/session');
    }

    getOnCallRules() {
        return axios.get(BASE_URL + '/rules');
    }

    udateOnCallDay(entity) {
        const payLoad = JSON.stringify(entity);
        return axios.post(BASE_URL,
            payLoad,
            Headers());
    }

    udateOnCallConfig(entity) {
        const payLoad = JSON.stringify(entity);
        return axios.post(BASE_URL + '/config',
            payLoad,
            Headers());
    }

    dispatchOnCallEmails() {
        return axios.post(BASE_URL + '/dispatchemails',
            '',
            Headers());
    }
} 