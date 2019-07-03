import React, { PropTypes } from 'react';

require('./scheduling-table-photo-name.scss');

function SchedulingTablePhotoName({ name, photoUrl = '' }) {
  return (
    <div
      className="scheduling-table-photo-name mdl-data-table__cell--non-numeric"
    >
      <img
        className="profile-icon"
        role="presentation"
        src={photoUrl}
      />
      <div className="name-label">{name}</div>
    </div>
  );
}

SchedulingTablePhotoName.propTypes = {
  name: PropTypes.string.isRequired,
  photoUrl: PropTypes.string,
};

export default SchedulingTablePhotoName;
