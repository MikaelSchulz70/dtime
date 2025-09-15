import React from 'react';
import '@testing-library/jest-dom';

// Mock react-modal for tests
jest.mock('react-modal', () => {
  const Modal = ({ children, isOpen, ...props }) => {
    return isOpen ? <div data-testid="modal" {...props}>{children}</div> : null;
  };
  Modal.setAppElement = jest.fn();
  return Modal;
});

// Setup DOM element for modal
beforeEach(() => {
  const root = document.createElement('div');
  root.id = 'root';
  if (!document.getElementById('root')) {
    document.body.appendChild(root);
  }
});

// Mock window.location
delete window.location;
window.location = {
  href: 'http://localhost',
  origin: 'http://localhost',
  protocol: 'http:',
  hostname: 'localhost',
  port: '',
  pathname: '/',
  search: '',
  hash: '',
  reload: jest.fn(),
  assign: jest.fn()
};

// Mock console methods to reduce noise in tests
global.console = {
  ...console,
  warn: jest.fn(),
  error: jest.fn(),
  log: jest.fn()
};

// Mock fetch globally
global.fetch = jest.fn();

// Mock window.alert
window.alert = jest.fn();

// Mock axios defaults
jest.mock('axios', () => ({
  get: jest.fn(() => Promise.resolve({ data: {} })),
  post: jest.fn(() => Promise.resolve({ data: {} })),
  put: jest.fn(() => Promise.resolve({ data: {} })),
  delete: jest.fn(() => Promise.resolve({ data: {} })),
  defaults: {
    xsrfHeaderName: 'X-XSRF-TOKEN',
    xsrfCookieName: 'XSRF-TOKEN'
  }
}));