import _ from 'lodash';
import 'whatwg-fetch';
import { normalize, Schema, arrayOf } from 'normalizr';
import { getTeams, createTeamEmployee } from './teams';
import { getAssociations } from './associations';
import * as actionTypes from '../constants/actionTypes';
import * as fieldUpdateStatus from '../constants/fieldUpdateStatus';
import { routeToMicroservice } from '../constants/paths';
import {
  emptyPromise,
  timestampExpired,
  checkStatus,
  checkCode,
  parseJSON,
} from '../utility';

/*
  Exported functions:
  * getEmployees
  * getEmployee
  * editEmployee
  * initializeEmployees
  * initializeEmployeeSidePanel
  * updateEmployeesSearchFilter
*/

// schemas!
const employeesSchema = new Schema('employees', { idAttribute: 'userId' });
const arrayOfEmployees = arrayOf(employeesSchema);

// employees

function requestEmployees() {
  return {
    type: actionTypes.REQUEST_EMPLOYEES,
  };
}

function receiveEmployees(data) {
  return {
    type: actionTypes.RECEIVE_EMPLOYEES,
    ...data,
  };
}

function requestEmployee() {
  return {
    type: actionTypes.REQUEST_EMPLOYEE,
  };
}

function receiveEmployee(data) {
  return {
    type: actionTypes.RECEIVE_EMPLOYEE,
    ...data,
  };
}

// state will update once an employeeId is available
function creatingEmployee() {
  return {
    type: actionTypes.CREATING_EMPLOYEE,
  };
}

function createdEmployee(data) {
  return {
    type: actionTypes.CREATED_EMPLOYEE,
    ...data,
  };
}

function updatingEmployee(data) {
  return {
    type: actionTypes.UPDATING_EMPLOYEE,
    ...data,
  };
}

function updatingEmployeeField(data) {
  return {
    type: actionTypes.UPDATING_EMPLOYEE_FIELD,
    ...data,
  };
}

function updatedEmployee(data) {
  return {
    type: actionTypes.UPDATED_EMPLOYEE,
    ...data,
  };
}

function fetchEmployees(companyId) {
  return (dispatch) => {
    // dispatch action to start the fetch
    dispatch(requestEmployees());
    const directoryPath = '/v1/company/directory/list';

    return fetch(
      routeToMicroservice('company', directoryPath, { companyId }),
      { credentials: 'include' })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        const normalized = normalize(
          _.get(data.directoryList, 'accounts', []),
          arrayOfEmployees
        );

        return dispatch(receiveEmployees({
          data: normalized.entities.employees,
          order: normalized.result,
          lastUpdate: Date.now(),
        }));
      });
  };
}

function fetchEmployee(companyId, employeeId) {
  return (dispatch) => {
    // dispatch action to start the fetch
    dispatch(requestEmployee());
    const directoryPath =
      '/v1/company/directory/get';

    return fetch(
      // eslint-disable-next-line max-len
      routeToMicroservice('company', directoryPath, { companyId, userId: employeeId }),
      { credentials: 'include' })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        const normalized = normalize(data.directoryEntry, employeesSchema);

        return dispatch(receiveEmployee({
          data: normalized.entities.employees,
        }));
      });
  };
}

function shouldFetchEmployees(state) {
  const employeesState = state.employees;
  const employeesData = employeesState.data;

  // it has never been fetched
  if (_.isEmpty(employeesData)) {
    return true;

  // it's currently being fetched
  } else if (employeesState.isFetching) {
    return false;

  // it's been in the UI for more than the allowed threshold
  } else if (!employeesState.lastUpdate ||
    (timestampExpired(employeesState.lastUpdate, 'EMPLOYEES'))
  ) {
    return true;

  // make sure we have a complete collection too
  } else if (!employeesState.completeSet) {
    return true;
  }

  // otherwise, fetch if it's been invalidated
  return employeesState.didInvalidate;
}

function shouldFetchEmployee(state, employeeId) {
  const employeesState = state.employees;
  const employeesData = employeesState.data;

  // no employee has ever been fetched
  if (_.isEmpty(employeesData)) {
    return true;

  // the needed employeeId is not available
  } else if (!_.has(employeesData, employeeId)) {
    return true;

  // the collection has been in the UI for more than the allowed threshold
  } else if (_.has(employeesData, employeeId) &&
    (timestampExpired(employeesData.lastUpdate, 'EMPLOYEES'))
  ) {
    return true;
  }

  // otherwise, fetch if it's been invalidated
  return employeesState.didInvalidate;
}

// employee filters

