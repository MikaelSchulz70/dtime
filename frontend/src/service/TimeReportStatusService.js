import { BaseService } from './BaseService';
import { Headers } from './ServiceUtil';
import axios from 'axios';

const BASE_URL = "/api/timereportstatus";

class TimeReportStatusService extends BaseService {
    constructor() {
        super(BASE_URL);
    }

    /**
     * Unclosed users for the month containing date (omit date for current month).
     */
    async getUnclosedUsers(date) {
        const query = date ? `?date=${date}` : '';
        const response = await axios.get(BASE_URL + query, Headers());
        return response.data;
    }

    async closeUserTimeReport(userId, closeDate) {
        const payLoad = JSON.stringify({
            userId: userId,
            closeDate: closeDate
        });
        const response = await axios.post(`${BASE_URL}/close`, payLoad, Headers());
        return response.data;
    }

    async openUserTimeReport(userId, closeDate) {
        const payLoad = JSON.stringify({
            userId: userId,
            closeDate: closeDate
        });
        const response = await axios.post(`${BASE_URL}/open`, payLoad, Headers());
        return response.data;
    }
}

export default new TimeReportStatusService();
