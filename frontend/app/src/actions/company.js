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


function requestCompany() {
  return {
    type: actionTypes.REQUEST_COMPANY,
  };
}

function receiveCompany(data) {
  return {
    type: actionTypes.RECEIVE_COMPANY,
    ...data,
  };
}

function fetchCompany(companyId) {
  return (dispatch) => {
    // dispatch an action when the request is initiated
    dispatch(requestCompany());
    const companyPath = '/v1/company/get';

    // eslint-disable-next-line max-len
    return fetch(routeToMicroservice('company', companyPath, { company_id: companyId }), {
      credentials: 'include',
    })
      .then(checkStatus)
      .then(parseJSON)
      .then(checkCode)
      .then(data =>
        dispatch(receiveCompany({
          data: data.company,
        }))
      );
  };
}

function shouldFetchCompany(companyId, state) {
  const companyState = state.company;
  const companyData = companyState.data;

  // it has never been fetched
  if (_.isEmpty(companyData)) {
    return true;

  // it's a different company
  } else if (companyData.id !== companyId) {
    return true;

  // it's currently being fetched
  } else if (companyState.isFetching) {
    return false;

  // it's been in the UI for more than the allowed threshold
  } else if (!companyState.lastUpdate ||
    (timestampExpired(companyState.lastUpdate, 'COMPANY'))
  ) {
    return true;
  }

  // otherwise, fetch if it's been invalidated, I suppose
  return companyState.didInvalidate;
}

export function getCompany(companyId) {
  return (dispatch, getState) => {
    if (shouldFetchCompany(companyId, getState())) {
      return dispatch(fetchCompany(companyId));
    }
    return emptyPromise();
  };
}

// TODO - need 'changeCompany' action - invalidate all company related data to force a refetch
