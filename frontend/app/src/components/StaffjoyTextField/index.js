import React, { PropTypes } from 'react';
import { Textfield } from 'react-mdl';
import classNames from 'classnames';

require('./staffjoy-text-field.scss');

function StaffjoyTextField({ isFocused, width, ...otherProps }) {
  const classes = classNames({
    active: isFocused,
  });

  let fieldWidth = '';

  if (typeof width === 'number') {
    fieldWidth = `${width}px`;
  } else if (width === 'full') {
    fieldWidth = '100%';
  } else {
    fieldWidth = '200px';
  }

  return (
    <Textfield
      className="staffjoy-text-field"
      floatingLabel
      inputClassName={classes}
      style={{ width: fieldWidth }}
      {...otherProps}
    />
  );
}

StaffjoyTextField.propTypes = {
  isFocused: PropTypes.bool,
  width: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
};

export default StaffjoyTextField;
