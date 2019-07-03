import React, { PropTypes } from 'react';

require('./employee-panel-photo-name.scss');

function EmployeePanelPhotoName({ name, photoUrl = '' }) {
  return (
    <div className="employee-panel-photo-name">
      <img
        className="profile-icon"
        role="presentation"
        src={photoUrl}
      />
      <h3 className="employee-name">{name}</h3>
    </div>
  );
}

EmployeePanelPhotoName.propTypes = {
  name: PropTypes.string.isRequired,
  photoUrl: PropTypes.string,
};

export default EmployeePanelPhotoName;
