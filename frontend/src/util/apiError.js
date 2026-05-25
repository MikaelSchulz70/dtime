import i18n from '../i18n';

function normalizeMessage(value) {
  if (value == null) {
    return null;
  }
  if (typeof value === 'string') {
    return value.trim() || null;
  }
  if (typeof value === 'object' && typeof value.message === 'string') {
    return value.message.trim() || null;
  }
  return null;
}

function mapPeriodErrorMessage(rawMessage) {
  if (!rawMessage) {
    return null;
  }
  const lower = rawMessage.toLowerCase();
  if (lower.includes('invalid.month') || lower.includes('month is between') || lower.includes('invalid value for \'month\'')) {
    return i18n.t('errors.period.invalidMonth');
  }
  if (lower.includes('invalid.week') || lower.includes('week must be') || lower.includes('invalid value for \'week\'')) {
    return i18n.t('errors.period.invalidWeek');
  }
  if (lower.includes('invalid.year') || lower.includes('system start year') || lower.includes('invalid value for \'year\'')) {
    return i18n.t('errors.period.invalidYear');
  }
  if (lower.includes('failed to convert') || lower.includes('invalid parameter type')) {
    return i18n.t('errors.period.invalidPeriod');
  }
  return null;
}

/**
 * Returns a short, user-friendly message from an axios/API error.
 */
export function getApiErrorMessage(error, fallbackKey = 'errors.unexpected') {
  const data = error?.response?.data;
  const raw = normalizeMessage(data?.message)
    || normalizeMessage(data?.error)
    || normalizeMessage(error?.message);

  const mapped = mapPeriodErrorMessage(raw);
  if (mapped) {
    return mapped;
  }

  if (raw && raw.length <= 200) {
    return raw;
  }

  if (raw) {
    return i18n.t('errors.period.invalidPeriod');
  }

  const status = error?.response?.status;
  if (status === 403) {
    return i18n.t('errors.forbidden');
  }
  if (status >= 500) {
    return i18n.t('errors.serverError');
  }
  if (status) {
    return i18n.t('errors.requestFailed', { status });
  }
  if (error?.request) {
    return i18n.t('errors.networkError');
  }

  return i18n.t(fallbackKey);
}
