// Runtime configuration for development
// This file is overwritten in production by docker-entrypoint.sh
window.APP_CONFIG = {
  REACT_APP_BACKEND_URL: 'http://localhost:8080',
  NODE_ENV: 'development'
};