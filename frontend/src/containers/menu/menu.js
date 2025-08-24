import React from "react";
import { Navbar, Nav, NavDropdown } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useSession } from "../../contexts/SessionContext";
import logo from "../../assets/logo.png";

const NavigationMenu = () => {
  const { session } = useSession();

  if (!session || !session.loggedInUser) {
    return null;
  }

  const renderUserMenu = () => {
    return (
      <Navbar bg="dark" variant="dark" expand="lg">
        <Navbar.Brand href="/">
          <img
            src={logo}
            alt="D-Time"
            style={{ height: "40px", marginRight: "10px" }}
          />
          <span>D-Time</span>
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="mr-auto">
            <Nav.Link as={Link} to="/time" eventKey={1}>Time</Nav.Link>
            <NavDropdown title="Admin" id="basic-nav-dropdown">
              <Nav.Link className="text-dark" as={Link} to="/userreport" eventKey={2}>Report</Nav.Link>
              <NavDropdown.Divider />
              <Nav.Link className="text-dark" as={Link} to="/changepwd" eventKey={3}>Change password</Nav.Link>
            </NavDropdown>
            <Nav.Link as={Link} to="/logout" eventKey={15}>Logout</Nav.Link>
          </Nav>
        </Navbar.Collapse>
        <Navbar.Text className="justify-content-end mr5 text-light">
          <span style={{ paddingRight: 10 }}>{session.loggedInUser.name}</span>
          <span>{session.currentDate ? session.currentDate.date : new Date().toLocaleDateString()}</span>
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
    <Navbar bg="dark" variant="dark" expand="lg">
      <Navbar.Brand href="/">
        <img
          src={logo}
          alt="D-Time"
          style={{ height: "40px", marginRight: "10px" }}
        />
        <span>D-Time</span>
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="basic-navbar-nav" />
      <Navbar.Collapse id="basic-navbar-nav">
        <Nav className="mr-auto">
          <Nav.Link as={Link} to="/time" eventKey={1}>Time</Nav.Link>
          <NavDropdown title="Admin" id="basic-nav-dropdown">
            <Nav.Link className="text-dark" as={Link} to="/users" eventKey={2}>User</Nav.Link>
            <Nav.Link className="text-dark" as={Link} to="/account" eventKey={3}>Account</Nav.Link>
            <Nav.Link className="text-dark" as={Link} to="/task" eventKey={4}>Task</Nav.Link>
            <Nav.Link className="text-dark" as={Link} to="/taskcontributor" eventKey={5}>Task contributor</Nav.Link>
            <NavDropdown.Divider />
            <Nav.Link className="text-dark" as={Link} to="/reports" eventKey={6}>Reports</Nav.Link>
            <Nav.Link className="text-dark" as={Link} to="/vacations" eventKey={7}>Vacations</Nav.Link>
            <NavDropdown.Divider />
            <Nav.Link className="text-dark" as={Link} to="/timereportstatus" eventKey={16}>Time Report Status</Nav.Link>
            <NavDropdown.Divider />
            <Nav.Link className="text-dark" as={Link} to="/changepwd" eventKey={12}>Change password</Nav.Link>
            <NavDropdown.Divider />
            <Nav.Link className="text-dark" as={Link} to="/system/properties" eventKey={13}>System properties</Nav.Link>
            <Nav.Link className="text-dark" as={Link} to="/specialdays" eventKey={14}>Special Days</Nav.Link>
          </NavDropdown>
          <Nav.Link as={Link} to="/logout" eventKey={22}>Logout</Nav.Link>
        </Nav>
      </Navbar.Collapse>
      <Navbar.Text className="justify-content-end mr5 text-light">
        <span style={{ paddingRight: 10 }}>{session.loggedInUser.name}</span>
        <span>{session.currentDate ? session.currentDate.date : new Date().toLocaleDateString()}</span>
      </Navbar.Text>
    </Navbar>
  );
};

export default NavigationMenu;