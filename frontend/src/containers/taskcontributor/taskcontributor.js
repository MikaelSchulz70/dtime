import React from "react";
import { Container, Card, Table, Row, Col, Alert } from 'react-bootstrap';
import TaskContributorService from '../../service/TaskContributorService';
import UserService from '../../service/UserService';
import *  as Constants from '../../common/Constants';


class TaskcontributorTableRow extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.state = { taskcontributor: this.props.taskcontributor };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    handleChange(event) {
        let taskcontributor = JSON.parse(JSON.stringify(this.state.taskcontributor));
        let field = event.target.name;
        let value = event.target.value;
        taskcontributor[field] = value;

        const self = this;
        const taskContributorService = new TaskContributorService();
        taskContributorService.udate(taskcontributor)
            .then(response => {
                self.props.activationStatusChanged(taskcontributor);
            })
            .catch(error => {
                console.error('Failed to update task contributor:', error);
                // Note: We could pass a callback here to show alerts in the parent component
            });
    }

    render() {
        if (this.state == null || this.state.taskcontributor == null) return null;

        return (
            <tr>
                <td>{this.state.taskcontributor.task.account.name}</td>
                <td>{this.state.taskcontributor.task.name}</td>
                <td>
                    <select className="form-control input-sm" name="activationStatus" value={this.state.taskcontributor.activationStatus} onChange={this.handleChange}>
                        <option value={Constants.ACTIVE_STATUS}>Active</option>
                        <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                    </select>
                </td>
            </tr>
        );
    }
};

class TaskContributorTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = { taskcontributors: this.props.taskcontributors };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props !== nextProps) {
            this.setState(nextProps);
        }
    }

    render() {
        if (this.state == null) return null;

        var accountNameFilter = this.props.accountNameFilter;
        var taskNameFilter = this.props.taskNameFilter;
        var statusFilter = this.props.statusFilter;

        if (this.props.taskcontributors != null) {
            var filteredTaskcontributors = this.props.taskcontributors.filter(function (taskcontributor) {
                return (taskcontributor.activationStatus === statusFilter) &&
                    (taskcontributor.task.account.name.toLowerCase().startsWith(accountNameFilter.toLowerCase())) &&
                    (taskcontributor.task.name.toLowerCase().startsWith(taskNameFilter.toLowerCase()));
            });

            var self = this;
            var rows = [];
            filteredTaskcontributors.forEach(function (taskcontributor) {
                var key = taskcontributor.task.account.id + '_' + taskcontributor.task.id;
                rows.push(
                    <TaskcontributorTableRow taskcontributor={taskcontributor} key={key} activationStatusChanged={self.props.activationStatusChanged} />);
            });
        }

        return (
            <Table striped bordered hover responsive>
                <thead>
                    <tr>
                        <th>Account</th>
                        <th>Task</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </Table>
        );
    }
};

export default class TaskContributor extends React.Component {
    constructor(props) {
        super(props);
        this.loadFromServer = this.loadFromServer.bind(this);
        this.filterChanged = this.filterChanged.bind(this);
        this.handleUserChange = this.handleUserChange.bind(this);
        this.handleUserSearch = this.handleUserSearch.bind(this);
        this.handleUserSelect = this.handleUserSelect.bind(this);
        this.handleKeyDown = this.handleKeyDown.bind(this);
        this.activationStatusChanged = this.activationStatusChanged.bind(this);

        this.state = {
            accountNameFilter: '',
            taskNameFilter: '',
            statusFilter: Constants.ACTIVE_STATUS,
            userSearchTerm: '',
            selectedUserId: 0,
            showUserDropdown: false,
            highlightedIndex: -1,
            alert: { show: false, message: '', type: 'success' }
        };
    }

    componentDidMount() {
        this.loadFromServer();
    }

    showAlert = (message, type = 'success') => {
        this.setState({ alert: { show: true, message, type } });
        setTimeout(() => this.setState({ alert: { show: false, message: '', type: 'success' } }), 5000);
    };

