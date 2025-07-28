import React from 'react';
import { Spinner, Container, Row, Col } from 'react-bootstrap';

const LoadingSpinner = ({ 
  size = 'sm', 
  variant = 'primary', 
  centered = false, 
  text = 'Loading...',
  fullPage = false 
}) => {
  const spinner = (
    <div className="d-flex align-items-center">
      <Spinner animation="border" variant={variant} size={size} className="me-2" />
      {text && <span>{text}</span>}
    </div>
  );

  if (fullPage) {
    return (
      <Container fluid className="d-flex justify-content-center align-items-center" 
                 style={{ minHeight: '50vh' }}>
        {spinner}
      </Container>
    );
  }

  if (centered) {
    return (
      <Row className="justify-content-center">
        <Col xs="auto">
          {spinner}
        </Col>
      </Row>
    );
  }

  return spinner;
};

export default LoadingSpinner;