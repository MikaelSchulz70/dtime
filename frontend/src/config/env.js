/**
 * Runtime Environment Configuration
 * 
 * This module provides environment variables that work at runtime,
 * not just at build time. It supports:
 * - Development: Uses process.env variables
 * - Production: Uses window.APP_CONFIG injected at container startup
 */

class Config {
  constructor() {
    this.config = {};
    this.loadConfig();
  }

  loadConfig() {
    // In development, use process.env
    if (process.env.NODE_ENV === 'development') {
      this.config = {
        BACKEND_URL: process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080',
        NODE_ENV: process.env.NODE_ENV || 'development'
      };
    } else {
      // In production, use runtime config from window.APP_CONFIG
      // This is injected by docker-entrypoint.sh
      const runtimeConfig = window.APP_CONFIG || {};
      
      this.config = {
        BACKEND_URL: runtimeConfig.REACT_APP_BACKEND_URL || 'http://localhost:8080',
        NODE_ENV: runtimeConfig.NODE_ENV || 'production'
      };
    }

    console.log('App Config Loaded:', {
      ...this.config,
      source: process.env.NODE_ENV === 'development' ? 'process.env' : 'window.APP_CONFIG'
    });
  }

  get(key) {
    return this.config[key];
  }

  // Getters for common config values
  get backendUrl() {
    return this.get('BACKEND_URL');
  }

  get nodeEnv() {
    return this.get('NODE_ENV');
  }

  get isDevelopment() {
    return this.nodeEnv === 'development';
  }

  get isProduction() {
    return this.nodeEnv === 'production';
  }
}

// Export singleton instance
export default new Config();