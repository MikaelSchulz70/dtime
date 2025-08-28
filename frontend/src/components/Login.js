import React, { useState } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert } from 'react-bootstrap';
import axios from 'axios';
import logo from '../assets/logo.png';

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const formDataToSend = new FormData();
      formDataToSend.append('username', formData.username);
      formDataToSend.append('password', formData.password);

      const response = await axios.post('/perform_login', formDataToSend, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        maxRedirects: 0 // Don't follow redirects automatically
      });

      // Login successful - redirect to main app
      window.location.href = '/';
    } catch (error) {
      console.error('Login error:', error);
      if (error.response?.status === 302) {
        // Handle redirect - check if it's success or error
        const location = error.response.headers.location;
        if (location && location.includes('error')) {
          setError('Invalid username or password.');
        } else {
          // Successful login redirect
          window.location.href = '/';
        }
      } else if (error.response?.status === 401) {
        setError('Invalid username or password.');
      } else {
        setError('Login failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Check if we have error or logout parameters in URL
  const urlParams = new URLSearchParams(window.location.search);
  const hasError = urlParams.get('error');
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
                  src={logo}
                  alt="D-Time"
                  style={{ maxWidth: '60%', height: 'auto' }}
                  className="mb-3"
                />
                <h2 className="text-center fw-bold text-success mb-4">Login</h2>
              </div>

              {/* Error Messages */}
              {hasError && (
                <Alert variant="danger" className="mb-3">
                  Invalid username or password.
                </Alert>
              )}

              {hasLogout && (
                <Alert variant="info" className="mb-3">
                  You have been logged out.
                </Alert>
              )}

              {error && (
                <Alert variant="danger" className="mb-3">
                  {error}
                </Alert>
              )}

              {/* Login Form */}
              <Form onSubmit={handleSubmit}>
                <Form.Group className="mb-3">
                  <Form.Control
                    type="email"
                    name="username"
                    placeholder="Username"
                    value={formData.username}
                    onChange={handleChange}
                    required
                    size="lg"
                    className="rounded-3"
                  />
                </Form.Group>

                <Form.Group className="mb-4">
                  <Form.Control
                    type="password"
                    name="password"
                    placeholder="Password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                    size="lg"
                    className="rounded-3"
                  />
                </Form.Group>

                <div className="d-grid">
                  <Button
                    variant="success"
                    type="submit"
                    disabled={loading}
                    size="lg"
                    className="rounded-3 fw-bold"
                  >
                    {loading ? 'Logging in...' : 'Log in'}
                  </Button>
                </div>
              </Form>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Login;