    loadFromServer() {
        console.log('TaskContributor loadFromServer called');
        const self = this;
        const userService = new UserService();
        userService.getByStatus(true)
            .then(response => {
                self.setState({ users: response.data });
            })
            .catch(error => {
                console.error('Failed to load users:', error);
                this.showAlert('Failed to load users', 'danger');
            });
    }

    activationStatusChanged(updatedtaskcontributor) {
        let taskcontributors = JSON.parse(JSON.stringify(this.state.taskcontributors));

        for (var i in taskcontributors) {
            if (taskcontributors[i].task.id === updatedtaskcontributor.task.id &&
                taskcontributors[i].task.account.id === updatedtaskcontributor.task.account.id) {
                taskcontributors[i] = updatedtaskcontributor;
                this.setState({ taskcontributors: taskcontributors });
                break;
            }
        }
    }

    filterChanged(event) {
        var value = event.target.value;
        const name = event.target.name;
        this.setState({ [name]: value });
    }

    handleUserSearch(event) {
        const value = event.target.value;
        this.setState({
            userSearchTerm: value,
            showUserDropdown: value.length > 0,
            highlightedIndex: -1 // Reset highlighting when typing
        });

        // If search is cleared, reset selection
        if (value === '') {
            this.handleUserSelection(0);
        }
    }

    handleUserSelect(user) {
        this.setState({
            userSearchTerm: `${user.firstName} ${user.lastName} (${user.email})`,
            showUserDropdown: false,
            highlightedIndex: -1
        });
        this.handleUserSelection(user.id);
    }

    handleKeyDown(event) {
        if (!this.state.showUserDropdown) return;

        const filteredUsers = this.state.users.filter(user => {
            if (!this.state.userSearchTerm) return true;
            const fullName = `${user.firstName} ${user.lastName} ${user.email}`.toLowerCase();
            return fullName.includes(this.state.userSearchTerm.toLowerCase());
        });

        const maxIndex = Math.min(filteredUsers.length - 1, 9); // Limit to 10 items

        switch (event.key) {
            case 'ArrowDown':
                event.preventDefault();
                this.setState(prevState => ({
                    highlightedIndex: prevState.highlightedIndex < maxIndex ? prevState.highlightedIndex + 1 : 0
                }));
                break;

            case 'ArrowUp':
                event.preventDefault();
                this.setState(prevState => ({
                    highlightedIndex: prevState.highlightedIndex > 0 ? prevState.highlightedIndex - 1 : maxIndex
                }));
                break;

            case 'Enter':
                event.preventDefault();
                if (this.state.highlightedIndex >= 0 && this.state.highlightedIndex <= maxIndex) {
                    const selectedUser = filteredUsers[this.state.highlightedIndex];
                    this.handleUserSelect(selectedUser);
                }
                break;

            case 'Escape':
                event.preventDefault();
                this.setState({ 
                    showUserDropdown: false, 
                    highlightedIndex: -1 
                });
                break;

            default:
                break;
        }
    }

    handleUserSelection(userId) {
        console.log('User selected:', userId);
        this.setState({ selectedUserId: userId });

        if (userId === 0) {
            this.setState({
                accountNameFilter: '',
                taskNameFilter: '',
                statusFilter: Constants.ACTIVE_STATUS,
                taskcontributors: null
            });
            return;
        }

        const self = this;
        const taskContributorService = new TaskContributorService();
        console.log('Loading task contributors for user:', userId);
        taskContributorService.getTaskContributor(userId)
            .then(response => {
                self.setState({ taskcontributors: response.data });
            })
            .catch(error => {
                console.error('Failed to load taskcontributors:', error.response?.status, error.response?.data);
                if (error.response?.status === 403) {
                    this.showAlert('Access denied: You need admin privileges to view task contributors', 'danger');
                } else {
                    this.showAlert('Failed to load taskcontributors: ' + (error.response?.data?.error || error.message), 'danger');
                }
            });
    }

    handleUserChange(event) {
        const userId = parseInt(event.target.value, 10);
        this.handleUserSelection(userId);
    }

