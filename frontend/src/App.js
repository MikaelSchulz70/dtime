import React, { Suspense } from 'react';
import { Switch, Route } from 'react-router-dom';
import { BrowserRouter, Redirect } from 'react-router-dom';
import { Link } from 'react-router-dom';
import axios from 'axios';

// Context Providers
import { SessionProvider, useSession } from './contexts/SessionContext';
import ToastProvider from './components/ToastProvider';
import ErrorBoundary from './components/ErrorBoundary';
import LoadingSpinner from './components/LoadingSpinner';

// Components
import NavigationMenu from './containers/menu/menu';
import Times from './containers/timereport/time';
import Users from './containers/user/users';
import UserDetails from './containers/user/userdetails';
import Account from './containers/account/account';
import AccountDetails from './containers/account/accountdetails';
import Task from './containers/task/task';
import TaskDetails from './containers/task/taskdetails';
import TaskContributor from './containers/taskcontributor/taskcontributor';
import Reports from './containers/report/Reports';
import UserReports from './containers/report/UserReports';
import Vacations from './containers/report/Vacations';
import PasswordChanger from './containers/user/changepwd';
import SystemConfig from './containers/system/systemConfig';
import FollowUpReport from './containers/followup/FollowUpReport';

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
            <Route exact path='/users' component={Users} />
            <Route exact path='/users/:userId' component={UserDetails} />
            <Route exact path='/account' component={Account} />
            <Route exact path='/account/:accountId' component={AccountDetails} />
            <Route exact path='/task' component={Task} />
            <Route exact path='/task/:taskId' component={TaskDetails} />
            <Route exact path='/tasks/:taskId' component={TaskDetails} />
            <Route exact path='/taskcontributor' component={TaskContributor} />
            <Route exact path='/reports' component={Reports} />
            <Route exact path='/userreport' component={UserReports} />
            <Route exact path='/vacations' component={Vacations} />
            <Route exact path='/followup' component={FollowUpReport} />
            <Route exact path='/changepwd' component={PasswordChanger} />
            <Route exact path='/system/properties' component={SystemConfig} />
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
  const { loading, isAuthenticated, error } = useSession();

  if (loading) {
    return <LoadingSpinner fullPage text="Loading session..." />;
  }

  if (!isAuthenticated()) {
    // Redirect to backend login page
    window.location.href = '/login';
    return <LoadingSpinner fullPage text="Redirecting to login..." />;
  }

  console.log('User authenticated, loading app');

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
      <SessionProvider>
        <ToastProvider>
          <AppContent />
        </ToastProvider>
      </SessionProvider>
    </BrowserRouter>
  </ErrorBoundary>
)

export default App;