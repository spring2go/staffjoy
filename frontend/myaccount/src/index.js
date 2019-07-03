import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import Raven from 'raven-js';
import configureStore from './stores/configureStore';
import App from './components/App';
import { detectEnvironment } from './utility';
import {
  ENV_NAME_DEVELOPMENT,
  ENV_NAME_PRODUCTION,
  SENTRY_UAT_KEY,
  SENTRY_PRODUCTION_KEY,
} from './constants/config';

require('../../third_party/node/material_design_lite/main');
require('./main.scss');

const currentEnv = detectEnvironment();

if (currentEnv !== ENV_NAME_DEVELOPMENT) {
  const sentryKey = (currentEnv === ENV_NAME_PRODUCTION) ?
    SENTRY_PRODUCTION_KEY : SENTRY_UAT_KEY;
  Raven
    .config(sentryKey)
    .install();
}

const store = configureStore();

ReactDOM.render(
  <Provider store={store}>
    <App />
  </Provider>,
  document.getElementById('app')
);
