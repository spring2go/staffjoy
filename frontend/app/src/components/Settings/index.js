import _ from 'lodash';
import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as actions from 'actions';
import * as constants from 'constants/constants';
import LoadingScreen from 'components/LoadingScreen';
import ConfirmationModal from 'components/ConfirmationModal';
import StaffjoyButton from 'components/StaffjoyButton';
import SearchField from 'components/SearchField';
import TeamJobs from './TeamJobs';

require('./settings.scss');

class Settings extends React.Component {
  constructor(props) {
    super(props);

    _.bindAll(
      this,
      'handleColorPickerChange',
      'handleJobColorClick',
      'handleSearchChange',
      'handleJobNameChange',
      'handleJobNameBlur',
      'handleJobNameKeyPress',
      'handleDeleteJobClick',
      'handleShowModalClick',
      'handleCancelModalClick',
      'handleNewJobNameChange',
      'handleNewJobNameBlur',
      'handleNewJobNameKeyPress',
      'handleAddNewJobClick',
      'handleNewJobDeleteIconClick',
    );
  }

  componentDidMount() {
    const {
      dispatch,
      companyId,
      teamId,
    } = this.props;

    // get the jobs
    dispatch(actions.initializeSettings(companyId, teamId));
  }

  componentWillReceiveProps(nextProps) {
    const {
      dispatch,
      teamId,
    } = this.props;

    // get the jobs
    if (teamId !== nextProps.teamId) {
      dispatch(
        actions.initializeSettings(nextProps.companyId, nextProps.teamId)
      );
    }
  }

  handleShowModalClick(jobId) {
    this.jobIdToDelete = jobId;
    this.modal.showModal();
  }

  handleCancelModalClick() {
    this.jobIdToDelete = null;
    this.modal.hideModal();
  }

  handleColorPickerChange({ hex, source }, jobId) {
    const {
      companyId,
      teamId,
    } = this.props;

    const color = hex.toUpperCase();

    this.props.updateTeamJob(companyId, teamId, jobId, { color });
  }

  handleJobColorClick(event, jobId) {
    const {
      colorPicker,
      setColorPicker,
    } = this.props;

    const jobIdVisible
      = jobId === colorPicker.jobIdVisible ? null : jobId;

    setColorPicker({
      jobIdVisible,
    });
  }

  handleSearchChange(event) {
    this.props.setFilters({ searchQuery: event.target.value });
  }

  handleJobNameChange(event, jobId) {
    const {
      teamId,
      setTeamJob,
    } = this.props;

    setTeamJob(
      teamId,
      jobId,
      { name: event.target.value }
    );
  }

  saveTeamJob(event, jobId) {
    const {
      companyId,
      teamId,
      updateTeamJobField,
      jobFieldsSaving,
    } = this.props;

    if (jobFieldsSaving.includes(jobId)) {
      return;
    }

    updateTeamJobField(
      companyId,
      teamId,
      jobId,
      { name: event.target.value },
    );
  }

  handleJobNameBlur(event, jobId) {
    this.saveTeamJob(event, jobId);
  }

  handleJobNameKeyPress(event, jobId) {
    if (event.key === 'Enter') {
      this.saveTeamJob(event, jobId);
    }
  }

  handleDeleteJobClick() {
    const {
      companyId,
      teamId,
      updateTeamJob,
    } = this.props;

    const {
      jobIdToDelete,
    } = this;

    this.modal.hideModal();

    updateTeamJob(
      companyId,
      teamId,
      jobIdToDelete,
      { archived: true },
    );

    this.jobIdToDelete = null;
  }

  handleNewJobNameChange(event) {
    const {
      setNewTeamJob,
    } = this.props;

    setNewTeamJob(
      { name: event.target.value },
    );
  }

  createNewJob(event) {
    const {
      companyId,
      teamId,
      jobFieldsSaving,
      createTeamJob,
    } = this.props;

    if (jobFieldsSaving.includes(constants.NEW_JOB_ID)) {
      return;
    }

    if (event.target.value !== '') {
      createTeamJob(
        companyId,
        teamId,
        {
          name: event.target.value,
          color: constants.DEFAULT_TEAM_JOB_COLOR,
        },
      );
    }
  }

  handleNewJobNameBlur(event) {
    this.createNewJob(event);
  }

  handleNewJobNameKeyPress(event) {
    if (event.key === 'Enter') {
      this.createNewJob(event);
    }
  }

  handleNewJobDeleteIconClick() {
    const {
      setNewTeamJob,
    } = this.props;

    setNewTeamJob(
      { isVisible: false }
    );
  }

  handleAddNewJobClick() {
    const {
      setNewTeamJob,
    } = this.props;

    setNewTeamJob(
      { isVisible: true },
    );
  }

