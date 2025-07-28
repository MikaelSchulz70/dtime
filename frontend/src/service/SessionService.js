import axios from 'axios';
import { BaseService } from './BaseService';

export default class SessionService extends BaseService {

    getSessionInfo() {
        return axios.get('/api/session', { timeout: 10000 });
    }

    logout() {
        return axios.post('/api/session/logout');
    }

    getCurrentUser() {
        return axios.get('/api/session/user');
    }
}