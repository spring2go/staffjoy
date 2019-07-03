import _ from 'lodash';
import 'whatwg-fetch';
import * as actionTypes from '../constants/actionTypes';
import { routeToMicroservice } from '../constants/paths';
import {
  emptyPromise,
  timestampExpired,
  checkStatus,
  parseJSON,
  checkCode,
} from '../utility';

/*
  Exported functions:
  * getAssociations
  * invalidateAssociations
*/

// action creators

function requestAssociations() {
  return {
    type: actionTypes.REQUEST_ASSOCIATIONS,
  };
}

function receiveAssociations(data) {
  return {
    type: actionTypes.RECEIVE_ASSOCIATIONS,
    ...data,
  };
}

function fetchAssociations(companyId) {
  return (dispatch) => {
    // dispatch action to start the fetch
    dispatch(requestAssociations());
    const associationPath = '/v1/company/directory/get_associations';

    return fetch(
      routeToMicroservice('company', associationPath, { companyId }),
      { credentials: 'include' })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then((data) => {
        const result = {};
        _.forEach(_.get(data.associationList, 'accounts', []), (account) => {
          result[account.account.userId] = {
            teams: _.get(account, 'teams', []),
          };
        });

        return dispatch(receiveAssociations({
          data: result,
          lastUpdate: Date.now(),
        }));
      });
  };
}

function shouldFetchAssociations(state) {
  const associationsState = state.associations;
  const associationsData = associationsState.data;

  // it has never been fetched
  if (_.isEmpty(associationsData)) {
    return true;

  // it's currently being fetched
  } else if (associationsState.isFetching) {
    return false;

  // it's been in the UI for more than the allowed threshold
  } else if (!associationsState.lastUpdate ||
    (timestampExpired(associationsState.lastUpdate, 'ASSOCIATIONS'))
  ) {
    return true;
  }

  // otherwise, fetch if it's been invalidated
  return associationsState.didInvalidate;
}

export function getAssociations(companyId) {
  return (dispatch, getState) => {
    if (shouldFetchAssociations(getState())) {
      return dispatch(fetchAssociations(companyId));
    }
    return emptyPromise();
  };
}

export function invalidateAssociations() {
  return {
    type: actionTypes.INVALIDATE_ASSOCIATIONS,
  };
}
