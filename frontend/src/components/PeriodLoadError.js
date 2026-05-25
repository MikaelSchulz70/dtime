import React from 'react';
import { Alert, Button } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { buildPeriodUrl, currentMonthPeriod } from '../util/periodNavigation';

export default function PeriodLoadError({ message, basePath, search = '', onGoToCurrent }) {
  const { t } = useTranslation();

  const handleGoToCurrent = () => {
    const url = buildPeriodUrl(basePath, currentMonthPeriod());
    const target = search ? `${url}?${search}` : url;
    onGoToCurrent(target);
  };

  return (
    <Alert variant="warning" className="period-load-error shadow-sm">
      <Alert.Heading className="h6 mb-2">{t('errors.period.heading')}</Alert.Heading>
      <p className="mb-3">{message}</p>
      <Button variant="outline-primary" size="sm" onClick={handleGoToCurrent}>
        {t('errors.period.goToCurrent')}
      </Button>
    </Alert>
  );
}
