/**
 * Shifts an ISO date (YYYY-MM-DD) by whole periods for GUI prev/next navigation.
 * The backend resolves the week/month/year that contains the returned date.
 */
export function shiftPeriodDate(isoDate, view, steps) {
  if (!isoDate || steps === 0) {
    return isoDate;
  }
  const [year, month, day] = isoDate.split('-').map(Number);
  const date = new Date(Date.UTC(year, month - 1, day));

  if (view === 'WEEK') {
    date.setUTCDate(date.getUTCDate() + steps * 7);
  } else if (view === 'MONTH') {
    date.setUTCMonth(date.getUTCMonth() + steps);
  } else if (view === 'YEAR') {
    date.setUTCFullYear(date.getUTCFullYear() + steps);
  } else {
    date.setUTCMonth(date.getUTCMonth() + steps);
  }

  return date.toISOString().slice(0, 10);
}
