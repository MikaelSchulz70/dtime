import React from 'react';
import {
    BarChart,
    Bar,
    PieChart,
    Pie,
    Cell,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer
} from 'recharts';
import * as Constants from '../common/Constants';

// Color palette for charts - using application's standard colors
const COLORS = [
    Constants.BRAND_SUCCESS,  // Primary green
    Constants.BRAND_INFO,     // Blue
    Constants.BRAND_WARNING,  // Yellow
    Constants.BRAND_DANGER,   // Red
    '#8884D8',               // Purple
    '#82CA9D',               // Light green
    '#FFC658',               // Orange
    '#FF7C80',               // Light red
    '#8DD1E1',               // Light blue
    '#D084D0',               // Light purple
    '#FFCF96',               // Light orange
    '#A4DE6C',               // Lime
    '#FFA07A',               // Salmon
    '#98D8C8',               // Teal
    '#F7DC6F'                // Light yellow
];

// Custom tooltip formatter
const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
        return (
            <div className="bg-white border rounded shadow-sm p-2" style={{ fontSize: '0.85rem' }}>
                <p className="mb-1 fw-bold">{label}</p>
                {payload.map((entry, index) => (
                    <p key={index} className="mb-0" style={{ color: entry.color }}>
                        {entry.name}: {entry.value}h
                    </p>
                ))}
            </div>
        );
    }
    return null;
};

// User Task Report Bar Chart
export const UserTaskBarChart = ({ userReports }) => {
    if (!userReports || userReports.length === 0) {
        return <div className="text-center text-muted p-4">No data available for chart</div>;
    }

    // Transform data for chart
    const chartData = userReports.map(user => ({
        name: user.fullName,
        totalHours: user.totalTime,
        taskCount: user.taskReports ? user.taskReports.length : 0
    }));

    return (
        <ResponsiveContainer width="100%" height={400}>
            <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 60 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis 
                    dataKey="name" 
                    angle={-45} 
                    textAnchor="end" 
                    height={80}
                    fontSize={12}
                />
                <YAxis fontSize={12} />
                <Tooltip content={<CustomTooltip />} />
                <Legend />
                <Bar dataKey="totalHours" fill={Constants.BRAND_SUCCESS} name="Total Hours" radius={[4, 4, 0, 0]} />
            </BarChart>
        </ResponsiveContainer>
    );
};

// Task Distribution Pie Chart
export const TaskDistributionPieChart = ({ userReports }) => {
    if (!userReports || userReports.length === 0) {
        return <div className="text-center text-muted p-4">No data available for chart</div>;
    }

    // Aggregate all tasks from all users
    const taskMap = new Map();
    userReports.forEach(user => {
        if (user.taskReports) {
            user.taskReports.forEach(task => {
                const key = `${task.accountName} - ${task.taskName}`;
                const currentHours = taskMap.get(key) || 0;
                taskMap.set(key, currentHours + task.totalHours);
            });
        }
    });

    // Convert to chart data
    const chartData = Array.from(taskMap.entries())
        .map(([name, hours]) => ({ name, value: hours }))
        .sort((a, b) => b.value - a.value)
        .slice(0, 10); // Top 10 tasks

    if (chartData.length === 0) {
        return <div className="text-center text-muted p-4">No task data available for chart</div>;
    }

    return (
        <ResponsiveContainer width="100%" height={400}>
            <PieChart>
                <Pie
                    data={chartData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ percent }) => `${(percent * 100).toFixed(1)}%`}
                    outerRadius={120}
                    fill="#8884d8"
                    dataKey="value"
                >
                    {chartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                </Pie>
                <Tooltip 
                    formatter={(value) => [`${value}h`, 'Hours']}
                    labelStyle={{ fontSize: '0.9rem', fontWeight: 'bold' }}
                />
                <Legend 
                    wrapperStyle={{ fontSize: '0.8rem' }}
                    formatter={(value) => value.length > 30 ? value.substring(0, 30) + '...' : value}
                />
            </PieChart>
        </ResponsiveContainer>
    );
};

