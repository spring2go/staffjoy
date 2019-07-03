import _ from 'lodash';
import * as actionTypes from '../constants/actionTypes';

const initialState = {
  accountUpdate: {},
  passwordUpdate: {},
};

export default function (state = initialState, action) {
  switch (action.type) {

    case actionTypes.FORM_CHANGE:
      return _.extend({}, state, {
        [action.form]: action.data,
      });

    default:
      return state;
  }
}
