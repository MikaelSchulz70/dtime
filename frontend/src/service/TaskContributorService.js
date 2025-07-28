import { BaseService } from './BaseService';

export default class TaskContributorService extends BaseService {
    constructor() {
        super('/api/taskcontributor');
    }

    getTaskContributors() {
        return this.getAll();
    }

    getTaskContributor(id) {
        return this.get(`/${id}`);
    }

    saveTaskContributor(taskContributor) {
        return taskContributor.id ? this.put(taskContributor) : this.post(taskContributor);
    }

    deleteTaskContributor(id) {
        return this.delete(`/${id}`);
    }
}
