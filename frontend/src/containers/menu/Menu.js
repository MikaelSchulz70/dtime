import React from "react";
import { Navbar, Nav, NavDropdown } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import logo from "../../assets/logo_white.png";

const NavigationMenu = ({ session }) => {

  if (!session || !session.loggedInUser) {
    return null;
  }

  const renderUserMenu = () => {
    return (
      <Navbar bg="success" variant="dark" expand="lg" className="navbar-professional">
        <Navbar.Brand href="/" className="d-flex align-items-center">
          <img
            src={logo}
            alt="D-Time"
            style={{ height: "40px", marginRight: "10px" }}
          />
          <span style={{ fontWeight: "600", fontSize: "1.25rem" }}>D-Time</span>
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="mr-auto">
            <Nav.Link as={Link} to="/time" eventKey={1} className="nav-link-professional">Time</Nav.Link>
            <NavDropdown title="Admin" id="basic-nav-dropdown" className="nav-dropdown-professional">
              <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/userreport" eventKey={2}>Report</Nav.Link>
              <NavDropdown.Divider />
              <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/changepwd" eventKey={3}>Change password</Nav.Link>
            </NavDropdown>
            <Nav.Link as={Link} to="/logout" eventKey={15} className="nav-link-professional">Logout</Nav.Link>
          </Nav>
        </Navbar.Collapse>
        <Navbar.Text className="justify-content-end mr-3 text-light">
          <span style={{ paddingRight: "15px", fontWeight: "500" }}>{session.loggedInUser.name}</span>
          <span style={{ opacity: "0.9" }}>{session.currentDate ? session.currentDate.date : new Date().toLocaleDateString()}</span>
        </Navbar.Text>
      </Navbar>
    );
  };

  // Check if user is admin based on isAdmin flag
  const isAdmin = session.loggedInUser.admin;

  // Render user menu for non-admin users
  if (!isAdmin) {
    return renderUserMenu();
  }

  // Render admin menu
  return (
    <Navbar bg="success" variant="dark" expand="lg" className="navbar-professional">
      <Navbar.Brand href="/" className="d-flex align-items-center">
        <img
          src={logo}
          alt="D-Time"
          style={{ height: "40px", marginRight: "10px" }}
        />
        <span style={{ fontWeight: "600", fontSize: "1.25rem" }}>D-Time</span>
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="basic-navbar-nav" />
      <Navbar.Collapse id="basic-navbar-nav">
        <Nav className="mr-auto">
          <Nav.Link as={Link} to="/time" eventKey={1} className="nav-link-professional">Time</Nav.Link>
          <NavDropdown title="Admin" id="basic-nav-dropdown" className="nav-dropdown-professional">
            <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/users" eventKey={2}>User</Nav.Link>
            <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/account" eventKey={3}>Account</Nav.Link>
            <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/task" eventKey={4}>Task</Nav.Link>
            <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/taskcontributor" eventKey={5}>Task contributor</Nav.Link>
            <NavDropdown.Divider />
            <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/reports" eventKey={6}>Reports</Nav.Link>
            <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/vacations" eventKey={7}>Vacations</Nav.Link>
            <NavDropdown.Divider />
            <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/timereportstatus" eventKey={16}>Time Report Status</Nav.Link>
            <NavDropdown.Divider />
            <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/changepwd" eventKey={12}>Change password</Nav.Link>
            <NavDropdown.Divider />
            <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/system/properties" eventKey={13}>System properties</Nav.Link>
            <Nav.Link className="text-dark dropdown-item-professional" as={Link} to="/specialdays" eventKey={14}>Special Days</Nav.Link>
          </NavDropdown>
          <Nav.Link as={Link} to="/logout" eventKey={22} className="nav-link-professional">Logout</Nav.Link>
        </Nav>
      </Navbar.Collapse>
      <Navbar.Text className="justify-content-end mr-3 text-light">
        <span style={{ paddingRight: "15px", fontWeight: "500" }}>{session.loggedInUser.name}</span>
        <span style={{ opacity: "0.9" }}>{session.currentDate ? session.currentDate.date : new Date().toLocaleDateString()}</span>
      </Navbar.Text>
    </Navbar>
  );
};

export default NavigationMenu;