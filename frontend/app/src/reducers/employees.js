import _ from 'lodash';
import * as actionTypes from 'constants/actionTypes';

const initialState = {
  isFetching: false,
  didInvalidate: false,
  completeSet: false,
  lastUpdate: false,
  data: {},
  order: [],
  filters: {},
  updatingFields: {},
};

export default function (state = initialState, action) {
  switch (action.type) {
    case actionTypes.INVALIDATE_EMPLOYEES:
      return _.extend({}, state, {
        didInvalidate: true,
        completeSet: false,
      });

    case actionTypes.REQUEST_EMPLOYEES:
      return _.extend({}, state, {
        didInvalidate: false,
        completeSet: false,
        isFetching: true,
      });

    case actionTypes.RECEIVE_EMPLOYEES:
      return _.extend({}, state, {
        didInvalidate: false,
        isFetching: false,
        completeSet: true,
        lastUpdate: action.lastUpdate,
        data: action.data,
        order: action.order,
      });

    case actionTypes.REQUEST_EMPLOYEE:
      return state;

    case actionTypes.RECEIVE_EMPLOYEE:
      return _.extend({}, state, {
        data: _.extend({}, state.data, action.data),
      });

    case actionTypes.SET_EMPLOYEE_FILTERS:
      return _.extend({}, state, {
        filters: _.extend({}, state.filters, action.data),
      });

    case actionTypes.CREATING_EMPLOYEE:
      return state;

    case actionTypes.CREATED_EMPLOYEE:
      return _.extend({}, state, {
        data: _.extend({}, state.data, action.data),
      });

    case actionTypes.UPDATING_EMPLOYEE:
      return _.extend({}, state, {
        data: _.extend({}, state.data, action.data),
      });

    case actionTypes.UPDATING_EMPLOYEE_FIELD:
      return _.extend({}, state, {
        updatingFields: _.merge({}, state.updatingFields, action.data),
      });

    case actionTypes.UPDATED_EMPLOYEE:
      return _.extend({}, state, {
        data: _.extend({}, state.data, action.data),
      });

    default:
      return state;
  }
}
