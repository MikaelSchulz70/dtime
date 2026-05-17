import React, { Component } from 'react';
import { Alert, Button, Container } from 'react-bootstrap';
import i18n from '../i18n';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null
    };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    this.setState({
      error: error,
      errorInfo: errorInfo
    });

    // Log error to external service in production
    if (process.env.NODE_ENV === 'production') {
      console.error('Error caught by boundary:', error, errorInfo);
    }
  }

  handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null
    });
  };

  render() {
    if (this.state.hasError) {
      return (
        <Container className="mt-4">
          <Alert variant="danger">
            <Alert.Heading>{i18n.t('errors.boundary.heading')}</Alert.Heading>
            <p>
              {i18n.t('errors.boundary.message')}
            </p>
            {process.env.NODE_ENV === 'development' && (
              <details className="mt-3" style={{ whiteSpace: 'pre-wrap' }}>
                <summary>{i18n.t('errors.boundary.devDetails')}</summary>
                <hr />
                <strong>{i18n.t('errors.boundary.errorLabel')}</strong> {this.state.error && this.state.error.toString()}
                <br />
                <strong>{i18n.t('errors.boundary.errorInfoLabel')}</strong> {this.state.errorInfo ? this.state.errorInfo.componentStack : i18n.t('errors.boundary.noAdditionalInfo')}
              </details>
            )}
            <hr />
            <div className="d-flex justify-content-end">
              <Button variant="outline-danger" onClick={this.handleRetry}>
                {i18n.t('common.buttons.tryAgain')}
              </Button>
              <Button 
                variant="danger" 
                className="ms-2"
                onClick={() => window.location.reload()}
              >
                {i18n.t('common.buttons.reloadPage')}
              </Button>
            </div>
          </Alert>
        </Container>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;