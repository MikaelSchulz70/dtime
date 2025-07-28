import { BaseService } from './BaseService';

export default class TaskService extends BaseService {
    constructor() {
        super('/api/task');
    }

    getTasks() {
        return this.getAll();
    }

    getTask(id) {
        return this.get(`/${id}`);
    }

    saveTask(task) {
        return task.id ? this.put(task) : this.post(task);
    }

    deleteTask(id) {
        return this.delete(`/${id}`);
    }
}
