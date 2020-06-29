import { BaseService } from './BaseService';
import { Headers } from './ServiceUtil';
import axios from 'axios';

const BASE_URL = "/api/users";

export default class UserService extends BaseService {
    constructor() {
        super(BASE_URL);
    }

    changePwd(entity) {
        const payLoad = JSON.stringify(entity);
        return axios.post(BASE_URL + '/changepwd',
            payLoad,
            Headers());
    }
} 