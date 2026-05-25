import React, { useCallback, useEffect, useState } from 'react';
import { Alert, Button, ProgressBar, Spinner, Table } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import {
  deleteOllamaModel,
  fetchInstalledOllamaModels,
  fetchModelManifest,
  formatModelSize,
  modelNameMatches,
  pullOllamaModel,
} from '../../service/ChatService';

const ChatModels = ({ onModelsChanged }) => {
  const { t } = useTranslation();
  const [installed, setInstalled] = useState([]);
  const [recommended, setRecommended] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activePull, setActivePull] = useState(null);
  const [pullProgress, setPullProgress] = useState(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    const [installedModels, manifest] = await Promise.all([
      fetchInstalledOllamaModels(),
      fetchModelManifest(),
    ]);
    setLoading(false);

    if (installedModels == null || manifest == null) {
      setError(t('chat.modelsLoadError'));
      return;
    }

    setInstalled(installedModels);
    setRecommended(manifest.models || []);
  }, [t]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const findInstalledName = (manifestName) =>
    installed.find((entry) => modelNameMatches(entry.name, manifestName))?.name;

  const handlePull = async (name) => {
    if (activePull) {
      return;
    }
    setError(null);
    setActivePull(name);
    setPullProgress({ status: 'pulling', percent: null, message: 'pulling manifest' });

    try {
      await pullOllamaModel(name, {
        onProgress: (progress) => {
          setPullProgress(progress);
        },
      });
      await loadData();
      if (onModelsChanged) {
        onModelsChanged();
      }
    } catch (err) {
      setError(err.message || t('chat.modelsPullError'));
    } finally {
      setActivePull(null);
      setPullProgress(null);
    }
  };

  const handleDelete = async (manifestName) => {
    const installedName = findInstalledName(manifestName);
    if (!installedName || activePull) {
      return;
    }
    if (!window.confirm(t('chat.modelsRemoveConfirm', { name: installedName }))) {
      return;
    }
    setError(null);
    try {
      await deleteOllamaModel(installedName);
      await loadData();
      if (onModelsChanged) {
        onModelsChanged();
      }
    } catch (err) {
      setError(err.message || t('chat.modelsRemoveError'));
    }
  };

  const formatModified = (iso) => {
    if (!iso) {
      return '';
    }
    try {
      return new Date(iso).toLocaleString();
    } catch {
      return iso;
    }
  };

  return (
    <div>
      <p className="text-muted small">{t('chat.modelsDescription')}</p>
      <Alert variant="warning" className="small py-2">
        {t('chat.modelsRamWarning')}
      </Alert>

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {activePull && (
        <Alert variant="info" className="mb-3">
          <div className="d-flex justify-content-between align-items-center mb-1">
            <span>{t('chat.modelsPulling', { name: activePull })}</span>
            <Spinner animation="border" size="sm" />
          </div>
          {pullProgress?.message && (
            <div className="small text-muted mb-1">{pullProgress.message}</div>
          )}
          {pullProgress?.percent != null ? (
            <ProgressBar now={pullProgress.percent} label={`${pullProgress.percent}%`} />
          ) : (
            <ProgressBar animated now={100} />
          )}
        </Alert>
      )}

      {loading ? (
        <div className="text-center py-4">
          <Spinner animation="border" role="status" />
        </div>
      ) : (
        <>
          <h6 className="mt-3">{t('chat.modelsRecommended')}</h6>
          <Table responsive size="sm" className="align-middle">
            <thead>
              <tr>
                <th>{t('chat.modelsName')}</th>
                <th>{t('chat.modelsDescriptionCol')}</th>
                <th>{t('chat.modelsStatus')}</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {recommended.map((entry) => {
                const installedEntry = installed.find((m) => modelNameMatches(m.name, entry.name));
                return (
                  <tr key={entry.name}>
                    <td>
                      <code>{entry.name}</code>
                      {entry.required && (
                        <span className="badge bg-secondary ms-1">{t('chat.modelsRequired')}</span>
                      )}
                    </td>
                    <td className="small text-muted">{entry.description}</td>
                    <td>
                      {installedEntry ? (
                        <span className="text-success">{t('chat.modelsInstalled')}</span>
                      ) : (
                        <span className="text-muted">{t('chat.modelsNotInstalled')}</span>
                      )}
                    </td>
                    <td className="text-end text-nowrap">
                      {installedEntry ? (
                        <>
                          <Button
                            variant="outline-primary"
                            size="sm"
                            className="me-1"
                            disabled={!!activePull}
                            onClick={() => handlePull(entry.name)}
                          >
                            {t('chat.modelsUpdate')}
                          </Button>
                          {!entry.required && (
                            <Button
                              variant="outline-danger"
                              size="sm"
                              disabled={!!activePull}
                              onClick={() => handleDelete(entry.name)}
                            >
                              {t('chat.modelsRemove')}
                            </Button>
                          )}
                        </>
                      ) : (
                        <Button
                          variant="primary"
                          size="sm"
                          disabled={!!activePull}
                          onClick={() => handlePull(entry.name)}
                        >
                          {t('chat.modelsInstall')}
                        </Button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </Table>

          <h6 className="mt-4">{t('chat.modelsInstalledList')}</h6>
          {installed.length === 0 ? (
            <p className="text-muted small">{t('chat.noModels')}</p>
          ) : (
            <Table responsive size="sm" className="align-middle">
              <thead>
                <tr>
                  <th>{t('chat.modelsName')}</th>
                  <th>{t('chat.modelsSize')}</th>
                  <th>{t('chat.modelsModified')}</th>
                </tr>
              </thead>
              <tbody>
                {installed.map((entry) => (
                  <tr key={entry.name}>
                    <td><code>{entry.name}</code></td>
                    <td>{formatModelSize(entry.size)}</td>
                    <td className="small text-muted">{formatModified(entry.modified_at)}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}

          <Button variant="outline-secondary" size="sm" onClick={loadData} disabled={loading || !!activePull}>
            {t('chat.refreshModels')}
          </Button>
        </>
      )}
    </div>
  );
};

export default ChatModels;
