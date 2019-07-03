import moment from 'moment';
import React, { PropTypes } from 'react';

import InfoStat from './InfoStat';
import { MOMENT_MONTH_YEAR_FORMAT } from '../../constants/config';


function StatsPanel({ memberSince }) {
  const memberSinceFormat = moment.utc(memberSince)
    .format(MOMENT_MONTH_YEAR_FORMAT);

  return (
    <div className="stats-panel">
      <InfoStat
        label="Member Since"
        stat={memberSinceFormat}
      />
    </div>
  );
}

StatsPanel.propTypes = {
  memberSince: PropTypes.string.isRequired,
};

export default StatsPanel;
