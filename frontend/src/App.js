import React, { Suspense } from 'react';
import { Switch, Route, BrowserRouter, Redirect, Link } from 'react-router-dom';
import axios from 'axios';
import './App.css';

// Context Providers
// import { SessionProvider, useSession } from './contexts/SessionContext'; // Temporarily disabled
import ToastProvider from './components/ToastProvider';
import ErrorBoundary from './components/ErrorBoundary';
import LoadingSpinner from './components/LoadingSpinner';

// Components
import NavigationMenu from './containers/menu/menu';
import Login from './components/Login';
import Times from './containers/timereport/time';
import Users from './containers/user/users';
import UserDetails from './containers/user/userdetails';
import UsersModal from './containers/user/UsersModal';
import Account from './containers/account/account';
import AccountDetails from './containers/account/accountdetails';
import AccountsModal from './containers/account/AccountsModal';
import Task from './containers/task/task';
import TaskDetails from './containers/task/taskdetails';
import TasksModal from './containers/task/TasksModal';
import TaskContributor from './containers/taskcontributor/taskcontributor';
import AdminReports from './containers/report/AdminReports';
import UserReports from './containers/report/UserReports';
import Vacations from './containers/report/Vacations';
import PasswordChanger from './containers/user/changepwd';
import SystemConfig from './containers/system/systemConfig';
import UnclosedUsersPage from './containers/timereportstatus/UnclosedUsersPage';
import SpecialDays from './containers/specialday/SpecialDays';

// Placeholder imports for missing components - these may not be used in routes
// TODO: Remove unused imports or create these components if needed
// import Activities from './containers/activity/activities';
// import ActivityDetails from './containers/activity/activitydetails';
// import Rates from './containers/rate/rates';
// import ParticipationRates from './containers/rate/participationrates';
// import FixRates from './containers/rate/fixrates';
// import CategoryFixRates from './containers/rate/categoryfixrates';

// Let ServiceUtil handle CSRF tokens manually for better control
// axios.defaults.xsrfHeaderName = 'X-XSRF-TOKEN';
// axios.defaults.xsrfCookieName = 'XSRF-TOKEN';

// Logout component - could be converted to functional component later
class Logout extends React.Component {
  componentDidMount() {
    axios.get('/logout')
      .then(response => {
        window.location.href = '/';
      })
      .catch(error => {
        console.error('Logout error:', error);
        // Force redirect even if logout fails
        window.location.href = '/';
      });
  }

  render() {
    return <LoadingSpinner fullPage text="Logging out..." />;
  }
}


const Main = () => (
  <main>
    <div className="container-fluid mr-10" style={{ marginTop: '20px' }}>
      <ErrorBoundary>
        <Suspense fallback={<LoadingSpinner fullPage text="Loading page..." />}>
          <Switch>
            <Route exact path='/' component={Times} />
            <Route exact path='/time' component={Times} />
            <Route exact path='/users' component={UsersModal} />
            <Route exact path='/users-old' component={Users} />
            <Route exact path='/users/:userId' component={UserDetails} />
            <Route exact path='/account' component={AccountsModal} />
            <Route exact path='/account-old' component={Account} />
            <Route exact path='/account/:accountId' component={AccountDetails} />
            <Route exact path='/task' component={TasksModal} />
            <Route exact path='/task-old' component={Task} />
            <Route exact path='/task/:taskId' component={TaskDetails} />
            <Route exact path='/tasks/:taskId' component={TaskDetails} />
            <Route exact path='/taskcontributor' component={TaskContributor} />
            <Route exact path='/reports' component={AdminReports} />
            <Route exact path='/userreport' component={UserReports} />
            <Route exact path='/vacations' component={Vacations} />
            <Route exact path='/timereportstatus' component={UnclosedUsersPage} />
            <Route exact path='/changepwd' component={PasswordChanger} />
            <Route exact path='/system/properties' component={SystemConfig} />
            <Route exact path='/specialdays' component={SpecialDays} />
            <Route exact path='/logout' component={Logout} />
            <Redirect to="/" />
          </Switch>
        </Suspense>
      </ErrorBoundary>
    </div>
  </main>
)

const Footer = () => (
  <footer className="bg-dark mt-4">
    <div className="container-fluid py-3 text-center">
      <div className="row">
        <div className="col-md-12">
          <Link className="text-light" to="/time">Dtime</Link>
        </div>
      </div>
    </div>
  </footer>
)


const AppContent = () => {
  const [session, setSession] = React.useState(null);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const checkSession = async () => {
      try {
        console.log('Checking session...');
        const response = await fetch('/api/session');
        if (response.ok) {
          const data = await response.json();
          setSession(data);
          console.log('Session found:', data);
        } else {
          console.log('No session found:', response.status);
          setSession(null);
        }
      } catch (error) {
        console.log('Session check error:', error);
        setSession(null);
      } finally {
        setLoading(false);
        console.log('Session check complete');
      }
    };
    
    checkSession();
  }, []);

  if (loading) {
    return <LoadingSpinner fullPage text="Loading session..." />;
  }

  const isAuthenticated = session && session.loggedInUser;

  if (!isAuthenticated) {
    return <Login />;
  }

  return (
    <div className="d-flex flex-column min-vh-100">
      <NavigationMenu />
      <div className="flex-grow-1">
        <Main />
      </div>
      <Footer />
    </div>
  );
};

const App = () => (
  <ErrorBoundary>
    <BrowserRouter>
      <ToastProvider>
        <AppContent />
      </ToastProvider>
    </BrowserRouter>
  </ErrorBoundary>
)

export default App;