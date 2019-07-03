import * as actionTypes from '../constants/actionTypes';

import {
  getTeamJobs,
  getTeam,
} from './teams';

function initialFetches(companyId, teamId) {
  return (dispatch) => {
    Promise.all([
      dispatch(getTeam(companyId, teamId)),
      dispatch(getTeamJobs(companyId, teamId)),
    ]);
  };
}

export function initializeSettings(
  companyId,
  teamId,
) {
  return (dispatch) => {
    // use promise to guarantee that current team is available in state
    dispatch(initialFetches(companyId, teamId));
  };
}

export function setColorPicker(colorPicker) {
  return {
    type: actionTypes.SET_COLOR_PICKER,
    colorPicker,
  };
}

export function setFilters(filters) {
  return {
    type: actionTypes.SET_SETTINGS_FILTERS,
    filters,
  };
}

export function setNewTeamJob(data) {
  return {
    type: actionTypes.SET_SETTINGS_NEW_TEAM_JOB,
    data,
  };
}
