import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as actions from '../../actions';

function ProfilePhoto({ photoUrl }) {
  return (
    <div className="profile-photo">
      <img
        className="circle-frame"
        role="presentation"
        src={photoUrl}
      />
    </div>
  );

  /*
    TODO add back in when app supports custom photo
      <input
        type="file"
        name="photo-upload"
        id="photo-upload"
        onChange={changePhoto}
        className="inputfile"
      />
      <label
        htmlFor="photo-upload"
        className="mdl-button mdl-js-button change-photo-button"
      >
        Change Photo
      </label>
    */
}

function mapDispatchToProps(dispatch) {
  return {
    changePhoto: bindActionCreators(
      actions.changePhoto,
      dispatch
    ),
  };
}

ProfilePhoto.propTypes = {
  photoUrl: PropTypes.string.isRequired,
  // changePhoto: PropTypes.func.isRequired,
};

export default connect(null, mapDispatchToProps)(ProfilePhoto);
