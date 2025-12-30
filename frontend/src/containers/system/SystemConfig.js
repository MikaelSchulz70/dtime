import React, { useState, useEffect, useCallback } from "react";
import SystemService from '../../service/SystemService';
import { useToast } from '../../components/Toast';
import { useTranslation } from 'react-i18next';

function SystemPropertyRow({ systemProperty: initialSystemProperty, showError }) {
    const [systemProperty, setSystemProperty] = useState(initialSystemProperty);

    const handleChange = useCallback((event) => {
        let updatedSystemProperty = JSON.parse(JSON.stringify(systemProperty));
        let field = event.target.name;
        let value = event.target.value;
        updatedSystemProperty[field] = value;
        setSystemProperty(updatedSystemProperty);
    }, [systemProperty]);

    const update = useCallback((event) => {
        var service = new SystemService();
        service.updateProperty(systemProperty)
            .then(response => {
                // Property updated successfully
            })
            .catch(error => {
                showError('Failed to update configuration: ' + (error.response?.data?.error || error.message));
            });
    }, [systemProperty, showError]);

    if (systemProperty == null) return null;

    return (
        <tr>
            <td>{systemProperty.name}</td>
            <td><input className="form-control" type="text" value={systemProperty.value} name="value" maxLength="100" onChange={handleChange} onBlur={update} /></td>
            <td>{systemProperty.systemPropertyType}</td>
            <td>{systemProperty.description}</td>
        </tr>
    );
}

function SystemPropertyTable({ systemProperties, showError }) {
    const { t } = useTranslation();
    if (systemProperties == null) return null;

    var systemPropRows = [];
    systemProperties.forEach(function (systemProperty) {
        systemPropRows.push(
            <SystemPropertyRow key={systemProperty.id} systemProperty={systemProperty} showError={showError} />
        );
    });

    return (
        <div className="row">
            <table className="table table-striped">
                <thead className="thead-inverse bg-success text-white">
                    <tr>
                        <th>{t('common.labels.name')}</th>
                        <th>{t('common.labels.value')}</th>
                        <th>{t('common.labels.type')}</th>
                        <th>{t('common.labels.description')}</th>
                    </tr>
                </thead>
                <tbody>{systemPropRows}</tbody>
            </table>
        </div>
    );
}


function SystemConfig(props) {
    const [systemConfig, setSystemConfig] = useState(null);
    const { showError } = useToast();
    const { t } = useTranslation();

    useEffect(() => {
        loadFromServer();
    }, []);

    const loadFromServer = useCallback(() => {
        var service = new SystemService();
        service.getSystemConfig()
            .then(response => {
                setSystemConfig(response.data);
            })
            .catch(error => {
                showError('Failed to load configuration: ' + (error.response?.data?.message || error.message));
            });
    }, [showError]);

    if (systemConfig == null) return null;

    return (
        <div className="container-fluid ml-2 mr-2">
            <h2>{t('system.title')}</h2>
            <SystemPropertyTable systemProperties={systemConfig.systemProperties} showError={showError} />
        </div>
    );
}

export default SystemConfig;