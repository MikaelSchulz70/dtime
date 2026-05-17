import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import UsersModal from '../UsersModal';
import UserService from '../../../service/UserService';

jest.mock('../../../service/UserService');
jest.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key, defaultValue) => defaultValue || key
  })
}));

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
    window.confirm = jest.fn(() => true);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('renders user list from API', async () => {
    render(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByText('John')).toBeInTheDocument();
      expect(screen.getByText('jane@example.com')).toBeInTheDocument();
    });
  });

  it('does not show add user button', async () => {
    render(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByText('John')).toBeInTheDocument();
    });

    expect(screen.queryByRole('button', { name: /add user/i })).not.toBeInTheDocument();
  });

  it('shows deactivate button only for active users', async () => {
    render(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Deactivate' })).toBeInTheDocument();
    });

    expect(screen.getAllByRole('button', { name: 'Deactivate' })).toHaveLength(1);
  });

  it('deactivates user when confirmed', async () => {
    const user = userEvent.setup();
    render(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Deactivate' })).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Deactivate' }));

    await waitFor(() => {
      expect(mockUserService.deactivate).toHaveBeenCalledWith(1);
    });
  });

  it('does not deactivate when confirmation is cancelled', async () => {
    window.confirm.mockReturnValue(false);
    const user = userEvent.setup();
    render(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Deactivate' })).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Deactivate' }));

    expect(mockUserService.deactivate).not.toHaveBeenCalled();
  });

  it('shows activate button only for inactive users', async () => {
    render(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Activate' })).toBeInTheDocument();
    });

    expect(screen.getAllByRole('button', { name: 'Activate' })).toHaveLength(1);
  });

  it('activates user when confirmed', async () => {
    const user = userEvent.setup();
    render(<UsersModal />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Activate' })).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Activate' }));

    await waitFor(() => {
      expect(mockUserService.activate).toHaveBeenCalledWith(2);
    });
  });
});
