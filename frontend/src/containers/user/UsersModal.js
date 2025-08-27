import React, { useState, useEffect } from 'react';
import { Container, Card, Table, Button, Modal, Form, Alert, Row, Col, Pagination } from 'react-bootstrap';
import UserService from '../../service/UserService';

const UsersModal = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [alert, setAlert] = useState({ show: false, message: '', type: 'success' });
    const [modalError, setModalError] = useState({ show: false, message: '' });
    const [fieldErrors, setFieldErrors] = useState({});
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        userRole: 'USER',
        activationStatus: 'ACTIVE'
    });
    const [filters, setFilters] = useState({
        firstName: '',
        lastName: '',
        status: 'ACTIVE'
    });
    const [pagination, setPagination] = useState({
        currentPage: 1,
        itemsPerPage: 10,
        totalPages: 0,
        totalElements: 0
    });

    useEffect(() => {
        loadUsers();
    }, [pagination.currentPage, pagination.itemsPerPage]);

    const loadUsers = async () => {
        setLoading(true);
        try {
            const userService = new UserService();
            const activeFilter = filters.status === '' ? null : (filters.status === 'ACTIVE');
            const response = await userService.getAllPaged(
                pagination.currentPage - 1, // Backend uses 0-based indexing
                pagination.itemsPerPage,
                'firstName',
                'asc',
                activeFilter,
                filters.firstName,
                filters.lastName
            );
            
            // Update pagination info from server response
            const serverResponse = response.data;
            setPagination(prev => ({
                ...prev,
                currentPage: serverResponse.currentPage + 1, // Convert to 1-based for UI
                totalPages: serverResponse.totalPages,
                totalElements: serverResponse.totalElements
            }));
            
            setUsers(serverResponse.content);
        } catch (error) {
            console.error('Error loading users:', error);
            showAlert('Failed to load users', 'danger');
        } finally {
            setLoading(false);
        }
    };

    const showAlert = (message, type = 'success') => {
        setAlert({ show: true, message, type });
        setTimeout(() => setAlert({ show: false, message: '', type: 'success' }), 5000);
    };

    const handleCreate = () => {
        setEditingUser(null);
        setFormData({
            firstName: '',
            lastName: '',
            email: '',
            password: '',
            userRole: 'USER',
            activationStatus: 'ACTIVE'
        });
        setModalError({ show: false, message: '' });
        setFieldErrors({});
        setShowModal(true);
    };

    const handleEdit = (user) => {
        setEditingUser(user);
        setFormData({
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            password: user.password, // Use the dummy password from backend
            userRole: user.userRole,
            activationStatus: user.activationStatus
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
        // Reset to first page and reload data when filters change
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
        
        if (!formData.firstName || !formData.lastName || !formData.email) {
            setModalError({ show: true, message: 'Please fill in all required fields' });
            return;
        }

        if (!editingUser && !formData.password) {
            setModalError({ show: true, message: 'Password is required for new users' });
            return;
        }

        try {
            const userService = new UserService();
            if (editingUser) {
                const userData = { ...formData, id: editingUser.id };
                await userService.update(userData);
                showAlert('User updated successfully');
            } else {
                await userService.create(formData);
                showAlert('User created successfully');
            }

            setShowModal(false);
            loadUsers();
        } catch (error) {
            console.error('Error saving user:', error);
            
            if (error.response?.status === 400 && error.response?.data?.fieldErrors) {
                // Handle field-level validation errors
                const errors = {};
                error.response.data.fieldErrors.forEach(fieldError => {
                    errors[fieldError.fieldName] = fieldError.fieldError;
                });
                setFieldErrors(errors);
            } else {
                // Handle general errors
                const errorMessage = error.response?.data?.message || error.message || 'Failed to save user';
                setModalError({ show: true, message: errorMessage });
            }
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this user?')) {
            return;
        }

        try {
            const userService = new UserService();
            await userService.delete(id);
            showAlert('User deleted successfully');
            loadUsers();
        } catch (error) {
            console.error('Error deleting user:', error);
            const errorMessage = error.response?.data?.message || error.message || 'Failed to delete user';
            showAlert(errorMessage, 'danger');
        }
    };

    // Server handles filtering and pagination, so we use users directly
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
                            <h4>Users</h4>
                        </Col>
                        <Col sm={6} className="text-end">
                            <Button variant="primary" size="sm" onClick={handleCreate}>
                                + Add User
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
                                placeholder="Filter by first name"
                                name="firstName"
                                value={filters.firstName}
                                onChange={handleFilterChange}
                            />
                        </Col>
                        <Col md={3}>
                            <Form.Control
                                type="text"
                                placeholder="Filter by last name"
                                name="lastName"
                                value={filters.lastName}
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
                        <Col md={3} className="d-flex align-items-center">
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

                    {/* Users Table */}
                    <Table striped bordered hover responsive>
                        <thead>
                            <tr>
                                <th>First Name</th>
                                <th>Last Name</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan="6" className="text-center">Loading...</td>
                                </tr>
                            ) : users.length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="text-center">No users found</td>
                                </tr>
                            ) : (
                                users.map(user => (
                                    <tr key={user.id}>
                                        <td>{user.firstName}</td>
                                        <td>{user.lastName}</td>
                                        <td>{user.email}</td>
                                        <td>{user.userRole}</td>
                                        <td>{user.activationStatus}</td>
                                        <td>
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                className="me-2"
                                                onClick={() => handleEdit(user)}
                                            >
                                                Edit
                                            </Button>
                                            <Button
                                                variant="outline-danger"
                                                size="sm"
                                                onClick={() => handleDelete(user.id)}
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

            {/* Add/Edit User Modal */}
            <Modal show={showModal} onHide={closeModal} size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>
                        {editingUser ? 'Edit User' : 'Create User'}
                    </Modal.Title>
                </Modal.Header>
                <Form onSubmit={handleSubmit}>
                    <Modal.Body>
                        {modalError.show && (
                            <Alert variant="danger" className="mb-3">
                                {modalError.message}
                            </Alert>
                        )}
                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>First Name *</Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="firstName"
                                        value={formData.firstName}
                                        onChange={handleInputChange}
                                        placeholder="Enter first name"
                                        maxLength="30"
                                        required
                                        isInvalid={!!fieldErrors.firstName}
                                    />
                                    {fieldErrors.firstName && (
                                        <Form.Control.Feedback type="invalid">
                                            {fieldErrors.firstName}
                                        </Form.Control.Feedback>
                                    )}
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Last Name *</Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="lastName"
                                        value={formData.lastName}
                                        onChange={handleInputChange}
                                        placeholder="Enter last name"
                                        maxLength="30"
                                        required
                                        isInvalid={!!fieldErrors.lastName}
                                    />
                                    {fieldErrors.lastName && (
                                        <Form.Control.Feedback type="invalid">
                                            {fieldErrors.lastName}
                                        </Form.Control.Feedback>
                                    )}
                                </Form.Group>
                            </Col>
                        </Row>
                        <Form.Group className="mb-3">
                            <Form.Label>Email *</Form.Label>
                            <Form.Control
                                type="email"
                                name="email"
                                value={formData.email}
                                onChange={handleInputChange}
                                placeholder="Enter email address"
                                maxLength="60"
                                required
                                isInvalid={!!fieldErrors.email}
                            />
                            {fieldErrors.email && (
                                <Form.Control.Feedback type="invalid">
                                    {fieldErrors.email}
                                </Form.Control.Feedback>
                            )}
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Password {!editingUser && '*'}</Form.Label>
                            <Form.Control
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleInputChange}
                                placeholder={editingUser ? "Change password or leave as-is to keep current" : "Enter password"}
                                maxLength="80"
                                required={!editingUser}
                                isInvalid={!!fieldErrors.password}
                            />
                            {fieldErrors.password && (
                                <Form.Control.Feedback type="invalid">
                                    {fieldErrors.password}
                                </Form.Control.Feedback>
                            )}
                        </Form.Group>
                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Role *</Form.Label>
                                    <Form.Select
                                        name="userRole"
                                        value={formData.userRole}
                                        onChange={handleInputChange}
                                        required
                                        isInvalid={!!fieldErrors.userRole}
                                    >
                                        <option value="USER">User</option>
                                        <option value="ADMIN">Admin</option>
                                    </Form.Select>
                                    {fieldErrors.userRole && (
                                        <Form.Control.Feedback type="invalid">
                                            {fieldErrors.userRole}
                                        </Form.Control.Feedback>
                                    )}
                                </Form.Group>
                            </Col>
                            <Col md={6}>
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
                            </Col>
                        </Row>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeModal}>
                            Cancel
                        </Button>
                        <Button variant="primary" type="submit">
                            {editingUser ? 'Update' : 'Create'}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </Container>
    );
};

export default UsersModal;