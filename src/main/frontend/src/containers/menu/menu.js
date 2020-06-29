import React from "react";
import { Navbar, Nav, NavDropdown } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import SessionService from "../../service/SessionService";

export default class NavigationMenu extends React.Component {
  constructor(props) {
    super(props);
    this.renderUserMenu = this.renderUserMenu.bind(this);
    this.refreshMenu = this.refreshMenu.bind(this);
  }

  refreshMenu(sessionInfo) {
    this.setState({ sessionInfo: sessionInfo });
  }

  componentDidMount() {
    SessionService.getSessionInfo(this.refreshMenu);
  }

  renderUserMenu() {
    return (
      <Navbar bg="dark" variant="dark" expand="lg">
        <Navbar.Brand href="/">Dtime</Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="mr-auto">
            <Nav.Link as={Link} to="/time" eventKey={1}>Time</Nav.Link>
            <NavDropdown title="Admin" id="basic-nav-dropdown">
              <Nav.Link className="text-dark" as={Link} to="/userreport" eventKey={2}>Report</Nav.Link>
              <NavDropdown.Divider />
              <Nav.Link className="text-dark" as={Link} to="/changepwd" eventKey={3}>Change password</Nav.Link>
            </NavDropdown>
            <NavDropdown title="OnCall" id="basic-nav-dropdown">
              <Nav.Link className="text-dark" as={Link} to="/oncall" eventKey={10}>Schedule</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/oncall/config" eventKey={11}>Config</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/oncall/alarms" eventKey={13}>Alarms</Nav.Link>
            </NavDropdown>
            <NavDropdown title="Misc" id="basic-nav-dropdown">
              <Nav.Link className="text-dark" as={Link} to="/misc/activities" eventKey={16}>Activities</Nav.Link>
            </NavDropdown>
            <Nav.Link as={Link} to="/logout" eventKey={15}>Logout</Nav.Link>
          </Nav>
        </Navbar.Collapse>
        <Navbar.Text className="justify-content-end mr5 text-light">
          <span style={{ paddingRight: 10 }}>{this.state.sessionInfo.loggedInUser.name}</span>
          <span>{this.state.sessionInfo.currentDate.date}</span>
        </Navbar.Text>
      </Navbar>
    );
  }

  render() {
    if (this.state == null || this.state.sessionInfo == null) return null;

    // Render user menu
    if (!this.state.sessionInfo.loggedInUser.admin) {
      return this.renderUserMenu();
    }

    // Render admin menu
    return (
      <Navbar bg="dark" variant="dark" expand="lg">
        <Navbar.Brand href="/">Dtime</Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="mr-auto">
            <Nav.Link as={Link} to="/time" eventKey={1}>Time</Nav.Link>
            <NavDropdown title="Admin" id="basic-nav-dropdown">
              <Nav.Link className="text-dark" as={Link} to="/users" eventKey={2}>User</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/companies" eventKey={3}>Company</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/projects" eventKey={4}>Project</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/assignments" eventKey={5}>Assignment</Nav.Link>
              <NavDropdown.Divider />
              <Nav.Link className="text-dark" as={Link} to="/reports" eventKey={6}>Reports</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/vacations" eventKey={7}>Vacations</Nav.Link>
              <NavDropdown.Divider />
              <Nav.Link className="text-dark" as={Link} to="/rates" eventKey={8}>Hour rates</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/fixrates" eventKey={9}>Monthly fix rates</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/basis/invoice" eventKey={10}>Invoice basis</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/followup" eventKey={11}>Follow up</Nav.Link>
              <NavDropdown.Divider />
              <Nav.Link className="text-dark" as={Link} to="/changepwd" eventKey={12}>Change password</Nav.Link>
              <NavDropdown.Divider />
              <Nav.Link className="text-dark" as={Link} to="/system/properties" eventKey={13}>System properties</Nav.Link>
            </NavDropdown>
            <NavDropdown title="OnCall" id="basic-nav-dropdown">
              <Nav.Link className="text-dark" as={Link} to="/oncall" eventKey={14}>Schedule</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/oncall/config" eventKey={15}>Config</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/oncall/rules" eventKey={16}>Rules</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/oncall/alarms" eventKey={17}>Alarms</Nav.Link>
              <Nav.Link className="text-dark" as={Link} to="/oncall/operation" eventKey={18}>Operation</Nav.Link>
            </NavDropdown>
            <NavDropdown title="Misc" id="basic-nav-dropdown">
              <Nav.Link className="text-dark" as={Link} to="/misc/activities" eventKey={20}>Activities</Nav.Link>
            </NavDropdown>
            <Nav.Link as={Link} to="/logout" eventKey={22}>Logout</Nav.Link>
          </Nav>
        </Navbar.Collapse>
        <Navbar.Text className="justify-content-end mr5 text-light">
          <span style={{ paddingRight: 10 }}>{this.state.sessionInfo.loggedInUser.name}</span>
          <span>{this.state.sessionInfo.currentDate.date}</span>
        </Navbar.Text>
      </Navbar>
    );
  }
}