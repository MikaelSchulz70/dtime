import { normalizeApiDate, normalizeTimeReportStatusReport } from '../timeReportStatus';

describe('timeReportStatus utils', () => {
  it('normalizes array dates from Jackson', () => {
    expect(normalizeApiDate([2026, 5, 1])).toBe('2026-05-01');
  });

  it('normalizes report payload and defaults missing users to empty list', () => {
    expect(normalizeTimeReportStatusReport({
      fromDate: [2026, 5, 1],
      toDate: '2026-05-31',
      workableHours: 160,
    })).toEqual({
      fromDate: '2026-05-01',
      toDate: '2026-05-31',
      workableHours: 160,
      unclosedUsers: [],
    });
  });
});
