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
  * getTeamJobs
*/

// schemas!
const teamJobsSchema = new Schema('jobs', { idAttribute: 'id' });
const arrayOfTeamJobs = arrayOf(teamJobsSchema);

// team jobs

function requestTeamJobs(teamId) {
  return {
    type: actionTypes.REQUEST_TEAM_JOBS,
    teamId,
  };
}

function receiveTeamJobs(teamId, data) {
  return {
    type: actionTypes.RECEIVE_TEAM_JOBS,
    teamId,
    ...data,
  };
}

function fetchTeamJobs(companyId, teamId) {
  return (dispatch) => {
    // dispatch action to start the fetch
    dispatch(requestTeamJobs(teamId));
    const jobPath = '/v1/company/job/list';

    return fetch(
      routeToMicroservice('company', jobPath, { companyId, teamId }),
      { credentials: 'include' })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        // eslint-disable-next-line max-len
        const normalized = normalize(_.get(data.jobList, 'jobs', []), arrayOfTeamJobs);

        return dispatch(receiveTeamJobs(teamId, {
          data: normalized.entities.jobs,
          order: normalized.result,
          lastUpdate: Date.now(),
        }));
      });
  };
}

function shouldFetchTeamJobs(state, teamId) {
  const jobsData = state.teams.jobs;
  const teamJobs = _.get(jobsData, teamId, {});

  // no team employees have ever been fetched
  if (_.isEmpty(jobsData)) {
    return true;

  // the needed teamId is empty
  } else if (_.isEmpty(teamJobs)) {
    return true;

  // teamJobs is at least partially populated with a trusted object at this point
  // the order of these is related to how the 1st fetch might play out

  // this data set is currently being fetched
  } else if (teamJobs.isFetching) {
    return false;

  // this data set is not complete
  } else if (!teamJobs.completeSet) {
    return true;

  // this data set is stale
  } else if (!teamJobs.lastUpdate ||
    (timestampExpired(teamJobs.lastUpdate, 'TEAM_JOBS'))
  ) {
    return true;
  }

  // check if invalidated
  return teamJobs.didInvalidate;
}

export function getTeamJobs(companyId, teamId) {
  return (dispatch, getState) => {
    if (shouldFetchTeamJobs(getState(), teamId)) {
      return dispatch(fetchTeamJobs(companyId, teamId));
    }
    return emptyPromise();
  };
}

export function setTeamJob(teamId, jobId, data) {
  return {
    type: actionTypes.SET_TEAM_JOB,
    teamId,
    jobId,
    data,
  };
}

function updatingTeamJob(teamId, jobId, data) {
  return {
    type: actionTypes.UPDATING_TEAM_JOB,
    teamId,
    jobId,
    data,
  };
}

function updatedTeamJob(teamId, jobId, data) {
  return {
    type: actionTypes.UPDATED_TEAM_JOB,
    teamId,
    jobId,
    data,
  };
}

function updatingTeamJobField(jobId) {
  return {
    type: actionTypes.UPDATING_TEAM_JOB_FIELD,
    jobId,
  };
}

function updatedTeamJobField(jobId) {
  return {
    type: actionTypes.UPDATED_TEAM_JOB_FIELD,
    jobId,
  };
}

function hideTeamJobFieldSuccess(jobId) {
  return {
    type: actionTypes.HIDE_TEAM_JOB_FIELD_SUCCESS,
    jobId,
  };
}

export function updateTeamJob(
  companyId,
  teamId,
  jobId,
  newData,
  callback
) {
  return (dispatch, getState) => {
    const jobs = _.get(getState().teams.jobs, teamId, {});
    const job = _.get(jobs.data, jobId, {});
    const updateData = _.extend({}, job, newData);
    updateData.companyId = companyId;
    updateData.teamId = teamId;
    updateData.id = jobId;
    dispatch(updatingTeamJob(teamId, jobId, newData));

    const jobPath =
      '/v1/company/job/update';

    return fetch(
      routeToMicroservice('company', jobPath),
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
      .then((data) => {
        const jobData = data.job;
        if (callback) {
          callback.call(null, jobData, null);
        }

        dispatch(updatedTeamJob(teamId, jobId, jobData));
      });
  };
}

export function updateTeamJobField(companyId, teamId, jobId, newData) {
  return (dispatch) => {
    dispatch(updatingTeamJobField(jobId));

    return dispatch(
      updateTeamJob(
        companyId,
        teamId,
        jobId,
        newData,
        (response, error) => {
          if (!error) {
            dispatch(updatedTeamJobField(jobId));
            setTimeout(() => {
              dispatch(hideTeamJobFieldSuccess(jobId));
            }, 1000);
          }
        }
      )
    );
  };
}

function creatingTeamJob(teamId) {
  return {
    type: actionTypes.CREATING_TEAM_JOB,
    teamId,
  };
}

function createdTeamJob(teamId, jobId, data) {
  return {
    type: actionTypes.CREATED_TEAM_JOB,
    teamId,
    jobId,
    data,
  };
}

export function createTeamJob(companyId, teamId, jobPayload) {
  return (dispatch) => {
    dispatch(creatingTeamJob());
    const jobPath = '/v1/company/job/create';
    const newJob = jobPayload;
    newJob.companyId = companyId;
    newJob.teamId = teamId;

    return fetch(
      routeToMicroservice('company', jobPath),
      {
        credentials: 'include',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newJob),
      })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        const jobData = data.job;
        dispatch(createdTeamJob(teamId, jobData.id, jobData));

        setTimeout(() => {
          dispatch(hideTeamJobFieldSuccess(jobData.id));
        }, 1000);
      });
  };
}
