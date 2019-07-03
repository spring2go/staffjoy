import moment from 'moment';
import 'moment-timezone';
import _ from 'lodash';

import * as actionTypes from '../constants/actionTypes';

import {
  getTeam,
  getTeamEmployees,
  getTeamJobs,
  getTeamShifts,
  createTeamShift,
  bulkUpdateTeamShifts,
  updateTeamShift,
} from './teams';

import {
  isoDatetimeToDate,
  getFirstDayOfWeek,
  localStorageAvailable,
  getHoursFromMeridiem,
  saveToLocal,
} from '../utility';

import { VIEW_SIZES, MOMENT_ISO_DATE } from '../constants/config';
import { UNASSIGNED_SHIFTS } from '../constants/constants';

// scheduling
// filters

function setFilters(filters) {
  return {
    type: actionTypes.SET_SCHEDULING_FILTERS,
    data: filters,
  };
}

function initializeFilters(teamId) {
  return (dispatch) => {
    // attempt to retreive a value from local storage, otherwise use a default
    const localStorageViewById = `teamScheduling-${teamId}-viewBy`;
    const localObj = localStorageAvailable() ? localStorage : {};
    const viewBy = _.get(localObj, localStorageViewById, 'employee');

    return dispatch(setFilters({
      limit: {},
      searchQuery: '',
      sortBy: 'alphabetical',
      viewBy,
    }));
  };
}

// params

function setParameters(startDate, viewType, range) {
  // TODO figure out how to have viewType and startDate added to route

  return {
    type: actionTypes.SET_SCHEDULING_PARAMS,
    data: {
      range,
      startDate,
      viewType,
    },
  };
}

function getRangeParams(startDate, viewType, timezone) {
  /*
    returns an object with start and stop params as utc moment objects

    note: currently does not support month
    */

  // make sure viewType is known
  if (!_.has(VIEW_SIZES, viewType)) {
    return false;
  }

  const startMomentLocal = moment.tz(startDate, timezone);
  const stopMomentLocal = startMomentLocal
    .clone()
    .add(VIEW_SIZES[viewType], 'days');

  return {
    start: startMomentLocal.utc(),
    stop: stopMomentLocal.utc(),
  };
}

function getInitialParameters(routeQuery, team) {
  const viewType = 'week';
  let startDate = _.get(routeQuery, 'start');

  if (_.isUndefined(startDate) || !moment(startDate).isValid()) {
    startDate = getFirstDayOfWeek(team.dayWeekStarts, moment());
  }

  // prune date to be a string e.g. 2016-09-27
  startDate = isoDatetimeToDate(startDate);

  const range = getRangeParams(startDate, viewType, team.timezone);

  return {
    startDate,
    viewType,
    range,
  };
}

function createQueryParams(stateParams) {
  /*
    translates state.scheduling.params into API params
  */

  const { range } = stateParams;

  return {
    shiftStartAfter: range.start.format(),
    shiftStartBefore: range.stop.format(),
  };
}

function initialFetches(companyId, teamId) {
  return dispatch => Promise.all([
    dispatch(getTeam(companyId, teamId)),
    dispatch(getTeamEmployees(companyId, teamId)),
    dispatch(getTeamJobs(companyId, teamId)),
  ]);
}

/*
  Exported Actions:
    * initializeScheduling
    * stepDateRange
    * changeCalendarView
    * updateSchedulingSearchFilter
    * changeViewBy
    * droppedSchedulingCard
*/

// initialization

export function initializeScheduling(
  companyId,
  teamId,
  routeQuery
) {
  return (dispatch, getState) => {
    // use promise to guarantee that current team is available in state
    dispatch(initialFetches(companyId, teamId)).then(() => {
      const state = getState();
      const team = state.teams.data[teamId];

      // get initial parameters so they can be used and dispatched
      const initialParams = getInitialParameters(routeQuery, team);

      // set parameters and filters to state
      dispatch(setParameters(
        initialParams.startDate,
        initialParams.viewType,
        initialParams.range,
      ));
      dispatch(initializeFilters(teamId));

      // params have been put into state, translate for API
      const queryParams = createQueryParams(initialParams);

      return dispatch(getTeamShifts(companyId, teamId, queryParams));
    });
  };
}

// parameter

