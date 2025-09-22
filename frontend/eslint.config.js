import js from '@eslint/js';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import globals from 'globals';

export default [
  js.configs.recommended,
  {
    ignores: [
      'dist/**',
      'build/**',
      'node_modules/**',
      'coverage/**',
      '*.min.js',
      'serviceWorker.js'
    ],
  },
  {
    files: ['**/*.{js,jsx}'],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node,
        ...globals.es6,
        ...globals.jest,
      },
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
          experimentalObjectRestSpread: true,
        },
      },
    },
    plugins: {
      react,
      'react-hooks': reactHooks,
    },
    rules: {
      ...react.configs.recommended.rules,
      ...reactHooks.configs.recommended.rules,
      'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
      'no-unused-vars': ['warn', { 
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_',
        ignoreRestSiblings: true 
      }],
      'no-unreachable': 'error',
      'react/prop-types': 'off',
      'react/no-deprecated': 'warn',
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn',
      'no-extra-semi': 'warn',
      'no-prototype-builtins': 'warn',
      'react/no-unescaped-entities': 'error',
    },
    settings: {
      react: {
        version: 'detect',
      },
    },
  },
];