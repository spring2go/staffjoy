import _ from 'lodash';
import 'whatwg-fetch';

// TODO when we have an API import request from 'request';
// import PhoneNumber from 'awesome-phonenumber';

import * as actionTypes from '../constants/actionTypes';
import { setFormResponse } from './forms';
import { getWhoAmI } from './whoami';
import {
  emptyPromise,
  timestampExpired,
  checkStatus,
  parseJSON,
  routeToMicroservice,
  checkCode,
} from '../utility';

// TODO delete this once we start fetching
const delay = ms => new Promise(resolve =>
  setTimeout(resolve, ms)
);

function updatingPassword() {
  return {
    type: actionTypes.UPDATING_PASSWORD,
  };
}

function updatedPassword() {
  return {
    type: actionTypes.UPDATED_PASSWORD,
  };
}

function updatePassword(userId, password) {
  return (dispatch) => {
    dispatch(updatingPassword());

    return fetch(routeToMicroservice(
      'account',
      '/v1/account/update_password'
    ), {
      credentials: 'include',
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        userId,
        password,
      }),
    })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then(() => {
        dispatch(setFormResponse('passwordUpdate', {
          type: 'success',
          message: 'Password updated!',
        }));
        return dispatch(updatedPassword());
      })
      .catch(() =>
        dispatch(setFormResponse('passwordUpdate', {
          type: 'danger',
          message: 'Passwords must be at least 6 characters long',
        }))
      );
  };
}

export function changePassword(newPassword) {
  return (dispatch, getState) =>
    dispatch(updatePassword(
      getState().whoami.data.userId,
      newPassword
    ));
}

function updatingPhoto() {
  return {
    type: actionTypes.UPDATING_PHOTO,
  };
}

function updatedPhoto(photoUrl) {
  return {
    type: actionTypes.UPDATED_PHOTO,
    data: {
      photoUrl,
    },
  };
}

function updatePhoto(userId, photoReference) {
  return (dispatch) => {
    dispatch(updatingPhoto());

    return delay(500).then(() => {
      const response = {
        data: {
          photoUrl: photoReference,
        },
      };

      return dispatch(updatedPhoto(response.data.photoUrl));
    });
  };
}

export function changePhoto(event) {
  const photoLocalLocation = event.target.value;

  return (dispatch, getState) =>
    dispatch(updatePhoto(getState().user.userId, photoLocalLocation));
}

// state will be update before the patch is made
function updatingUser(data) {
  return {
    type: actionTypes.UPDATING_USER,
    ...data,
  };
}

function updatedUser(data) {
  return {
    type: actionTypes.UPDATED_USER,
    ...data,
  };
}

function updateUser(userId, data) {
  return (dispatch, getState) => {
    const userData = getState().user.data;
    const originalEmail = userData.email;
    let successMessage = 'Success!';

    if (data.email !== originalEmail) {
      successMessage += ' Check your email for a confirmation link.';
    }

    dispatch(updatingUser({ data }));

    return fetch(routeToMicroservice('account', '/v1/account/update'), {
      credentials: 'include',
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(_.extend({}, userData, data)),
    })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((response) => {
        dispatch(setFormResponse('accountUpdate', {
          type: 'success',
          message: successMessage,
        }));
        return dispatch(updatedUser({
          data: response.account,
          lastUpdate: Date.now(),
        }));
      })
      .catch(() =>
        dispatch(setFormResponse('accountUpdate', {
          type: 'danger',
          message: 'Unable to save changes',
        }))
      );
  };
}

function requestUser() {
  return {
    type: actionTypes.REQUEST_USER,
  };
}

function receiveUser(data) {
  return {
    type: actionTypes.RECEIVE_USER,
    ...data,
  };
}

function fetchUser(userId) {
  return (dispatch) => {
    // dispatch action to start the fetch
    dispatch(requestUser());

    // eslint-disable-next-line max-len
    return fetch(routeToMicroservice('account', `/v1/account/get?userId=${userId}`), {
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

/*
  Exported funcitons:
  * initialize  // gets the userId and then calls getUser
  * getUser     // data for the user (needs a user id)
  * changeAccountData
  * modifyUserAttribute
*/

export function getUser(userId) {
  return (dispatch, getState) => {
    if (shouldFetchUser(getState())) {
      return dispatch(fetchUser(userId));
    }
    return emptyPromise();
  };
}

export function initialize() {
  return (dispatch, getState) => {
    dispatch(getWhoAmI()).then(() => {
      const userId = getState().whoami.data.userId;

      return dispatch(getUser(userId));
    });
  };
}

export function changeAccountData(email, name, phoneNumber) {
  // make API call to save the submitted changes
  return (dispatch, getState) =>
    dispatch(updateUser(getState().whoami.data.userId, {
      email,
      name,
      phoneNumber,
    }));
}

export function modifyUserAttribute(event) {
  const target = event.target;
  const inputType = target.getAttribute('type');
  const attribute = target.getAttribute('data-model-attribute');
  const payload = {};

  if (inputType === 'checkbox') {
    payload[attribute] = target.checked;
  } else {
    payload[attribute] = target.value;
  }

  return (dispatch, getState) => {
    dispatch(updateUser(getState().whoami.data.userId, payload));
  };
}