export function stepDateRange(companyId, teamId, direction) {
  return (dispatch, getState) => {
    // probably available in state, but just make sure team data is available
    dispatch(
      initialFetches(companyId, teamId)
    ).then(() => {
      const currentState = getState();
      const team = currentState.teams.data[teamId];
      let currentParams = currentState.scheduling.params;

      // highly unlikely the params haevn't been initialized yet
      // but this will give a chance for recovery
      if (_.isEmpty(currentParams)) {
        currentParams = getInitialParameters({}, team);

        // set parameters and filters to state
        dispatch(setParameters(
          currentParams.startDate,
          currentParams.viewType,
          currentParams.range,
        ));
      }

      // make sure it was a valid direction
      if (['left', 'right'].indexOf(direction) < 0) {
        return false;
      }

      const viewType = currentParams.viewType;
      const numDirection = (direction === 'left') ? -1 : 1;
      const momentDate = moment(currentParams.startDate);

      switch (viewType) {
        case 'day':
          momentDate.add(numDirection, 'days');
          break;

        case 'week':
          momentDate.add(numDirection * 7, 'days');
          break;

        case 'month':
          momentDate.add(numDirection, 'months');
          break;

        default:
          break;
      }

      // prune date to be a string e.g. 2016-09-27
      const startDate = isoDatetimeToDate(momentDate);

      // set params that contain the params for the shifts
      const range = getRangeParams(startDate, viewType, team.timezone);
      dispatch(setParameters(startDate, viewType, range));

      // params have been put into state, translate for API
      const queryParams = createQueryParams({
        startDate,
        viewType,
        range,
      });

      // now get the new shifts
      return dispatch(getTeamShifts(companyId, teamId, queryParams));
    });
  };
}

export function changeCalendarView(companyId, teamId, newView) {
  return (dispatch, getState) => {
    // probably available in state, but just make sure team data is available
    dispatch(
      initialFetches(companyId, teamId)
    ).then(() => {
      const currentState = getState();
      const team = currentState.teams.data[teamId];
      let currentParams = currentState.scheduling.params;

      // highly unlikely the params haevn't been initialized yet
      // but this will give a chance for recovery
      if (_.isEmpty(currentParams)) {
        currentParams = getInitialParameters({}, team);

        // set parameters and filters to state
        dispatch(setParameters(
          currentParams.startDate,
          currentParams.viewType,
          currentParams.range,
        ));
      }

      // make sure its a valid view type
      if (['week', 'day'].indexOf(newView) < 0) {
        return false;
      }

      // do nothing if it's the same view
      if (newView === currentParams.viewType) {
        return true;
      }

      let momentDate = moment(currentParams.startDate);

      switch (newView) {
        // sets to the current day of the week
        // will be today if on the current week
        case 'day':
          momentDate.day(moment().day());
          break;

        // goes to whatever week you are on
        case 'week':
          momentDate = getFirstDayOfWeek(team.dayWeekStarts, momentDate);
          break;

        default:
          break;
      }

      // prune date to be a string e.g. 2016-09-27
      const startDate = isoDatetimeToDate(momentDate);

      // set params that contain the params for the shifts
      const range = getRangeParams(startDate, newView, team.timezone);
      dispatch(setParameters(startDate, newView, range));

      // params have been put into state, translate for API
      const queryParams = createQueryParams({
        startDate,
        viewType: newView,
        range,
      });

      // now get the new shifts
      return dispatch(getTeamShifts(companyId, teamId, queryParams));
    });
  };
}

// view filters

export function updateSchedulingSearchFilter(query) {
  return setFilters({ searchQuery: query });
}

export function changeViewBy(viewBy, teamId) {
  saveToLocal({
    teamId,
    data: {
      viewBy,
    },
  }, 'teamScheduling');

  return setFilters({ viewBy });
}

// drag interactions

export function droppedSchedulingCard(
  companyId,
  teamId,
  shiftId,
  oldColumnId,
  sectionId,
  newColumnId
) {
  /*
    Here we go, this ones a doozy

    Concepts:
    * companyId, teamId - tell route for patching
    * shiftId - tells us everything we want to know about
        the current state of the shift
    * sectionId - tells us the row that the shift was just dropped into
    * shiftColumnId - tells us the column that the shift was just dropped into
    * thunk dispatch + getState tell us everything else needed
      * viewBy - whether the sectionId is an employee or a a role

    Strategy:
    * Let react-dnd (the drag and drop library) do it's thing and then fire
        into our own action creator (this guy here)
    * This function can study the inputs, and determine the appropriate changes
        for the shift
    * It will then dispatch a shift patch event, and the table will recognize
        the new state
  */
  return (dispatch, getState) => {
    let attributeId = sectionId;
    const state = getState();
    const { viewBy } = state.scheduling.filters;
    const shift = state.teams.shifts[teamId].data[shiftId];
    const team = state.teams.data[teamId];
    const { timezone } = team;
    const momentOldCol = moment(oldColumnId);
    const momentNewCol = moment(newColumnId);
    const daysAdjustment = moment.duration(momentNewCol - momentOldCol).days();
    let attribute = '';

    // extract shift data for adjustments
    const { start, stop } = shift;
    const newStart = moment.utc(start).tz(timezone)
                                      .add(daysAdjustment, 'days')
                                      .utc()
                                      .format();
    const newStop = moment.utc(stop).tz(timezone)
                                    .add(daysAdjustment, 'days')
                                    .utc()
                                    .format();

    // use view by to determine whether sectionId refers to employee or role
    if (viewBy === 'employee') {
      attribute = 'userId';
    } else if (viewBy === 'job') {
      attribute = 'jobId';
    }

    // unassigned shifts need to be set to empty string
    if (sectionId === UNASSIGNED_SHIFTS) {
      attributeId = '';
    }

    return dispatch(
      updateTeamShift(
        companyId,
        teamId,
        shiftId,
        _.extend({}, shift, {
          [attribute]: attributeId,
          start: newStart,
          stop: newStop,
        })
      )
    );
  };
}

