import React from 'react';

/** contentAlign 'end': label + sort icon grouped right (use with text-end body cells). */
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
    const handleClick = () => {
        onSort(field);
    };

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
            title={title || `Click to sort by ${children}`}
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