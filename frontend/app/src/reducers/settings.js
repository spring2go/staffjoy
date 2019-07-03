import _ from 'lodash';
import * as actionTypes from 'constants/actionTypes';
import * as constants from 'constants/constants';

const initialState = {
  filters: {
    isFetching: true,
  },
  colorPicker: {
    jobIdVisible: null,
  },
  jobFieldsSaving: [],
  jobFieldsShowSuccess: [],
  newJob: constants.DEFAULT_NEW_JOB,
};

export default function (state = initialState, action) {
  let jobSavingIdx;
  let jobSuccessIdx;

  switch (action.type) {
    case actionTypes.SET_COLOR_PICKER:
      return _.extend({}, state, {
        colorPicker: action.colorPicker,
      });

    case actionTypes.SET_SETTINGS_FILTERS:
      return _.extend({}, state, {
        filters: action.filters,
      });

    case actionTypes.CREATING_TEAM_JOB:
      return _.extend({}, state, {
        jobFieldsSaving: state.jobFieldsSaving
                                .concat(constants.NEW_JOB_ID),
      });

    case actionTypes.CREATED_TEAM_JOB:
      jobSavingIdx = state.jobFieldsSaving
                                  .indexOf(constants.NEW_JOB_ID);

      return _.extend({}, state, {
        jobFieldsSaving: [
          ...state.jobFieldsSaving.slice(0, jobSavingIdx),
          ...state.jobFieldsSaving.slice(jobSavingIdx + 1),
        ],
        jobFieldsShowSuccess: state.jobFieldsShowSuccess
                                    .concat(action.data.id),
        newJob: constants.DEFAULT_NEW_JOB,
      });

    case actionTypes.UPDATING_TEAM_JOB_FIELD:
      return _.extend({}, state, {
        jobFieldsSaving: state.jobFieldsSaving.concat(action.jobId),
      });

    case actionTypes.UPDATED_TEAM_JOB_FIELD:
      jobSavingIdx = state.jobFieldsSaving.indexOf(action.jobId);

      return _.extend({}, state, {
        jobFieldsSaving: [
          ...state.jobFieldsSaving.slice(0, jobSavingIdx),
          ...state.jobFieldsSaving.slice(jobSavingIdx + 1),
        ],
        jobFieldsShowSuccess: state.jobFieldsShowSuccess.concat(action.jobId),
      });

    case actionTypes.HIDE_TEAM_JOB_FIELD_SUCCESS:
      jobSuccessIdx = state.jobFieldsShowSuccess.indexOf(action.jobId);

      return _.extend({}, state, {
        jobFieldsShowSuccess: [
          ...state.jobFieldsShowSuccess.slice(0, jobSuccessIdx),
          ...state.jobFieldsShowSuccess.slice(jobSuccessIdx + 1),
        ],
      });

    case actionTypes.SET_SETTINGS_NEW_TEAM_JOB:
      return _.extend({}, state, {
        newJob: _.extend({}, state.newJob, action.data),
      });

    default:
      return state;
  }
}
