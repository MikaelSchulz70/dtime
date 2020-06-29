import { BaseService } from './BaseService';
import axios from 'axios';


const BASE_URL = "/api/rates";

export default class RateService extends BaseService {
    constructor() {
        super(BASE_URL);
    }

    getCurrentRates() {
        return axios.get(BASE_URL);
    };

    getRatesForAssignment(idAssignment) {
        return axios.get(BASE_URL + '/' + idAssignment);
    };
} 