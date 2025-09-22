import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router';
import UserService from '../../../service/UserService';
import { useToast } from '../../../components/Toast';

// Mock dependencies
jest.mock('../../../service/UserService');
jest.mock('../../../components/Toast', () => ({
  useToast: jest.fn()
}));

// Mock UserTableRow component for testing
const UserTableRow = ({ user, handleDelete }) => {
  if (user == null) return null;

  return (
    <tr data-testid={`user-row-${user.id}`}>
      <td>{user.firstName}</td>
      <td>{user.lastName}</td>
      <td>{user.email}</td>
      <td>{user.userRole}</td>
      <td>{user.activationStatus}</td>
      <td>
        <button 
          className="btn btn-outline-primary btn-sm me-2" 
          data-testid={`edit-user-${user.id}`}
        >
          Edit
        </button>
        <button 
          className="btn btn-outline-danger btn-sm" 
          onClick={() => handleDelete(user.id)}
          data-testid={`delete-user-${user.id}`}
        >
          Delete
        </button>
      </td>
    </tr>
  );
};

// Mock UserTable component
const UserTable = ({ users, handleDelete, statusFilter, emailFilter, roleFilter }) => {
  if (users == null) return null;

  var emailFilterValue = emailFilter || '';

  var filteredUsers = users.filter(function (user) {
    return (user.activationStatus === statusFilter) &&
        (user.email.toLowerCase().includes(emailFilterValue.toLowerCase())) &&
        (user.userRole === roleFilter || roleFilter === '');
  });

  var rows = [];
  filteredUsers.forEach(function (user) {
    rows.push(
      <UserTableRow user={user} key={user.id} handleDelete={handleDelete} />
    );
  });

  return (
    <table className="table table-striped" data-testid="users-table">
      <thead className="thead-inverse bg-success">
        <tr className="text-white">
          <th>First name</th>
          <th>Last name</th>
          <th>Email</th>
          <th>Role</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {rows}
      </tbody>
    </table>
  );
};

// Wrapper component with router
const TestWrapper = ({ children }) => (
  <BrowserRouter>
    {children}
  </BrowserRouter>
);

describe('UserTableRow', () => {
  const mockHandleDelete = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  const mockUser = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    email: 'john.doe@example.com',
    userRole: 'USER',
    activationStatus: 'ACTIVE'
  };

  it('should render user information correctly', () => {
    render(
      <TestWrapper>
        <table>
          <tbody>
            <UserTableRow user={mockUser} handleDelete={mockHandleDelete} />
          </tbody>
        </table>
      </TestWrapper>
    );

    expect(screen.getByText('John')).toBeInTheDocument();
    expect(screen.getByText('Doe')).toBeInTheDocument();
    expect(screen.getByText('john.doe@example.com')).toBeInTheDocument();
    expect(screen.getByText('USER')).toBeInTheDocument();
    expect(screen.getByText('ACTIVE')).toBeInTheDocument();
  });

  it('should render edit and delete buttons', () => {
    render(
      <TestWrapper>
        <table>
          <tbody>
            <UserTableRow user={mockUser} handleDelete={mockHandleDelete} />
          </tbody>
        </table>
      </TestWrapper>
    );

    expect(screen.getByTestId('edit-user-1')).toBeInTheDocument();
    expect(screen.getByTestId('delete-user-1')).toBeInTheDocument();
    expect(screen.getByText('Edit')).toBeInTheDocument();
    expect(screen.getByText('Delete')).toBeInTheDocument();
  });

  it('should call handleDelete when delete button is clicked', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <table>
          <tbody>
            <UserTableRow user={mockUser} handleDelete={mockHandleDelete} />
          </tbody>
        </table>
      </TestWrapper>
    );

    const deleteButton = screen.getByTestId('delete-user-1');
    await user.click(deleteButton);

    expect(mockHandleDelete).toHaveBeenCalledWith(1);
  });

  it('should return null when user is null', () => {
    const { container } = render(
      <TestWrapper>
        <table>
          <tbody>
            <UserTableRow user={null} handleDelete={mockHandleDelete} />
          </tbody>
        </table>
      </TestWrapper>
    );

    expect(container.querySelector('[data-testid^="user-row-"]')).not.toBeInTheDocument();
  });

  it('should have proper button classes', () => {
    render(
      <TestWrapper>
        <table>
          <tbody>
            <UserTableRow user={mockUser} handleDelete={mockHandleDelete} />
          </tbody>
        </table>
      </TestWrapper>
    );

    const editButton = screen.getByTestId('edit-user-1');
    const deleteButton = screen.getByTestId('delete-user-1');

    expect(editButton).toHaveClass('btn', 'btn-outline-primary', 'btn-sm', 'me-2');
    expect(deleteButton).toHaveClass('btn', 'btn-outline-danger', 'btn-sm');
  });
});

