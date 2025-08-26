import React, { useState, useEffect } from 'react';
import { Container, Card, Table, Button, Modal, Form, Alert, Row, Col } from 'react-bootstrap';
import AccountService from '../../service/AccountService';

const AccountsModal = () => {
    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [editingAccount, setEditingAccount] = useState(null);
    const [alert, setAlert] = useState({ show: false, message: '', type: 'success' });
    const [modalError, setModalError] = useState({ show: false, message: '' });
    const [fieldErrors, setFieldErrors] = useState({});
    const [formData, setFormData] = useState({
        name: '',
        activationStatus: 'ACTIVE'
    });
    const [filters, setFilters] = useState({
        name: '',
        status: 'ACTIVE'
    });

    useEffect(() => {
        loadAccounts();
    }, []);

    const loadAccounts = async () => {
        setLoading(true);
        try {
            const accountService = new AccountService();
            const response = await accountService.getAll();
            setAccounts(response.data);
        } catch (error) {
            console.error('Error loading accounts:', error);
            showAlert('Failed to load accounts', 'danger');
        } finally {
            setLoading(false);
        }
    };

    const showAlert = (message, type = 'success') => {
        setAlert({ show: true, message, type });
        setTimeout(() => setAlert({ show: false, message: '', type: 'success' }), 5000);
    };

    const handleCreate = () => {
        setEditingAccount(null);
        setFormData({
            name: '',
            activationStatus: 'ACTIVE'
        });
        setModalError({ show: false, message: '' });
        setFieldErrors({});
        setShowModal(true);
    };

    const handleEdit = (account) => {
        setEditingAccount(account);
        setFormData({
            name: account.name,
            activationStatus: account.activationStatus
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
        
        if (!formData.name) {
            setModalError({ show: true, message: 'Please fill in all required fields' });
            return;
        }

        try {
            const accountService = new AccountService();
            if (editingAccount) {
                const accountData = { ...formData, id: editingAccount.id };
                await accountService.update(accountData);
                showAlert('Account updated successfully');
            } else {
                await accountService.create(formData);
                showAlert('Account created successfully');
            }

            setShowModal(false);
            loadAccounts();
        } catch (error) {
            console.error('Error saving account:', error);
            
            if (error.response?.status === 400 && error.response?.data?.fieldErrors) {
                // Handle field-level validation errors
                const errors = {};
                error.response.data.fieldErrors.forEach(fieldError => {
                    errors[fieldError.fieldName] = fieldError.fieldError;
                });
                setFieldErrors(errors);
            } else {
                // Handle general errors
                const errorMessage = error.response?.data?.message || error.message || 'Failed to save account';
                setModalError({ show: true, message: errorMessage });
            }
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this account?')) {
            return;
        }

        try {
            const accountService = new AccountService();
            await accountService.delete(id);
            showAlert('Account deleted successfully');
            loadAccounts();
        } catch (error) {
            console.error('Error deleting account:', error);
            const errorMessage = error.response?.data?.message || error.message || 'Failed to delete account';
            showAlert(errorMessage, 'danger');
        }
    };

    // Filter accounts based on filter criteria
    const filteredAccounts = accounts.filter(account => {
        return (
            account.name.toLowerCase().includes(filters.name.toLowerCase()) &&
            (filters.status === '' || account.activationStatus === filters.status)
        );
    });

    const closeModal = () => {
        setShowModal(false);
        setModalError({ show: false, message: '' });
        setFieldErrors({});
    };

    return (
        <Container fluid className="mt-4">
            <Card>
                <Card.Header>
                    <Row>
                        <Col sm={6}>
                            <h4>Accounts</h4>
                        </Col>
                        <Col sm={6} className="text-end">
                            <Button variant="primary" size="sm" onClick={handleCreate}>
                                + Add Account
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
                        <Col md={4}>
                            <Form.Control
                                type="text"
                                placeholder="Filter by name"
                                name="name"
                                value={filters.name}
                                onChange={handleFilterChange}
                            />
                        </Col>
                        <Col md={4}>
                            <Form.Select name="status" value={filters.status} onChange={handleFilterChange}>
                                <option value="">All Status</option>
                                <option value="ACTIVE">Active</option>
                                <option value="INACTIVE">Inactive</option>
                            </Form.Select>
                        </Col>
                    </Row>

                    {/* Accounts Table */}
                    <Table striped bordered hover responsive>
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan="3" className="text-center">Loading...</td>
                                </tr>
                            ) : filteredAccounts.length === 0 ? (
                                <tr>
                                    <td colSpan="3" className="text-center">No accounts found</td>
                                </tr>
                            ) : (
                                filteredAccounts.map(account => (
                                    <tr key={account.id}>
                                        <td>{account.name}</td>
                                        <td>{account.activationStatus}</td>
                                        <td>
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                className="me-2"
                                                onClick={() => handleEdit(account)}
                                            >
                                                Edit
                                            </Button>
                                            <Button
                                                variant="outline-danger"
                                                size="sm"
                                                onClick={() => handleDelete(account.id)}
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

            {/* Add/Edit Account Modal */}
            <Modal show={showModal} onHide={closeModal}>
                <Modal.Header closeButton>
                    <Modal.Title>
                        {editingAccount ? 'Edit Account' : 'Create Account'}
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
                                placeholder="Enter account name"
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
                            {editingAccount ? 'Update' : 'Create'}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </Container>
    );
};

export default AccountsModal;