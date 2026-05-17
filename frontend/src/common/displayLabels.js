import * as Constants from './Constants';
import { TaskTypeI18nKeys } from './TaskType';

const UserRoleI18nKeys = {
    USER: 'users.roles.USER',
    ADMIN: 'users.roles.ADMIN',
};

export function formatActivationStatus(status, t) {
    if (status === Constants.ACTIVE_STATUS || status === 'ACTIVE') {
        return t('common.status.active');
    }
    if (status === Constants.INACTIVE_STATUS || status === 'INACTIVE') {
        return t('common.status.inactive');
    }
    return status;
}

export function formatTaskType(taskType, t) {
    const key = TaskTypeI18nKeys[taskType];
    return key ? t(key) : taskType;
}

export function formatUserRole(role, t) {
    const key = UserRoleI18nKeys[role];
    return key ? t(key) : role;
}
