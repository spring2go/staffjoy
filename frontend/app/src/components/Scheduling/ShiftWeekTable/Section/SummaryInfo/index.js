import _ from 'lodash';
import moment from 'moment';
import 'moment-timezone';
import React, { PropTypes } from 'react';
import { getFormattedDuration } from '../../../../../utility';

require('./section-summary-info.scss');

class SectionSummaryInfo extends React.Component {

  summarizeShifts() {
    const { shifts, timezone } = this.props;

    const durationMs = _.reduce(shifts, (duration, shift) => {
      const momentStart = moment.utc(shift.start).tz(timezone);
      const momentStop = moment.utc(shift.stop).tz(timezone);
      const currentDuration = momentStop - momentStart;

      return duration + currentDuration;
    }, 0);

    return getFormattedDuration(durationMs);
  }

  render() {
    return (
      <div className="section-summary-info">
        <span>{this.summarizeShifts()}</span>
      </div>
    );
  }

}

SectionSummaryInfo.propTypes = {
  shifts: PropTypes.array.isRequired,
  timezone: PropTypes.string.isRequired,
};

export default SectionSummaryInfo;
