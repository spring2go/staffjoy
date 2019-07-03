import _ from 'lodash';
import 'whatwg-fetch';
import { normalize, Schema, arrayOf } from 'normalizr';
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
  * getTeams
  * getTeam
*/

// schemas!
const teamSchema = new Schema('teams', { idAttribute: 'id' });
const arrayOfTeams = arrayOf(teamSchema);

// teams

function requestTeams() {
  return {
    type: actionTypes.REQUEST_TEAMS,
  };
}

function receiveTeams(data) {
  return {
    type: actionTypes.RECEIVE_TEAMS,
    ...data,
  };
}

function fetchTeams(companyId) {
  return (dispatch) => {
    // dispatch action to start the fetch
    dispatch(requestTeams());

    return fetch(
      routeToMicroservice('company', '/v1/company/team/list', { companyId }),
      { credentials: 'include' })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        // eslint-disable-next-line max-len
        const normalized = normalize(_.get(data.teamList, 'teams', []), arrayOfTeams);

        return dispatch(receiveTeams({
          data: normalized.entities.teams,
          order: normalized.result,
          lastUpdate: Date.now(),
        }));
      });
  };
}

function shouldFetchTeams(state) {
  const teamsState = state.teams;
  const teamsData = teamsState.data;

  // it has never been fetched
  if (_.isEmpty(teamsData)) {
    return true;

  // it's currently being fetched
  } else if (teamsState.isFetching) {
    return false;

  // it's been in the UI for more than the allowed threshold
  } else if (!teamsState.lastUpdate ||
    (timestampExpired(teamsState.lastUpdate, 'TEAMS'))
  ) {
    return true;

  // make sure we have a complete collection too
  } else if (!teamsState.completeSet) {
    return true;
  }

  // otherwise, fetch if it's been invalidated
  return teamsState.didInvalidate;
}

// determines if should fetch teams or extract from current state
export function getTeams(companyId) {
  return (dispatch, getState) => {
    if (shouldFetchTeams(getState())) {
      return dispatch(fetchTeams(companyId));
    }
    return emptyPromise();
  };
}

// team

function requestTeam() {
  return {
    type: actionTypes.REQUEST_TEAM,
  };
}

function receiveTeam(data) {
  return {
    type: actionTypes.RECEIVE_TEAM,
    ...data,
  };
}

function fetchTeam(companyId, teamId) {
  return (dispatch) => {
    // dispatch action to start the fetch
    dispatch(requestTeam());
    const teamPath = '/v1/company/team/get';

    return fetch(
      routeToMicroservice('company', teamPath, { companyId, teamId }),
      { credentials: 'include' })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        const normalized = normalize(data.team, teamSchema);

        return dispatch(receiveTeam({
          data: normalized.entities.teams,
        }));
      });
  };
}

function shouldFetchTeam(state, teamId) {
  const teamsState = state.teams;
  const teamsData = teamsState.data;

  // no team has ever been fetched
  if (_.isEmpty(teamsData)) {
    return true;

  // the needed teamId is not available
  } else if (!_.has(teamsData, teamId)) {
    return true;

  // it's been in the UI for more than the allowed threshold
  } else if (!teamsState.lastUpdate ||
    (timestampExpired(teamsState.lastUpdate, 'TEAMS'))
  ) {
    return true;
  }

  // otherwise, fetch if it's been invalidated
  return teamsState.didInvalidate;
}

export function getTeam(companyId, teamId) {
  return (dispatch, getState) => {
    if (shouldFetchTeam(getState(), teamId)) {
      return dispatch(fetchTeam(companyId, teamId));
    }
    return emptyPromise();
  };
}

export {
  getTeamJobs,
  updateTeamJob,
  updateTeamJobField,
  setTeamJob,
  createTeamJob,
} from './jobs';
export { getTeamEmployees, createTeamEmployee } from './employees';
export {
  getTeamShifts,
  updateTeamShift,
  bulkUpdateTeamShifts,
  deleteTeamShift,
  createTeamShift,
} from './shifts';
