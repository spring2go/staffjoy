import _ from 'lodash';
import React, { PropTypes } from 'react';
import PlusIcon from 'components/SVGs/PlusIcon';
import TableHeader from './TableHeader';
import JobRow from './JobRow';

require('./team-jobs.scss');

export default class TeamJobs extends React.Component {

  getQueriedJobs() {
    const {
      jobs,
      filters,
    } = this.props;

    const searchQuery = _.get(filters, 'searchQuery', '');

    if (searchQuery === '') {
      return jobs;
    }

    const queriedJobs = {};

    _.forEach(jobs, (job, id) => {
      if (job.name.toLowerCase().includes(searchQuery.toLowerCase())) {
        queriedJobs[id] = job;
      }
    });

    return queriedJobs;
  }

  render() {
    const {
      colorPicker,
      handleJobColorClick,
      handleColorPickerChange,
      handleJobNameChange,
      handleJobNameBlur,
      handleJobNameKeyPress,
      handleShowModalClick,
      handleNewJobNameChange,
      handleNewJobNameBlur,
      handleNewJobNameKeyPress,
      handleAddNewJobClick,
      handleNewJobDeleteIconClick,
      jobFieldsSaving,
      jobFieldsShowSuccess,
      newJob,
    } = this.props;

    return (
      <table className="mdl-data-table mdl-js-data-table staffjoy-table">
        <TableHeader />
        <tbody>
          {_.map(this.getQueriedJobs(), (job) => {
            if (job.archived) {
              return null;
            }

            return (
              <JobRow
                key={`job-row-${job.id}`}
                job={job}
                colorPicker={colorPicker}
                handleJobNameChange={handleJobNameChange}
                handleJobNameBlur={handleJobNameBlur}
                handleJobNameKeyPress={handleJobNameKeyPress}
                handleJobColorClick={handleJobColorClick}
                handleColorPickerChange={handleColorPickerChange}
                handleShowModalClick={handleShowModalClick}
                jobFieldsSaving={jobFieldsSaving}
                jobFieldsShowSuccess={jobFieldsShowSuccess}
              />
            );
          })}
          {
            newJob.isVisible
            &&
            <JobRow
              isNewJob
              job={newJob}
              colorPicker={colorPicker}
              handleJobNameChange={handleNewJobNameChange}
              handleJobNameBlur={handleNewJobNameBlur}
              handleJobNameKeyPress={handleNewJobNameKeyPress}
              handleShowModalClick={handleNewJobDeleteIconClick}
              jobFieldsSaving={jobFieldsSaving}
              jobFieldsShowSuccess={jobFieldsShowSuccess}
            />
          }
          <tr
            className="table-row-new-job"
            onClick={(event) => {
              if (newJob.isVisible) {
                return;
              }

              handleAddNewJobClick(event);
            }}
          >
            <td colSpan="3">
              <PlusIcon
                fill="#9a9699"
                width="26px"
                height="26px"
              />
              Add New Job
            </td>
          </tr>
        </tbody>
      </table>
    );
  }
}

TeamJobs.propTypes = {
  jobs: PropTypes.object.isRequired,
  newJob: PropTypes.object.isRequired,
  colorPicker: PropTypes.object.isRequired,
  handleJobColorClick: PropTypes.func.isRequired,
  handleColorPickerChange: PropTypes.func.isRequired,
  handleJobNameChange: PropTypes.func.isRequired,
  handleJobNameBlur: PropTypes.func.isRequired,
  handleJobNameKeyPress: PropTypes.func.isRequired,
  handleShowModalClick: PropTypes.func.isRequired,
  handleNewJobNameChange: PropTypes.func.isRequired,
  handleNewJobNameBlur: PropTypes.func.isRequired,
  handleNewJobNameKeyPress: PropTypes.func.isRequired,
  handleAddNewJobClick: PropTypes.func.isRequired,
  handleNewJobDeleteIconClick: PropTypes.func.isRequired,
  filters: PropTypes.object.isRequired,
  jobFieldsSaving: PropTypes.array.isRequired,
  jobFieldsShowSuccess: PropTypes.array.isRequired,
};
