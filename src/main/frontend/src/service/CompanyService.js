import { BaseService } from './BaseService';

const BASE_URL = "/api/companies";

export default class CompanyService extends BaseService {
    constructor() {
        super(BASE_URL);
    }
} 