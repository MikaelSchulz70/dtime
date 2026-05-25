import React from 'react';
import { Navigate } from 'react-router';
import { currentMonthPeriod } from '../util/periodNavigation';

export default function CurrentMonthRedirect({ basePath, search = '' }) {
  const { year, month } = currentMonthPeriod();
  const suffix = search ? `?${search}` : '';
  return <Navigate to={`${basePath}/year/${year}/month/${month}${suffix}`} replace />;
}
