import { BaseService } from './BaseService';

const BASE_URL = "/api/projects";

export default class ProjectService extends BaseService {
    constructor() {
        super(BASE_URL);
    }
} 