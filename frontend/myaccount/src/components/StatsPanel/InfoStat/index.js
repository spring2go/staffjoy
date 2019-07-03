import React, { PropTypes } from 'react';

function InfoStat({ label, stat }) {
  return (
    <div className="info-stat">
      <span>
        <span className="label">{label}:</span>
        {stat}
      </span>
    </div>
  );
}

InfoStat.propTypes = {
  label: PropTypes.string.isRequired,
  stat: PropTypes.string.isRequired,
};

export default InfoStat;