// Account Hours Bar Chart
export const AccountHoursChart = ({ userReports }) => {
    if (!userReports || userReports.length === 0) {
        return <div className="text-center text-muted p-4">No data available for chart</div>;
    }

    // Aggregate hours by account
    const accountMap = new Map();
    userReports.forEach(user => {
        if (user.taskReports) {
            user.taskReports.forEach(task => {
                const accountName = task.accountName;
                const currentHours = accountMap.get(accountName) || 0;
                accountMap.set(accountName, currentHours + task.totalHours);
            });
        }
    });

    // Convert to chart data
    const chartData = Array.from(accountMap.entries())
        .map(([name, hours]) => ({ name, hours }))
        .sort((a, b) => b.hours - a.hours);

    if (chartData.length === 0) {
        return <div className="text-center text-muted p-4">No account data available for chart</div>;
    }

    return (
        <ResponsiveContainer width="100%" height={400}>
            <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 60 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis 
                    dataKey="name" 
                    angle={-45} 
                    textAnchor="end" 
                    height={80}
                    fontSize={12}
                />
                <YAxis fontSize={12} />
                <Tooltip content={<CustomTooltip />} />
                <Legend />
                <Bar dataKey="hours" fill={Constants.BRAND_INFO} name="Hours" radius={[4, 4, 0, 0]} />
            </BarChart>
        </ResponsiveContainer>
    );
};

// Time Trend Chart for detailed time entries
export const TimeTrendChart = ({ timeReportTasks, days }) => {
    if (!timeReportTasks || !days || timeReportTasks.length === 0 || days.length === 0) {
        return <div className="text-center text-muted p-4">No data available for chart</div>;
    }

    // Transform data for time trend
    const chartData = days.map((day, dayIndex) => {
        const dataPoint = { day: day.day };
        let totalForDay = 0;

        timeReportTasks.forEach((task) => {
            const taskKey = `${task.task.account.name.substring(0, 15)} - ${task.task.name.substring(0, 15)}`;
            const timeEntry = task.timeEntries[dayIndex];
            const hours = timeEntry?.time ? parseFloat(timeEntry.time) : 0;
            dataPoint[taskKey] = hours;
            totalForDay += hours;
        });

        dataPoint.total = totalForDay;
        return dataPoint;
    });

    // Get unique task keys for the legend
    const taskKeys = [];
    if (timeReportTasks.length > 0) {
        timeReportTasks.forEach(task => {
            const taskKey = `${task.task.account.name.substring(0, 15)} - ${task.task.name.substring(0, 15)}`;
            taskKeys.push(taskKey);
        });
    }

    return (
        <ResponsiveContainer width="100%" height={400}>
            <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 60 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis 
                    dataKey="day" 
                    fontSize={12}
                />
                <YAxis fontSize={12} />
                <Tooltip content={<CustomTooltip />} />
                <Legend wrapperStyle={{ fontSize: '0.8rem' }} />
                {taskKeys.map((taskKey, index) => (
                    <Bar 
                        key={taskKey} 
                        dataKey={taskKey} 
                        stackId="time" 
                        fill={COLORS[index % COLORS.length]} 
                        name={taskKey}
                    />
                ))}
            </BarChart>
        </ResponsiveContainer>
    );
};

// Chart View Toggle Component
export const ChartViewToggle = ({ viewMode, onViewChange }) => {
    return (
        <div className="btn-group btn-group-sm" role="group">
            <button
                type="button"
                className={`btn ${viewMode === 'table' ? 'btn-primary' : 'btn-outline-primary'}`}
                onClick={() => onViewChange('table')}
            >
                📋 Table
            </button>
            <button
                type="button"
                className={`btn ${viewMode === 'chart' ? 'btn-primary' : 'btn-outline-primary'}`}
                onClick={() => onViewChange('chart')}
            >
                📊 Charts
            </button>
        </div>
    );
};