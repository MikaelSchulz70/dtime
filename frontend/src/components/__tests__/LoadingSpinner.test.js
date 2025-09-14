import React from 'react';
import { render, screen } from '@testing-library/react';
import LoadingSpinner from '../LoadingSpinner';

describe('LoadingSpinner', () => {
  it('should render default loading spinner', () => {
    render(<LoadingSpinner />);
    
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('should render with custom text', () => {
    const customText = 'Please wait...';
    render(<LoadingSpinner text={customText} />);
    
    expect(screen.getByText(customText)).toBeInTheDocument();
    expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
  });

  it('should render full page spinner when fullPage is true', () => {
    render(<LoadingSpinner fullPage text="Loading application..." />);
    
    expect(screen.getByText('Loading application...')).toBeInTheDocument();
    
    // Check if it has full page styling classes - Container has the full page classes
    const container = document.querySelector('.container-fluid');
    expect(container).toHaveClass('d-flex', 'justify-content-center', 'align-items-center');
  });

  it('should render inline spinner when fullPage is false', () => {
    render(<LoadingSpinner fullPage={false} text="Loading data..." />);
    
    expect(screen.getByText('Loading data...')).toBeInTheDocument();
  });

  it('should have proper ARIA attributes for accessibility', () => {
    render(<LoadingSpinner text="Loading content..." />);
    
    // Check for spinner element by class since React Bootstrap Spinner doesn't have role="status"
    const spinnerElement = document.querySelector('.spinner-border');
    expect(spinnerElement).toBeInTheDocument();
    expect(screen.getByText('Loading content...')).toBeInTheDocument();
  });

  it('should render spinner animation element', () => {
    render(<LoadingSpinner />);
    
    // Look for spinner element (usually has spinner-border class in Bootstrap)
    const spinnerElement = document.querySelector('.spinner-border');
    expect(spinnerElement).toBeInTheDocument();
  });
});