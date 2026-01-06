import React, { useState, useEffect } from 'react';
import { Table, Row, Col, Form, Button, Card } from 'react-bootstrap';
import { Bar, Doughnut } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
    ArcElement,
} from 'chart.js';
import axios from 'axios';
import { useToast } from '../../components/Toast';
import { useTranslation } from 'react-i18next';
import { useTableSort } from '../../hooks/useTableSort';
import SortableTableHeader from '../../components/SortableTableHeader';
import { ChartViewToggle } from '../../components/Charts';

// Register ChartJS components
ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
    ArcElement
);

const BillableTaskTypeReportTable = ({ report }) => {
    const { t } = useTranslation();
    const { showError } = useToast();
    const [reportData, setReportData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [viewMode, setViewMode] = useState('table');
    
    const { sortedData: sortedReportData, requestSort, getSortIcon } = useTableSort(
        reportData, 
        'totalHours'
    );

    useEffect(() => {
        if (report && report.fromDate && report.toDate) {
            loadReportData();
        }
    }, [report]);

    const loadReportData = async () => {
        setLoading(true);
        try {
            const response = await axios.get('/api/report/billable-task-type', {
                params: { 
                    fromDate: report.fromDate, 
                    toDate: report.toDate 
                }
            });
            setReportData(response.data);
        } catch (error) {
            console.error('Error loading billable task type report:', error);
            showError('Failed to load report: ' + (error.response?.data?.message || error.message));
        } finally {
            setLoading(false);
        }
    };

    const getTaskTypeLabel = (taskType) => {
        const labels = {
            'NORMAL': 'Normal',
            'VACATION': 'Vacation',
            'SICK_LEAVE': 'Sick Leave',
            'PARENTAL_LEAVE': 'Parental Leave'
        };
        return labels[taskType] || taskType;
    };

    const getBillableLabel = (isBillable) => {
        return isBillable ? 'Billable' : 'Non-billable';
    };

    // Calculate totals
    const totalHours = reportData.reduce((sum, item) => sum + item.totalHours, 0);
    const billableHours = reportData.filter(item => item.isBillable).reduce((sum, item) => sum + item.totalHours, 0);
    const nonBillableHours = reportData.filter(item => !item.isBillable).reduce((sum, item) => sum + item.totalHours, 0);
    const totalTasks = reportData.reduce((sum, item) => sum + item.taskCount, 0);

    // Prepare chart data
    const getBarChartData = () => {
        const labels = sortedReportData.map(item => `${getTaskTypeLabel(item.taskType)} (${getBillableLabel(item.isBillable)})`);
        const data = sortedReportData.map(item => item.totalHours);

        return {
            labels,
            datasets: [
                {
                    label: 'Hours',
                    data,
                    backgroundColor: sortedReportData.map(item => 
                        item.isBillable ? 'rgba(40, 167, 69, 0.8)' : 'rgba(108, 117, 125, 0.8)'
                    ),
                    borderColor: sortedReportData.map(item => 
                        item.isBillable ? 'rgba(40, 167, 69, 1)' : 'rgba(108, 117, 125, 1)'
                    ),
                    borderWidth: 1,
                },
            ],
        };
    };

    const getDoughnutChartData = () => {
        return {
            labels: ['Billable Hours', 'Non-billable Hours'],
            datasets: [
                {
                    data: [billableHours, nonBillableHours],
                    backgroundColor: [
                        'rgba(40, 167, 69, 0.8)',
                        'rgba(108, 117, 125, 0.8)',
                    ],
                    borderColor: [
                        'rgba(40, 167, 69, 1)',
                        'rgba(108, 117, 125, 1)',
                    ],
                    borderWidth: 1,
                },
            ],
        };
    };

    const chartOptions = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top',
            },
            title: {
                display: true,
                text: 'Billable vs Non-billable Hours by Task Type',
            },
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: {
                    callback: function(value) {
                        return value + 'h';
                    }
                }
            }
        }
    };

    const doughnutOptions = {
        responsive: true,
        plugins: {
            legend: {
                position: 'right',
            },
            title: {
                display: true,
                text: 'Total Billable vs Non-billable Hours',
            },
            tooltip: {
                callbacks: {
                    label: function(context) {
                        return context.label + ': ' + context.parsed + 'h';
                    }
                }
            }
        },
    };

    if (loading) {
        return (
            <div className="col-12">
                <div className="text-center py-4">
                    <div className="spinner-border text-success" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                    <p className="mt-2">Loading billable task type report...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="col-12">
            <Card>
                <Card.Header className="bg-success text-white">
                    <Row className="align-items-center">
                        <Col>
                            <h5 className="mb-0 fw-bold text-white">Billable & Task Type Analysis</h5>
                        </Col>
                        <Col xs="auto">
                            <ChartViewToggle viewMode={viewMode} onViewChange={setViewMode} />
                        </Col>
                    </Row>
                </Card.Header>
                <Card.Body>
                    {/* Summary Statistics */}
                    <Row className="mb-3">
                        <Col md={3}>
                            <div className="text-center p-3 bg-light rounded">
                                <h5 className="text-success mb-1">{totalHours.toFixed(2)}h</h5>
                                <small className="text-muted">Total Hours</small>
                            </div>
                        </Col>
                        <Col md={3}>
                            <div className="text-center p-3 bg-success text-white rounded">
                                <h5 className="mb-1">{billableHours.toFixed(2)}h</h5>
                                <small>Billable Hours</small>
                            </div>
                        </Col>
                        <Col md={3}>
                            <div className="text-center p-3 bg-secondary text-white rounded">
                                <h5 className="mb-1">{nonBillableHours.toFixed(2)}h</h5>
                                <small>Non-billable Hours</small>
                            </div>
                        </Col>
                        <Col md={3}>
                            <div className="text-center p-3 bg-info text-white rounded">
                                <h5 className="mb-1">{totalTasks}</h5>
                                <small>Total Tasks</small>
                            </div>
                        </Col>
                    </Row>

                    {/* Table or Chart View */}
                    {viewMode === 'table' ? (
                        <>
                            <h5>Detailed Breakdown</h5>
                            {sortedReportData.length === 0 ? (
                                <p className="text-muted">No data available for the selected date range.</p>
                            ) : (
                                <Table striped bordered hover responsive>
                                    <thead className="bg-success text-white">
                                        <tr>
                                            <SortableTableHeader 
                                                field="taskType" 
                                                onSort={requestSort} 
                                                getSortIcon={getSortIcon}
                                                className="text-white"
                                            >
                                                Task Type
                                            </SortableTableHeader>
                                            <SortableTableHeader 
                                                field="isBillable" 
                                                onSort={requestSort} 
                                                getSortIcon={getSortIcon}
                                                className="text-white"
                                            >
                                                Billable Status
                                            </SortableTableHeader>
                                            <SortableTableHeader 
                                                field="totalHours" 
                                                onSort={requestSort} 
                                                getSortIcon={getSortIcon}
                                                className="text-white text-end"
                                            >
                                                Total Hours
                                            </SortableTableHeader>
                                            <SortableTableHeader 
                                                field="taskCount" 
                                                onSort={requestSort} 
                                                getSortIcon={getSortIcon}
                                                className="text-white text-end"
                                            >
                                                Number of Tasks
                                            </SortableTableHeader>
                                            <SortableTableHeader 
                                                field="percentage" 
                                                onSort={requestSort} 
                                                getSortIcon={getSortIcon}
                                                className="text-white text-end"
                                            >
                                                % of Total
                                            </SortableTableHeader>
                                            <SortableTableHeader 
                                                field="description" 
                                                onSort={requestSort} 
                                                getSortIcon={getSortIcon}
                                                className="text-white"
                                            >
                                                Description
                                            </SortableTableHeader>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {sortedReportData.map((item, index) => (
                                            <tr key={index}>
                                                <td className="fw-medium">{getTaskTypeLabel(item.taskType)}</td>
                                                <td>
                                                    <span className={`badge ${item.isBillable ? 'bg-success' : 'bg-secondary'}`}>
                                                        {getBillableLabel(item.isBillable)}
                                                    </span>
                                                </td>
                                                <td className="text-end">{item.totalHours.toFixed(2)} hrs</td>
                                                <td className="text-end">{item.taskCount}</td>
                                                <td className="text-end">{totalHours > 0 ? ((item.totalHours / totalHours) * 100).toFixed(1) : 0}%</td>
                                                <td>{item.description}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                    <tfoot className="bg-light">
                                        <tr>
                                            <td colSpan={2}><strong>Total</strong></td>
                                            <td className="text-end"><strong>{totalHours.toFixed(2)} hrs</strong></td>
                                            <td className="text-end"><strong>{totalTasks}</strong></td>
                                            <td className="text-end"><strong>100%</strong></td>
                                            <td></td>
                                        </tr>
                                    </tfoot>
                                </Table>
                            )}
                        </>
                    ) : (
                        <>
                            <h5>Visual Analysis</h5>
                            {sortedReportData.length === 0 ? (
                                <p className="text-muted">No data available to display charts.</p>
                            ) : (
                                <Row className="mb-4">
                                    <Col md={8}>
                                        <Card>
                                            <Card.Header>
                                                <h6>Hours by Task Type & Billability</h6>
                                            </Card.Header>
                                            <Card.Body>
                                                <Bar data={getBarChartData()} options={chartOptions} />
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                    <Col md={4}>
                                        <Card>
                                            <Card.Header>
                                                <h6>Billable vs Non-billable Distribution</h6>
                                            </Card.Header>
                                            <Card.Body>
                                                <Doughnut data={getDoughnutChartData()} options={doughnutOptions} />
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                </Row>
                            )}
                        </>
                    )}
                </Card.Body>
            </Card>
        </div>
    );
};

export default BillableTaskTypeReportTable;