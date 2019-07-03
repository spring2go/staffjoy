import React, { PropTypes } from 'react';
import { Textfield } from 'react-mdl';
import classNames from 'classnames';

require('./search-field.scss');

function SearchField({
  width,
  onChange = {},
  darkBackground,
  disabled = false,
}) {
  const classes = classNames({
    'search-container': true,
    'dark-container': darkBackground,
  });

  return (
    <div className={classes} style={{ width }}>
      <i className="material-icons">search</i>
      <Textfield
        onChange={onChange}
        label="Search"
        style={{ width: width - 45 }}
        disabled={disabled}
      />
    </div>
  );
}

SearchField.propTypes = {
  width: PropTypes.number.isRequired,
  onChange: PropTypes.func,
  darkBackground: PropTypes.bool,
  disabled: PropTypes.bool,
};

export default SearchField;
