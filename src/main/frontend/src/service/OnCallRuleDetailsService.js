import { BaseService } from './BaseService';
import axios from 'axios';

const BASE_URL = "/api/oncall/rules";

export default class OnCallRuleDetailsService extends BaseService {
    constructor() {
        super(BASE_URL);
    }

    getByProjectId(projectId) {
        return axios.get(this.url + '/project/' + projectId);
    }
} 