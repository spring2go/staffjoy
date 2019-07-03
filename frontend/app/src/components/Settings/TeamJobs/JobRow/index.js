import React, { PropTypes } from 'react';
import { TwitterPicker } from 'react-color';
import { Spinner } from 'react-mdl';
import DeleteIcon from 'components/SVGs/DeleteIcon';
import * as constants from 'constants/constants';

require('./job-row.scss');

export default class JobRow extends React.Component {

  componentDidMount() {
    if (this.props.isNewJob) {
      this.input.focus();
    }
  }

  render() {
    const {
      job,
      colorPicker,
      handleJobNameChange,
      handleJobNameBlur,
      handleJobNameKeyPress,
      handleJobColorClick,
      handleColorPickerChange,
      handleShowModalClick,
      jobFieldsSaving,
      jobFieldsShowSuccess,
    } = this.props;

    return (
      <tr
        key={`table-row-${job.id}`}
        className="table-row-job"
      >
        <td
          className="job-name-cell"
        >
          <input
            className="job-name-input"
            ref={(input) => { this.input = input; }}
            value={job.name}
            onChange={(event) => { handleJobNameChange(event, job.id); }}
            onBlur={(event) => { handleJobNameBlur(event, job.id); }}
            onKeyPress={(event) => { handleJobNameKeyPress(event, job.id); }}
          />
          {
            jobFieldsSaving.includes(job.id)
            &&
            <Spinner singleColor />
          }
          {
            jobFieldsShowSuccess.includes(job.id)
            &&
            !jobFieldsSaving.includes(job.id)
            &&
            <div className="job-form-field-success">
              <i className="material-icons">check_circle</i>
              <span>Saved!</span>
            </div>
          }
        </td>
        <td
          className="job-color-cell"
        >
          <div
            style={{
              backgroundColor: `${job.color}`,
            }}
            className="job-color-circle"
            onClick={event => handleJobColorClick(event, job.id)}
          />
          <div className="color-picker-container">
            {
              colorPicker.jobIdVisible === job.id
              &&
              <div>
                <div
                  className="color-picker-overlay"
                  onClick={event => handleJobColorClick(event, null)}
                />
                <TwitterPicker
                  triangle="top-right"
                  colors={constants.COLOR_PICKER_COLORS}
                  onChange={({ hex, source }, event) => {
                    handleColorPickerChange({ hex, source }, job.id, event);
                  }}
                />
              </div>
            }
          </div>
        </td>
        <td className="job-delete-cell">
          <DeleteIcon
            fill="#666666"
            width="25"
            height="25"
            onClick={() => { handleShowModalClick(job.id); }}
          />
        </td>
      </tr>
    );
  }
}

JobRow.propTypes = {
  isNewJob: PropTypes.bool,
  job: PropTypes.object,
  colorPicker: PropTypes.object.isRequired,
  handleJobColorClick: PropTypes.func,
  handleColorPickerChange: PropTypes.func,
  handleJobNameChange: PropTypes.func.isRequired,
  handleJobNameBlur: PropTypes.func.isRequired,
  handleJobNameKeyPress: PropTypes.func.isRequired,
  handleShowModalClick: PropTypes.func.isRequired,
  jobFieldsSaving: PropTypes.array.isRequired,
  jobFieldsShowSuccess: PropTypes.array.isRequired,
};

JobRow.defaultProps = {
  job: {},
  isNewJob: false,
  handleJobColorClick: () => {},
  handleColorPickerChange: () => {},
};
