import React, { useState, useEffect } from 'react';
import { Container, Card, Table, Row, Col, Form, Button } from 'react-bootstrap';
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

const BillableTaskTypeReport = () => {
    const { t } = useTranslation();
    const { showError } = useToast();
    const [reportData, setReportData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [viewMode, setViewMode] = useState('table'); // 'table' or 'charts'
    const [fromDate, setFromDate] = useState(() => {
        const today = new Date();
        const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
        return firstDay.toISOString().split('T')[0];
    });
    const [toDate, setToDate] = useState(() => {
        const today = new Date();
        return today.toISOString().split('T')[0];
    });

    useEffect(() => {
        loadReportData();
    }, []);

    const loadReportData = async () => {
        setLoading(true);
        try {
            const response = await axios.get('/api/report/billable-task-type', {
                params: { fromDate, toDate }
            });
            setReportData(response.data);
        } catch (error) {
            console.error('Error loading billable task type report:', error);
            showError('Failed to load report: ' + (error.response?.data?.message || error.message));
        } finally {
            setLoading(false);
        }
    };

    const handleDateRangeSubmit = (e) => {
        e.preventDefault();
        loadReportData();
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

    // Prepare chart data
    const getBarChartData = () => {
        const labels = reportData.map(item => `${getTaskTypeLabel(item.taskType)} (${getBillableLabel(item.isBillable)})`);
        const data = reportData.map(item => item.totalHours);

        return {
            labels,
            datasets: [
                {
                    label: 'Hours',
                    data,
                    backgroundColor: reportData.map(item => 
                        item.isBillable ? 'rgba(40, 167, 69, 0.8)' : 'rgba(108, 117, 125, 0.8)'
                    ),
                    borderColor: reportData.map(item => 
                        item.isBillable ? 'rgba(40, 167, 69, 1)' : 'rgba(108, 117, 125, 1)'
                    ),
                    borderWidth: 1,
                },
            ],
        };
    };

    const getDoughnutChartData = () => {
        const billableHours = reportData
            .filter(item => item.isBillable)
            .reduce((sum, item) => sum + item.totalHours, 0);
        
        const nonBillableHours = reportData
            .filter(item => !item.isBillable)
            .reduce((sum, item) => sum + item.totalHours, 0);

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

    return (
        <Container fluid className="mt-4">
            <Card>
                <Card.Header>
                    <Row className="align-items-center">
                        <Col>
                            <h4>Billable & Task Type Report</h4>
                        </Col>
                        <Col xs="auto">
                            <Button
                                variant={viewMode === 'table' ? 'primary' : 'outline-primary'}
                                size="sm"
                                className="me-2"
                                onClick={() => setViewMode('table')}
                            >
                                📊 Table
                            </Button>
                            <Button
                                variant={viewMode === 'charts' ? 'primary' : 'outline-primary'}
                                size="sm"
                                onClick={() => setViewMode('charts')}
                            >
                                📈 Charts
                            </Button>
                        </Col>
                    </Row>
                </Card.Header>
                <Card.Body>
                    {/* Date Range Filter */}
                    <Form onSubmit={handleDateRangeSubmit} className="mb-4">
                        <Row className="align-items-end">
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>From Date</Form.Label>
                                    <Form.Control
                                        type="date"
                                        value={fromDate}
                                        onChange={(e) => setFromDate(e.target.value)}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label>To Date</Form.Label>
                                    <Form.Control
                                        type="date"
                                        value={toDate}
                                        onChange={(e) => setToDate(e.target.value)}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Button type="submit" variant="primary" disabled={loading}>
                                    {loading ? 'Loading...' : 'Generate Report'}
                                </Button>
                            </Col>
                        </Row>
                    </Form>

                    {/* Table View */}
                    {viewMode === 'table' && (
                        <>
                            <h5>Report Summary</h5>
                            {reportData.length === 0 ? (
                                <p className="text-muted">No data available for the selected date range.</p>
                            ) : (
                                <Table striped bordered hover responsive>
                                    <thead className="bg-success text-white">
                                        <tr>
                                            <th>Task Type</th>
                                            <th>Billable Status</th>
                                            <th>Total Hours</th>
                                            <th>Number of Tasks</th>
                                            <th>Description</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {reportData.map((item, index) => (
                                            <tr key={index}>
                                                <td>{getTaskTypeLabel(item.taskType)}</td>
                                                <td>
                                                    <span className={`badge ${item.isBillable ? 'bg-success' : 'bg-secondary'}`}>
                                                        {getBillableLabel(item.isBillable)}
                                                    </span>
                                                </td>
                                                <td>{item.totalHours.toFixed(2)}h</td>
                                                <td>{item.taskCount}</td>
                                                <td>{item.description}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>
                            )}
                        </>
                    )}

                    {/* Charts View */}
                    {viewMode === 'charts' && reportData.length > 0 && (
                        <>
                            <h5>Visual Analysis</h5>
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
                        </>
                    )}

                    {viewMode === 'charts' && reportData.length === 0 && (
                        <p className="text-muted">No data available to display charts.</p>
                    )}
                </Card.Body>
            </Card>
        </Container>
    );
};

export default BillableTaskTypeReport;