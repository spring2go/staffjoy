import _ from 'lodash';
import * as actionTypes from 'constants/actionTypes';

const initialState = {
  isFetching: false,
  didInvalidate: false,
  completeSet: false,
  lastUpdate: false,
  data: {},
  order: [],
  employees: {},
  jobs: {},
  shifts: {},
};

function hasNestedProperty(state, nestedProperty) {
  return _.has(state, nestedProperty) && _.isPlainObject(state[nestedProperty]);
}

function extendNestedCollection(state, nestedProperty, id, newData) {
  /*
    extends nested data for you because it gets kind of messy
  */

  // state must have the nestedProperty to work with, and it must be an object
  if (!hasNestedProperty(state, nestedProperty)) {
    return false;
  }

  // create the new object that is nested
  // e.g. state.employees.4 is what needs to be updated
  const extendedIdData = _.extend({},
    _.get(state[nestedProperty], id, {}),
    newData
  );

  // create copy of state that will be returned
  const newState = _.extend({}, state);

  // set the new nested data object into it's place in the hierarchy
  newState[nestedProperty][id] = extendedIdData;
  return newState;
}

function extendNestedModel(
  state,
  nestedProperty,
  propertyId,
  modelId,
  newData
) {
  // state must have the nestedProperty to work with, and it must be an object
  if (!hasNestedProperty(state, nestedProperty)) {
    return false;
  }

  const newState = _.extend({}, state);
  const nestedPropertyState = _.get(newState[nestedProperty], propertyId, {});
  const nestedCollection = _.get(nestedPropertyState, 'data', {});

  // extend the model in nestedCollection
  nestedCollection[modelId] = _.extend({},
    _.get(nestedCollection, modelId, {}),
    newData
  );


  const extendedNestedCollection = _.extend({}, nestedCollection);

  // set the updated nested collection into nestedPropertyState
  nestedPropertyState.data = extendedNestedCollection;

  // finally update new state
  newState[nestedProperty][propertyId] = nestedPropertyState;
  return newState;
}

function removeNestedModel(state, nestedProperty, propertyId, modelId) {
  // state must have the nestedProperty to work with, and it must be an object
  if (!hasNestedProperty(state, nestedProperty)) {
    return false;
  }

  const newState = _.extend({}, state);
  const nestedPropertyState = _.get(newState[nestedProperty], propertyId, {});
  let nestedCollection = _.get(nestedPropertyState, 'data', {});

  // remove the model from the nestedCollection
  nestedCollection = _.omit(_.extend({}, nestedCollection), modelId);

  // set the updated nested collection into nestedPropertyState
  nestedPropertyState.data = nestedCollection;

  // finally update new state
  newState[nestedProperty][propertyId] = nestedPropertyState;
  return newState;
}

