import { BaseService } from './BaseService';
import axios from 'axios';


const BASE_URL = "/api/fixrates";

export default class RateService extends BaseService {
    constructor() {
        super(BASE_URL);
    }

    getCurrentFixRates() {
        return axios.get(BASE_URL);
    };

    getRatesForProject(idProject) {
        return axios.get(BASE_URL + '/' + idProject);
    };
} 