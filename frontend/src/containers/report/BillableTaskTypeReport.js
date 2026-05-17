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
import { formatTaskType } from '../../common/displayLabels';

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
            showError(t('reports.messages.loadFailed', { message: error.response?.data?.message || error.message }));
        } finally {
            setLoading(false);
        }
    };

    const handleDateRangeSubmit = (e) => {
        e.preventDefault();
        loadReportData();
    };

    const getBillableLabel = (isBillable) => {
        return isBillable ? t('reports.billable.billable') : t('reports.billable.nonBillable');
    };

    // Prepare chart data
    const getBarChartData = () => {
        const labels = reportData.map(item => `${formatTaskType(item.taskType, t)} (${getBillableLabel(item.isBillable)})`);
        const data = reportData.map(item => item.totalHours);

        return {
            labels,
            datasets: [
                {
                    label: t('reports.cardHours'),
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
            labels: [t('reports.billable.billableHours'), t('reports.billable.nonBillableHours')],
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
                text: t('reports.billable.hoursByTaskTypeChart'),
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
                text: t('reports.billable.totalBillableChart'),
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
                            <h4>{t('reports.billableTaskTypeReport')}</h4>
                        </Col>
                        <Col xs="auto">
                            <Button
                                variant={viewMode === 'table' ? 'primary' : 'outline-primary'}
                                size="sm"
                                className="me-2"
                                onClick={() => setViewMode('table')}
                            >
                                {t('reports.billable.tableView')}
                            </Button>
                            <Button
                                variant={viewMode === 'charts' ? 'primary' : 'outline-primary'}
                                size="sm"
                                onClick={() => setViewMode('charts')}
                            >
                                {t('reports.billable.chartsView')}
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
                                    <Form.Label>{t('reports.billable.fromDate')}</Form.Label>
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
                                    <Form.Label>{t('reports.billable.toDate')}</Form.Label>
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
                                    {loading ? t('common.loading.default') : t('reports.billable.generateReport')}
                                </Button>
                            </Col>
                        </Row>
                    </Form>

                    {/* Table View */}
                    {viewMode === 'table' && (
                        <>
                            <h5>{t('reports.reportSummary')}</h5>
                            {reportData.length === 0 ? (
                                <p className="text-muted">{t('reports.billable.noDataForRange')}</p>
                            ) : (
                                <Table striped bordered hover responsive>
                                    <thead className="bg-success text-white">
                                        <tr>
                                            <th>{t('reports.billable.taskTypeColumn')}</th>
                                            <th>{t('reports.billable.billableStatusColumn')}</th>
                                            <th className="col-num">{t('reports.billable.totalHoursColumn')}</th>
                                            <th className="col-num">{t('reports.billable.numberOfTasksColumn')}</th>
                                            <th>{t('common.labels.description')}</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {reportData.map((item, index) => (
                                            <tr key={index}>
                                                <td>{formatTaskType(item.taskType, t)}</td>
                                                <td>
                                                    <span className={`badge ${item.isBillable ? 'bg-success' : 'bg-secondary'}`}>
                                                        {getBillableLabel(item.isBillable)}
                                                    </span>
                                                </td>
                                                <td className="col-num">{item.totalHours.toFixed(2)}h</td>
                                                <td className="col-num">{item.taskCount}</td>
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
                            <h5>{t('reports.visualAnalysis')}</h5>
                            <Row className="mb-4">
                                <Col md={8}>
                                    <Card>
                                        <Card.Header>
                                            <h6>{t('reports.hoursByTaskTypeBillability')}</h6>
                                        </Card.Header>
                                        <Card.Body>
                                            <Bar data={getBarChartData()} options={chartOptions} />
                                        </Card.Body>
                                    </Card>
                                </Col>
                                <Col md={4}>
                                    <Card>
                                        <Card.Header>
                                            <h6>{t('reports.billableDistribution')}</h6>
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
                        <p className="text-muted">{t('reports.noChartsData')}</p>
                    )}
                </Card.Body>
            </Card>
        </Container>
    );
};

export default BillableTaskTypeReport;