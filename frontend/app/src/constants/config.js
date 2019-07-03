import _ from 'lodash';

// environment names
export const ENV_NAME_DEVELOPMENT = 'DEV';
export const ENV_NAME_UAT = 'UAT';
export const ENV_NAME_PRODUCTION = 'PROD';

/*eslint-disable */
export const SENTRY_PRODUCTION_KEY = 'https://c037c826f1864e28a674dcba23350b4b@sentry.io/106472';
export const SENTRY_UAT_KEY = 'https://6d2bfa8c878d4905b2537647032f61c8@sentry.io/106473';
/*eslint-enable */

// these constants are configuration settings in the application
export const NO_TRANSPARENCY = 1;
export const DEFAULT_TRANSPARENCY = 0.3;

export const VIEW_SIZES = {
  week: 7,
  day: 1,
};

const DEFAULT_REFETCH_INTERVAL = 10;

// in seconds
const REFETCH_INTERVALS = {
  ASSOCIATIONS: 60,
  WHOAMI: 30,
  COMPANY: 60,
  EMPLOYEES: 60,
  TEAMS: 60,
  TEAM_EMPLOYEES: 60,
  TEAM_JOBS: 60,
  TEAM_SHIFTS: 10,
  DEFAULT: DEFAULT_REFETCH_INTERVAL,
};

export function getRefetchInterval(endpoint) {
  return _.get(REFETCH_INTERVALS, endpoint, DEFAULT_REFETCH_INTERVAL);
}

// moment date object displays
export const MOMENT_DAY_DATE_DISPLAY = 'ddd M/D';
export const MOMENT_DATE_DISPLAY = 'M/D';
export const MOMENT_ISO_DATE = 'YYYY-MM-DD';
export const MOMENT_CALENDAR_START_DISPLAY = 'MMM D';
export const MOMENT_CALENDAR_END_DISPLAY = 'MMM D, YYYY';
export const MOMENT_CALENDAR_TIME_DISPLAY = 'h:mm A z';
export const MOMENT_SHIFT_CARD_TIMES = 'h:mm a';
export const API_TIME_FORMAT = 'YYYY-MM-DDTHH:mm:ss';

export const SCHEDULING_VIEW_BY_OPTIONS = [
  {
    id: 'employee',
    name: 'Employee',
  },
  {
    id: 'job',
    name: 'Job',
  },
];

export const UNASSIGNED_SHIFT_NAME = 'Unassigned Shift';

export const DAY_NAME_LETTER_MAP = {
  monday: 'M',
  tuesday: 'T',
  wednesday: 'W',
  thursday: 'Th',
  friday: 'F',
  saturday: 'Sa',
  sunday: 'Su',
};