    render() {
        if (this.state == null || this.state.users == null) {
            return (
                <Container fluid className="mt-4">
                    <div className="text-center">
                        <div className="spinner-border" role="status">
                            <span className="visually-hidden">Loading...</span>
                        </div>
                    </div>
                </Container>
            );
        }

        // Filter users based on search term
        const filteredUsers = this.state.users.filter(user => {
            if (!this.state.userSearchTerm) return true;
            const fullName = `${user.firstName} ${user.lastName} ${user.email}`.toLowerCase();
            return fullName.includes(this.state.userSearchTerm.toLowerCase());
        });

        return (
            <Container fluid className="mt-4">
                <Card>
                    <Card.Header>
                        <Row>
                            <Col sm={6}>
                                <h4>Task Contributors</h4>
                            </Col>
                        </Row>
                    </Card.Header>
                    <Card.Body>
                        {this.state.alert.show && (
                            <Alert variant={this.state.alert.type} dismissible onClose={() => this.setState({ alert: { show: false, message: '', type: 'success' } })}>
                                {this.state.alert.message}
                            </Alert>
                        )}

                        {/* Filters */}
                        <Row className="mb-3">
                            <Col md={3} style={{ position: 'relative' }}>
                                <input
                                    type="text"
                                    className="form-control input-sm dataLiveSearch"
                                    placeholder="Search for a user..."
                                    value={this.state.userSearchTerm}
                                    onChange={this.handleUserSearch}
                                    onKeyDown={this.handleKeyDown}
                                    onFocus={() => this.setState({ showUserDropdown: true })}
                                    onBlur={() => setTimeout(() => this.setState({ showUserDropdown: false }), 200)}
                                />
                                {this.state.showUserDropdown && filteredUsers.length > 0 && (
                                    <div className="dropdown-menu show" style={{
                                        position: 'absolute',
                                        top: '100%',
                                        left: 0,
                                        right: 0,
                                        maxHeight: '300px',
                                        overflowY: 'auto',
                                        zIndex: 1000
                                    }}>
                                        {filteredUsers.slice(0, 10).map((user, index) => (
                                            <button
                                                key={user.id}
                                                type="button"
                                                className={`dropdown-item ${index === this.state.highlightedIndex ? 'active' : ''}`}
                                                onClick={() => this.handleUserSelect(user)}
                                                style={{ textAlign: 'left', whiteSpace: 'nowrap' }}
                                            >
                                                <strong>{user.firstName} {user.lastName}</strong>
                                                <br />
                                                <small className="text-muted">{user.email}</small>
                                            </button>
                                        ))}
                                    </div>
                                )}
                            </Col>
                            <Col md={2}>
                                <input 
                                    className="form-control input-sm" 
                                    type="text" 
                                    placeholder="Filter by account" 
                                    name="accountNameFilter" 
                                    value={this.state.accountNameFilter}
                                    onChange={this.filterChanged} 
                                />
                            </Col>
                            <Col md={2}>
                                <input 
                                    className="form-control input-sm" 
                                    type="text" 
                                    placeholder="Filter by task" 
                                    name="taskNameFilter" 
                                    value={this.state.taskNameFilter}
                                    onChange={this.filterChanged} 
                                />
                            </Col>
                            <Col md={2}>
                                <select 
                                    className="form-control input-sm" 
                                    name="statusFilter" 
                                    value={this.state.statusFilter}
                                    onChange={this.filterChanged}
                                >
                                    <option value={Constants.ACTIVE_STATUS}>Active</option>
                                    <option value={Constants.INACTIVE_STATUS}>Inactive</option>
                                </select>
                            </Col>
                        </Row>

                        {/* Task Contributors Table */}
                        <TaskContributorTable
                            taskcontributors={this.state.taskcontributors}
                            accountNameFilter={this.state.accountNameFilter}
                            taskNameFilter={this.state.taskNameFilter}
                            statusFilter={this.state.statusFilter}
                            activationStatusChanged={this.activationStatusChanged} 
                        />
                    </Card.Body>
                </Card>
            </Container>
        );
    }
};

