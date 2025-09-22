import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { BrowserRouter } from 'react-router';
import NavigationMenu from '../Menu';

// Mock the logo import
jest.mock('../../../assets/logo_white.png', () => 'test-logo.png');

const renderWithRouter = (component) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('NavigationMenu', () => {
  const mockUserSession = {
    loggedInUser: {
      name: 'John Doe',
      admin: false
    },
    currentDate: {
      date: '2023-01-15'
    }
  };

  const mockAdminSession = {
    loggedInUser: {
      name: 'Admin User',
      admin: true
    },
    currentDate: {
      date: '2023-01-15'
    }
  };

  const mockSessionWithoutDate = {
    loggedInUser: {
      name: 'John Doe',
      admin: false
    }
  };

  beforeEach(() => {
    // Mock Date.prototype.toLocaleDateString
    Date.prototype.toLocaleDateString = jest.fn(() => '1/15/2023');
  });

  describe('Session validation', () => {
    it('should return null when no session is provided', () => {
      const { container } = renderWithRouter(<NavigationMenu session={null} />);
      expect(container.firstChild).toBeNull();
    });

    it('should return null when session has no loggedInUser', () => {
      const sessionWithoutUser = { someOtherProperty: 'value' };
      const { container } = renderWithRouter(<NavigationMenu session={sessionWithoutUser} />);
      expect(container.firstChild).toBeNull();
    });

    it('should return null when loggedInUser is null', () => {
      const sessionWithNullUser = { loggedInUser: null };
      const { container } = renderWithRouter(<NavigationMenu session={sessionWithNullUser} />);
      expect(container.firstChild).toBeNull();
    });
  });

  describe('Basic rendering', () => {
    it('should render the navbar when session is valid', () => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      expect(screen.getByRole('navigation')).toBeInTheDocument();
      expect(screen.getByText('D-Time')).toBeInTheDocument();
    });

    it('should display the logo with correct attributes', () => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      const logo = screen.getByRole('img', { name: /d-time/i });
      expect(logo).toBeInTheDocument();
      expect(logo).toHaveAttribute('src', 'test-logo.png');
      expect(logo).toHaveAttribute('alt', 'D-Time');
    });

    it('should display user name in navbar text', () => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      expect(screen.getByText('John Doe')).toBeInTheDocument();
    });

    it('should display current date when provided', () => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      expect(screen.getByText('2023-01-15')).toBeInTheDocument();
    });

    it('should display default date when currentDate is not provided', () => {
      renderWithRouter(<NavigationMenu session={mockSessionWithoutDate} />);
      
      expect(screen.getByText('1/15/2023')).toBeInTheDocument();
    });
  });

  describe('User (non-admin) navigation', () => {
    beforeEach(() => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
    });

    it('should render basic navigation links for non-admin users', () => {
      expect(screen.getByRole('link', { name: 'Time' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /admin/i })).toBeInTheDocument(); // Dropdown toggle
      expect(screen.getByRole('link', { name: 'Logout' })).toBeInTheDocument();
    });

    it('should have correct routing for Time link', () => {
      const timeLink = screen.getByRole('link', { name: 'Time' });
      expect(timeLink).toHaveAttribute('href', '/time');
    });

    it('should have correct routing for Logout link', () => {
      const logoutLink = screen.getByRole('link', { name: 'Logout' });
      expect(logoutLink).toHaveAttribute('href', '/logout');
    });

    it('should show limited admin dropdown options for non-admin users', () => {
      // Non-admin users should see only Report and Change password in the dropdown
      // But we need to click/expand the dropdown first to see these items
      const adminDropdown = screen.getByRole('button', { name: /admin/i });
      fireEvent.click(adminDropdown);
      
      expect(screen.getByRole('link', { name: 'Report' })).toBeInTheDocument();
      expect(screen.getByRole('link', { name: 'Change password' })).toBeInTheDocument();
    });

    it('should have correct routing for Report link', () => {
      const adminDropdown = screen.getByRole('button', { name: /admin/i });
      fireEvent.click(adminDropdown);
      
      const reportLink = screen.getByRole('link', { name: 'Report' });
      expect(reportLink).toHaveAttribute('href', '/userreport');
    });

    it('should have correct routing for Change password link', () => {
      const adminDropdown = screen.getByRole('button', { name: /admin/i });
      fireEvent.click(adminDropdown);
      
      const changePwdLink = screen.getByRole('link', { name: 'Change password' });
      expect(changePwdLink).toHaveAttribute('href', '/changepwd');
    });

    it('should not show admin-only navigation links for non-admin users', () => {
      // These should not be visible for non-admin users
      expect(screen.queryByText('User')).not.toBeInTheDocument();
      expect(screen.queryByText('Account')).not.toBeInTheDocument();
      expect(screen.queryByText('Task')).not.toBeInTheDocument();
      expect(screen.queryByText('System properties')).not.toBeInTheDocument();
    });
  });

  describe('Admin navigation', () => {
    beforeEach(() => {
      renderWithRouter(<NavigationMenu session={mockAdminSession} />);
    });

    it('should render all navigation links for admin users', () => {
      expect(screen.getByRole('link', { name: 'Time' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /admin/i })).toBeInTheDocument();
      expect(screen.getByRole('link', { name: 'Logout' })).toBeInTheDocument();
    });

    it('should show admin-specific dropdown options', () => {
      const adminDropdown = screen.getByRole('button', { name: /admin/i });
      fireEvent.click(adminDropdown);
      
      expect(screen.getByText('User')).toBeInTheDocument();
      expect(screen.getByText('Account')).toBeInTheDocument();
      expect(screen.getByText('Task')).toBeInTheDocument();
      expect(screen.getByText('Task contributor')).toBeInTheDocument();
      expect(screen.getByText('Reports')).toBeInTheDocument();
      expect(screen.getByText('Vacations')).toBeInTheDocument();
      expect(screen.getByText('Time Report Status')).toBeInTheDocument();
      expect(screen.getByText('System properties')).toBeInTheDocument();
      expect(screen.getByText('Special Days')).toBeInTheDocument();
    });

    it('should have correct routing for admin links', () => {
      const adminDropdown = screen.getByRole('button', { name: /admin/i });
      fireEvent.click(adminDropdown);
      
      expect(screen.getByRole('link', { name: 'User' })).toHaveAttribute('href', '/users');
      expect(screen.getByText('Account').closest('a')).toHaveAttribute('href', '/account');
      expect(screen.getByText('Task').closest('a')).toHaveAttribute('href', '/task');
      expect(screen.getByText('Task contributor').closest('a')).toHaveAttribute('href', '/taskcontributor');
      expect(screen.getByText('Reports').closest('a')).toHaveAttribute('href', '/reports');
      expect(screen.getByText('Vacations').closest('a')).toHaveAttribute('href', '/vacations');
      expect(screen.getByText('Time Report Status').closest('a')).toHaveAttribute('href', '/timereportstatus');
      expect(screen.getByText('System properties').closest('a')).toHaveAttribute('href', '/system/properties');
      expect(screen.getByText('Special Days').closest('a')).toHaveAttribute('href', '/specialdays');
    });

    it('should display admin user name', () => {
      expect(screen.getByText('Admin User')).toBeInTheDocument();
    });

    it('should have correct routing for Time link in admin menu', () => {
      const timeLink = screen.getByRole('link', { name: 'Time' });
      expect(timeLink).toHaveAttribute('href', '/time');
    });

    it('should have correct routing for Logout link in admin menu', () => {
      const logoutLink = screen.getByRole('link', { name: 'Logout' });
      expect(logoutLink).toHaveAttribute('href', '/logout');
    });

    it('should show Change password option for admin users', () => {
      const adminDropdown = screen.getByRole('button', { name: /admin/i });
      fireEvent.click(adminDropdown);
      
      expect(screen.getByText('Change password')).toBeInTheDocument();
      expect(screen.getByText('Change password').closest('a')).toHaveAttribute('href', '/changepwd');
    });
  });

  describe('CSS classes and styling', () => {
    it('should apply correct CSS classes to navbar', () => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      const navbar = screen.getByRole('navigation');
      expect(navbar).toHaveClass('navbar-professional');
      expect(navbar).toHaveClass('bg-success');
      expect(navbar).toHaveClass('navbar-dark');
    });

    it('should apply correct CSS classes to navigation links', () => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      const timeLink = screen.getByRole('link', { name: 'Time' });
      expect(timeLink).toHaveClass('nav-link-professional');
    });

    it('should apply correct CSS classes to dropdown', () => {
      renderWithRouter(<NavigationMenu session={mockAdminSession} />);
      
      const dropdownContainer = document.querySelector('.nav-dropdown-professional');
      expect(dropdownContainer).toBeInTheDocument();
      
      const dropdown = screen.getByRole('button', { name: /admin/i });
      expect(dropdown).toHaveClass('dropdown-toggle');
    });

    it('should apply correct CSS classes to dropdown items', () => {
      renderWithRouter(<NavigationMenu session={mockAdminSession} />);
      
      const adminDropdown = screen.getByRole('button', { name: /admin/i });
      fireEvent.click(adminDropdown);
      
      const userLink = screen.getByText('User');
      expect(userLink).toHaveClass('text-dark');
      expect(userLink).toHaveClass('dropdown-item-professional');
    });
  });

  describe('Bootstrap components', () => {
    it('should render Bootstrap Navbar components', () => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      expect(screen.getByRole('navigation')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /toggle navigation/i })).toBeInTheDocument();
    });

    it('should have collapsible navigation', () => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      const collapse = document.getElementById('basic-navbar-nav');
      expect(collapse).toBeInTheDocument();
      expect(collapse).toHaveClass('navbar-collapse');
    });

    it('should render NavDropdown with correct structure', () => {
      renderWithRouter(<NavigationMenu session={mockAdminSession} />);
      
      const adminDropdown = screen.getByRole('button', { name: /admin/i });
      expect(adminDropdown).toBeInTheDocument();
      expect(adminDropdown).toHaveAttribute('id', 'basic-nav-dropdown');
    });
  });

  describe('Edge cases and error handling', () => {
    it('should handle session with undefined loggedInUser properties', () => {
      const incompleteSession = {
        loggedInUser: {
          // Missing name and admin properties
        },
        currentDate: {
          date: '2023-01-15'
        }
      };
      
      renderWithRouter(<NavigationMenu session={incompleteSession} />);
      
      expect(screen.getByRole('navigation')).toBeInTheDocument();
      // Should handle missing name gracefully - check for user info section
      const userSection = document.querySelector('.navbar-text');
      expect(userSection).toBeInTheDocument();
    });

    it('should handle session with null currentDate', () => {
      const sessionWithNullDate = {
        loggedInUser: {
          name: 'John Doe',
          admin: false
        },
        currentDate: null
      };
      
      renderWithRouter(<NavigationMenu session={sessionWithNullDate} />);
      
      expect(screen.getByText('1/15/2023')).toBeInTheDocument(); // Should fall back to default
    });

    it('should handle admin property as falsy values', () => {
      const sessionWithFalsyAdmin = {
        loggedInUser: {
          name: 'John Doe',
          admin: 0 // Falsy but not false
        }
      };
      
      renderWithRouter(<NavigationMenu session={sessionWithFalsyAdmin} />);
      
      // Should render user menu (not admin menu)
      expect(screen.queryByText('User')).not.toBeInTheDocument();
      
      // Need to click dropdown to see Report option
      const adminDropdown = screen.getByRole('button', { name: /admin/i });
      fireEvent.click(adminDropdown);
      expect(screen.getByText('Report')).toBeInTheDocument();
    });

    it('should handle admin property as truthy values', () => {
      const sessionWithTruthyAdmin = {
        loggedInUser: {
          name: 'Admin User',
          admin: 'true' // Truthy but not boolean true
        }
      };
      
      renderWithRouter(<NavigationMenu session={sessionWithTruthyAdmin} />);
      
      // Should render admin menu - need to click dropdown to see items
      const adminDropdown = screen.getByRole('button', { name: /admin/i });
      fireEvent.click(adminDropdown);
      
      expect(screen.getByText('User')).toBeInTheDocument();
      expect(screen.getByText('Account')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA attributes', () => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      const toggleButton = screen.getByRole('button', { name: /toggle navigation/i });
      expect(toggleButton).toHaveAttribute('aria-controls', 'basic-navbar-nav');
    });

    it('should have proper alt text for logo', () => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      const logo = screen.getByRole('img');
      expect(logo).toHaveAttribute('alt', 'D-Time');
    });

    it('should have proper role attributes', () => {
      renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      expect(screen.getByRole('navigation')).toBeInTheDocument();
      expect(screen.getByRole('img')).toBeInTheDocument();
    });
  });

  describe('Link navigation', () => {
    it('should render all links as React Router Links', () => {
      renderWithRouter(<NavigationMenu session={mockAdminSession} />);
      
      // All navigation links should be rendered as <a> tags (Link components render as anchors)
      const links = screen.getAllByRole('link');
      expect(links.length).toBeGreaterThan(0);
      
      // Check that each link has an href attribute (indicating proper Link usage)
      links.forEach(link => {
        expect(link).toHaveAttribute('href');
      });
    });

    it('should have correct href values for all admin links', () => {
      renderWithRouter(<NavigationMenu session={mockAdminSession} />);
      
      const expectedLinks = [
        { text: 'Time', href: '/time' },
        { text: 'Logout', href: '/logout' }
      ];
      
      expectedLinks.forEach(({ text, href }) => {
        const link = screen.getByRole('link', { name: text });
        expect(link).toHaveAttribute('href', href);
      });
    });
  });

  describe('Menu structure differences', () => {
    it('should render different menu structures for admin vs non-admin', () => {
      const { rerender } = renderWithRouter(<NavigationMenu session={mockUserSession} />);
      
      // Non-admin should have limited options
      expect(screen.queryByText('User')).not.toBeInTheDocument();
      
      // Check dropdown for non-admin user
      let adminDropdown = screen.getByRole('button', { name: /admin/i });
      fireEvent.click(adminDropdown);
      expect(screen.getByText('Report')).toBeInTheDocument();
      
      // Rerender with admin session
      rerender(<BrowserRouter><NavigationMenu session={mockAdminSession} /></BrowserRouter>);
      
      // Admin should have all options - check dropdown for admin user
      adminDropdown = screen.getByRole('button', { name: /admin/i });
      fireEvent.click(adminDropdown);
      expect(screen.getByText('User')).toBeInTheDocument();
      expect(screen.getByText('Reports')).toBeInTheDocument();
    });
  });
});