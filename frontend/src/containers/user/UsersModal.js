import React, { useState, useEffect, useRef } from 'react';
import { Container, Card, Table, Button, Alert, Row, Col, Pagination } from 'react-bootstrap';
import UserService from '../../service/UserService';
import { useTranslation } from 'react-i18next';
import { useTableSort } from '../../hooks/useTableSort';
import SortableTableHeader from '../../components/SortableTableHeader';
import { useConfirm } from '../../components/ConfirmProvider';
import { formatActivationStatus, formatUserRole } from '../../common/displayLabels';

const UsersModal = () => {
    const { t } = useTranslation();
    const confirm = useConfirm();
    const [users, setUsers] = useState([]);
    const { sortedData: sortedUsers, requestSort, getSortIcon } = useTableSort(users, 'firstName');
    const [loading, setLoading] = useState(false);
    const [pendingActionId, setPendingActionId] = useState(null);
    const [alert, setAlert] = useState({ show: false, message: '', type: 'success' });
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

    const searchTimeoutRef = useRef(null);

    useEffect(() => {
        loadUsers();
    }, [pagination.currentPage, pagination.itemsPerPage]);

    useEffect(() => {
        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }

        searchTimeoutRef.current = setTimeout(() => {
            loadUsers();
        }, 300);

        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, [filters.firstName, filters.lastName]);

    useEffect(() => {
        loadUsers();
    }, [filters.status]);

    useEffect(() => {
        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, []);

    const loadUsers = async () => {
        setLoading(true);
        try {
            const userService = new UserService();
            const activeFilter = filters.status === '' ? null : (filters.status === 'ACTIVE');

            const response = await userService.getAllPaged(
                pagination.currentPage - 1,
                pagination.itemsPerPage,
                'firstName',
                'asc',
                activeFilter,
                filters.firstName,
                filters.lastName
            );

            const serverResponse = response.data;
            setPagination(prev => ({
                ...prev,
                currentPage: serverResponse.currentPage + 1,
                totalPages: serverResponse.totalPages,
                totalElements: serverResponse.totalElements
            }));

            setUsers(serverResponse.content);
        } catch (error) {
            console.error('Error loading users:', error);
            showAlert(t('users.errors.loadFailed'), 'danger');
        } finally {
            setLoading(false);
        }
    };

    const showAlert = (message, type = 'success') => {
        setAlert({ show: true, message, type });
        setTimeout(() => setAlert({ show: false, message: '', type: 'success' }), 5000);
    };

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({
            ...prev,
            [name]: value
        }));
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

    const handleDeactivate = async (user) => {
        const confirmMessage = t('users.deactivateConfirm', {
            name: `${user.firstName} ${user.lastName}`.trim(),
            email: user.email,
        });

        const confirmed = await confirm({
            message: confirmMessage,
            confirmLabel: t('users.deactivate'),
            variant: 'danger',
        });
        if (!confirmed) {
            return;
        }

        setPendingActionId(user.id);
        try {
            const userService = new UserService();
            await userService.deactivate(user.id);
            showAlert(t('users.deactivateSuccess'));
            loadUsers();
        } catch (error) {
            console.error('Error deactivating user:', error);
            const errorMessage = error.response?.data?.error
                || error.response?.data?.message
                || error.message
                || t('users.errors.deactivateFailed');
            showAlert(errorMessage, 'danger');
        } finally {
            setPendingActionId(null);
        }
    };

    const handleActivate = async (user) => {
        const confirmMessage = t('users.activateConfirm', {
            name: `${user.firstName} ${user.lastName}`.trim(),
            email: user.email,
        });

        const confirmed = await confirm({
            message: confirmMessage,
            confirmLabel: t('users.activate'),
            variant: 'primary',
        });
        if (!confirmed) {
            return;
        }

        setPendingActionId(user.id);
        try {
            const userService = new UserService();
            await userService.activate(user.id);
            showAlert(t('users.activateSuccess'));
            loadUsers();
        } catch (error) {
            console.error('Error activating user:', error);
            const errorMessage = error.response?.data?.error
                || error.response?.data?.message
                || error.message
                || t('users.errors.activateFailed');
            showAlert(errorMessage, 'danger');
        } finally {
            setPendingActionId(null);
        }
    };

    const totalItems = pagination.totalElements;
    const totalPages = pagination.totalPages;
    const startIndex = (pagination.currentPage - 1) * pagination.itemsPerPage;
    const endIndex = Math.min(startIndex + pagination.itemsPerPage, totalItems);

    return (
        <Container fluid className="mt-4">
            <Card>
                <Card.Header>
                    <Row>
                        <Col>
                            <h4 className="mb-0">{t('users.title')}</h4>
                            <small className="text-muted">
                                {t('users.authentikHint')}
                            </small>
                        </Col>
                    </Row>
                </Card.Header>
                <Card.Body>
                    {alert.show && (
                        <Alert variant={alert.type} dismissible onClose={() => setAlert({ show: false, message: '', type: 'success' })}>
                            {alert.message}
                        </Alert>
                    )}

                    <Row className="mb-3">
                        <Col md={3}>
                            <input
                                className="form-control"
                                type="text"
                                placeholder={t('users.filters.firstName')}
                                name="firstName"
                                value={filters.firstName}
                                onChange={handleFilterChange}
                            />
                        </Col>
                        <Col md={3}>
                            <input
                                className="form-control"
                                type="text"
                                placeholder={t('users.filters.lastName')}
                                name="lastName"
                                value={filters.lastName}
                                onChange={handleFilterChange}
                            />
                        </Col>
                        <Col md={3}>
                            <select className="form-select" name="status" value={filters.status} onChange={handleFilterChange}>
                                <option value="">{t('users.filters.allStatus')}</option>
                                <option value="ACTIVE">{t('users.status.active')}</option>
                                <option value="INACTIVE">{t('users.status.inactive')}</option>
                            </select>
                        </Col>
                        <Col md={3} className="d-flex align-items-center">
                            <span className="me-2">{t('users.pagination.show')}:</span>
                            <select
                                className="form-select form-select-sm"
                                style={{ width: 'auto' }}
                                value={pagination.itemsPerPage}
                                onChange={handlePageSizeChange}
                            >
                                <option value={10}>10</option>
                                <option value={50}>50</option>
                                <option value={100}>100</option>
                            </select>
                            <span className="ms-2">{t('users.pagination.entries')}</span>
                        </Col>
                    </Row>

                    <Table striped bordered hover responsive>
                        <thead className="bg-success">
                            <tr>
                                <SortableTableHeader field="firstName" onSort={requestSort} getSortIcon={getSortIcon} className="text-white">
                                    {t('users.columns.firstName')}
                                </SortableTableHeader>
                                <SortableTableHeader field="lastName" onSort={requestSort} getSortIcon={getSortIcon} className="text-white">
                                    {t('users.columns.lastName')}
                                </SortableTableHeader>
                                <SortableTableHeader field="email" onSort={requestSort} getSortIcon={getSortIcon} className="text-white">
                                    {t('users.columns.email')}
                                </SortableTableHeader>
                                <SortableTableHeader field="userRole" onSort={requestSort} getSortIcon={getSortIcon} className="text-white">
                                    {t('users.columns.role')}
                                </SortableTableHeader>
                                <SortableTableHeader field="activationStatus" onSort={requestSort} getSortIcon={getSortIcon} className="text-white">
                                    {t('users.columns.status')}
                                </SortableTableHeader>
                                <th className="text-white">{t('users.columns.actions')}</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan="6" className="text-center">{t('common.loading.default')}</td>
                                </tr>
                            ) : (sortedUsers || []).length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="text-center">{t('users.empty')}</td>
                                </tr>
                            ) : (
                                (sortedUsers || []).map(user => (
                                    <tr key={user.id}>
                                        <td>{user.firstName}</td>
                                        <td>{user.lastName}</td>
                                        <td>{user.email}</td>
                                        <td>{formatUserRole(user.userRole, t)}</td>
                                        <td>{formatActivationStatus(user.activationStatus, t)}</td>
                                        <td>
                                            {user.activationStatus === 'ACTIVE' ? (
                                                <Button
                                                    variant="outline-warning"
                                                    size="sm"
                                                    disabled={pendingActionId === user.id}
                                                    onClick={() => handleDeactivate(user)}
                                                >
                                                    {pendingActionId === user.id
                                                        ? t('common.loading.default')
                                                        : t('users.deactivate')}
                                                </Button>
                                            ) : (
                                                <Button
                                                    variant="outline-success"
                                                    size="sm"
                                                    disabled={pendingActionId === user.id}
                                                    onClick={() => handleActivate(user)}
                                                >
                                                    {pendingActionId === user.id
                                                        ? t('common.loading.default')
                                                        : t('users.activate')}
                                                </Button>
                                            )}
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </Table>

                    {totalPages > 1 && (
                        <Row className="mt-3">
                            <Col className="d-flex justify-content-between align-items-center">
                                <div className="text-muted">
                                    {t('users.pagination.showing', {
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
        </Container>
    );
};

export default UsersModal;
