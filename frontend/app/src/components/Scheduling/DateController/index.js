import React, { PropTypes } from 'react';
import moment from 'moment';
import 'moment-timezone';
import momentPropTypes from 'react-moment-proptypes';

import SquareButton from 'components/SquareButton';
import {
  MOMENT_CALENDAR_START_DISPLAY,
  MOMENT_CALENDAR_END_DISPLAY,
  MOMENT_CALENDAR_TIME_DISPLAY,
} from '../../../constants/config';

require('./scheduling-date-controller.scss');

class SchedulingDateController extends React.Component {

  componentWillMount() {
    this.setTime();
    const intervalId = window.setInterval(() => this.setTime(), 1000);
    this.setState({ intervalId });
  }

  componentWillUnmount() {
    clearInterval(this.state.intervalId);
  }

  setTime() {
    const { timezone } = this.props;

    const time = moment.tz(timezone).format(MOMENT_CALENDAR_TIME_DISPLAY);
    this.setState({ time });
  }

  render() {
    const { queryStart, queryStop, stepDateRange, timezone,
      disabled } = this.props;
    const { time } = this.state;
    const startDisplay = moment.utc(queryStart).tz(timezone)
      .format(MOMENT_CALENDAR_START_DISPLAY);
    const stopDisplay = moment.utc(queryStop).tz(timezone)
      .subtract(1, 'days')
      .format(MOMENT_CALENDAR_END_DISPLAY);

    return (
      <div className="scheduling-date-controls-container mdl-grid">
        <div className="date-buttons mdl-cell-3-col">
          <SquareButton
            name="chevron_left"
            onClick={stepDateRange}
            data-direction="left"
            disabled={disabled}
          />
          <SquareButton
            name="chevron_right"
            onClick={stepDateRange}
            data-direction="right"
            disabled={disabled}
          />
        </div>
        <div className="time-displays mdl-cell-9-col">
          <div className="date-range">{startDisplay} - {stopDisplay}</div>
          <div className="current-time">{time}</div>
        </div>
      </div>
    );
  }
}

SchedulingDateController.propTypes = {
  queryStart: momentPropTypes.momentObj.isRequired,
  queryStop: momentPropTypes.momentObj.isRequired,
  timezone: PropTypes.string.isRequired,
  stepDateRange: PropTypes.func.isRequired,
  disabled: PropTypes.bool,
};

SchedulingDateController.defaultProps = {
  disabled: false,
};

export default SchedulingDateController;
