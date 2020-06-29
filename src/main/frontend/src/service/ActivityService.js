import { BaseService } from './BaseService';
import { Headers } from './ServiceUtil';
import axios from 'axios';

const BASE_URL = "/api/activities";

export default class ActivityService extends BaseService {
    constructor() {
        super(BASE_URL);
    }

    vote(id) {
        return axios.post(this.url + '/vote/' + id,
            '',
            Headers());
    }

    resetVotes() {
        return axios.put(this.url + '/resetvotes',
            '',
            Headers());
    }
} 