import React, { useState, useEffect } from 'react';
import { Container, Card, Table, Button, Modal, Form, Alert, Row, Col } from 'react-bootstrap';
import UserService from '../../service/UserService';

const UsersModal = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [alert, setAlert] = useState({ show: false, message: '', type: 'success' });
    const [modalError, setModalError] = useState({ show: false, message: '' });
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

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        setLoading(true);
        try {
            const userService = new UserService();
            const response = await userService.getAll();
            setUsers(response.data);
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
        setShowModal(true);
    };

    const handleEdit = (user) => {
        setEditingUser(user);
        setFormData({
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            password: '', // Don't show existing password
            userRole: user.userRole,
            activationStatus: user.activationStatus
        });
        setModalError({ show: false, message: '' });
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
            const userData = editingUser ? { ...formData, id: editingUser.id } : formData;
            await userService.addOrUdate(userData);
            showAlert(editingUser ? 'User updated successfully' : 'User created successfully');

            setShowModal(false);
            loadUsers();
        } catch (error) {
            console.error('Error saving user:', error);
            const errorMessage = error.response?.data?.message || error.message || 'Failed to save user';
            setModalError({ show: true, message: errorMessage });
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

    // Filter users based on filter criteria
    const filteredUsers = users.filter(user => {
        return (
            user.firstName.toLowerCase().includes(filters.firstName.toLowerCase()) &&
            user.lastName.toLowerCase().includes(filters.lastName.toLowerCase()) &&
            (filters.status === '' || user.activationStatus === filters.status)
        );
    });

    const closeModal = () => {
        setShowModal(false);
        setModalError({ show: false, message: '' });
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
                            ) : filteredUsers.length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="text-center">No users found</td>
                                </tr>
                            ) : (
                                filteredUsers.map(user => (
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
                                    />
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
                                    />
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
                            />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Password {!editingUser && '*'}</Form.Label>
                            <Form.Control
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleInputChange}
                                placeholder={editingUser ? "Leave blank to keep current password" : "Enter password"}
                                maxLength="80"
                                required={!editingUser}
                            />
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
                                    >
                                        <option value="USER">User</option>
                                        <option value="ADMIN">Admin</option>
                                    </Form.Select>
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
                                    >
                                        <option value="ACTIVE">Active</option>
                                        <option value="INACTIVE">Inactive</option>
                                    </Form.Select>
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