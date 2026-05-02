import React, { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Container, Row } from 'react-bootstrap';
import { useLocation } from 'react-router';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
// import logo from '../assets/logo.png'; // Using public logo instead

const Login = () => {
  const LAST_OIDC_USER_KEY = 'dtime.lastOidcUser';
  const sanitizeDisplayName = (value) => (value || '').replace(/\s*-\s*$/, '').trim();
  const { t } = useTranslation();
  const location = useLocation();
  const [oidcEnabled, setOidcEnabled] = useState(false);
  const [loading, setLoading] = useState(true);
  const [rememberedUser, setRememberedUser] = useState('');

  useEffect(() => {
    const checkOidcAuth = async () => {
      try {
        const response = await axios.get('/api/auth/oidc/status');
        setOidcEnabled(Boolean(response.data?.enabled));
      } catch (error) {
        console.error('Failed to check OIDC auth status:', error);
        setOidcEnabled(false);
      } finally {
        setLoading(false);
      }
    };

    checkOidcAuth();
  }, []);

  useEffect(() => {
    const cachedUser = window.localStorage.getItem(LAST_OIDC_USER_KEY);
    if (cachedUser) {
      setRememberedUser(sanitizeDisplayName(cachedUser));
    }
  }, []);

  const handleAuthentikLogin = () => {
    window.location.href = '/oauth2/authorization/authentik';
  };

  const handleSwitchUserLogin = () => {
    window.localStorage.removeItem(LAST_OIDC_USER_KEY);
    window.location.href = '/api/auth/oidc/switch-user';
  };

  const urlParams = new URLSearchParams(location.search);
  const hasError = urlParams.get('error');
  const errorReason = urlParams.get('reason');
  const hasLogout = urlParams.get('logout');

  return (
    <Container className="min-vh-100 d-flex align-items-center justify-content-center">
      <Row className="w-100 justify-content-center">
        <Col xs={12} sm={8} md={6} lg={4}>
          <Card className="shadow-lg border-0">
            <Card.Body className="p-5">
              {/* Logo */}
              <div className="text-center mb-4">
                <img
                  src="/logo.png"
                  alt="D-Time"
                  style={{ maxWidth: '60%', height: 'auto' }}
                  className="mb-3"
                />
                <h2 className="text-center fw-bold text-success mb-4">{t('auth.login.title')}</h2>
              </div>

              {/* Error Messages */}
              {hasError && (
                <Alert variant="danger" className="mb-3">
                  {hasError === 'oauth'
                    ? `OIDC login failed${errorReason ? ` (${errorReason})` : ''}.`
                    : t('auth.login.errors.invalidCredentials')}
                </Alert>
              )}

              {hasLogout && (
                <Alert variant="info" className="mb-3">
                  {t('auth.login.logoutMessage')}
                </Alert>
              )}

              <div className="d-grid">
                <Button
                  variant="success"
                  onClick={handleAuthentikLogin}
                  size="lg"
                  className="rounded-3 fw-bold"
                  disabled={loading || !oidcEnabled}
                >
                  {loading
                    ? t('common.loading.loggingIn')
                    : rememberedUser
                      ? `Continue as ${rememberedUser}`
                      : 'Sign in with Authentik'}
                </Button>
              </div>
              <div className="d-grid mt-2">
                <Button
                  variant="outline-secondary"
                  onClick={handleSwitchUserLogin}
                  className="rounded-3"
                  disabled={loading || !oidcEnabled}
                >
                  Sign in as another user
                </Button>
              </div>
              {!loading && !oidcEnabled && (
                <Alert variant="warning" className="mt-3 mb-0">
                  OIDC login is not enabled in backend configuration.
                </Alert>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Login;