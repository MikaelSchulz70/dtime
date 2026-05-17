/**
 * Runtime environment configuration for the browser.
 * - Development: defaults to https://localhost:8443 (webpack proxies /api to backend)
 * - Production: window.APP_CONFIG from docker-entrypoint.sh
 */

const DEFAULT_BACKEND_URL = 'https://localhost:8443';
const DEFAULT_OLLAMA_BRIDGE_URL = 'http://localhost:8082';
const DEFAULT_OLLAMA_MODEL = 'llama3.2';

class Config {
  constructor() {
    this.config = {};
    this.loadConfig();
  }

  loadConfig() {
    const runtimeConfig =
      typeof window !== 'undefined' && window.APP_CONFIG ? window.APP_CONFIG : {};

    this.config = {
      BACKEND_URL: runtimeConfig.REACT_APP_BACKEND_URL || DEFAULT_BACKEND_URL,
      OLLAMA_BRIDGE_URL: runtimeConfig.OLLAMA_BRIDGE_URL || DEFAULT_OLLAMA_BRIDGE_URL,
      OLLAMA_DEFAULT_MODEL: runtimeConfig.OLLAMA_DEFAULT_MODEL || DEFAULT_OLLAMA_MODEL,
      NODE_ENV: runtimeConfig.NODE_ENV || 'development',
    };
  }

  get(key) {
    return this.config[key];
  }

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

  get ollamaBridgeUrl() {
    return this.get('OLLAMA_BRIDGE_URL');
  }

  get ollamaDefaultModel() {
    return this.get('OLLAMA_DEFAULT_MODEL');
  }
}

export default new Config();
