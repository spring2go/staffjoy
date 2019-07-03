import moment from 'moment';
import 'moment-timezone';
import _ from 'lodash';

import {
  ENV_NAME_DEVELOPMENT,
  ENV_NAME_UAT,
  ENV_NAME_PRODUCTION,
  getRefetchInterval,
  DAY_NAME_LETTER_MAP,
} from 'constants/config';

import {
  UAT_APEX,
  PRODUCTION_APEX,
} from 'constants/paths';

import {
  MILISECONDS_TO_SECONDS,
  DAYS_OF_WEEK,
  WEEK_LENGTH,
} from 'constants/constants';

export function hexToRGBAString(hexCode, alpha) {
  /*
    input: string hex code, alpha value
    return: a string of an rgba function
  */
  const hex = hexCode.replace('#', '');
  const r = parseInt(hex.substring(0, 2), 16);
  const g = parseInt(hex.substring(2, 4), 16);
  const b = parseInt(hex.substring(4, 6), 16);

  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

export function timestampExpired(timestamp, endpoint = 'DEFAULT') {
  /*
    input: timestamp and a (str) endpoint name
    output: true if the timestamp has elapsed longer than the endpoint allows
  */
  const timeDiff = (Date.now() - timestamp) / MILISECONDS_TO_SECONDS;
  return timeDiff > getRefetchInterval(endpoint);
}

export function emptyPromise(val = null) {
  /* creates an empty promise for cases when data doesn't need to be fetched */
  return new Promise((resolve) => { resolve(val); });
}

export function getFirstDayOfWeek(weekStartsOn, momentDay) {
  /*
    - inputs: (str) weekStartsOn - a day of the week, lowercase
              (moment object) momentDay - the day of the particular week
    - return: a new moment object for the start of the week
  */

  // must supply a valid day
  if (!_.has(DAYS_OF_WEEK, weekStartsOn)) {
    return false;
  }

  // must give a valid moment object
  if (!moment.isMoment(momentDay)) {
    return false;
  }

  const weekStartIndex = DAYS_OF_WEEK[weekStartsOn];
  const adjustment = ((WEEK_LENGTH - weekStartIndex) + momentDay.day())
    % WEEK_LENGTH;

  return momentDay.clone().subtract(adjustment, 'days');
}

export function normalizeToMidnight(momentObj) {
  /*
    input: a moment object
    output: moment object of midnight of same day

    notes: naive to timezones
  */

  return momentObj
    .hour(0)
    .minute(0)
    .second(0)
    .millisecond(0);
}

export function isoDatetimeToDate(isoString) {
  /*
    - takes a ISO8601 datetime string and returns just the calendar date
    - naive to timezones
  */

  return normalizeToMidnight(moment(isoString))
    .format('YYYY-MM-DD');
}

export function localStorageAvailable() {
  /* tests if localStorage is available via a functional test */

  const test = 'test';
  try {
    localStorage.setItem(test, test);
    localStorage.removeItem(test);
    return true;
  } catch (e) {
    return false;
  }
}

export function arrayToObjectBucket(list) {
  const result = {};

  _.forEach(list, (value) => {
    result[value] = [];
  });

  return result;
}

export function formatPhoneNumber(phoneNumber) {
  return phoneNumber;
}

export function detectEnvironment() {
  let env = ENV_NAME_DEVELOPMENT;
  const url = window.location.href.toLowerCase();
  const domain = url.split('/')[2];

  if (domain.endsWith(PRODUCTION_APEX)) {
    env = ENV_NAME_PRODUCTION;
  } else if (domain.endsWith(UAT_APEX)) {
    env = ENV_NAME_UAT;
  }

  return env;
}

export function getFormattedDuration(miliseconds) {
  const result = [];
  const duration = moment.duration(miliseconds);
  const hours = duration.hours() + (duration.days() * 24);
  const minutes = duration.minutes();

  if (hours !== 0) {
    result.push(`${hours} hr`);
  }

  // add minutes if they exist, or if the times are the same
  if (minutes !== 0 || result.length === 0) {
    result.push(`${minutes} m`);
  }

  return result.join(' ');
}

export function formattedDifferenceFromMoment(startMoment, stopMoment) {
  // both inputs must be moment objects
  if (!moment.isMoment(startMoment) || !moment.isMoment(stopMoment)) {
    return false;
  }

  return getFormattedDuration(stopMoment - startMoment);
}

export function getHoursFromMeridiem(meridiem) {
  return (meridiem.toLowerCase() === 'pm') ? 12 : 0;
}

export function getLetterFromDayName(dayName) {
  return _.get(DAY_NAME_LETTER_MAP, dayName.toLowerCase(), '');
}

export function checkStatus(response) {
  if (response.status >= 200 && response.status < 300) {
    return response;
  }

  const error = new Error(response.statusText);
  error.response = response;
  throw error;
}

export function checkCode(data) {
  if (data.code === 'SUCCESS') {
    return data;
  }

  const error = new Error(data.message);
  error.data = data;
  throw error;
}

export function parseJSON(response) {
  return response.json();
}

export function saveToLocal(data, prefix) {
  // see if anything needs to go into local storage
  if (!_.isEmpty(data)) {
    if (localStorageAvailable()) {
      const teamId = _.get(data, 'teamId', '');
      const filterData = _.get(data, 'data', {});
      const localStorageBaseId = `${prefix}-${teamId}`;

      // localStorage does not support nested objects
      // so give everything a unique key
      _.forEach(filterData, (value, key) => {
        localStorage.setItem(`${localStorageBaseId}-${key}`, value);
      });
    }
  }
}
