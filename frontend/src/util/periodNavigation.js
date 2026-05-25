/**
 * Path-based period navigation using ISO-8601 week rules.
 */

function isoWeekMondayFromParts(weekBasedYear, week) {
  const jan4 = new Date(Date.UTC(weekBasedYear, 0, 4));
  const dayOfWeek = jan4.getUTCDay() || 7;
  const mondayWeek1 = new Date(jan4);
  mondayWeek1.setUTCDate(jan4.getUTCDate() - dayOfWeek + 1);
  const monday = new Date(mondayWeek1);
  monday.setUTCDate(mondayWeek1.getUTCDate() + (week - 1) * 7);
  return monday;
}

function isoWeekYear(date) {
  const d = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate()));
  d.setUTCDate(d.getUTCDate() + 4 - (d.getUTCDay() || 7));
  return d.getUTCFullYear();
}

function isoWeekNumber(date) {
  const d = new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate()));
  d.setUTCDate(d.getUTCDate() + 4 - (d.getUTCDay() || 7));
  const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
  return Math.ceil((((d - yearStart) / 86400000) + 1) / 7);
}

export function currentMonthPeriod(date = new Date()) {
  return { mode: 'month', year: date.getFullYear(), month: date.getMonth() + 1 };
}

export function currentWeekPeriod(date = new Date()) {
  const utc = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
  return { mode: 'week', year: isoWeekYear(utc), week: isoWeekNumber(utc) };
}

export function parsePeriodFromParams(params) {
  const year = parseInt(params.year, 10);
  if (Number.isNaN(year)) {
    return null;
  }
  if (params.month !== undefined) {
    return { mode: 'month', year, month: parseInt(params.month, 10) };
  }
  if (params.week !== undefined) {
    return { mode: 'week', year, week: parseInt(params.week, 10) };
  }
  return { mode: 'year', year };
}

export function shiftPeriodPath(period, delta) {
  if (!period || delta === 0) {
    return period;
  }
  if (period.mode === 'month') {
    const date = new Date(Date.UTC(period.year, period.month - 1, 15));
    date.setUTCMonth(date.getUTCMonth() + delta);
    return { mode: 'month', year: date.getUTCFullYear(), month: date.getUTCMonth() + 1 };
  }
  if (period.mode === 'year') {
    return { mode: 'year', year: period.year + delta };
  }
  if (period.mode === 'week') {
    const monday = isoWeekMondayFromParts(period.year, period.week);
    monday.setUTCDate(monday.getUTCDate() + delta * 7);
    return { mode: 'week', year: isoWeekYear(monday), week: isoWeekNumber(monday) };
  }
  return period;
}

export function buildPeriodUrl(basePath, period) {
  if (period.mode === 'month') {
    return `${basePath}/year/${period.year}/month/${period.month}`;
  }
  if (period.mode === 'week') {
    return `${basePath}/year/${period.year}/week/${period.week}`;
  }
  if (period.mode === 'year') {
    return `${basePath}/year/${period.year}`;
  }
  return basePath;
}