  render() {
    const {
      team,
      jobs,
      colorPicker,
      filters,
      jobFieldsSaving,
      jobFieldsShowSuccess,
      newJob,
      isFetching,
    } = this.props;

    if (isFetching) {
      return (
        <LoadingScreen />
      );
    }

    return (
      <div className="settings-container">
        <div className="settings-header">
          <div className="settings-team-name">
            <span>{team.name}</span>
          </div>
          <div className="settings-tabs-container">
            <div>
              <span>Jobs</span>
            </div>
          </div>
        </div>
        <div className="jobs-table-scrolling-panel">
          <div className="jobs-table-container">
            <SearchField
              width={200}
              onChange={this.handleSearchChange}
            />
            <TeamJobs
              jobs={jobs}
              colorPicker={colorPicker}
              handleJobColorClick={this.handleJobColorClick}
              handleColorPickerChange={this.handleColorPickerChange}
              handleJobNameChange={this.handleJobNameChange}
              handleJobNameBlur={this.handleJobNameBlur}
              handleJobNameKeyPress={this.handleJobNameKeyPress}
              handleShowModalClick={this.handleShowModalClick}
              handleNewJobNameChange={this.handleNewJobNameChange}
              handleNewJobNameBlur={this.handleNewJobNameBlur}
              handleNewJobNameKeyPress={this.handleNewJobNameKeyPress}
              handleAddNewJobClick={this.handleAddNewJobClick}
              handleNewJobDeleteIconClick={this.handleNewJobDeleteIconClick}
              filters={filters}
              jobFieldsSaving={jobFieldsSaving}
              jobFieldsShowSuccess={jobFieldsShowSuccess}
              newJob={newJob}
            />
          </div>
        </div>
        <ConfirmationModal
          ref={(modal) => { this.modal = modal; }}
          title="Confirmation"
          content={'Shifts with this job will remain unchanged. Are you '
                  + 'sure you want to archive this job?'}
          buttons={[
            <StaffjoyButton
              buttonType="outline"
              size="tiny"
              key="cancel-button"
              onClick={this.handleCancelModalClick}
            >
              Cancel
            </StaffjoyButton>,
            <StaffjoyButton
              buttonType="outline"
              size="tiny"
              key="yes-button"
              style={{ float: 'right' }}
              onClick={this.handleDeleteJobClick}
            >
              Yes
            </StaffjoyButton>,
          ]}
        />
      </div>
    );
  }
}

Settings.propTypes = {
  companyId: PropTypes.string.isRequired,
  teamId: PropTypes.string.isRequired,
  team: PropTypes.object.isRequired,
  dispatch: PropTypes.func.isRequired,
  jobs: PropTypes.object.isRequired,
  colorPicker: PropTypes.object.isRequired,
  setTeamJob: PropTypes.func.isRequired,
  filters: PropTypes.object.isRequired,
  jobFieldsSaving: PropTypes.array.isRequired,
  jobFieldsShowSuccess: PropTypes.array.isRequired,
  newJob: PropTypes.object.isRequired,
  createTeamJob: PropTypes.func.isRequired,
  updateTeamJob: PropTypes.func.isRequired,
  updateTeamJobField: PropTypes.func.isRequired,
  setNewTeamJob: PropTypes.func.isRequired,
  setFilters: PropTypes.func.isRequired,
  setColorPicker: PropTypes.func.isRequired,
  isFetching: PropTypes.bool.isRequired,
};

function mapStateToProps(state, ownProps) {
  const teamId = ownProps.routeParams.teamId;

  // consts for team data
  const team = _.get(state.teams.data, teamId, {});
  const isTeamFetching = state.teams.isFetching;


  // consts for job data
  const jobState = _.get(state.teams.jobs, teamId, {});
  const jobs = _.get(jobState, 'data', {});
  const isJobFetching = _.get(jobState, 'isFetching', true);

  const colorPicker = _.get(state.settings, 'colorPicker', {});
  const filters = _.get(state.settings, 'filters', {});
  const jobFieldsSaving = _.get(state.settings, 'jobFieldsSaving', {});
  const jobFieldsShowSuccess = _.get(
    state.settings,
    'jobFieldsShowSuccess',
    {}
  );
  const newJob = _.get(state.settings, 'newJob', {});

  const isFetching = isTeamFetching || isJobFetching;

  return {
    companyId: ownProps.routeParams.companyId,
    teamId,
    team,
    jobs,
    colorPicker,
    filters,
    jobFieldsSaving,
    jobFieldsShowSuccess,
    newJob,
    isFetching,
  };
}

const mapDispatchToProps = dispatch => ({
  dispatch,
  createTeamJob: (companyId, teamId, jobPayload) => {
    dispatch(actions.createTeamJob(companyId, teamId, jobPayload));
  },
  updateTeamJob: (companyId, teamId, jobId, newData) => {
    dispatch(actions.updateTeamJob(companyId, teamId, jobId, newData));
  },
  updateTeamJobField: (companyId, teamId, jobId, newData) => {
    dispatch(
      actions.updateTeamJobField(companyId, teamId, jobId, newData)
    );
  },
  setTeamJob: (teamId, jobId, newData) => {
    dispatch(actions.setTeamJob(teamId, jobId, newData));
  },
  setNewTeamJob: (data) => {
    dispatch(actions.setNewTeamJob(data));
  },
  setFilters: (filters) => {
    dispatch(actions.setFilters(filters));
  },
  setColorPicker: (colorPicker) => {
    dispatch(actions.setColorPicker(colorPicker));
  },
});

export default connect(
  mapStateToProps,
  mapDispatchToProps,
)(Settings);
