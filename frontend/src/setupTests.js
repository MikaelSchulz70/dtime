import React from 'react';
import '@testing-library/jest-dom';

// Polyfill Web APIs required by React Router 7
import { TextEncoder, TextDecoder } from 'util';

global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder;

// Mock URL.createObjectURL and revokeObjectURL
global.URL.createObjectURL = jest.fn(() => 'mock-object-url');
global.URL.revokeObjectURL = jest.fn();

// Mock additional Web APIs that React Router 7 might need
if (typeof global.ReadableStream === 'undefined') {
  global.ReadableStream = class MockReadableStream {};
}

if (typeof global.TransformStream === 'undefined') {
  global.TransformStream = class MockTransformStream {};
}

if (typeof global.WritableStream === 'undefined') {
  global.WritableStream = class MockWritableStream {};
}

// Mock Request and Response if needed
if (typeof global.Request === 'undefined') {
  global.Request = class MockRequest {};
}

if (typeof global.Response === 'undefined') {
  global.Response = class MockResponse {};
}

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