import { createStore, applyMiddleware } from 'redux';
import { hashHistory } from 'react-router';
import { routerMiddleware } from 'react-router-redux';
import createLogger from 'redux-logger';
import thunk from 'redux-thunk';
import rootReducer from 'reducers/index';
import { detectEnvironment } from 'utility';
import { ENV_NAME_DEVELOPMENT } from 'constants/config';

const router = routerMiddleware(hashHistory);
const middlewares = [router, thunk];

if (detectEnvironment() === ENV_NAME_DEVELOPMENT) {
  const logger = createLogger();
  middlewares.push(logger);
}

const createStoreWithMiddleware = applyMiddleware(...middlewares)(createStore);

export default function configureStore(initialState) {
  return createStoreWithMiddleware(rootReducer, initialState);
}
