import _ from 'lodash';
import { detectEnvironment } from '../utility';

import {
  ENV_NAME_DEVELOPMENT,
  ENV_NAME_UAT,
  ENV_NAME_PRODUCTION,
} from './config';

// apex for the various staffjoy environments
export const DEVELOPMENT_APEX = '.staffjoy-v2.local';
export const UAT_APEX = '.staffjoy-uat.local';
export const PRODUCTION_APEX = '.staffjoy.com';

// http prefixes
export const HTTP_PREFIX = 'http://';
export const HTTPS_PREFIX = 'https://';

// externally routes are all defined by names
export const ROOT_PATH = 'ROOT_PATH';
export const COMPANY_BASE = 'COMPANY_BASE';
export const COMPANY_EMPLOYEES = 'COMPANY_EMPLOYEES';
export const COMPANY_EMPLOYEE = 'COMPANY_EMPLOYEE';
export const COMPANY_HISTORY = 'COMPANY_HISTORY';
export const TEAM_BASE = 'TEAM_BASE';
export const TEAM_SCHEDULING = 'TEAM_SCHEDULING';
export const TEAM_SETTINGS = 'TEAM_SETTINGS';
export const TEAM_SHIFT_BOARD = 'TEAM_SHIFT_BOARD';

// these internal variables are used for constructing routes
const rootPath = '/';
const companies = '/companies/';

// company level navigation
const companiesBase = `${companies}:companyId`;
const companyEmployees = `${companiesBase}/employees/`;
const companyEmployee = `${companyEmployees}:employeeId`;
const companyHistory = `${companiesBase}/history/`;

const companyTeams = `${companiesBase}/teams/`;

// team level navigation
const teamsBase = `${companyTeams}:teamId`;
const teamScheduling = `${teamsBase}/scheduling/`;
const teamSettings = `${teamsBase}/settings/`;
const teamShiftBoard = `${teamsBase}/shiftboard/`;

// this function will generate the proper path via the path name
export function getRoute(routeName, params = {}) {
  switch (routeName) {
    case ROOT_PATH:
      return rootPath;

    case COMPANY_BASE:
      if (_.has(params, 'companyId')) {
        return companies + params.companyId;
      }
      return companiesBase;

    case COMPANY_EMPLOYEES:
      if (_.has(params, 'companyId')) {
        return companyEmployees.replace(':companyId', params.companyId);
      }
      return companyEmployees;

    case COMPANY_EMPLOYEE:
      if (_.has(params, 'companyId') && _.has(params, 'employeeId')) {
        return companyEmployee
          .replace(':companyId', params.companyId)
          .replace(':employeeId', params.employeeId);
      }
      return companyEmployee;

    case COMPANY_HISTORY:
      if (_.has(params, 'companyId')) {
        return companyHistory.replace(':companyId', params.companyId);
      }
      return companyHistory;

    case TEAM_BASE:
      if (_.has(params, 'companyId') && _.has(params, 'teamId')) {
        return teamsBase
          .replace(':companyId', params.companyId)
          .replace(':teamId', params.teamId);
      }
      return teamsBase;

    case TEAM_SCHEDULING:
      if (_.has(params, 'companyId') && _.has(params, 'teamId')) {
        return teamScheduling
          .replace(':companyId', params.companyId)
          .replace(':teamId', params.teamId);
      }
      return teamScheduling;

    case TEAM_SETTINGS:
      if (_.has(params, 'companyId') && _.has(params, 'teamId')) {
        return teamSettings
          .replace(':companyId', params.companyId)
          .replace(':teamId', params.teamId);
      }
      return teamSettings;

    case TEAM_SHIFT_BOARD:
      if (_.has(params, 'companyId') && _.has(params, 'teamId')) {
        return teamShiftBoard
          .replace(':companyId', params.companyId)
          .replace(':teamId', params.teamId);
      }
      return teamShiftBoard;

    default:
      return rootPath;
  }
}

export function routeToMicroservice(service, path = '', urlParams = {}) {
  const devRoute = `${HTTP_PREFIX}${service}${DEVELOPMENT_APEX}${path}`;
  let fullPath = '';

  switch (detectEnvironment()) {
    case ENV_NAME_DEVELOPMENT:
      fullPath = devRoute;
      break;

    case ENV_NAME_UAT: // use HTTP for demo
      fullPath = `${HTTP_PREFIX}${service}${UAT_APEX}${path}`;
      break;

    case ENV_NAME_PRODUCTION:
      fullPath = `${HTTPS_PREFIX}${service}${PRODUCTION_APEX}${path}`;
      break;

    default:
      fullPath = devRoute;
      break;
  }

  if (!_.isEmpty(urlParams)) {
    fullPath += '?';
    let index = 0;
    _.forEach(urlParams, (value, key) => {
      if (index > 0) {
        fullPath += '&';
      }
      fullPath += `${key}=${value}`;
      index += 1;
    });
  }

  return fullPath;
}