function setFilters(filters) {
  return {
    type: actionTypes.SET_EMPLOYEE_FILTERS,
    data: filters,
  };
}

function initializeFilters() {
  return dispatch =>
    dispatch(setFilters({
      searchQuery: '',
      limitTeam: {},
      status: {},
    }));
}

export function updateEmployeesSearchFilter(query) {
  return setFilters({ searchQuery: query });
}

export function getEmployees(companyId) {
  return (dispatch, getState) => {
    if (shouldFetchEmployees(getState())) {
      return dispatch(fetchEmployees(companyId));
    }
    return emptyPromise();
  };
}

export function getEmployee(companyId, employeeId) {
  return (dispatch, getState) => {
    if (shouldFetchEmployee(getState())) {
      return dispatch(fetchEmployee(companyId, employeeId));
    }
    return emptyPromise();
  };
}

export function createEmployee(companyId, employeeData, callback) {
  return (dispatch) => {
    dispatch(creatingEmployee());
    const directoryPath = '/v1/company/directory/create';
    const newEmployee = employeeData;
    newEmployee.companyId = companyId;

    return fetch(
      routeToMicroservice('company', directoryPath),
      {
        credentials: 'include',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newEmployee),
      })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        const dEntry = data.directoryEntry;
        if (callback) {
          callback(dEntry);
        }

        const normalized = normalize(dEntry, employeesSchema);
        return dispatch(createdEmployee({
          data: normalized.entities.employees,
        }));
      });
  };
}

export function updateEmployee(companyId, employeeId, newData, callback) {
  return (dispatch, getState) => {
    const employeeData = _.get(getState().employees.data, employeeId, {});
    const updateData = _.extend({}, employeeData, newData);
    updateData.companyId = companyId;
    dispatch(updatingEmployee({ data: { [employeeId]: updateData } }));

    const directoryPath =
      '/v1/company/directory/update';

    return fetch(
      routeToMicroservice('company', directoryPath),
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
        const dEntry = data.directoryEntry;
        const normalized = normalize(dEntry, employeesSchema);

        if (callback) {
          callback.call(null, dEntry, null);
        }

        return dispatch(updatedEmployee({
          data: normalized.entities.employees,
        }));
      });
  };
}

export function createEmployeeFromForm(companyId) {
  return (dispatch, getState) => {
    const { values } = getState().form['create-employee'];
    const teams = _.pickBy(values.teams);

    // a name and a team must be selected
    if (_.has(values, 'full_name') && !_.isEmpty(teams)) {
      const payload = {
        name: values.full_name,
      };

      // will add email and phone number if they exist
      if (_.has(values, 'email')) {
        payload.email = values.email;
      }

      if (_.has(values, 'phoneNumber')) {
        payload.phoneNumber = values.phoneNumber;
      }

      payload.companyId = companyId;

      dispatch(createEmployee(companyId, payload, (response) => {
        _.forEach(teams, (value, keyId) => {
          dispatch(
            createTeamEmployee(companyId, keyId, response.userId)
          );
        });
      }));
    }
  };
}

export function updateEmployeeField(companyId, employeeId, fieldName) {
  return (dispatch, getState) => {
    const { values } = getState().form['employee-side-panel'];
    const value = values[fieldName];
    dispatch(updatingEmployeeField({
      data: { [employeeId]: { [fieldName]: fieldUpdateStatus.UPDATING } },
    }));

    return dispatch(
      updateEmployee(
        companyId,
        employeeId,
        { [fieldName]: value },
        (response, error) => {
          if (!error) {
            dispatch(updatingEmployeeField({
              data: {
                [employeeId]: { [fieldName]: fieldUpdateStatus.SUCCESS },
              },
            }));
          }
        },
      )
    );
  };
}

function initialTableFetches(companyId) {
  return dispatch => Promise.all([
    dispatch(getTeams(companyId)),
    dispatch(getEmployees(companyId)),
    dispatch(getAssociations(companyId)),
  ]);
}

export function initializeEmployees(companyId) {
  return (dispatch) => {
    dispatch(initializeFilters());
    return dispatch(initialTableFetches(companyId));
  };
}

function initialEmployeeSidePanelFetches(companyId, employeeId) {
  return dispatch => Promise.all([
    dispatch(getTeams(companyId)),
    dispatch(getEmployee(companyId, employeeId)),
  ]);
}

export function initializeEmployeeSidePanel(companyId, employeeId) {
  return (dispatch) => {
    dispatch(initialEmployeeSidePanelFetches(companyId, employeeId));
  };
}
