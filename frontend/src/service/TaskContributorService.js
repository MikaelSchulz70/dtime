import axios from 'axios';
import { Headers } from './ServiceUtil';

const BASE_URL = "/api/taskcontributor";

export default class TaskContributorService {

    getTaskContributor(userId) {
        return axios.get(BASE_URL + '/' + userId);
    }

    udate(entity) {
        const payLoad = JSON.stringify(entity);
        return axios.post(BASE_URL,
            payLoad,
            Headers());
    }

    getMyTaskContributors() {
        return axios.get(BASE_URL + '/self');
    }

    selfAssign(taskId) {
        return axios.post(BASE_URL + '/self/' + taskId, null, Headers());
    }

    selfUnassign(taskId) {
        return axios.delete(BASE_URL + '/self/' + taskId, Headers());
    }
}
