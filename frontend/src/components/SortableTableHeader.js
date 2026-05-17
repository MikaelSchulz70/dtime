import React from 'react';
import { useTranslation } from 'react-i18next';

/** contentAlign 'end': label + sort icon grouped right (optional; numeric columns use col-num). */
const SortableTableHeader = ({ 
    field, 
    onSort, 
    getSortIcon, 
    children, 
    className = '',
    style = {},
    title = '',
    contentAlign = 'between',
    ...props 
}) => {
    const { t } = useTranslation();
    const handleClick = () => {
        onSort(field);
    };

    const defaultTitle =
        typeof children === 'string' || typeof children === 'number'
            ? t('accessibility.sortBy', { column: children })
            : t('accessibility.sortColumn');

    return (
        <th 
            className={`sortable-header ${className}`}
            style={{
                cursor: 'pointer',
                userSelect: 'none',
                position: 'relative',
                ...style
            }}
            onClick={handleClick}
            title={title || defaultTitle}
            {...props}
        >
            <div
                className={
                    contentAlign === 'end'
                        ? 'd-flex align-items-center justify-content-end gap-1'
                        : 'd-flex align-items-center justify-content-between'
                }
            >
                <span>{children}</span>
                <span
                    className={contentAlign === 'end' ? 'sort-icon' : 'sort-icon ms-1'}
                    style={{ fontSize: '0.8rem', opacity: 0.7 }}
                >
                    {getSortIcon(field)}
                </span>
            </div>
        </th>
    );
};

export default SortableTableHeader;