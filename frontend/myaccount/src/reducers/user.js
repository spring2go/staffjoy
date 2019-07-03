import _ from 'lodash';
import * as actionTypes from '../constants/actionTypes';

const initialState = {
  isFetching: false,
  didInvalidate: false,
  lastUpdate: false,
  data: {},
};

export default function (state = initialState, action) {
  switch (action.type) {
    case actionTypes.INVALIDATE_USER:
      return _.extend({}, state, {
        didInvalidate: true,
      });

    case actionTypes.REQUEST_USER:
      return _.extend({}, state, {
        didInvalidate: false,
        isFetching: true,
      });

    case actionTypes.RECEIVE_USER:
      return _.extend({}, state, {
        didInvalidate: false,
        isFetching: false,
        lastUpdate: action.lastUpdate,
        data: action.data,
      });

    case actionTypes.UPDATING_USER:
      return _.extend({}, state, {
        data: _.extend({}, state.data, action.data),
      });

    case actionTypes.UPDATED_USER:
      return _.extend({}, state, {
        lastUpdate: action.lastUpdate,
        data: _.extend({}, state.data, action.data),
      });

    case actionTypes.UPDATING_PASSWORD:
      return state;

    case actionTypes.UPDATED_PASSWORD:
      return state;

    case actionTypes.UPDATING_PHOTO:
      return state;

    case actionTypes.UPDATED_PHOTO:
      return _.extend({}, state, {
        data: _.extend({}, state.data, action.data),
        lastUpdate: action.lastUpdate,
      });

    default:
      return state;
  }
}
