import { useState, useMemo } from 'react';

export const useTableSort = (data, defaultSortField = null) => {
    const [sortConfig, setSortConfig] = useState({
        field: defaultSortField,
        direction: 'asc'
    });

    const sortedData = useMemo(() => {
        if (!sortConfig.field || !data) return data;

        return [...data].sort((a, b) => {
            const aValue = getNestedValue(a, sortConfig.field);
            const bValue = getNestedValue(b, sortConfig.field);

            if (aValue === null || aValue === undefined) return 1;
            if (bValue === null || bValue === undefined) return -1;

            // Handle different data types
            let comparison = 0;
            if (typeof aValue === 'string' && typeof bValue === 'string') {
                comparison = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
            } else if (typeof aValue === 'number' && typeof bValue === 'number') {
                comparison = aValue - bValue;
            } else if (aValue instanceof Date && bValue instanceof Date) {
                comparison = aValue.getTime() - bValue.getTime();
            } else {
                // Convert to string and compare
                comparison = String(aValue).toLowerCase().localeCompare(String(bValue).toLowerCase());
            }

            return sortConfig.direction === 'desc' ? -comparison : comparison;
        });
    }, [data, sortConfig]);

    const requestSort = (field) => {
        let direction = 'asc';
        if (sortConfig.field === field && sortConfig.direction === 'asc') {
            direction = 'desc';
        }
        setSortConfig({ field, direction });
    };

    const getSortIcon = (field) => {
        if (sortConfig.field !== field) {
            return '↕️'; // Sortable indicator
        }
        return sortConfig.direction === 'asc' ? '⬆️' : '⬇️';
    };

    return {
        sortedData,
        requestSort,
        getSortIcon,
        sortConfig
    };
};

// Helper function to get nested object values
const getNestedValue = (obj, path) => {
    return path.split('.').reduce((value, key) => {
        return value && value[key] !== undefined ? value[key] : null;
    }, obj);
};