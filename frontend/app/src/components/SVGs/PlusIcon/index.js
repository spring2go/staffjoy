import React, { PropTypes } from 'react';

function PlusIcon({
  fill = '#000000',
  width,
  height,
  onClick = () => {},
}) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      version="1"
      viewBox="0 0 24 24"
      enableBackground="new 0 0 24 24"
      width={width}
      height={height}
    >
      <path
        style={{
          textIndent: '0',
          textAlign: 'start',
          lineHeight: 'normal',
          textTransform: 'none',
          blockProgression: 'tb',
        }}
        d={'M 11 5 L 11 11 L 5 11 L 5 13 L 11 13 L 11 19 L 13 19 L 13 13 L'
          + '19 13 L 19 11 L 13 11 L 13 5 L 11 5 z'}
        overflow="visible"
        enableBackground="accumulate"
        fontFamily="Bitstream Vera Sans"
        fill={fill}
        onClick={onClick}
      />
    </svg>
  );
}

PlusIcon.propTypes = {
  fill: PropTypes.string,
  width: PropTypes.string,
  height: PropTypes.string,
  onClick: PropTypes.func,
};

export default PlusIcon;
