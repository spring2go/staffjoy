import _ from 'lodash';
import 'whatwg-fetch';
import { normalize, Schema, arrayOf } from 'normalizr';
import { invalidateAssociations } from '../associations';
import * as actionTypes from '../../constants/actionTypes';
import { routeToMicroservice } from '../../constants/paths';
import {
  emptyPromise,
  timestampExpired,
  checkStatus,
  parseJSON,
  checkCode,
} from '../../utility';

/*
  Exported functions:
  * getTeamEmployees
  * createTeamEmployee
*/

// schemas!
const teamEmployeesSchema = new Schema(
  'employees',
  { idAttribute: 'userId' }
);
const arrayOfTeamEmployees = arrayOf(teamEmployeesSchema);

// team employees

function requestTeamEmployees(teamId) {
  return {
    type: actionTypes.REQUEST_TEAM_EMPLOYEES,
    teamId,
  };
}

function receiveTeamEmployees(teamId, data) {
  return {
    type: actionTypes.RECEIVE_TEAM_EMPLOYEES,
    teamId,
    ...data,
  };
}

function creatingTeamEmployee(teamId) {
  return {
    type: actionTypes.CREATING_TEAM_EMPLOYEE,
    teamId,
  };
}

function createdTeamEmployee(teamId, userId, data) {
  return {
    type: actionTypes.CREATED_TEAM_EMPLOYEE,
    teamId,
    userId,
    ...data,
  };
}

function fetchTeamEmployees(companyId, teamId) {
  return (dispatch) => {
    // dispatch action to start the fetch
    dispatch(requestTeamEmployees(teamId));
    const teamEmployeePath =
      '/v1/company/worker/list';

    return fetch(
      routeToMicroservice('company', teamEmployeePath, { companyId, teamId }),
      { credentials: 'include' })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        const normalized = normalize(
          _.get(data.workerEntries, 'workers', []),
          arrayOfTeamEmployees
        );

        return dispatch(receiveTeamEmployees(teamId, {
          data: normalized.entities.employees,
          order: normalized.result,
          lastUpdate: Date.now(),
        }));
      });
  };
}

function shouldFetchTeamEmployees(state, teamId) {
  const employeesData = state.teams.employees;
  const teamEmployees = _.get(employeesData, teamId, {});

  // no team employees have ever been fetched
  if (_.isEmpty(employeesData)) {
    return true;

  // the needed teamId is empty
  } else if (_.isEmpty(teamEmployees)) {
    return true;

  // teamEmployees is at least partially populated with a trusted object at this point
  // the order of these is related to how the 1st fetch might play out

  // this data set is currently being fetched
  } else if (teamEmployees.isFetching) {
    return false;

  // this data set is not complete
  } else if (!teamEmployees.completeSet) {
    return true;

  // this data set is stale
  } else if (!teamEmployees.lastUpdate ||
    (timestampExpired(teamEmployees.lastUpdate, 'TEAM_EMPLOYEES'))
  ) {
    return true;
  }

  // check if invalidated
  return teamEmployees.didInvalidate;
}

export function getTeamEmployees(companyId, teamId) {
  return (dispatch, getState) => {
    if (shouldFetchTeamEmployees(getState(), teamId)) {
      return dispatch(fetchTeamEmployees(companyId, teamId));
    }
    return emptyPromise();
  };
}

export function createTeamEmployee(companyId, teamId, userId) {
  return (dispatch) => {
    dispatch(creatingTeamEmployee(teamId));
    const workerPath = '/v1/company/worker/create';

    return fetch(
      routeToMicroservice('company', workerPath), {
        credentials: 'include',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ companyId, teamId, userId }),
      })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        const dEntry = data.directoryEntry;
        dispatch(invalidateAssociations());
        return dispatch(createdTeamEmployee(teamId, dEntry.userId, {
          data: dEntry,
        }));
      });
  };
}
