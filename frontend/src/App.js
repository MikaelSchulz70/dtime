import React, { Suspense, useEffect } from 'react';
import { Routes, Route, BrowserRouter, Navigate, Link } from 'react-router';
import axios from 'axios';
import './App.css';

// Context Providers
// import { SessionProvider, useSession } from './contexts/SessionContext'; // Temporarily disabled
import { ToastProvider } from './components/Toast';
import ErrorBoundary from './components/ErrorBoundary';
import LoadingSpinner from './components/LoadingSpinner';

// Components
import NavigationMenu from './containers/menu/Menu';
import Login from './components/Login';
import Times from './containers/timereport/Time';
import Users from './containers/user/Users';
import UserDetails from './containers/user/UserDetails';
import UsersModal from './containers/user/UsersModal';
import Account from './containers/account/Account';
import AccountDetails from './containers/account/AccountDetails';
import AccountsModal from './containers/account/AccountsModal';
import Task from './containers/task/Task';
import TaskDetails from './containers/task/TaskDetails';
import TasksModal from './containers/task/TasksModal';
import TaskContributor from './containers/taskcontributor/TaskContributor';
import AdminReports from './containers/report/AdminReports';
import UserReports from './containers/report/UserReports';
import Vacations from './containers/report/Vacations';
import PasswordChanger from './containers/user/ChangePassword';
import SystemConfig from './containers/system/SystemConfig';
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

// Logout component - converted to functional component with hooks
function Logout() {
  useEffect(() => {
    axios.get('/logout')
      .then(response => {
        window.location.href = '/';
      })
      .catch(error => {
        console.error('Logout error:', error);
        // Force redirect even if logout fails
        window.location.href = '/';
      });
  }, []);

  return <LoadingSpinner fullPage text="Logging out..." />;
}


const Main = () => (
  <main>
    <div className="container-fluid mr-10" style={{ marginTop: '20px' }}>
      <ErrorBoundary>
        <Suspense fallback={<LoadingSpinner fullPage text="Loading page..." />}>
          <Routes>
            <Route path='/' element={<Times />} />
            <Route path='/time' element={<Times />} />
            <Route path='/users' element={<UsersModal />} />
            <Route path='/users-old' element={<Users />} />
            <Route path='/users/:userId' element={<UserDetails />} />
            <Route path='/account' element={<AccountsModal />} />
            <Route path='/account-old' element={<Account />} />
            <Route path='/account/:accountId' element={<AccountDetails />} />
            <Route path='/task' element={<TasksModal />} />
            <Route path='/task-old' element={<Task />} />
            <Route path='/task/:taskId' element={<TaskDetails />} />
            <Route path='/tasks/:taskId' element={<TaskDetails />} />
            <Route path='/taskcontributor' element={<TaskContributor />} />
            <Route path='/reports' element={<AdminReports />} />
            <Route path='/userreport' element={<UserReports />} />
            <Route path='/vacations' element={<Vacations />} />
            <Route path='/timereportstatus' element={<UnclosedUsersPage />} />
            <Route path='/changepwd' element={<PasswordChanger />} />
            <Route path='/system/properties' element={<SystemConfig />} />
            <Route path='/specialdays' element={<SpecialDays />} />
            <Route path='/logout' element={<Logout />} />
            <Route path='*' element={<Navigate to="/" replace />} />
          </Routes>
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
      <NavigationMenu session={session} />
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