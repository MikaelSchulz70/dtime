import axios from 'axios';
import { Headers } from './ServiceUtil';

export class BaseService {
    constructor(url) {
        this.url = url;
    }

    getAll() {
        return axios.get(this.url);
    }

    getByStatus(active) {
        return axios.get(this.url + '?active=' + active);
    }

    get(id) {
        return axios.get(this.url + '/' + id);
    }

    delete(id) {
        return axios.delete(this.url + '/' + id, Headers());
    }

    create(entity) {
        const payLoad = JSON.stringify(entity);
        return axios.post(this.url,
            payLoad,
            Headers());
    }

    update(entity) {
        const payLoad = JSON.stringify(entity);
        return axios.put(`${this.url}`,
            payLoad,
            Headers());
    }

    validate(id, attribute, value) {
        let data = JSON.stringify({ id: id, name: attribute, value: value });
        return axios.post(this.url + '/validate', data, Headers());
    }
}