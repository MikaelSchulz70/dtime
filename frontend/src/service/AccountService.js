import { BaseService } from './BaseService';
import { Headers } from './ServiceUtil';
import axios from 'axios';

const BASE_URL = "/api/account";

export default class AccountService extends BaseService {
    constructor() {
        super(BASE_URL);
    }

    getAllPaged(page, size, sort, direction, active, name) {
        const params = new URLSearchParams();
        if (page !== undefined) params.append('page', page);
        if (size !== undefined) params.append('size', size);
        if (sort) params.append('sort', sort);
        if (direction) params.append('direction', direction);
        if (active !== null && active !== undefined && active !== '') params.append('active', active);
        if (name) params.append('name', name);

        return axios.get(`${BASE_URL}/paged?${params.toString()}`, Headers());
    }
}