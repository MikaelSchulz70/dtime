export const TaskType = {
    NORMAL: 'NORMAL',
    VACATION: 'VACATION',
    SICK_LEAVE: 'SICK_LEAVE',
    PARENTAL_LEAVE: 'PARENTAL_LEAVE'
};

export const TaskTypeLabels = {
    [TaskType.NORMAL]: 'Normal',
    [TaskType.VACATION]: 'Vacation',
    [TaskType.SICK_LEAVE]: 'Sick Leave',
    [TaskType.PARENTAL_LEAVE]: 'Parental Leave'
};

/** i18n keys under tasks.types.* for use with t() */
export const TaskTypeI18nKeys = {
    [TaskType.NORMAL]: 'tasks.types.normal',
    [TaskType.VACATION]: 'tasks.types.vacation',
    [TaskType.SICK_LEAVE]: 'tasks.types.sickLeave',
    [TaskType.PARENTAL_LEAVE]: 'tasks.types.parentalLeave',
};