import React, { useState, useEffect } from 'react';
import { Container, Card, Table, Button, Modal, Form, Alert, Row, Col } from 'react-bootstrap';
import TaskService from '../../service/TaskService';
import AccountService from '../../service/AccountService';

const TasksModal = () => {
    const [tasks, setTasks] = useState([]);
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
        activationStatus: 'ACTIVE'
    });
    const [filters, setFilters] = useState({
        name: '',
        status: 'ACTIVE',
        account: ''
    });

    useEffect(() => {
        loadTasks();
        loadAccounts();
    }, []);

    const loadTasks = async () => {
        setLoading(true);
        try {
            const taskService = new TaskService();
            const response = await taskService.getAll();
            setTasks(response.data);
        } catch (error) {
            console.error('Error loading tasks:', error);
            showAlert('Failed to load tasks', 'danger');
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
            showAlert('Failed to load accounts', 'danger');
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
            activationStatus: 'ACTIVE'
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
            activationStatus: task.activationStatus
        });
        setModalError({ show: false, message: '' });
        setFieldErrors({});
        setShowModal(true);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setModalError({ show: false, message: '' });
        setFieldErrors({});
        
        if (!formData.name || !formData.accountId) {
            setModalError({ show: true, message: 'Please fill in all required fields' });
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
                showAlert('Task updated successfully');
            } else {
                await taskService.create(taskData);
                showAlert('Task created successfully');
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
                const errorMessage = error.response?.data?.message || error.message || 'Failed to save task';
                setModalError({ show: true, message: errorMessage });
            }
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this task?')) {
            return;
        }

        try {
            const taskService = new TaskService();
            await taskService.delete(id);
            showAlert('Task deleted successfully');
            loadTasks();
        } catch (error) {
            console.error('Error deleting task:', error);
            const errorMessage = error.response?.data?.message || error.message || 'Failed to delete task';
            showAlert(errorMessage, 'danger');
        }
    };

    // Filter tasks based on filter criteria
    const filteredTasks = tasks.filter(task => {
        return (
            task.name.toLowerCase().includes(filters.name.toLowerCase()) &&
            (filters.status === '' || task.activationStatus === filters.status) &&
            (filters.account === '' || task.account?.id.toString() === filters.account)
        );
    });

    const closeModal = () => {
        setShowModal(false);
        setModalError({ show: false, message: '' });
        setFieldErrors({});
    };

    const getAccountName = (accountId) => {
        const account = accounts.find(acc => acc.id === accountId);
        return account ? account.name : 'Unknown';
    };

    return (
        <Container fluid className="mt-4">
            <Card>
                <Card.Header>
                    <Row>
                        <Col sm={6}>
                            <h4>Tasks</h4>
                        </Col>
                        <Col sm={6} className="text-end">
                            <Button variant="primary" size="sm" onClick={handleCreate}>
                                + Add Task
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
                                placeholder="Filter by name"
                                name="name"
                                value={filters.name}
                                onChange={handleFilterChange}
                            />
                        </Col>
                        <Col md={3}>
                            <Form.Select name="status" value={filters.status} onChange={handleFilterChange}>
                                <option value="">All Status</option>
                                <option value="ACTIVE">Active</option>
                                <option value="INACTIVE">Inactive</option>
                            </Form.Select>
                        </Col>
                        <Col md={3}>
                            <Form.Select name="account" value={filters.account} onChange={handleFilterChange}>
                                <option value="">All Accounts</option>
                                {accounts.map(account => (
                                    <option key={account.id} value={account.id}>
                                        {account.name}
                                    </option>
                                ))}
                            </Form.Select>
                        </Col>
                    </Row>

                    {/* Tasks Table */}
                    <Table striped bordered hover responsive>
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Type</th>
                                <th>Account</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan="5" className="text-center">Loading...</td>
                                </tr>
                            ) : filteredTasks.length === 0 ? (
                                <tr>
                                    <td colSpan="5" className="text-center">No tasks found</td>
                                </tr>
                            ) : (
                                filteredTasks.map(task => (
                                    <tr key={task.id}>
                                        <td>{task.name}</td>
                                        <td>{task.taskType}</td>
                                        <td>{task.account?.name || 'Unknown'}</td>
                                        <td>{task.activationStatus}</td>
                                        <td>
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                className="me-2"
                                                onClick={() => handleEdit(task)}
                                            >
                                                Edit
                                            </Button>
                                            <Button
                                                variant="outline-danger"
                                                size="sm"
                                                onClick={() => handleDelete(task.id)}
                                            >
                                                Delete
                                            </Button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </Table>
                </Card.Body>
            </Card>

            {/* Add/Edit Task Modal */}
            <Modal show={showModal} onHide={closeModal}>
                <Modal.Header closeButton>
                    <Modal.Title>
                        {editingTask ? 'Edit Task' : 'Create Task'}
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
                            <Form.Label>Name *</Form.Label>
                            <Form.Control
                                type="text"
                                name="name"
                                value={formData.name}
                                onChange={handleInputChange}
                                placeholder="Enter task name"
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
                            <Form.Label>Type *</Form.Label>
                            <Form.Select
                                name="taskType"
                                value={formData.taskType}
                                onChange={handleInputChange}
                                required
                                isInvalid={!!fieldErrors.taskType}
                            >
                                <option value="NORMAL">Normal</option>
                                <option value="VACATION">Vacation</option>
                                <option value="SICK_LEAVE">Sick Leave</option>
                                <option value="PARENTAL_LEAVE">Parental Leave</option>
                            </Form.Select>
                            {fieldErrors.taskType && (
                                <Form.Control.Feedback type="invalid">
                                    {fieldErrors.taskType}
                                </Form.Control.Feedback>
                            )}
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Account *</Form.Label>
                            <Form.Select
                                name="accountId"
                                value={formData.accountId}
                                onChange={handleInputChange}
                                required
                                isInvalid={!!fieldErrors.accountId}
                            >
                                <option value="">Select an account</option>
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
                            <Form.Label>Status *</Form.Label>
                            <Form.Select
                                name="activationStatus"
                                value={formData.activationStatus}
                                onChange={handleInputChange}
                                required
                                isInvalid={!!fieldErrors.activationStatus}
                            >
                                <option value="ACTIVE">Active</option>
                                <option value="INACTIVE">Inactive</option>
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
                            Cancel
                        </Button>
                        <Button variant="primary" type="submit">
                            {editingTask ? 'Update' : 'Create'}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </Container>
    );
};

export default TasksModal;