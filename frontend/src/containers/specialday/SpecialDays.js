import React, { useState, useEffect } from 'react';
import { Button, Table, Modal, Form, Alert, Container, Row, Col, Card } from 'react-bootstrap';
import SpecialDayService from '../../service/SpecialDayService';

const SpecialDays = () => {
    const [specialDays, setSpecialDays] = useState([]);
    const [availableYears, setAvailableYears] = useState([]);
    const [selectedYear, setSelectedYear] = useState(null);
    const [loading, setLoading] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [editingSpecialDay, setEditingSpecialDay] = useState(null);
    const [alert, setAlert] = useState({ show: false, message: '', type: 'success' });
    const [modalError, setModalError] = useState({ show: false, message: '' });
    const [formData, setFormData] = useState({
        name: '',
        dayType: 'PUBLIC_HOLIDAY',
        date: ''
    });

    useEffect(() => {
        loadAvailableYears();
    }, []);

    useEffect(() => {
        if (selectedYear) {
            loadSpecialDaysForYear(selectedYear);
        }
    }, [selectedYear]);

    const showAlert = (message, type = 'success') => {
        setAlert({ show: true, message, type });
        setTimeout(() => setAlert({ show: false, message: '', type: 'success' }), 5000);
    };

    const loadAvailableYears = async () => {
        try {
            const years = await SpecialDayService.getAvailableYears();
            setAvailableYears(years);
            if (years.length > 0) {
                setSelectedYear(years[0]); // Select the most recent year by default
            }
        } catch (error) {
            console.error('Error loading available years:', error);
            const errorMessage = error.response?.data?.message || error.message || 'Failed to load available years';
            showAlert(errorMessage, 'danger');
        }
    };

    const loadSpecialDaysForYear = async (year) => {
        try {
            setLoading(true);
            const data = await SpecialDayService.getSpecialDaysByYear(year);
            setSpecialDays(data);
        } catch (error) {
            console.error('Error loading special days:', error);
            const errorMessage = error.response?.data?.message || error.message || 'Failed to load special days';
            showAlert(errorMessage, 'danger');
        } finally {
            setLoading(false);
        }
    };

    const handleCreate = () => {
        setEditingSpecialDay(null);
        setFormData({
            name: '',
            dayType: 'PUBLIC_HOLIDAY',
            date: ''
        });
        setModalError({ show: false, message: '' });
        setShowModal(true);
    };

    const handleEdit = (specialDay) => {
        setEditingSpecialDay(specialDay);
        setFormData({
            name: specialDay.name,
            dayType: specialDay.dayType,
            date: specialDay.date
        });
        setModalError({ show: false, message: '' });
        setShowModal(true);
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this special day?')) {
            return;
        }

        try {
            await SpecialDayService.deleteSpecialDay(id);
            showAlert('Special day deleted successfully');
            if (selectedYear) {
                loadSpecialDaysForYear(selectedYear);
            }
            loadAvailableYears(); // Refresh years in case last day of a year was deleted
        } catch (error) {
            console.error('Error deleting special day:', error);
            const errorMessage = error.response?.data?.message || error.message || 'Failed to delete special day';
            showAlert(errorMessage, 'danger');
        }
    };

    const handleDeleteYear = async (year) => {
        if (!window.confirm(`Are you sure you want to delete all special days for year ${year}?`)) {
            return;
        }

        try {
            await SpecialDayService.deleteSpecialDaysByYear(year);
            showAlert(`All special days for year ${year} deleted successfully`);
            loadAvailableYears();
            if (selectedYear === year) {
                setSpecialDays([]);
                setSelectedYear(availableYears.find(y => y !== year) || null);
            }
        } catch (error) {
            showAlert('Failed to delete special days for year', 'danger');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setModalError({ show: false, message: '' });

        if (!formData.name || !formData.date || !formData.dayType) {
            setModalError({ show: true, message: 'Please fill in all required fields' });
            return;
        }

        try {
            if (editingSpecialDay) {
                console.log('Submitting update for special day:', editingSpecialDay.id, formData);
                const result = await SpecialDayService.updateSpecialDay(editingSpecialDay.id, formData);
                console.log('Update successful:', result);
                showAlert('Special day updated successfully');
            } else {
                console.log('Submitting create for special day:', formData);
                const result = await SpecialDayService.createSpecialDay(formData);
                console.log('Create successful:', result);
                showAlert('Special day created successfully');
            }

            setShowModal(false);
            const year = new Date(formData.date).getFullYear();
            setSelectedYear(year);
            loadSpecialDaysForYear(year);
            loadAvailableYears();
        } catch (error) {
            console.error('Error saving special day:', error);
            const errorMessage = error.response?.data?.message || error.message || 'Failed to save special day';
            setModalError({ show: true, message: errorMessage });
        }
    };

    const handleFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;

        if (!file.name.endsWith('.json')) {
            showAlert('Please select a JSON file', 'danger');
            return;
        }

        try {
            await SpecialDayService.uploadSpecialDays(file);
            showAlert('Special days uploaded successfully');
            loadAvailableYears();
            if (selectedYear) {
                loadSpecialDaysForYear(selectedYear);
            }
        } catch (error) {
            console.error('Error uploading special days:', error);
            const errorMessage = error.response?.data?.message || error.message || 'Failed to upload special days';
            showAlert(errorMessage, 'danger');
        }

        // Reset file input
        event.target.value = '';
    };

    const downloadSampleJson = () => {
        const sampleData = [
            {
                name: "New Year's Day",
                dayType: "PUBLIC_HOLIDAY",
                date: "2025-01-01"
            },
            {
                name: "Christmas Eve",
                dayType: "HALF_DAY",
                date: "2025-12-24"
            }
        ];

        const dataStr = JSON.stringify(sampleData, null, 2);
        const dataUri = 'data:application/json;charset=utf-8,' + encodeURIComponent(dataStr);

        const exportFileDefaultName = 'special-days-sample.json';

        const linkElement = document.createElement('a');
        linkElement.setAttribute('href', dataUri);
        linkElement.setAttribute('download', exportFileDefaultName);
        linkElement.click();
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    return (
        <Container fluid>
            {alert.show && (
                <Alert variant={alert.type} dismissible onClose={() => setAlert({ show: false, message: '', type: 'success' })}>
                    {alert.message}
                </Alert>
            )}

            <Row className="mb-3">
                <Col>
                    <h2>Special Days Management</h2>
                </Col>
                <Col xs="auto">
                    <Button variant="outline-secondary" size="sm" onClick={downloadSampleJson} className="me-2">
                        Download Sample JSON
                    </Button>
                    <input
                        type="file"
                        accept=".json"
                        onChange={handleFileUpload}
                        style={{ display: 'none' }}
                        id="file-upload"
                    />
                    <label htmlFor="file-upload">
                        <Button variant="outline-primary" size="sm" as="span" className="me-2">
                            Upload JSON
                        </Button>
                    </label>
                    <Button variant="primary" size="sm" onClick={handleCreate}>
                        + Add Special Day
                    </Button>
                </Col>
            </Row>

            <Row className="mb-3">
                <Col xs="auto">
                    <label htmlFor="year-select" className="form-label">Select Year:</label>
                </Col>
                <Col xs="auto">
                    <select
                        id="year-select"
                        className="form-select"
                        value={selectedYear || ''}
                        onChange={(e) => setSelectedYear(parseInt(e.target.value))}
                    >
                        <option value="">Select year</option>
                        {availableYears.map(year => (
                            <option key={year} value={year}>{year}</option>
                        ))}
                    </select>
                </Col>
                {selectedYear && (
                    <Col xs="auto">
                        <Button
                            variant="danger"
                            size="sm"
                            onClick={() => handleDeleteYear(selectedYear)}
                        >
                            Delete All for {selectedYear}
                        </Button>
                    </Col>
                )}
            </Row>

            <Card>
                <Card.Body>
                    {loading ? (
                        <div className="text-center">
                            <div className="spinner-border" role="status">
                                <span className="visually-hidden">Loading...</span>
                            </div>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table table-striped table-hover">
                                <thead className="table-dark">
                                    <tr>
                                        <th>Name</th>
                                        <th>Date</th>
                                        <th>Type</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {specialDays.map(specialDay => (
                                        <tr key={specialDay.id}>
                                            <td>{specialDay.name}</td>
                                            <td>{specialDay.date}</td>
                                            <td>
                                                <span className={`badge ${specialDay.dayType === 'PUBLIC_HOLIDAY' ? 'bg-primary' : 'bg-warning'}`}>
                                                    {specialDay.dayType === 'PUBLIC_HOLIDAY' ? 'Public Holiday' : 'Half Day'}
                                                </span>
                                            </td>
                                            <td>
                                                <Button
                                                    variant="outline-primary"
                                                    size="sm"
                                                    className="me-2"
                                                    onClick={() => handleEdit(specialDay)}
                                                >
                                                    Edit
                                                </Button>
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    onClick={() => handleDelete(specialDay.id)}
                                                >
                                                    Delete
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                    {specialDays.length === 0 && (
                                        <tr>
                                            <td colSpan="4" className="text-center text-muted">
                                                {selectedYear ? 'No special days found for this year' : 'Please select a year to view special days'}
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    )}
                </Card.Body>
            </Card>

            <Modal show={showModal} onHide={() => { setShowModal(false); setModalError({ show: false, message: '' }); }}>
                <Modal.Header closeButton>
                    <Modal.Title>
                        {editingSpecialDay ? 'Edit Special Day' : 'Create Special Day'}
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
                                placeholder="Enter special day name"
                                maxLength="40"
                                required
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Type *</Form.Label>
                            <Form.Select
                                name="dayType"
                                value={formData.dayType}
                                onChange={handleInputChange}
                                required
                            >
                                <option value="PUBLIC_HOLIDAY">Public Holiday</option>
                                <option value="HALF_DAY">Half Day</option>
                            </Form.Select>
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Date *</Form.Label>
                            <Form.Control
                                type="date"
                                name="date"
                                value={formData.date}
                                onChange={handleInputChange}
                                required
                            />
                        </Form.Group>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={() => { setShowModal(false); setModalError({ show: false, message: '' }); }}>
                            Cancel
                        </Button>
                        <Button variant="primary" type="submit">
                            {editingSpecialDay ? 'Update' : 'Create'}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </Container>
    );
};

export default SpecialDays;