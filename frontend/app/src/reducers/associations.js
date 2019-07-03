import _ from 'lodash';
import * as actionTypes from 'constants/actionTypes';

const initialState = {
  isFetching: false,
  didInvalidate: false,
  lastUpdate: false,
  data: {},
};

export default function (state = initialState, action) {
  switch (action.type) {
    case actionTypes.INVALIDATE_ASSOCIATIONS:
      return _.extend({}, state, {
        didInvalidate: true,
      });

    case actionTypes.REQUEST_ASSOCIATIONS:
      return _.extend({}, state, {
        didInvalidate: false,
        isFetching: true,
      });

    case actionTypes.RECEIVE_ASSOCIATIONS:
      return _.extend({}, state, {
        didInvalidate: false,
        isFetching: false,
        lastUpdate: action.lastUpdate,
        data: action.data,
      });

    default:
      return state;
  }
}
