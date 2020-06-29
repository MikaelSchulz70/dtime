import axios from 'axios';
import { Headers } from './ServiceUtil';


const BASE_URL = "/api/system";

export default class SystemService {

    getSystemConfig() {
        return axios.get(BASE_URL + '/config');
    }

    udateProperty(entity) {
        const payLoad = JSON.stringify(entity);
        return axios.put(BASE_URL + '/systemproperty',
            payLoad,
            Headers());
    }
} 