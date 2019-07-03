import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';
import createLogger from 'redux-logger';
import rootReducer from '../reducers/index';
import { detectEnvironment } from '../utility';

const middlewares = [thunk];

if (detectEnvironment() === 'DEV') {
  const logger = createLogger();
  middlewares.push(logger);
}

const createStoreWithMiddleware = applyMiddleware(...middlewares)(createStore);

export default function configureStore(initialState) {
  return createStoreWithMiddleware(rootReducer, initialState);
}
