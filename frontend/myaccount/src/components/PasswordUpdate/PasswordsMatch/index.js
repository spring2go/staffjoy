import _ from 'lodash';
import classNames from 'classnames';
import React, { PropTypes } from 'react';

function PasswordsMatch({ original, matching }) {
  const style = classNames({
    'password-compare': true,
    hidden: _.isEmpty(matching) || original === matching,
    danger: original !== matching,
  });

  return (
    <p className={style}>Passwords do not match</p>
  );
}
PasswordsMatch.propTypes = {
  original: PropTypes.string.isRequired,
  matching: PropTypes.string.isRequired,
};

export default PasswordsMatch;
