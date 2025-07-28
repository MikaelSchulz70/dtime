import React, { Component } from 'react';
import { Alert, Button, Container } from 'react-bootstrap';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null
    };
  }

  static getDerivedStateFromError(error) {
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
            <Alert.Heading>Oops! Something went wrong</Alert.Heading>
            <p>
              We're sorry, but something unexpected happened. Please try refreshing the page
              or contact support if the problem persists.
            </p>
            {process.env.NODE_ENV === 'development' && (
              <details className="mt-3" style={{ whiteSpace: 'pre-wrap' }}>
                <summary>Error Details (Development Only)</summary>
                <hr />
                <strong>Error:</strong> {this.state.error && this.state.error.toString()}
                <br />
                <strong>Error Info:</strong> {this.state.errorInfo.componentStack}
              </details>
            )}
            <hr />
            <div className="d-flex justify-content-end">
              <Button variant="outline-danger" onClick={this.handleRetry}>
                Try Again
              </Button>
              <Button 
                variant="danger" 
                className="ms-2"
                onClick={() => window.location.reload()}
              >
                Reload Page
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