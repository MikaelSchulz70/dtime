import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { BrowserRouter, Redirect } from 'react-router-dom';
import { Link } from 'react-router-dom';
import NavigationMenu from './containers/menu/Menu';
import Times from './containers/time/Time';
import Users from './containers/user/Users';
import UserDetails from './containers/user/UserDetails';
import Companies from './containers/company/Companies';
import CompanyDetails from './containers/company/CompanyDetails';
import Projects from './containers/project/Projects';
import ProjectDetails from './containers/project/ProjectDetails';
import Assignment from './containers/assignment/Assignments';
import Reports from './containers/report/Reports';
import UserReports from './containers/report/UserReports';
import Vacations from './containers/report/Vacations';
import PasswordChanger from './containers/user/ChangePwd';
import SystemConfig from './containers/system/SystemConfig';
import OnCallSchedule from './containers/oncall/OnCallSchedule';
import OnCallConfig from './containers/oncall/OnCallConfig';
import OnCallRules from './containers/oncall/OnCallRules';
import OnCallRuleDetails from './containers/oncall/OnCallRuleDetails';
import OnCallAlarms from './containers/oncall/OnCallAlarms';
import OnCallAlarmDetails from './containers/oncall/OnCallAlarmDetails';
import OnCallOperation from './containers/oncall/OnCallOperation';
import Activities from './containers/activity/activities';
import ActivityDetails from './containers/activity/activitydetails';
import Rates from './containers/rate/rates';
import AssignmentRates from './containers/rate/assignmentrates';
import FixRates from './containers/rate/fixrates';
import ProjectFixRates from './containers/rate/projectfixrates';
import InvoiceBasis from './containers/basis/InvoiceBasis';
import FollowUpReport from './containers/followup/FollowUpReport'
import axios from 'axios';

axios.defaults.xsrfHeaderName = 'X-CSRFToken';
axios.defaults.xsrfCookieName = 'csrftoken';

class Logout extends React.Component {

  componentDidMount() {
    axios.get('/logout')
      .then(response => {
        window.location.href = '/';
      });
  }

  render() {
    return null;
  };
};

const Main = () => (
  <main>
    <div className="container-fluid mr-10" style={{ marginTop: '20px' }}>
      <Switch>
        <Route exact path='/' component={Times} />
        <Route exact path='/time' component={Times} />
        <Route exact path='/users' component={Users} />
        <Route exact path='/users/:userId' component={UserDetails} />
        <Route exact path='/companies' component={Companies} />
        <Route exact path='/companies/:companyId' component={CompanyDetails} />
        <Route exact path='/projects' component={Projects} />
        <Route exact path='/projects/:projectId' component={ProjectDetails} />
        <Route exact path='/assignments' component={Assignment} />
        <Route exact path='/rates' component={Rates} />
        <Route exact path='/rates/:assignmentId' component={AssignmentRates} />
        <Route exact path='/fixrates' component={FixRates} />
        <Route exact path='/fixrates/:projectId' component={ProjectFixRates} />
        <Route exact path='/reports' component={Reports} />
        <Route exact path='/userreport' component={UserReports} />
        <Route exact path='/vacations' component={Vacations} />
        <Route exact path='/basis/invoice' component={InvoiceBasis} />
        <Route exact path='/followup' component={FollowUpReport} />
        <Route exact path='/changepwd' component={PasswordChanger} />
        <Route exact path='/system/properties' component={SystemConfig} />
        <Route exact path='/oncall' component={OnCallSchedule} />
        <Route exact path='/oncall/config' component={OnCallConfig} />
        <Route exact path='/oncall/rules' component={OnCallRules} />
        <Route exact path='/oncall/rules/:projectId' component={OnCallRuleDetails} />
        <Route exact path='/oncall/alarms' component={OnCallAlarms} />
        <Route exact path='/oncall/alarms/:alarmId' component={OnCallAlarmDetails} />
        <Route exact path='/oncall/operation' component={OnCallOperation} />
        <Route exact path='/misc/activities' component={Activities} />
        <Route exact path='/misc/activities/add' component={ActivityDetails} />
        <Route exact path='/logout' component={Logout} />
        <Redirect to="/" />
      </Switch>
    </div>
  </main >
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


const App = () => (
  <div>
    <BrowserRouter>
      <NavigationMenu />
      <Main />
      <Footer />
    </BrowserRouter>
  </div>
)

export default App;