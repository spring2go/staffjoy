import _ from 'lodash';

// environment names
export const ENV_NAME_DEVELOPMENT = 'DEV';
export const ENV_NAME_UAT = 'UAT';
export const ENV_NAME_PRODUCTION = 'PROD';

/*eslint-disable */
export const SENTRY_PRODUCTION_KEY = 'https://c037c826f1864e28a674dcba23350b4b@sentry.io/106472';
export const SENTRY_UAT_KEY = 'https://6d2bfa8c878d4905b2537647032f61c8@sentry.io/106473';
/*eslint-enable */

// apex for the various staffjoy environments
export const HTTP_PREFIX = 'http://';
export const HTTPS_PREFIX = 'https://';
export const DEVELOPMENT_APEX = '.staffjoy-v2.local';
export const UAT_APEX = '.staffjoy-uat.local';
export const PRODUCTION_APEX = '.staffjoy.com';

const DEFAULT_REFETCH_INTERVAL = 10;

const REFETCH_INTERVALS = {
  USER: 30,
  WHOAMI: 30,
  DEFAULT: DEFAULT_REFETCH_INTERVAL,
};

export function getRefetchInterval(endpoint) {
  return _.get(REFETCH_INTERVALS, endpoint, DEFAULT_REFETCH_INTERVAL);
}

export const MOMENT_MONTH_YEAR_FORMAT = 'MMMM YYYY';
