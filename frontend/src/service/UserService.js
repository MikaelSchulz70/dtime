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

    getAllPaged(page, size, sort, direction, active, firstName, lastName) {
        const params = new URLSearchParams();
        if (page !== undefined) params.append('page', page);
        if (size !== undefined) params.append('size', size);
        if (sort) params.append('sort', sort);
        if (direction) params.append('direction', direction);
        if (active !== null && active !== undefined && active !== '') params.append('active', active);
        if (firstName) params.append('firstName', firstName);
        if (lastName) params.append('lastName', lastName);

        return axios.get(`${BASE_URL}/paged?${params.toString()}`, Headers());
    }
} 