export default function (state = initialState, action) {
  switch (action.type) {
    case actionTypes.INVALIDATE_TEAMS:
      return _.extend({}, state, {
        didInvalidate: true,
        completeSet: false,
      });

    case actionTypes.REQUEST_TEAMS:
      return _.extend({}, state, {
        didInvalidate: false,
        completeSet: false,
        isFetching: true,
      });

    case actionTypes.RECEIVE_TEAMS:
      return _.extend({}, state, {
        didInvalidate: false,
        isFetching: false,
        completeSet: true,
        lastUpdate: action.lastUpdate,
        data: action.data,
        order: action.order,
      });

    case actionTypes.REQUEST_TEAM:
      return state;

    case actionTypes.RECEIVE_TEAM:
      return _.extend({}, state, {
        data: _.extend({}, state.data, action.data),
      });

    case actionTypes.REQUEST_TEAM_EMPLOYEES:
      return extendNestedCollection(state, 'employees', action.teamId, {
        isFetching: true,
        didInvalidate: false,
        completeSet: false,
      });

    case actionTypes.RECEIVE_TEAM_EMPLOYEES:
      return extendNestedCollection(state, 'employees', action.teamId, {
        didInvalidate: false,
        isFetching: false,
        completeSet: true,
        lastUpdate: action.lastUpdate,
        data: action.data,
        order: action.order,
      });

    case actionTypes.INVALIDATE_TEAM_EMPLOYEES:
      return extendNestedCollection(state, 'employees', action.teamId, {
        didInvalidate: true,
        completeSet: false,
      });

    case actionTypes.CREATING_TEAM_EMPLOYEE:
      return state;

    case actionTypes.CREATED_TEAM_EMPLOYEE:
      return extendNestedModel(
        state,
        'employees',
        action.teamId,
        action.userId,
        action.data,
      );

    case actionTypes.REQUEST_TEAM_JOBS:
      return extendNestedCollection(state, 'jobs', action.teamId, {
        isFetching: true,
        didInvalidate: false,
        completeSet: false,
      });

    case actionTypes.RECEIVE_TEAM_JOBS:
      return extendNestedCollection(state, 'jobs', action.teamId, {
        didInvalidate: false,
        isFetching: false,
        completeSet: true,
        lastUpdate: action.lastUpdate,
        data: action.data,
        order: action.order,
      });

    case actionTypes.UPDATING_TEAM_JOB:
      return extendNestedModel(
        state,
        'jobs',
        action.teamId,
        action.jobId,
        action.data,
      );

    case actionTypes.UPDATED_TEAM_JOB:
      return extendNestedModel(
        state,
        'jobs',
        action.teamId,
        action.jobId,
        action.data,
      );

    case actionTypes.SET_TEAM_JOB:
      return extendNestedModel(
        state,
        'jobs',
        action.teamId,
        action.jobId,
        action.data,
      );

    case actionTypes.CREATING_TEAM_JOB:
      return state;

    case actionTypes.CREATED_TEAM_JOB:
      return extendNestedModel(
        state,
        'jobs',
        action.teamId,
        action.jobId,
        action.data,
      );

    case actionTypes.INVALIDATE_TEAM_JOBS:
      return extendNestedCollection(state, 'jobs', action.teamId, {
        didInvalidate: true,
        completeSet: false,
      });

    case actionTypes.REQUEST_TEAM_SHIFTS:
      return extendNestedCollection(state, 'shifts', action.teamId, {
        isFetching: true,
        didInvalidate: false,
        completeSet: false,
        params: action.params,
      });

    case actionTypes.RECEIVE_TEAM_SHIFTS:
      return extendNestedCollection(state, 'shifts', action.teamId, {
        didInvalidate: false,
        isFetching: false,
        completeSet: true,
        lastUpdate: action.lastUpdate,
        data: action.data,
        order: action.order,
      });

    case actionTypes.INVALIDATE_TEAM_SHIFTS:
      return extendNestedCollection(state, 'shifts', action.teamId, {
        didInvalidate: true,
        completeSet: false,
      });

    case actionTypes.CREATING_TEAM_SHIFT:
      return state;

    case actionTypes.CREATED_TEAM_SHIFT:
      return extendNestedModel(
        state,
        'shifts',
        action.teamId,
        action.shiftId,
        action.data,
      );

    case actionTypes.BULK_UPDATING_TEAM_SHIFTS:
      return extendNestedCollection(state, 'shifts', action.teamId, {
        isSaving: true,
      });

    case actionTypes.BULK_UPDATED_TEAM_SHIFTS:
      return extendNestedCollection(state, 'shifts', action.teamId, {
        data: action.data,
        isSaving: false,
      });

    case actionTypes.UPDATING_TEAM_SHIFT:
      return extendNestedModel(
        state,
        'shifts',
        action.teamId,
        action.shiftId,
        action.data,
      );

    case actionTypes.UPDATED_TEAM_SHIFT:
      return extendNestedModel(
        state,
        'shifts',
        action.teamId,
        action.shiftId,
        action.data,
      );

    // do not remove model from state until confirmed by API
    case actionTypes.DELETING_TEAM_SHIFT:
      return state;

    case actionTypes.DELETED_TEAM_SHIFT:
      return removeNestedModel(
        state,
        'shifts',
        action.teamId,
        action.shiftId
      );

    default:
      return state;
  }
}
