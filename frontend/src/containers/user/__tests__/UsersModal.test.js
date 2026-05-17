import React from 'react';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import UsersModal from '../UsersModal';
import UserService from '../../../service/UserService';
import { renderWithProviders } from '../../../test-utils/renderWithProviders';

jest.mock('../../../service/UserService');
jest.mock('react-i18next', () => {
  const en = require('../../../locales/en.json');
  const resolve = (key) => {
    const value = key.split('.').reduce((obj, part) => obj?.[part], en);
    return typeof value === 'string' ? value : key;
  };
  return {
    useTranslation: () => ({
      t: (key, options) => {
        let text = resolve(key);
        if (options && typeof options === 'object') {
          Object.entries(options).forEach(([name, val]) => {
            text = text.replace(new RegExp(`{{${name}}}`, 'g'), String(val));
          });
        }
        return text;
      },
    }),
  };
});

describe('UsersModal', () => {
  let mockUserService;

  const mockUsersResponse = {
    data: {
      content: [
        {
          id: 1,
          firstName: 'John',
          lastName: 'Doe',
          email: 'john@example.com',
          userRole: 'USER',
          activationStatus: 'ACTIVE'
        },
        {
          id: 2,
          firstName: 'Jane',
          lastName: 'Smith',
          email: 'jane@example.com',
          userRole: 'ADMIN',
          activationStatus: 'INACTIVE'
        }
      ],
      currentPage: 0,
      totalPages: 1,
      totalElements: 2,
      pageSize: 10
    }
  };

  beforeEach(() => {
    mockUserService = {
      getAllPaged: jest.fn().mockResolvedValue(mockUsersResponse),
      deactivate: jest.fn().mockResolvedValue({}),
      activate: jest.fn().mockResolvedValue({})
    };
    UserService.mockImplementation(() => mockUserService);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('renders user list from API', async () => {
    renderWithProviders(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByText('John')).toBeInTheDocument();
      expect(screen.getByText('jane@example.com')).toBeInTheDocument();
    });
  });

  it('renders translated role labels instead of raw enum values', async () => {
    renderWithProviders(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByText('User')).toBeInTheDocument();
      expect(screen.getByText('Admin')).toBeInTheDocument();
    });
    expect(screen.queryByText('USER')).not.toBeInTheDocument();
    expect(screen.queryByText('ADMIN')).not.toBeInTheDocument();
  });

  it('does not show add user button', async () => {
    renderWithProviders(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByText('John')).toBeInTheDocument();
    });

    expect(screen.queryByRole('button', { name: /add user/i })).not.toBeInTheDocument();
  });

  it('shows deactivate button only for active users', async () => {
    renderWithProviders(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Deactivate' })).toBeInTheDocument();
    });

    expect(screen.getAllByRole('button', { name: 'Deactivate' })).toHaveLength(1);
  });

  it('deactivates user when confirmed', async () => {
    const user = userEvent.setup();
    renderWithProviders(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Deactivate' })).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Deactivate' }));
    const confirmButtons = screen.getAllByRole('button', { name: 'Deactivate' });
    await user.click(confirmButtons[confirmButtons.length - 1]);

    await waitFor(() => {
      expect(mockUserService.deactivate).toHaveBeenCalledWith(1);
    });
  });

  it('does not deactivate when confirmation is cancelled', async () => {
    const user = userEvent.setup();
    renderWithProviders(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Deactivate' })).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Deactivate' }));
    await user.click(screen.getByRole('button', { name: 'Cancel' }));

    expect(mockUserService.deactivate).not.toHaveBeenCalled();
  });

  it('shows activate button only for inactive users', async () => {
    renderWithProviders(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Activate' })).toBeInTheDocument();
    });

    expect(screen.getAllByRole('button', { name: 'Activate' })).toHaveLength(1);
  });

  it('activates user when confirmed', async () => {
    const user = userEvent.setup();
    renderWithProviders(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Activate' })).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Activate' }));
    const confirmButtons = screen.getAllByRole('button', { name: 'Activate' });
    await user.click(confirmButtons[confirmButtons.length - 1]);

    await waitFor(() => {
      expect(mockUserService.activate).toHaveBeenCalledWith(2);
    });
  });
});
