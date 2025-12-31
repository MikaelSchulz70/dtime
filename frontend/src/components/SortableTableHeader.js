import React from 'react';

const SortableTableHeader = ({ 
    field, 
    onSort, 
    getSortIcon, 
    children, 
    className = '',
    style = {},
    title = '',
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
            <div className="d-flex align-items-center justify-content-between">
                <span>{children}</span>
                <span className="sort-icon ms-1" style={{ fontSize: '0.8rem', opacity: 0.7 }}>
                    {getSortIcon(field)}
                </span>
            </div>
        </th>
    );
};

export default SortableTableHeader;