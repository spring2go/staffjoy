import _ from 'lodash';
import 'whatwg-fetch';
import * as actionTypes from '../constants/actionTypes';
import { routeToMicroservice } from '../constants/paths';
import {
  emptyPromise,
  timestampExpired,
  checkStatus,
  checkCode,
  parseJSON,
} from '../utility';

import { getWhoAmI } from './whoami';

function shouldFetchUser(state) {
  const userState = state.user;
  const userData = userState.data;

  // it has never been fetched
  if (_.isEmpty(userData)) {
    return true;

  // it's currently being fetched
  } else if (userState.isFetching) {
    return false;

  // it's been in the UI for more than the allowed threshold
  } else if (!userState.lastUpdate ||
    (timestampExpired(userState.lastUpdate, 'USER'))
  ) {
    return true;
  }

  // otherwise, fetch if it's been invalidated
  return userState.didInvalidate;
}

function requestUser() {
  return {
    type: actionTypes.REQUEST_USER,
  };
}

function receiveUser(data) {
  return {
    type: actionTypes.RECEIVE_USER,
    lastUpdate: Date.now(),
    ...data,
  };
}

function fetchUser(userId) {
  return (dispatch) => {
    dispatch(requestUser());

    // eslint-disable-next-line max-len
    return fetch(routeToMicroservice('account', '/v1/account/get', { userId }), {
      credentials: 'include',
    })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then(data =>
        dispatch(receiveUser({
          data: data.account,
          lastUpdate: Date.now(),
        }))
      );
  };
}

export function getUser() {
  return (dispatch, getState) => {
    // whoami data is required
    dispatch(getWhoAmI()).then(() => {
      const state = getState();
      const userId = state.whoami.data.userId;

      if (shouldFetchUser(state)) {
        return dispatch(fetchUser(userId));
      }

      return emptyPromise();
    });
  };
}
