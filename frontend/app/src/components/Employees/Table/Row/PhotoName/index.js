import React, { PropTypes } from 'react';

require('./table-photo-name.scss');

function TablePhotoName({ name, photoUrl = '' }) {
  return (
    <td className="table-photo-name mdl-data-table__cell--non-numeric">
      <img
        className="profile-icon"
        role="presentation"
        src={photoUrl}
      />
      <span className="name-label">{name}</span>
    </td>
  );
}

TablePhotoName.propTypes = {
  name: PropTypes.string.isRequired,
  photoUrl: PropTypes.string,
};

export default TablePhotoName;
