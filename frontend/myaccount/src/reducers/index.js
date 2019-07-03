import { combineReducers } from 'redux';
import forms from './forms';
import user from './user';
import whoami from './whoami';

export default combineReducers({
  forms,
  user,
  whoami,
});
