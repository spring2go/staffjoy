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
  Exported Actions:
  * getShifts
  * createTeamShift
  * updateTeamShift
  * deleteTeamShift
*/

// schema!
const shiftSchema = new Schema('shifts', { idAttribute: 'id' });
const arrayOfShifts = arrayOf(shiftSchema);

// shifts
function requestTeamShifts(teamId, params) {
  return {
    type: actionTypes.REQUEST_TEAM_SHIFTS,
    teamId,
    params,
  };
}

function receiveTeamShifts(teamId, data) {
  return {
    type: actionTypes.RECEIVE_TEAM_SHIFTS,
    teamId,
    ...data,
  };
}

// state will update once a shiftId is available
function creatingTeamShift(teamId) {
  return {
    type: actionTypes.CREATING_TEAM_SHIFT,
    teamId,
  };
}

function createdTeamShift(teamId, shiftId, data) {
  return {
    type: actionTypes.CREATED_TEAM_SHIFT,
    teamId,
    shiftId,
    ...data,
  };
}

// state will update with the response
function bulkUpdatingTeamShifts(teamId, params) {
  return {
    type: actionTypes.BULK_UPDATING_TEAM_SHIFTS,
    teamId,
    params,
  };
}

function bulkUpdatedTeamShifts(teamId, data) {
  return {
    type: actionTypes.BULK_UPDATED_TEAM_SHIFTS,
    teamId,
    ...data,
  };
}

// state will update before the request is made
function updatingTeamShift(teamId, shiftId, data) {
  return {
    type: actionTypes.UPDATING_TEAM_SHIFT,
    teamId,
    shiftId,
    ...data,
  };
}

function updatedTeamShift(teamId, shiftId, data) {
  return {
    type: actionTypes.UPDATED_TEAM_SHIFT,
    teamId,
    shiftId,
    ...data,
  };
}

function deletingTeamShift(teamId, shiftId) {
  return {
    type: actionTypes.DELETING_TEAM_SHIFT,
    teamId,
    shiftId,
  };
}

function deletedTeamShift(teamId, shiftId) {
  return {
    type: actionTypes.DELETED_TEAM_SHIFT,
    teamId,
    shiftId,
  };
}

function fetchTeamShifts(companyId, teamId, params) {
  return (dispatch) => {
    // dispatch action to start the fetch
    dispatch(requestTeamShifts(teamId, params));
    const shiftPath = '/v1/company/shift/list_shifts';
    const listShiftRequest = params;
    listShiftRequest.companyId = companyId;
    listShiftRequest.teamId = teamId;

    return fetch(
      routeToMicroservice('company', shiftPath),
      {
        credentials: 'include',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(listShiftRequest),
      })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        // eslint-disable-next-line max-len
        const normalized = normalize(_.get(data.shiftList, 'shifts', []), arrayOfShifts);

        return dispatch(receiveTeamShifts(teamId, {
          data: normalized.entities.shifts,
          order: normalized.result,
          lastUpdate: Date.now(),
        }));
      });
  };
}

function shouldFetchTeamShifts(state, teamId, params) {
  const shiftsData = state.teams.shifts;
  const teamShifts = _.get(shiftsData, teamId, {});

  // it has never been fetched before
  if (_.isEmpty(shiftsData)) {
    return true;

  // the needed teamId is empty
  } else if (_.isEmpty(teamShifts)) {
    return true;

  // teamShifts is at least partially populated with a trusted object at this point
  // the order of these is related to how the 1st fetch might play out

  // the params must be the same as last time
  } else if (!_.isEqual(shiftsData.params, params)) {
    return true;

  // this data set is currently being fetched
  } else if (teamShifts.isFetching) {
    return false;

  // this data set is not complete
  } else if (!teamShifts.completeSet) {
    return true;

  // this data set is stale
  } else if (!teamShifts.lastUpdate ||
    (timestampExpired(teamShifts.lastUpdate, 'TEAM_SHIFTS'))
  ) {
    return true;
  }

  // check if invalidated
  return teamShifts.didInvalidate;
}

export function getTeamShifts(companyId, teamId, params) {
  return (dispatch, getState) => {
    if (shouldFetchTeamShifts(getState(), teamId, params)) {
      return dispatch(fetchTeamShifts(companyId, teamId, params));
    }
    return emptyPromise();
  };
}

export function createTeamShift(companyId, teamId, shiftPayload) {
  return (dispatch) => {
    dispatch(creatingTeamShift(teamId));
    const shiftPath = '/v1/company/shift/create';
    const newShift = shiftPayload;
    newShift.companyId = companyId;
    newShift.teamId = teamId;

    return fetch(
      routeToMicroservice('company', shiftPath),
      {
        credentials: 'include',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newShift),
      })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        const shiftDto = data.shift;
        dispatch(createdTeamShift(teamId, shiftDto.id, {
          data: shiftDto,
        }));
      });
  };
}

export function updateTeamShift(companyId, teamId, shiftId, newData) {
  return (dispatch, getState) => {
    const shifts = _.get(getState().teams.shifts, teamId, {});
    const shift = _.get(shifts.data, shiftId, {});
    const updateData = _.extend({}, shift, newData);
    updateData.companyId = companyId;
    updateData.teamId = teamId;
    updateData.id = shiftId;
    dispatch(updatingTeamShift(teamId, shiftId, { data: updateData }));

    const shiftPath =
      '/v1/company/shift/update';

    return fetch(
      routeToMicroservice('company', shiftPath),
      {
        credentials: 'include',
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updateData),
      })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then(data =>
        dispatch(updatedTeamShift(teamId, shiftId, {
          data: data.shift,
        }))
      );
  };
}

export function bulkUpdateTeamShifts(companyId, teamId, putBody) {
  return (dispatch) => {
    const butsRequest = putBody;
    butsRequest.companyId = companyId;
    butsRequest.teamId = teamId;
    dispatch(bulkUpdatingTeamShifts(teamId, butsRequest));

    const shiftPath = '/v1/company/shift/bulk_publish';

    return fetch(
      routeToMicroservice('company', shiftPath),
      {
        credentials: 'include',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(butsRequest),
      })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        // eslint-disable-next-line max-len
        const normalized = normalize(_.get(data.shiftList, 'shifts', []), arrayOfShifts);

        return dispatch(bulkUpdatedTeamShifts(teamId, {
          data: normalized.entities.shifts,
        }));
      });
  };
}

export function deleteTeamShift(companyId, teamId, shiftId) {
  return (dispatch) => {
    dispatch(deletingTeamShift(teamId, shiftId));

    const shiftPath =
      '/v1/company/shift/delete';

    return fetch(
      routeToMicroservice('company', shiftPath, { shiftId, teamId, companyId }),
      {
        credentials: 'include',
        method: 'DELETE',
      })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then(() =>
        dispatch(deletedTeamShift(teamId, shiftId))
      );
  };
}
