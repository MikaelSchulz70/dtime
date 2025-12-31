import React, { useState, useEffect } from 'react';
import { Button, Table, Modal, Form, Alert, Container, Row, Col, Card } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import SpecialDayService from '../../service/SpecialDayService';
import * as Constants from '../../common/Constants';
import { useTableSort } from '../../hooks/useTableSort';
import SortableTableHeader from '../../components/SortableTableHeader';

const SpecialDays = () => {
    const { t } = useTranslation();
    const [specialDays, setSpecialDays] = useState([]);
    const { sortedData: sortedSpecialDays, requestSort, getSortIcon } = useTableSort(specialDays, 'date');
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
        if (!window.confirm(t('specialDays.messages.specialDayDeleteConfirm'))) {
            return;
        }

        try {
            await SpecialDayService.deleteSpecialDay(id);
            showAlert(t('specialDays.messages.specialDayDeleted'));
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
        if (!window.confirm(t('specialDays.messages.yearDeleteConfirm', { year }))) {
            return;
        }

        try {
            await SpecialDayService.deleteSpecialDaysByYear(year);
            showAlert(t('specialDays.messages.yearDeleted', { year }));
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
                showAlert(t('specialDays.messages.specialDayUpdated'));
            } else {
                console.log('Submitting create for special day:', formData);
                const result = await SpecialDayService.createSpecialDay(formData);
                console.log('Create successful:', result);
                showAlert(t('specialDays.messages.specialDayCreated'));
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
            showAlert(t('specialDays.messages.pleaseSelectJson'), 'danger');
            return;
        }

        try {
            await SpecialDayService.uploadSpecialDays(file);
            showAlert(t('specialDays.messages.uploadSuccess'));
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
        <div className="container-fluid ml-2 mr-2">
            {alert.show && (
                <Alert variant={alert.type} dismissible onClose={() => setAlert({ show: false, message: '', type: 'success' })}>
                    {alert.message}
                </Alert>
            )}

            <h2>{t('specialDays.title')}</h2>
            <div className="row mb-3 align-items-center">
                {/* Left side - Year controls */}
                <div className="col-auto">
                    <label htmlFor="year-select" className="form-label me-2">{t('specialDays.selectYear')}</label>
                    <select
                        id="year-select"
                        className="form-select d-inline-block"
                        style={{ width: 'auto' }}
                        value={selectedYear || ''}
                        onChange={(e) => setSelectedYear(parseInt(e.target.value))}
                    >
                        <option value="">{t('specialDays.selectYearOption')}</option>
                        {availableYears.map(year => (
                            <option key={year} value={year}>{year}</option>
                        ))}
                    </select>
                </div>
                
                {/* Right side - Action buttons */}
                <div className="col text-end">
                    {selectedYear && (
                        <Button
                            variant="danger"
                            size="sm"
                            onClick={() => handleDeleteYear(selectedYear)}
                            className="me-2"
                        >
                            {t('specialDays.deleteAllForYear', { year: selectedYear })}
                        </Button>
                    )}
                    <Button variant="outline-secondary" size="sm" onClick={downloadSampleJson} className="me-2">
                        {t('specialDays.downloadSampleJson')}
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
                            {t('specialDays.uploadJson')}
                        </Button>
                    </label>
                    <Button variant="primary" size="sm" onClick={handleCreate}>
                        {t('specialDays.addSpecialDay')}
                    </Button>
                </div>
            </div>

            <Card>
                <Card.Body>
                    {loading ? (
                        <div className="text-center">
                            <div className="spinner-border" role="status">
                                <span className="visually-hidden">{t('common.loading.default')}</span>
                            </div>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table table-striped table-hover">
                                <thead className="table-dark">
                                    <tr>
                                        <SortableTableHeader 
                                            field="name" 
                                            onSort={requestSort} 
                                            getSortIcon={getSortIcon}
                                            className="text-white"
                                        >
                                            {t('common.labels.name')}
                                        </SortableTableHeader>
                                        <SortableTableHeader 
                                            field="date" 
                                            onSort={requestSort} 
                                            getSortIcon={getSortIcon}
                                            className="text-white"
                                        >
                                            {t('common.labels.date')}
                                        </SortableTableHeader>
                                        <SortableTableHeader 
                                            field="dayType" 
                                            onSort={requestSort} 
                                            getSortIcon={getSortIcon}
                                            className="text-white"
                                        >
                                            {t('common.labels.type')}
                                        </SortableTableHeader>
                                        <th className="text-white">{t('common.labels.actions')}</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {sortedSpecialDays.map(specialDay => (
                                        <tr key={specialDay.id}>
                                            <td>{specialDay.name}</td>
                                            <td>{specialDay.date}</td>
                                            <td>
                                                <span 
                                                    className="badge"
                                                    style={{
                                                        backgroundColor: specialDay.dayType === 'PUBLIC_HOLIDAY' ? Constants.MAJOR_HOLIDAY_COLOR : Constants.HALF_DAY_COLOR,
                                                        color: specialDay.dayType === 'PUBLIC_HOLIDAY' ? '#0c5460' : '#0288d1',
                                                        border: `1px solid ${specialDay.dayType === 'PUBLIC_HOLIDAY' ? '#bee5eb' : '#81d4fa'}`
                                                    }}
                                                >
                                                    {specialDay.dayType === 'PUBLIC_HOLIDAY' ? t('specialDays.types.publicHoliday') : t('specialDays.types.halfDay')}
                                                </span>
                                            </td>
                                            <td>
                                                <Button
                                                    variant="outline-primary"
                                                    size="sm"
                                                    className="me-2"
                                                    onClick={() => handleEdit(specialDay)}
                                                >
                                                    {t('common.buttons.edit')}
                                                </Button>
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    onClick={() => handleDelete(specialDay.id)}
                                                >
                                                    {t('common.buttons.delete')}
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                    {sortedSpecialDays.length === 0 && (
                                        <tr>
                                            <td colSpan="4" className="text-center text-muted">
                                                {selectedYear ? t('specialDays.messages.noSpecialDaysFound') : t('specialDays.messages.selectYearMessage')}
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
                        {editingSpecialDay ? t('specialDays.editSpecialDay') : t('specialDays.createSpecialDay')}
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
                                placeholder={t('specialDays.placeholders.enterSpecialDayName')}
                                maxLength="40"
                                required
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>{t('common.labels.type')} *</Form.Label>
                            <Form.Select
                                name="dayType"
                                value={formData.dayType}
                                onChange={handleInputChange}
                                required
                            >
                                <option value="PUBLIC_HOLIDAY">{t('specialDays.types.publicHoliday')}</option>
                                <option value="HALF_DAY">{t('specialDays.types.halfDay')}</option>
                            </Form.Select>
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>{t('common.labels.date')} *</Form.Label>
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
                            {t('common.buttons.cancel')}
                        </Button>
                        <Button variant="primary" type="submit">
                            {editingSpecialDay ? t('common.buttons.update') : t('common.buttons.create')}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </div>
    );
};

export default SpecialDays;