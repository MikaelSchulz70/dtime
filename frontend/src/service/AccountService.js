import { BaseService } from './BaseService';

export default class AccountService extends BaseService {
    constructor() {
        super('/api/account');
    }
}