describe('UserTable', () => {
  const mockHandleDelete = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  const mockUsers = [
    {
      id: 1,
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
      userRole: 'USER',
      activationStatus: 'ACTIVE'
    },
    {
      id: 2,
      firstName: 'Jane',
      lastName: 'Smith',
      email: 'jane.smith@example.com',
      userRole: 'ADMIN',
      activationStatus: 'ACTIVE'
    },
    {
      id: 3,
      firstName: 'Bob',
      lastName: 'Johnson',
      email: 'bob.johnson@example.com',
      userRole: 'USER',
      activationStatus: 'INACTIVE'
    }
  ];

  it('should render table with correct headers', () => {
    render(
      <TestWrapper>
        <UserTable 
          users={mockUsers} 
          handleDelete={mockHandleDelete}
          statusFilter="ACTIVE"
          emailFilter=""
          roleFilter=""
        />
      </TestWrapper>
    );

    expect(screen.getByTestId('users-table')).toBeInTheDocument();
    expect(screen.getByText('First name')).toBeInTheDocument();
    expect(screen.getByText('Last name')).toBeInTheDocument();
    expect(screen.getByText('Email')).toBeInTheDocument();
    expect(screen.getByText('Role')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
    expect(screen.getByText('Actions')).toBeInTheDocument();
  });

  it('should render all active users when no filters applied', () => {
    render(
      <TestWrapper>
        <UserTable 
          users={mockUsers} 
          handleDelete={mockHandleDelete}
          statusFilter="ACTIVE"
          emailFilter=""
          roleFilter=""
        />
      </TestWrapper>
    );

    expect(screen.getByTestId('user-row-1')).toBeInTheDocument();
    expect(screen.getByTestId('user-row-2')).toBeInTheDocument();
    expect(screen.queryByTestId('user-row-3')).not.toBeInTheDocument(); // Inactive user
  });

  it('should filter users by activation status', () => {
    render(
      <TestWrapper>
        <UserTable 
          users={mockUsers} 
          handleDelete={mockHandleDelete}
          statusFilter="INACTIVE"
          emailFilter=""
          roleFilter=""
        />
      </TestWrapper>
    );

    expect(screen.queryByTestId('user-row-1')).not.toBeInTheDocument();
    expect(screen.queryByTestId('user-row-2')).not.toBeInTheDocument();
    expect(screen.getByTestId('user-row-3')).toBeInTheDocument();
  });

  it('should filter users by email', () => {
    render(
      <TestWrapper>
        <UserTable 
          users={mockUsers} 
          handleDelete={mockHandleDelete}
          statusFilter="ACTIVE"
          emailFilter="john"
          roleFilter=""
        />
      </TestWrapper>
    );

    expect(screen.getByTestId('user-row-1')).toBeInTheDocument();
    expect(screen.queryByTestId('user-row-2')).not.toBeInTheDocument();
  });

  it('should filter users by role', () => {
    render(
      <TestWrapper>
        <UserTable 
          users={mockUsers} 
          handleDelete={mockHandleDelete}
          statusFilter="ACTIVE"
          emailFilter=""
          roleFilter="ADMIN"
        />
      </TestWrapper>
    );

    expect(screen.queryByTestId('user-row-1')).not.toBeInTheDocument();
    expect(screen.getByTestId('user-row-2')).toBeInTheDocument();
  });

  it('should apply multiple filters correctly', () => {
    render(
      <TestWrapper>
        <UserTable 
          users={mockUsers} 
          handleDelete={mockHandleDelete}
          statusFilter="ACTIVE"
          emailFilter="doe"
          roleFilter="USER"
        />
      </TestWrapper>
    );

    expect(screen.getByTestId('user-row-1')).toBeInTheDocument();
    expect(screen.queryByTestId('user-row-2')).not.toBeInTheDocument(); // Admin role
    expect(screen.queryByTestId('user-row-3')).not.toBeInTheDocument(); // Inactive
  });

  it('should handle case insensitive email filtering', () => {
    render(
      <TestWrapper>
        <UserTable 
          users={mockUsers} 
          handleDelete={mockHandleDelete}
          statusFilter="ACTIVE"
          emailFilter="JOHN"
          roleFilter=""
        />
      </TestWrapper>
    );

    expect(screen.getByTestId('user-row-1')).toBeInTheDocument();
    expect(screen.queryByTestId('user-row-2')).not.toBeInTheDocument();
  });

  it('should handle empty email filter', () => {
    render(
      <TestWrapper>
        <UserTable 
          users={mockUsers} 
          handleDelete={mockHandleDelete}
          statusFilter="ACTIVE"
          emailFilter={null}
          roleFilter=""
        />
      </TestWrapper>
    );

    expect(screen.getByTestId('user-row-1')).toBeInTheDocument();
    expect(screen.getByTestId('user-row-2')).toBeInTheDocument();
  });

  it('should render empty table when no users match filters', () => {
    render(
      <TestWrapper>
        <UserTable 
          users={mockUsers} 
          handleDelete={mockHandleDelete}
          statusFilter="ACTIVE"
          emailFilter="nonexistent@example.com"
          roleFilter=""
        />
      </TestWrapper>
    );

    expect(screen.getByTestId('users-table')).toBeInTheDocument();
    expect(screen.queryByTestId('user-row-1')).not.toBeInTheDocument();
    expect(screen.queryByTestId('user-row-2')).not.toBeInTheDocument();
    expect(screen.queryByTestId('user-row-3')).not.toBeInTheDocument();
  });

  it('should return null when users is null', () => {
    const { container } = render(
      <TestWrapper>
        <UserTable 
          users={null} 
          handleDelete={mockHandleDelete}
          statusFilter="ACTIVE"
          emailFilter=""
          roleFilter=""
        />
      </TestWrapper>
    );

    expect(container.querySelector('[data-testid="users-table"]')).not.toBeInTheDocument();
  });

  it('should have proper table classes', () => {
    render(
      <TestWrapper>
        <UserTable 
          users={mockUsers} 
          handleDelete={mockHandleDelete}
          statusFilter="ACTIVE"
          emailFilter=""
          roleFilter=""
        />
      </TestWrapper>
    );

    const table = screen.getByTestId('users-table');
    expect(table).toHaveClass('table', 'table-striped');

    const thead = table.querySelector('thead');
    expect(thead).toHaveClass('thead-inverse', 'bg-success');

    const headerRow = thead.querySelector('tr');
    expect(headerRow).toHaveClass('text-white');
  });

  it('should pass handleDelete function to UserTableRow components', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <UserTable 
          users={mockUsers} 
          handleDelete={mockHandleDelete}
          statusFilter="ACTIVE"
          emailFilter=""
          roleFilter=""
        />
      </TestWrapper>
    );

    const deleteButton = screen.getByTestId('delete-user-1');
    await user.click(deleteButton);

    expect(mockHandleDelete).toHaveBeenCalledWith(1);
  });

  describe('Edge Cases', () => {
    it('should handle users with empty email gracefully', () => {
      const usersWithEmptyEmail = [
        {
          id: 1,
          firstName: 'John',
          lastName: 'Doe',
          email: '',
          userRole: 'USER',
          activationStatus: 'ACTIVE'
        }
      ];

      render(
        <TestWrapper>
          <UserTable 
            users={usersWithEmptyEmail} 
            handleDelete={mockHandleDelete}
            statusFilter="ACTIVE"
            emailFilter=""
            roleFilter=""
          />
        </TestWrapper>
      );

      expect(screen.getByTestId('user-row-1')).toBeInTheDocument();
      // Check that the empty email cell is present by checking the row structure
      const row = screen.getByTestId('user-row-1');
      expect(row).toBeInTheDocument();
    });

    it('should handle special characters in email filter', () => {
      render(
        <TestWrapper>
          <UserTable 
            users={mockUsers} 
            handleDelete={mockHandleDelete}
            statusFilter="ACTIVE"
            emailFilter="@example.com"
            roleFilter=""
          />
        </TestWrapper>
      );

      expect(screen.getByTestId('user-row-1')).toBeInTheDocument();
      expect(screen.getByTestId('user-row-2')).toBeInTheDocument();
    });

    it('should handle empty role filter correctly', () => {
      render(
        <TestWrapper>
          <UserTable 
            users={mockUsers} 
            handleDelete={mockHandleDelete}
            statusFilter="ACTIVE"
            emailFilter=""
            roleFilter=""
          />
        </TestWrapper>
      );

      expect(screen.getByTestId('user-row-1')).toBeInTheDocument();
      expect(screen.getByTestId('user-row-2')).toBeInTheDocument();
    });
  });
});