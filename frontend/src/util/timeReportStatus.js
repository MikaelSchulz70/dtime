export function normalizeApiDate(value) {
  if (value == null) {
    return '';
  }
  if (typeof value === 'string') {
    return value;
  }
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day] = value;
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  }
  return String(value);
}

export function normalizeTimeReportStatusReport(response) {
  if (!response || typeof response !== 'object') {
    return response;
  }

  return {
    ...response,
    fromDate: normalizeApiDate(response.fromDate),
    toDate: normalizeApiDate(response.toDate),
    unclosedUsers: Array.isArray(response.unclosedUsers) ? response.unclosedUsers : [],
  };
}
