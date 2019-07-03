import React, { PropTypes } from 'react';
import classNames from 'classnames';

require('./time-selector-number-button.scss');

function TimeSelectorNumberButton({
  display,
  dataValue,
  currentValue,
  onClick,
  ...otherProps
}) {
  const classes = classNames({
    'time-selector-number-button': true,
    selected: currentValue === dataValue,
  });

  return (
    <button
      className={classes}
      data-time-value={dataValue}
      onClick={onClick}
      {...otherProps}
    >
      {display}
    </button>
  );
}

TimeSelectorNumberButton.propTypes = {
  onClick: PropTypes.func.isRequired,
  display: React.PropTypes.oneOfType([
    React.PropTypes.string,
    React.PropTypes.number,
  ]),
  dataValue: React.PropTypes.oneOfType([
    React.PropTypes.string,
    React.PropTypes.number,
  ]),
  currentValue: React.PropTypes.oneOfType([
    React.PropTypes.string,
    React.PropTypes.number,
  ]),
};

export default TimeSelectorNumberButton;
