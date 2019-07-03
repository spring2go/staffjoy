import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { Router, Route, IndexRoute, hashHistory, IndexRedirect }
  from 'react-router';
import { syncHistoryWithStore } from 'react-router-redux';
import Raven from 'raven-js';
import configureStore from 'stores/configureStore';
import Launcher from 'components/Launcher';
import App from 'components/App';
import Employees from 'components/Employees';
import EmployeeSidePanel from 'components/Employees/EmployeeSidePanel';
import InfoSidePanel from 'components/Employees/InfoSidePanel';
import Scheduling from 'components/Scheduling';
import Settings from 'components/Settings';
import { Title, OtherTitle } from 'components/Title';
import * as paths from 'constants/paths';
import { detectEnvironment } from './utility';
import {
  ENV_NAME_DEVELOPMENT,
  ENV_NAME_PRODUCTION,
  SENTRY_STAGING_KEY,
  SENTRY_PRODUCTION_KEY,
} from './constants/config';

require('../../third_party/node/material_design_lite/main');
require('./main.scss');

const currentEnv = detectEnvironment();

if (currentEnv !== ENV_NAME_DEVELOPMENT) {
  const sentryKey = (currentEnv === ENV_NAME_PRODUCTION) ?
    SENTRY_PRODUCTION_KEY : SENTRY_STAGING_KEY;
  Raven
    .config(sentryKey)
    .install();
}

const store = configureStore();
const history = syncHistoryWithStore(hashHistory, store);

ReactDOM.render(
  <Provider store={store}>
    <Router history={history}>
      <Route path={paths.getRoute(paths.ROOT_PATH)}>

        {/* Company Launcher  */}
        <IndexRoute component={Launcher} />

        {/* Base page for a specific company */}
        <Route path={paths.getRoute(paths.COMPANY_BASE)} component={App}>
          <IndexRedirect to={paths.getRoute(paths.COMPANY_EMPLOYEES)} />
          <Route
            path={paths.getRoute(paths.COMPANY_EMPLOYEES)}
            component={Employees}
          >
            <IndexRoute component={InfoSidePanel} />
            <Route
              path={paths.getRoute(paths.COMPANY_EMPLOYEE)}
              component={EmployeeSidePanel}
            />
          </Route>
          <Route
            path={paths.getRoute(paths.COMPANY_HISTORY)}
            component={Title}
          />

          {/* Base page for a team within a company  */}
          <Route path={paths.getRoute(paths.TEAM_BASE)}>
            <IndexRedirect to={paths.getRoute(paths.TEAM_SCHEDULING)} />
            <Route
              path={paths.getRoute(paths.TEAM_SCHEDULING)}
              component={Scheduling}
            />
            <Route
              path={paths.getRoute(paths.TEAM_SETTINGS)}
              component={Settings}
            />
            <Route
              path={paths.getRoute(paths.TEAM_SHIFT_BOARD)}
              component={OtherTitle}
            />
          </Route>
        </Route>
      </Route>
    </Router>
  </Provider>,
  document.getElementById('app')
);
