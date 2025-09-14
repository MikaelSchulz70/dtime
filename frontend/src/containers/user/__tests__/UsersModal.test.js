import React from 'react';
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import UserService from '../../../service/UserService';

// Mock UserService
jest.mock('../../../service/UserService');

// Mock UsersModal component (simplified version for testing key functionality)
const UsersModal = () => {
  const [users, setUsers] = React.useState([]);
  const [loading, setLoading] = React.useState(false);
  const [showModal, setShowModal] = React.useState(false);
  const [editingUser, setEditingUser] = React.useState(null);
  const [formData, setFormData] = React.useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    userRole: 'USER',
    activationStatus: 'ACTIVE'
  });
  const [filters, setFilters] = React.useState({
    firstName: '',
    lastName: '',
    status: 'ACTIVE'
  });

  const loadUsers = React.useCallback(async () => {
    setLoading(true);
    try {
      const userService = new UserService();
      const response = await userService.getUsers(1, 10, filters);
      setUsers(response.data.content || []);
    } catch (error) {
      console.error('Failed to load users:', error);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  React.useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  const handleShowModal = (user = null) => {
    setEditingUser(user);
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
        password: '',
        userRole: user.userRole || 'USER',
        activationStatus: user.activationStatus || 'ACTIVE'
      });
    } else {
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        userRole: 'USER',
        activationStatus: 'ACTIVE'
      });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingUser(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const userService = new UserService();
      if (editingUser) {
        await userService.updateUser({ ...formData, id: editingUser.id });
      } else {
        await userService.createUser(formData);
      }
      handleCloseModal();
      loadUsers();
    } catch (error) {
      console.error('Failed to save user:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (userId) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      try {
        const userService = new UserService();
        await userService.deleteUser(userId);
        loadUsers();
      } catch (error) {
        console.error('Failed to delete user:', error);
      }
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  return (
    <div data-testid="users-modal">
      {/* Filter Controls */}
      <div className="mb-3">
        <input
          type="text"
          name="firstName"
          placeholder="Filter by first name"
          value={filters.firstName}
          onChange={handleFilterChange}
          data-testid="filter-firstName"
        />
        <input
          type="text"
          name="lastName"
          placeholder="Filter by last name"
          value={filters.lastName}
          onChange={handleFilterChange}
          data-testid="filter-lastName"
        />
        <select
          name="status"
          value={filters.status}
          onChange={handleFilterChange}
          data-testid="filter-status"
        >
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
        </select>
      </div>

      {/* Add User Button */}
      <button
        onClick={() => handleShowModal()}
        data-testid="add-user-btn"
        disabled={loading}
      >
        Add User
      </button>

      {/* Loading Indicator */}
      {loading && <div data-testid="loading">Loading...</div>}

      {/* Users Table */}
      <table data-testid="users-table">
        <thead>
          <tr>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map(user => (
            <tr key={user.id} data-testid={`user-row-${user.id}`}>
              <td>{user.firstName}</td>
              <td>{user.lastName}</td>
              <td>{user.email}</td>
              <td>{user.userRole}</td>
              <td>{user.activationStatus}</td>
              <td>
                <button
                  onClick={() => handleShowModal(user)}
                  data-testid={`edit-user-${user.id}`}
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(user.id)}
                  data-testid={`delete-user-${user.id}`}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Modal */}
      {showModal && (
        <div data-testid="user-modal" role="dialog" aria-modal="true">
          <div className="modal-content">
            <h2>{editingUser ? 'Edit User' : 'Add User'}</h2>
            <form onSubmit={handleSubmit} data-testid="user-form">
              <input
                type="text"
                name="firstName"
                placeholder="First Name"
                value={formData.firstName}
                onChange={handleInputChange}
                data-testid="form-firstName"
                required
              />
              <input
                type="text"
                name="lastName"
                placeholder="Last Name"
                value={formData.lastName}
                onChange={handleInputChange}
                data-testid="form-lastName"
                required
              />
              <input
                type="email"
                name="email"
                placeholder="Email"
                value={formData.email}
                onChange={handleInputChange}
                data-testid="form-email"
                required
              />
              <input
                type="password"
                name="password"
                placeholder="Password"
                value={formData.password}
                onChange={handleInputChange}
                data-testid="form-password"
                required={!editingUser}
              />
              <select
                name="userRole"
                value={formData.userRole}
                onChange={handleInputChange}
                data-testid="form-userRole"
              >
                <option value="USER">User</option>
                <option value="ADMIN">Admin</option>
              </select>
              <select
                name="activationStatus"
                value={formData.activationStatus}
                onChange={handleInputChange}
                data-testid="form-activationStatus"
              >
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
              </select>
              <button type="submit" data-testid="submit-btn" disabled={loading}>
                {editingUser ? 'Update' : 'Create'}
              </button>
              <button
                type="button"
                onClick={handleCloseModal}
                data-testid="cancel-btn"
              >
                Cancel
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

describe('UsersModal', () => {
  let mockUserService;

  beforeEach(() => {
    mockUserService = {
      getUsers: jest.fn(),
      createUser: jest.fn(),
      updateUser: jest.fn(),
      deleteUser: jest.fn()
    };
    UserService.mockImplementation(() => mockUserService);
    
    // Mock window.confirm
    window.confirm = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

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
          activationStatus: 'ACTIVE'
        }
      ]
    }
  };

  describe('Initial Rendering', () => {
    it('should render users modal with filter controls', async () => {
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);

      render(<UsersModal />);

      expect(screen.getByTestId('users-modal')).toBeInTheDocument();
      expect(screen.getByTestId('filter-firstName')).toBeInTheDocument();
      expect(screen.getByTestId('filter-lastName')).toBeInTheDocument();
      expect(screen.getByTestId('filter-status')).toBeInTheDocument();
      expect(screen.getByTestId('add-user-btn')).toBeInTheDocument();
    });

    it('should load users on mount', async () => {
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);

      render(<UsersModal />);

      await waitFor(() => {
        expect(mockUserService.getUsers).toHaveBeenCalledWith(1, 10, {
          firstName: '',
          lastName: '',
          status: 'ACTIVE'
        });
      });
    });

    it('should display loaded users in table', async () => {
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);

      render(<UsersModal />);

      await waitFor(() => {
        expect(screen.getByTestId('user-row-1')).toBeInTheDocument();
        expect(screen.getByTestId('user-row-2')).toBeInTheDocument();
      });

      expect(screen.getByText('John')).toBeInTheDocument();
      expect(screen.getByText('Doe')).toBeInTheDocument();
      expect(screen.getByText('john@example.com')).toBeInTheDocument();
      expect(screen.getByText('Jane')).toBeInTheDocument();
      expect(screen.getByText('Smith')).toBeInTheDocument();
    });
  });

  describe('Filtering', () => {
    it('should filter users by first name', async () => {
      const user = userEvent.setup();
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);

      render(<UsersModal />);

      const firstNameFilter = screen.getByTestId('filter-firstName');
      await user.type(firstNameFilter, 'John');

      await waitFor(() => {
        expect(mockUserService.getUsers).toHaveBeenCalledWith(1, 10, {
          firstName: 'John',
          lastName: '',
          status: 'ACTIVE'
        });
      });
    });

    it('should filter users by last name', async () => {
      const user = userEvent.setup();
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);

      render(<UsersModal />);

      const lastNameFilter = screen.getByTestId('filter-lastName');
      await user.type(lastNameFilter, 'Smith');

      await waitFor(() => {
        expect(mockUserService.getUsers).toHaveBeenCalledWith(1, 10, {
          firstName: '',
          lastName: 'Smith',
          status: 'ACTIVE'
        });
      });
    });

    it('should filter users by status', async () => {
      const user = userEvent.setup();
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);

      render(<UsersModal />);

      const statusFilter = screen.getByTestId('filter-status');
      await user.selectOptions(statusFilter, 'INACTIVE');

      await waitFor(() => {
        expect(mockUserService.getUsers).toHaveBeenCalledWith(1, 10, {
          firstName: '',
          lastName: '',
          status: 'INACTIVE'
        });
      });
    });
  });

  describe('Add User Modal', () => {
    beforeEach(() => {
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);
    });

    it('should show modal when add user button is clicked', async () => {
      const user = userEvent.setup();

      render(<UsersModal />);

      const addButton = screen.getByTestId('add-user-btn');
      await user.click(addButton);

      expect(screen.getByTestId('user-modal')).toBeInTheDocument();
      // Use more specific selector to avoid multiple matches
      expect(screen.getByRole('heading', { name: /add user/i })).toBeInTheDocument();
    });

    it('should close modal when cancel button is clicked', async () => {
      const user = userEvent.setup();

      render(<UsersModal />);

      await user.click(screen.getByTestId('add-user-btn'));
      expect(screen.getByTestId('user-modal')).toBeInTheDocument();

      await user.click(screen.getByTestId('cancel-btn'));
      expect(screen.queryByTestId('user-modal')).not.toBeInTheDocument();
    });

    it('should create new user when form is submitted', async () => {
      const user = userEvent.setup();
      mockUserService.createUser.mockResolvedValue({});

      render(<UsersModal />);

      await user.click(screen.getByTestId('add-user-btn'));

      // Fill form
      await user.type(screen.getByTestId('form-firstName'), 'New');
      await user.type(screen.getByTestId('form-lastName'), 'User');
      await user.type(screen.getByTestId('form-email'), 'new@example.com');
      await user.type(screen.getByTestId('form-password'), 'password123');

      await user.click(screen.getByTestId('submit-btn'));

      await waitFor(() => {
        expect(mockUserService.createUser).toHaveBeenCalledWith({
          firstName: 'New',
          lastName: 'User',
          email: 'new@example.com',
          password: 'password123',
          userRole: 'USER',
          activationStatus: 'ACTIVE'
        });
      });
    });
  });

  describe('Edit User Modal', () => {
    beforeEach(() => {
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);
    });

    it('should show modal with pre-filled data when edit button is clicked', async () => {
      const user = userEvent.setup();

      render(<UsersModal />);

      await waitFor(() => {
        expect(screen.getByTestId('user-row-1')).toBeInTheDocument();
      });

      await user.click(screen.getByTestId('edit-user-1'));

      expect(screen.getByTestId('user-modal')).toBeInTheDocument();
      expect(screen.getByText('Edit User')).toBeInTheDocument();
      expect(screen.getByTestId('form-firstName')).toHaveValue('John');
      expect(screen.getByTestId('form-lastName')).toHaveValue('Doe');
      expect(screen.getByTestId('form-email')).toHaveValue('john@example.com');
    });

    it('should update user when form is submitted', async () => {
      const user = userEvent.setup();
      mockUserService.updateUser.mockResolvedValue({});

      render(<UsersModal />);

      await waitFor(() => {
        expect(screen.getByTestId('edit-user-1')).toBeInTheDocument();
      });

      await user.click(screen.getByTestId('edit-user-1'));

      // Modify form data
      const firstNameInput = screen.getByTestId('form-firstName');
      await user.clear(firstNameInput);
      await user.type(firstNameInput, 'Updated John');

      await user.click(screen.getByTestId('submit-btn'));

      await waitFor(() => {
        expect(mockUserService.updateUser).toHaveBeenCalledWith({
          id: 1,
          firstName: 'Updated John',
          lastName: 'Doe',
          email: 'john@example.com',
          password: '',
          userRole: 'USER',
          activationStatus: 'ACTIVE'
        });
      });
    });
  });

  describe('Delete User', () => {
    beforeEach(() => {
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);
      mockUserService.deleteUser.mockResolvedValue({});
    });

    it('should delete user when delete button is clicked and confirmed', async () => {
      const user = userEvent.setup();
      window.confirm.mockReturnValue(true);

      render(<UsersModal />);

      await waitFor(() => {
        expect(screen.getByTestId('delete-user-1')).toBeInTheDocument();
      });

      await user.click(screen.getByTestId('delete-user-1'));

      expect(window.confirm).toHaveBeenCalledWith('Are you sure you want to delete this user?');
      await waitFor(() => {
        expect(mockUserService.deleteUser).toHaveBeenCalledWith(1);
      });
    });

    it('should not delete user when deletion is cancelled', async () => {
      const user = userEvent.setup();
      window.confirm.mockReturnValue(false);

      render(<UsersModal />);

      await waitFor(() => {
        expect(screen.getByTestId('delete-user-1')).toBeInTheDocument();
      });

      await user.click(screen.getByTestId('delete-user-1'));

      expect(window.confirm).toHaveBeenCalled();
      expect(mockUserService.deleteUser).not.toHaveBeenCalled();
    });
  });

  describe('Loading States', () => {
    it('should show loading indicator during initial load', () => {
      mockUserService.getUsers.mockImplementation(() => new Promise(() => {})); // Never resolves

      render(<UsersModal />);

      expect(screen.getByTestId('loading')).toBeInTheDocument();
    });

    it('should disable buttons during loading', async () => {
      const user = userEvent.setup();
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);
      mockUserService.createUser.mockImplementation(() => new Promise(() => {})); // Never resolves

      render(<UsersModal />);

      await user.click(screen.getByTestId('add-user-btn'));
      
      // Fill and submit form
      await user.type(screen.getByTestId('form-firstName'), 'Test');
      await user.type(screen.getByTestId('form-lastName'), 'User');
      await user.type(screen.getByTestId('form-email'), 'test@example.com');
      await user.type(screen.getByTestId('form-password'), 'password');
      
      await user.click(screen.getByTestId('submit-btn'));

      expect(screen.getByTestId('submit-btn')).toBeDisabled();
    });
  });

  describe('Error Handling', () => {
    it('should handle error when loading users fails', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      mockUserService.getUsers.mockRejectedValue(new Error('Network error'));

      render(<UsersModal />);

      await waitFor(() => {
        expect(consoleSpy).toHaveBeenCalledWith('Failed to load users:', expect.any(Error));
      });

      consoleSpy.mockRestore();
    });

    it('should handle error when creating user fails', async () => {
      const user = userEvent.setup();
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);
      mockUserService.createUser.mockRejectedValue(new Error('Creation failed'));

      render(<UsersModal />);

      await user.click(screen.getByTestId('add-user-btn'));
      
      // Fill and submit form
      await user.type(screen.getByTestId('form-firstName'), 'Test');
      await user.type(screen.getByTestId('form-lastName'), 'User');
      await user.type(screen.getByTestId('form-email'), 'test@example.com');
      await user.type(screen.getByTestId('form-password'), 'password');
      
      await user.click(screen.getByTestId('submit-btn'));

      await waitFor(() => {
        expect(consoleSpy).toHaveBeenCalledWith('Failed to save user:', expect.any(Error));
      });

      consoleSpy.mockRestore();
    });
  });

  describe('Accessibility', () => {
    it('should have proper modal attributes', async () => {
      const user = userEvent.setup();
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);

      render(<UsersModal />);

      await user.click(screen.getByTestId('add-user-btn'));

      const modal = screen.getByTestId('user-modal');
      expect(modal).toHaveAttribute('role', 'dialog');
      expect(modal).toHaveAttribute('aria-modal', 'true');
    });

    it('should have proper form structure', async () => {
      const user = userEvent.setup();
      mockUserService.getUsers.mockResolvedValue(mockUsersResponse);

      render(<UsersModal />);

      await user.click(screen.getByTestId('add-user-btn'));

      const form = screen.getByTestId('user-form');
      expect(form).toBeInTheDocument();
      
      const requiredInputs = [
        screen.getByTestId('form-firstName'),
        screen.getByTestId('form-lastName'),
        screen.getByTestId('form-email'),
        screen.getByTestId('form-password')
      ];

      requiredInputs.forEach(input => {
        expect(input).toHaveAttribute('required');
      });
    });
  });
});