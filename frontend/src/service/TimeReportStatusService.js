import { BaseService } from './BaseService';
import { Headers } from './ServiceUtil';
import axios from 'axios';

const BASE_URL = "/api/timereportstatus";

class TimeReportStatusService extends BaseService {
    constructor() {
        super(BASE_URL);
    }

    async getUnclosedUsersForMonth(year, month) {
        const response = await axios.get(`${BASE_URL}/year/${year}/month/${month}`, Headers());
        return response.data;
    }

    async getClosedUsersForMonth(year, month) {
        const response = await axios.get(`${BASE_URL}/year/${year}/month/${month}?closed=true`, Headers());
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
