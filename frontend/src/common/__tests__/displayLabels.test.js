import { formatActivationStatus, formatTaskType, formatUserRole } from '../displayLabels';

const t = (key) => {
  const map = {
    'common.status.active': 'Active',
    'common.status.inactive': 'Inactive',
    'tasks.types.normal': 'Normal',
    'users.roles.USER': 'User',
    'users.roles.ADMIN': 'Admin',
  };
  return map[key] ?? key;
};

describe('displayLabels', () => {
  it('formatActivationStatus translates known statuses', () => {
    expect(formatActivationStatus('ACTIVE', t)).toBe('Active');
    expect(formatActivationStatus('INACTIVE', t)).toBe('Inactive');
    expect(formatActivationStatus('UNKNOWN', t)).toBe('UNKNOWN');
  });

  it('formatTaskType translates known task types', () => {
    expect(formatTaskType('NORMAL', t)).toBe('Normal');
    expect(formatTaskType('UNKNOWN', t)).toBe('UNKNOWN');
  });

  it('formatUserRole translates known roles', () => {
    expect(formatUserRole('USER', t)).toBe('User');
    expect(formatUserRole('ADMIN', t)).toBe('Admin');
    expect(formatUserRole('CUSTOM', t)).toBe('CUSTOM');
  });
});
