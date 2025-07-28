import { BaseService } from './BaseService';
import axios from 'axios';

const BASE_URL = "/api/followup";

export default class FollowUpService extends BaseService {
    constructor() {
        super(BASE_URL);
    }

    getCurrentReport(view, type) {
        return axios.get(BASE_URL + '?view=' + view + '&type=' + type);
    };

    getPreviousReport(view, type, date) {
        return axios.get(BASE_URL + '/previous?view=' + view + '&type=' + type + '&date=' + date);
    };

    getNextReport(view, type, date) {
        return axios.get(BASE_URL + '/next?view=' + view + '&type=' + type + '&date=' + date);
    };
} 