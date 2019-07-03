import _ from 'lodash';
import * as actionTypes from 'constants/actionTypes';

const initialState = {
  params: {
    isFetching: true,
  },
  filters: {
    isFetching: true,
  },
  modal: {
    modalOpen: false,
    formData: {},
  },
};

export default function (state = initialState, action) {
  switch (action.type) {
    case actionTypes.SET_SCHEDULING_PARAMS:
      return _.extend({}, state, {
        params: _.extend({}, state.params, {
          isFetching: false,
          ...action.data,
        }),
      });

    case actionTypes.SET_SCHEDULING_FILTERS:
      return _.extend({}, state, {
        filters: _.extend({}, state.filters, {
          isFetching: false,
          ...action.data,
        }),
      });

    case actionTypes.TOGGLE_SCHEDULING_MODAL:
      return _.extend({}, state, {
        modal: _.extend({}, state.modal, {
          modalOpen: action.status,
        }),
      });

    case actionTypes.UPDATE_SCHEDULING_MODAL_FORM_DATA:
      return _.extend({}, state, {
        modal: _.extend({}, state.modal, {
          formData: _.extend({}, state.modal.formData, action.data),
        }),
      });

    case actionTypes.CLEAR_SCHEDULING_MODAL_FORM_DATA:
      return _.extend({}, state, {
        modal: _.extend({}, state.modal, {
          formData: {},
        }),
      });

    default:
      return state;
  }
}
