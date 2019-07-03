import _ from 'lodash';
import * as actionTypes from 'constants/actionTypes';

const initialState = {
  isFetching: false,
  didInvalidate: false,
  lastUpdate: false,
  data: {},
  intercomSettings: {},
};

export default function (state = initialState, action) {
  switch (action.type) {
    case actionTypes.INVALIDATE_WHO_AM_I:
      return _.extend({}, state, { didInvalidate: true });
    case actionTypes.REQUEST_WHO_AM_I:
      return _.extend({}, state, {
        didInvalidate: false,
        isFetching: true,
      });
    case actionTypes.RECEIVE_WHO_AM_I:
      return _.extend({}, state, {
        didInvalidate: false,
        isFetching: false,
        lastUpdate: action.lastUpdate,
        data: action.data,
      });
    case actionTypes.REQUEST_INTERCOM_SETTINGS:
      return state;
    case actionTypes.RECEIVE_INTERCOM_SETTINGS:
      return _.extend({}, state, {
        intercomSettings: action.data,
      });
    default:
      return state;
  }
}
