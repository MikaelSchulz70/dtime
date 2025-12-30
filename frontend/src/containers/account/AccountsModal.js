import React, { useState, useEffect, useRef } from 'react';
import { Container, Card, Table, Button, Modal, Form, Alert, Row, Col, Pagination } from 'react-bootstrap';
import AccountService from '../../service/AccountService';
import { useTranslation } from 'react-i18next';

const AccountsModal = () => {
    const { t } = useTranslation();
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
    const [pagination, setPagination] = useState({
        currentPage: 1,
        itemsPerPage: 10,
        totalPages: 0,
        totalElements: 0
    });
    
    const searchTimeoutRef = useRef(null);

    useEffect(() => {
        loadAccounts();
    }, [pagination.currentPage, pagination.itemsPerPage]);
    
    // Handle filter changes with debouncing for name
    useEffect(() => {
        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }
        
        searchTimeoutRef.current = setTimeout(() => {
            console.log('Account name filter effect triggered, loading accounts with filters:', filters);
            loadAccounts();
        }, 300);
        
        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, [filters.name]);
    
    // Status filter changes should be immediate
    useEffect(() => {
        console.log('Account status filter changed, loading accounts immediately');
        loadAccounts();
    }, [filters.status]);
    
    // Cleanup timeout on component unmount
    useEffect(() => {
        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, []);

    const loadAccounts = async () => {
        setLoading(true);
        console.log('Loading accounts with filters:', filters, 'pagination:', pagination);
        try {
            const accountService = new AccountService();
            const activeFilter = filters.status === '' ? null : (filters.status === 'ACTIVE');
            console.log('Account API call parameters:', {
                page: pagination.currentPage - 1,
                size: pagination.itemsPerPage,
                sort: 'name',
                direction: 'asc',
                active: activeFilter,
                name: filters.name
            });
            
            const response = await accountService.getAllPaged(
                pagination.currentPage - 1, // Backend uses 0-based indexing
                pagination.itemsPerPage,
                'name',
                'asc',
                activeFilter,
                filters.name
            );
            
            // Update pagination info from server response
            const serverResponse = response.data;
            setPagination(prev => ({
                ...prev,
                currentPage: serverResponse.currentPage + 1, // Convert to 1-based for UI
                totalPages: serverResponse.totalPages,
                totalElements: serverResponse.totalElements
            }));
            
            setAccounts(serverResponse.content);
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
        console.log('Account filter changed:', { name, value });
        
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

    // Server handles filtering and pagination, so we use accounts directly
    const totalItems = pagination.totalElements;
    const totalPages = pagination.totalPages;
    const startIndex = (pagination.currentPage - 1) * pagination.itemsPerPage;
    const endIndex = Math.min(startIndex + pagination.itemsPerPage, totalItems);

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
                                + {t('accounts.addAccount')}
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
                        <Col md={4} className="d-flex align-items-center">
                            <span className="me-2">Show:</span>
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
                            <span className="ms-2">entries</span>
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
                            ) : accounts.length === 0 ? (
                                <tr>
                                    <td colSpan="3" className="text-center">No accounts found</td>
                                </tr>
                            ) : (
                                accounts.map(account => (
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
                    
                    {/* Pagination */}
                    {totalPages > 1 && (
                        <Row className="mt-3">
                            <Col className="d-flex justify-content-between align-items-center">
                                <div className="text-muted">
                                    Showing {Math.min(startIndex + 1, totalItems)} to {endIndex} of {totalItems} entries
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

            {/* Add/Edit Account Modal */}
            <Modal show={showModal} onHide={closeModal}>
                <Modal.Header closeButton>
                    <Modal.Title>
                        {editingAccount ? t('accounts.editAccount') : t('accounts.createAccount')}
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