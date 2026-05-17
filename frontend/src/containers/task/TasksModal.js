import React, { useState, useEffect, useRef } from 'react';
import { Container, Card, Table, Button, Modal, Form, Alert, Row, Col, Pagination } from 'react-bootstrap';
import TaskService from '../../service/TaskService';
import AccountService from '../../service/AccountService';
import { useTranslation } from 'react-i18next';
import { useTableSort } from '../../hooks/useTableSort';
import SortableTableHeader from '../../components/SortableTableHeader';
import { useConfirm } from '../../components/ConfirmProvider';
import { formatActivationStatus, formatTaskType } from '../../common/displayLabels';

const TasksModal = () => {
    const { t } = useTranslation();
    const confirm = useConfirm();
    const [tasks, setTasks] = useState([]);
    const { sortedData: sortedTasks, requestSort, getSortIcon } = useTableSort(tasks, 'name');
    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [editingTask, setEditingTask] = useState(null);
    const [alert, setAlert] = useState({ show: false, message: '', type: 'success' });
    const [modalError, setModalError] = useState({ show: false, message: '' });
    const [fieldErrors, setFieldErrors] = useState({});
    const [formData, setFormData] = useState({
        name: '',
        taskType: 'NORMAL',
        accountId: '',
        activationStatus: 'ACTIVE',
        isBillable: false
    });
    const [filters, setFilters] = useState({
        name: '',
        status: 'ACTIVE',
        account: ''
    });
    const [pagination, setPagination] = useState({
        currentPage: 1,
        itemsPerPage: 10,
        totalPages: 0,
        totalElements: 0
    });

    const searchTimeoutRef = useRef(null);

    useEffect(() => {
        loadTasks();
        loadAccounts();
    }, []);

    useEffect(() => {
        loadTasks();
    }, [pagination.currentPage, pagination.itemsPerPage]);

    // Handle filter changes with debouncing for name
    useEffect(() => {
        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }

        searchTimeoutRef.current = setTimeout(() => {
            console.log('Task name filter effect triggered, loading tasks with filters:', filters);
            loadTasks();
        }, 300);

        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, [filters.name]);

    // Status and account filter changes should be immediate
    useEffect(() => {
        console.log('Task status/account filter changed, loading tasks immediately');
        loadTasks();
    }, [filters.status, filters.account]);

    // Cleanup timeout on component unmount
    useEffect(() => {
        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, []);

    const loadTasks = async () => {
        setLoading(true);
        console.log('Loading tasks with filters:', filters, 'pagination:', pagination);
        try {
            const taskService = new TaskService();
            const activeFilter = filters.status === '' ? null : (filters.status === 'ACTIVE');
            const accountFilter = filters.account === '' ? null : filters.account;
            console.log('Task API call parameters:', {
                page: pagination.currentPage - 1,
                size: pagination.itemsPerPage,
                sort: 'name',
                direction: 'asc',
                active: activeFilter,
                name: filters.name,
                account: accountFilter
            });

            const response = await taskService.getAllPaged(
                pagination.currentPage - 1, // Backend uses 0-based indexing
                pagination.itemsPerPage,
                'name',
                'asc',
                activeFilter,
                filters.name,
                accountFilter
            );

            // Update pagination info from server response
            const serverResponse = response.data;
            setPagination(prev => ({
                ...prev,
                currentPage: serverResponse.currentPage + 1, // Convert to 1-based for UI
                totalPages: serverResponse.totalPages,
                totalElements: serverResponse.totalElements
            }));

            setTasks(serverResponse.content);
        } catch (error) {
            console.error('Error loading tasks:', error);
            showAlert(t('tasks.messages.loadTasksFailed'), 'danger');
        } finally {
            setLoading(false);
        }
    };

    const loadAccounts = async () => {
        try {
            const accountService = new AccountService();
            const response = await accountService.getAll();
            setAccounts(response.data.filter(account => account.activationStatus === 'ACTIVE'));
        } catch (error) {
            console.error('Error loading accounts:', error);
            showAlert(t('tasks.messages.loadAccountsFailed'), 'danger');
        }
    };

    const showAlert = (message, type = 'success') => {
        setAlert({ show: true, message, type });
        setTimeout(() => setAlert({ show: false, message: '', type: 'success' }), 5000);
    };

    const handleCreate = () => {
        setEditingTask(null);
        setFormData({
            name: '',
            taskType: 'NORMAL',
            accountId: accounts.length > 0 ? accounts[0].id : '',
            activationStatus: 'ACTIVE',
            isBillable: false
        });
        setModalError({ show: false, message: '' });
        setFieldErrors({});
        setShowModal(true);
    };

    const handleEdit = (task) => {
        setEditingTask(task);
        setFormData({
            name: task.name,
            taskType: task.taskType,
            accountId: task.account?.id || '',
            activationStatus: task.activationStatus,
            isBillable: task.isBillable || false
        });
        setModalError({ show: false, message: '' });
        setFieldErrors({});
        setShowModal(true);
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        console.log('Task filter changed:', { name, value });

        setFilters(prev => ({
            ...prev,
            [name]: value
        }));

        // Reset to first page when filters change
        setPagination(prev => ({ ...prev, currentPage: 1 }));
    };

    const handlePageSizeChange = (e) => {
        const newPageSize = parseInt(e.target.value, 10);
        setPagination({
            currentPage: 1,
            itemsPerPage: newPageSize
        });
    };

    const handlePageChange = (pageNumber) => {
        setPagination(prev => ({
            ...prev,
            currentPage: pageNumber
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setModalError({ show: false, message: '' });
        setFieldErrors({});

        if (!formData.name || !formData.accountId) {
            setModalError({ show: true, message: t('common.messages.fillRequiredFields') });
            return;
        }

        try {
            const taskService = new TaskService();
            const taskData = {
                ...formData,
                account: { id: formData.accountId }
            };

            if (editingTask) {
                taskData.id = editingTask.id;
                await taskService.update(taskData);
                showAlert(t('tasks.messages.taskUpdated'));
            } else {
                await taskService.create(taskData);
                showAlert(t('tasks.messages.taskCreated'));
            }

            setShowModal(false);
            loadTasks();
        } catch (error) {
            console.error('Error saving task:', error);

            if (error.response?.status === 400 && error.response?.data?.fieldErrors) {
                // Handle field-level validation errors
                const errors = {};
                error.response.data.fieldErrors.forEach(fieldError => {
                    errors[fieldError.fieldName] = fieldError.fieldError;
                });
                setFieldErrors(errors);
            } else {
                // Handle general errors
                const errorMessage = error.response?.data?.message || error.message || t('tasks.messages.saveFailed');
                setModalError({ show: true, message: errorMessage });
            }
        }
    };

    const handleDelete = async (id) => {
        const confirmed = await confirm({
            message: t('tasks.messages.taskDeleteConfirm'),
            title: t('common.messages.confirmDelete'),
            confirmLabel: t('common.buttons.delete'),
            variant: 'danger',
        });
        if (!confirmed) {
            return;
        }

        try {
            const taskService = new TaskService();
            await taskService.delete(id);
            showAlert(t('tasks.messages.taskDeleted'));
            loadTasks();
        } catch (error) {
            console.error('Error deleting task:', error);
            const errorMessage = error.response?.data?.message || error.message || t('tasks.messages.deleteTaskFailed');
            showAlert(errorMessage, 'danger');
        }
    };

    // Server handles filtering and pagination, so we use tasks directly
    const totalItems = pagination.totalElements;
    const totalPages = pagination.totalPages;
    const startIndex = (pagination.currentPage - 1) * pagination.itemsPerPage;
    const endIndex = Math.min(startIndex + pagination.itemsPerPage, totalItems);

    const closeModal = () => {
        setShowModal(false);
        setModalError({ show: false, message: '' });
        setFieldErrors({});
    };

    const getAccountName = (accountId) => {
        const account = accounts.find(acc => acc.id === accountId);
        return account ? account.name : t('common.labels.unknownUser');
    };

    return (
        <Container fluid className="mt-4">
            <Card>
                <Card.Header>
                    <Row>
                        <Col sm={6}>
                            <h4>{t('tasks.title')}</h4>
                        </Col>
                        <Col sm={6} className="text-end">
                            <Button variant="primary" size="sm" onClick={handleCreate}>
                                + {t('tasks.addTask')}
                            </Button>
                        </Col>
                    </Row>
                </Card.Header>
                <Card.Body>
                    {alert.show && (
                        <Alert variant={alert.type} dismissible onClose={() => setAlert({ show: false, message: '', type: 'success' })}>
                            {alert.message}
                        </Alert>
                    )}

                    {/* Filters */}
                    <Row className="mb-3">
                        <Col md={3}>
                            <Form.Control
                                type="text"
                                placeholder={t('tasks.placeholders.filterByName')}
                                name="name"
                                value={filters.name}
                                onChange={handleFilterChange}
                            />
                        </Col>
                        <Col md={3}>
                            <Form.Select name="status" value={filters.status} onChange={handleFilterChange}>
                                <option value="">{t('tasks.filters.allStatus')}</option>
                                <option value="ACTIVE">{t('common.status.active')}</option>
                                <option value="INACTIVE">{t('common.status.inactive')}</option>
                            </Form.Select>
                        </Col>
                        <Col md={3}>
                            <Form.Select name="account" value={filters.account} onChange={handleFilterChange}>
                                <option value="">{t('tasks.filters.allAccounts')}</option>
                                {accounts.map(account => (
                                    <option key={account.id} value={account.id}>
                                        {account.name}
                                    </option>
                                ))}
                            </Form.Select>
                        </Col>
                        <Col md={3} className="d-flex align-items-center">
                            <span className="me-2">{t('common.pagination.show')}</span>
                            <Form.Select
                                size="sm"
                                style={{ width: 'auto' }}
                                value={pagination.itemsPerPage}
                                onChange={handlePageSizeChange}
                            >
                                <option value={10}>10</option>
                                <option value={50}>50</option>
                                <option value={100}>100</option>
                            </Form.Select>
                            <span className="ms-2">{t('common.pagination.entries')}</span>
                        </Col>
                    </Row>

                    {/* Tasks Table */}
                    <Table striped bordered hover responsive>
                        <thead className="bg-success">
                            <tr>
                                <SortableTableHeader
                                    field="name"
                                    onSort={requestSort}
                                    getSortIcon={getSortIcon}
                                    className="text-white"
                                >
                                    {t('tasks.columns.name')}
                                </SortableTableHeader>
                                <SortableTableHeader
                                    field="taskType"
                                    onSort={requestSort}
                                    getSortIcon={getSortIcon}
                                    className="text-white"
                                >
                                    {t('tasks.columns.type')}
                                </SortableTableHeader>
                                <SortableTableHeader
                                    field="account.name"
                                    onSort={requestSort}
                                    getSortIcon={getSortIcon}
                                    className="text-white"
                                >
                                    {t('tasks.columns.account')}
                                </SortableTableHeader>
                                <SortableTableHeader
                                    field="activationStatus"
                                    onSort={requestSort}
                                    getSortIcon={getSortIcon}
                                    className="text-white"
                                >
                                    {t('tasks.columns.status')}
                                </SortableTableHeader>
                                <SortableTableHeader
                                    field="isBillable"
                                    onSort={requestSort}
                                    getSortIcon={getSortIcon}
                                    className="text-white"
                                >
                                    {t('tasks.columns.billable')}
                                </SortableTableHeader>
                                <th className="text-white">{t('tasks.columns.actions')}</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan="6" className="text-center">{t('common.loading.default')}</td>
                                </tr>
                            ) : (sortedTasks || []).length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="text-center">{t('tasks.messages.noTasksFound')}</td>
                                </tr>
                            ) : (
                                (sortedTasks || []).map(task => (
                                    <tr key={task.id}>
                                        <td>{task.name}</td>
                                        <td>{formatTaskType(task.taskType, t)}</td>
                                        <td>{task.account?.name || t('common.labels.unknownUser')}</td>
                                        <td>{formatActivationStatus(task.activationStatus, t)}</td>
                                        <td>
                                            {task.isBillable ? (
                                                <span className="text-success">{t('tasks.billable.yes')}</span>
                                            ) : (
                                                <span className="text-muted">{t('tasks.billable.no')}</span>
                                            )}
                                        </td>
                                        <td>
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                className="me-2"
                                                onClick={() => handleEdit(task)}
                                            >
                                                {t('common.buttons.edit')}
                                            </Button>
                                            <Button
                                                variant="outline-danger"
                                                size="sm"
                                                onClick={() => handleDelete(task.id)}
                                            >
                                                {t('common.buttons.delete')}
                                            </Button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </Table>

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <Row className="mt-3">
                            <Col className="d-flex justify-content-between align-items-center">
                                <div className="text-muted">
                                    {t('tasks.pagination.showing', {
                                        from: Math.min(startIndex + 1, totalItems),
                                        to: endIndex,
                                        total: totalItems,
                                    })}
                                </div>
                                <Pagination size="sm">
                                    <Pagination.Prev
                                        disabled={pagination.currentPage === 1}
                                        onClick={() => handlePageChange(pagination.currentPage - 1)}
                                    />
                                    {[...Array(totalPages)].map((_, index) => (
                                        <Pagination.Item
                                            key={index + 1}
                                            active={index + 1 === pagination.currentPage}
                                            onClick={() => handlePageChange(index + 1)}
                                        >
                                            {index + 1}
                                        </Pagination.Item>
                                    ))}
                                    <Pagination.Next
                                        disabled={pagination.currentPage === totalPages}
                                        onClick={() => handlePageChange(pagination.currentPage + 1)}
                                    />
                                </Pagination>
                            </Col>
                        </Row>
                    )}
                </Card.Body>
            </Card>

            {/* Add/Edit Task Modal */}
            <Modal show={showModal} onHide={closeModal}>
                <Modal.Header closeButton>
                    <Modal.Title>
                        {editingTask ? t('tasks.editTask') : t('tasks.createTask')}
                    </Modal.Title>
                </Modal.Header>
                <Form onSubmit={handleSubmit}>
                    <Modal.Body>
                        {modalError.show && (
                            <Alert variant="danger" className="mb-3">
                                {modalError.message}
                            </Alert>
                        )}
                        <Form.Group className="mb-3">
                            <Form.Label>{t('common.labels.name')} *</Form.Label>
                            <Form.Control
                                type="text"
                                name="name"
                                value={formData.name}
                                onChange={handleInputChange}
                                placeholder={t('tasks.placeholders.enterTaskName')}
                                required
                                isInvalid={!!fieldErrors.name}
                            />
                            {fieldErrors.name && (
                                <Form.Control.Feedback type="invalid">
                                    {fieldErrors.name}
                                </Form.Control.Feedback>
                            )}
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>{t('common.labels.type')} *</Form.Label>
                            <Form.Select
                                name="taskType"
                                value={formData.taskType}
                                onChange={handleInputChange}
                                required
                                isInvalid={!!fieldErrors.taskType}
                            >
                                <option value="NORMAL">{t('tasks.types.normal')}</option>
                                <option value="VACATION">{t('tasks.types.vacation')}</option>
                                <option value="SICK_LEAVE">{t('tasks.types.sickLeave')}</option>
                                <option value="PARENTAL_LEAVE">{t('tasks.types.parentalLeave')}</option>
                            </Form.Select>
                            {fieldErrors.taskType && (
                                <Form.Control.Feedback type="invalid">
                                    {fieldErrors.taskType}
                                </Form.Control.Feedback>
                            )}
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>{t('common.labels.account')} *</Form.Label>
                            <Form.Select
                                name="accountId"
                                value={formData.accountId}
                                onChange={handleInputChange}
                                required
                                isInvalid={!!fieldErrors.accountId}
                            >
                                <option value="">{t('tasks.messages.selectAccount')}</option>
                                {accounts.map(account => (
                                    <option key={account.id} value={account.id}>
                                        {account.name}
                                    </option>
                                ))}
                            </Form.Select>
                            {fieldErrors.accountId && (
                                <Form.Control.Feedback type="invalid">
                                    {fieldErrors.accountId}
                                </Form.Control.Feedback>
                            )}
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>{t('tasks.labels.billable')} *</Form.Label>
                            <Form.Control
                                className="form-check-input"
                                type="checkbox"
                                name="isBillable"
                                id="isBillable"
                                checked={formData.isBillable}
                                onChange={handleInputChange}
                                style={{ marginTop: '2px' }}
                            />
                            {fieldErrors.isBillable && (
                                <div className="invalid-feedback d-block">
                                    {fieldErrors.isBillable}
                                </div>
                            )}
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>{t('common.labels.status')} *</Form.Label>
                            <Form.Select
                                name="activationStatus"
                                value={formData.activationStatus}
                                onChange={handleInputChange}
                                required
                                isInvalid={!!fieldErrors.activationStatus}
                            >
                                <option value="ACTIVE">{t('common.status.active')}</option>
                                <option value="INACTIVE">{t('common.status.inactive')}</option>
                            </Form.Select>
                            {fieldErrors.activationStatus && (
                                <Form.Control.Feedback type="invalid">
                                    {fieldErrors.activationStatus}
                                </Form.Control.Feedback>
                            )}
                        </Form.Group>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeModal}>
                            {t('common.buttons.cancel')}
                        </Button>
                        <Button variant="primary" type="submit">
                            {editingTask ? t('common.buttons.update') : t('common.buttons.create')}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </Container>
    );
};

export default TasksModal;