import { combineReducers } from 'redux';
import { routerReducer } from 'react-router-redux';
import { reducer as formReducer } from 'redux-form';
import associations from './associations';
import company from './company';
import employees from './employees';
import scheduling from './scheduling';
import teams from './teams';
import user from './user';
import whoami from './whoami';
import settings from './settings';

export default combineReducers({
  routing: routerReducer,
  form: formReducer,
  associations,
  company,
  employees,
  scheduling,
  teams,
  user,
  whoami,
  settings,
});
