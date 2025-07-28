import { BaseService } from './BaseService';

export default class AccountService extends BaseService {
    constructor() {
        super('/api/account');
    }

    getAccounts() {
        return this.getAll();
    }

    getAccount(id) {
        return this.get(`/${id}`);
    }

    saveAccount(account) {
        return account.id ? this.put(account) : this.post(account);
    }

    deleteAccount(id) {
        return this.delete(`/${id}`);
    }
}