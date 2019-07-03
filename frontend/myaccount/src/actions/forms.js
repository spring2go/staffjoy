import * as actionTypes from '../constants/actionTypes';

export function setFormResponse(form, data) {
  return {
    type: actionTypes.FORM_CHANGE,
    form,
    data,
  };
}
