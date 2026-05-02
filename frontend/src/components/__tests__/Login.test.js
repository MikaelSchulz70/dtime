import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import axios from 'axios';
import Login from '../Login';

jest.mock('axios');
const mockedAxios = axios;

jest.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key) => key,
  }),
}));

const renderLoginAt = (search = '') => {
  const path = search ? `/login${search}` : '/login';
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/login" element={<Login />} />
      </Routes>
    </MemoryRouter>
  );
};

describe('Login (OIDC / Authentik)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    window.localStorage.clear();
    Object.defineProperty(window, 'location', {
      value: { href: '' },
      writable: true,
      configurable: true,
    });
    mockedAxios.get.mockResolvedValue({ data: { enabled: false } });
  });

  it('loads OIDC status from backend', async () => {
    renderLoginAt();
    await waitFor(() => {
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/auth/oidc/status');
    });
  });

  it('shows warning when OIDC is disabled', async () => {
    mockedAxios.get.mockResolvedValue({ data: { enabled: false } });
    renderLoginAt();

    await waitFor(() => {
      expect(screen.getByText(/OIDC login is not enabled/i)).toBeInTheDocument();
    });
    expect(screen.getByRole('button', { name: /Sign in with Authentik/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /Sign in as another user/i })).toBeDisabled();
  });

  it('enables Authentik buttons when OIDC is enabled', async () => {
    mockedAxios.get.mockResolvedValue({ data: { enabled: true } });
    renderLoginAt();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Sign in with Authentik/i })).not.toBeDisabled();
    });
    expect(screen.getByRole('button', { name: /Sign in as another user/i })).not.toBeDisabled();
  });

  it('shows Continue as when last user is cached in localStorage', async () => {
    window.localStorage.setItem('dtime.lastOidcUser', 'Pat Paterson');
    mockedAxios.get.mockResolvedValue({ data: { enabled: true } });
    renderLoginAt();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Continue as Pat Paterson/i })).toBeInTheDocument();
    });
  });

  it('redirects to OAuth authorization when primary button is clicked', async () => {
    const user = userEvent.setup();
    mockedAxios.get.mockResolvedValue({ data: { enabled: true } });
    renderLoginAt();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Sign in with Authentik/i })).not.toBeDisabled();
    });
    await user.click(screen.getByRole('button', { name: /Sign in with Authentik/i }));
    expect(window.location.href).toBe('/oauth2/authorization/authentik');
  });

  it('clears cached user and redirects to switch-user endpoint', async () => {
    const user = userEvent.setup();
    window.localStorage.setItem('dtime.lastOidcUser', 'Someone');
    mockedAxios.get.mockResolvedValue({ data: { enabled: true } });
    renderLoginAt();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Sign in as another user/i })).not.toBeDisabled();
    });
    await user.click(screen.getByRole('button', { name: /Sign in as another user/i }));
    expect(window.localStorage.getItem('dtime.lastOidcUser')).toBeNull();
    expect(window.location.href).toBe('/api/auth/oidc/switch-user');
  });

  it('shows OIDC failure message when error=oauth', async () => {
    mockedAxios.get.mockResolvedValue({ data: { enabled: true } });
    renderLoginAt('?error=oauth&reason=invalid_id_token');

    await waitFor(() => {
      expect(screen.getByText(/OIDC login failed \(invalid_id_token\)/i)).toBeInTheDocument();
    });
  });

  it('shows translated invalid credentials for non-oauth error param', async () => {
    mockedAxios.get.mockResolvedValue({ data: { enabled: true } });
    renderLoginAt('?error=true');

    await waitFor(() => {
      expect(screen.getByText('auth.login.errors.invalidCredentials')).toBeInTheDocument();
    });
  });

  it('shows logout message when logout query is present', async () => {
    mockedAxios.get.mockResolvedValue({ data: { enabled: true } });
    renderLoginAt('?logout=1');

    await waitFor(() => {
      expect(screen.getByText('auth.login.logoutMessage')).toBeInTheDocument();
    });
  });

  it('treats OIDC as disabled when status request fails', async () => {
    mockedAxios.get.mockRejectedValue(new Error('network'));
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
    renderLoginAt();

    await waitFor(() => {
      expect(screen.getByText(/OIDC login is not enabled/i)).toBeInTheDocument();
    });
    consoleSpy.mockRestore();
  });

  it('renders logo and title', async () => {
    renderLoginAt();
    expect(screen.getByAltText('D-Time')).toHaveAttribute('src', '/logo.png');
    await waitFor(() => {
      expect(screen.getByText('auth.login.title')).toBeInTheDocument();
    });
  });
});