export function editTeamShiftFromModal(
  companyId,
  teamId,
  shiftId,
  timezone
) {
  return (dispatch, getState) => {
    const state = getState();
    const formData = state.scheduling.modal.formData;
    const shiftData = state.teams.shifts[teamId].data[shiftId];

    const momentStart = moment.utc(shiftData.start).tz(timezone);
    const momentStop = moment.utc(shiftData.stop).tz(timezone);

    // modify for start
    if (_.get(formData, 'startHour', '') !== '') {
      momentStart.hour(
        parseInt(formData.startHour, 10) +
        getHoursFromMeridiem(formData.startMeridiem)
      );
    }

    if (_.get(formData, 'startMinute', '') !== '') {
      momentStart.minute(parseInt(formData.startMinute, 10));
    }

    // modify for stop
    if (_.get(formData, 'stopHour', '') !== '') {
      momentStop.hour(
        parseInt(formData.stopHour, 10) +
        getHoursFromMeridiem(formData.stopMeridiem)
      );
    }

    if (_.get(formData, 'stopMinute', '') !== '') {
      momentStop.minute(parseInt(formData.stopMinute, 10));
    }

    return dispatch(
      updateTeamShift(
        companyId,
        teamId,
        shiftId,
        _.extend({}, shiftData, {
          start: momentStart.utc().format(),
          stop: momentStop.utc().format(),
        })
      )
    );
  };
}

export function createTeamShiftsFromModal(companyId, teamId, timezone) {
  return (dispatch, getState) => {
    const state = getState();
    const shifts = _.values(state.teams.shifts[teamId].data);
    const allShiftsPublished = !_.some(shifts, x => !x.published);
    const formData = state.scheduling.modal.formData;
    const selectedDays = _.pickBy(formData.selectedDays);
    const selectedEmployees = _.pickBy(formData.selectedEmployees);
    const defaultPublishState = (allShiftsPublished && shifts.length > 0);
    const selectedJob = formData.selectedJob;

    _.forEach(selectedDays, (value, key) => {
      // create a time on this date
      const momentStart = moment.tz(key, MOMENT_ISO_DATE, timezone);
      const momentStop = moment.tz(key, MOMENT_ISO_DATE, timezone);

      // modify for start
      momentStart
      .hour(
        parseInt(formData.startHour, 10) +
        getHoursFromMeridiem(formData.startMeridiem)
      )
      .minute(parseInt(formData.startMinute, 10));

      // modify for stop
      momentStop
      .hour(
        parseInt(formData.stopHour, 10) +
        getHoursFromMeridiem(formData.stopMeridiem)
      )
      .minute(parseInt(formData.stopMinute, 10));

      // loop through employees and create for each
      _.forEach(selectedEmployees, (employeeVal, employeeKey) => {
        const requestPayload = {
          start: momentStart.utc().format(),
          stop: momentStop.utc().format(),
          userId: employeeKey,
          published: defaultPublishState,
        };

        if (selectedJob !== undefined) {
          requestPayload.jobId = selectedJob;
        }

        dispatch(createTeamShift(companyId, teamId, requestPayload));
      });
    });
  };
}

export function publishTeamShifts(companyId, teamId) {
  return (dispatch, getState) => {
    const state = getState();
    const { params } = state.scheduling;
    const shifts = _.values(state.teams.shifts[teamId].data);

    // if all published - it will be false => unpublish all
    // if mixed - will be true => publish remaining ones
    // if all unpublished - will be true => publish all
    const published = _.some(shifts, x => !x.published);

    // get range params from query state
    const putParams = createQueryParams(params);
    putParams.published = published;

    return dispatch(bulkUpdateTeamShifts(companyId, teamId, putParams));
  };
}

export function toggleSchedulingModal(status) {
  return {
    type: actionTypes.TOGGLE_SCHEDULING_MODAL,
    status,
  };
}

export function updateSchedulingModalFormData(data) {
  return {
    type: actionTypes.UPDATE_SCHEDULING_MODAL_FORM_DATA,
    data,
  };
}

export function clearSchedulingModalFormData() {
  return {
    type: actionTypes.CLEAR_SCHEDULING_MODAL_FORM_DATA,
  };
}
