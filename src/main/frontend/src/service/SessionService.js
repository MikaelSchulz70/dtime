import axios from 'axios';

const BASE_URL = '/api/session';

export default class SessionService {

    static getSessionInfo(callback) {
        axios.get(BASE_URL)
            .then(response => {
                callback(response.data);
            })
            .catch(error => {
                callback(error);
            });
    };
}