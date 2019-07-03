import _ from 'lodash';
import React, { PropTypes } from 'react';

require('./table-section-photo-name.scss');

function TableSectionPhotoName({ name, photoUrl = '' }) {
  let photoSection = null;

  if (!_.isEmpty(photoUrl)) {
    photoSection = (
      <div className="photo-column">
        <img
          className="profile-icon"
          role="presentation"
          src={photoUrl}
        />
      </div>
    );
  }

  return (
    <div className="shift-row-photo-name">
      {photoSection}
      <div className="name-column">
        <h4 className="row-name">{name}</h4>
      </div>
    </div>
  );
}

TableSectionPhotoName.propTypes = {
  name: PropTypes.string.isRequired,
  photoUrl: PropTypes.string,
};

export default TableSectionPhotoName;
