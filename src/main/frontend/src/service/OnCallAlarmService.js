import axios from 'axios';
import { Headers } from './ServiceUtil';
import { BaseService } from './BaseService';

const BASE_URL = "/api/oncall/alarms";

export default class OnCallAlarmService extends BaseService {
    constructor() {
        super(BASE_URL);
    }

    udateAlarm(entity) {
        const payLoad = JSON.stringify(entity);
        return axios.post(BASE_URL,
            payLoad,
            Headers());
    }

    truncateAlarms() {
        return axios.post(BASE_URL + '/truncate',
            '',
            Headers());
    }
} 