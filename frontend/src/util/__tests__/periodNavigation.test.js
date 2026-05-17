import { shiftPeriodDate } from '../periodNavigation';

describe('shiftPeriodDate', () => {
  it('shifts month backward', () => {
    expect(shiftPeriodDate('2026-05-15', 'MONTH', -1)).toBe('2026-04-15');
  });

  it('shifts month forward', () => {
    expect(shiftPeriodDate('2026-05-15', 'MONTH', 1)).toBe('2026-06-15');
  });

  it('shifts week backward', () => {
    expect(shiftPeriodDate('2026-05-15', 'WEEK', -1)).toBe('2026-05-08');
  });
});
