import {
  shiftPeriodPath,
  buildPeriodUrl,
  currentMonthPeriod,
} from '../periodNavigation';

describe('periodNavigation path helpers', () => {
  test('shiftPeriodPath shifts month across year boundary', () => {
    const next = shiftPeriodPath({ mode: 'month', year: 2026, month: 12 }, 1);
    expect(next).toEqual({ mode: 'month', year: 2027, month: 1 });
  });

  test('shiftPeriodPath shifts ISO week across year boundary', () => {
    const next = shiftPeriodPath({ mode: 'week', year: 2025, week: 52 }, 1);
    expect(next).toEqual({ mode: 'week', year: 2026, week: 1 });
  });

  test('shiftPeriodPath shifts ISO week 53 to week 1', () => {
    const next = shiftPeriodPath({ mode: 'week', year: 2020, week: 53 }, 1);
    expect(next).toEqual({ mode: 'week', year: 2021, week: 1 });
  });

  test('shiftPeriodPath shifts ISO week 1 back to week 53', () => {
    const prev = shiftPeriodPath({ mode: 'week', year: 2021, week: 1 }, -1);
    expect(prev).toEqual({ mode: 'week', year: 2020, week: 53 });
  });

  test('buildPeriodUrl builds month and week paths', () => {
    expect(buildPeriodUrl('/time', { mode: 'month', year: 2026, month: 7 }))
      .toBe('/time/year/2026/month/7');
    expect(buildPeriodUrl('/time', { mode: 'week', year: 2026, week: 36 }))
      .toBe('/time/year/2026/week/36');
    expect(buildPeriodUrl('/reports', { mode: 'year', year: 2026 }))
      .toBe('/reports/year/2026');
  });

  test('currentMonthPeriod returns today month', () => {
    const period = currentMonthPeriod(new Date('2026-07-15T12:00:00'));
    expect(period).toEqual({ mode: 'month', year: 2026, month: 7 });
  });
});
