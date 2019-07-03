import React, { PropTypes } from 'react';

function DeleteIcon({
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
      width={width}
      height={height}
      fill={fill}
      onClick={onClick}
    >
      <path
        d={'M12,2C6.5,2,2,6.5,2,12c0,5.5,4.5,10,10,10s10-4.5,10-10C22,6.5,'
          + '17.5,2,12,2z M16.9,15.5l-1.4,1.4L12,13.4l-3.5,3.5 l-1.4-1.4l3.'
          + '5-3.5L7.1,8.5l1.4-1.4l3.5,3.5l3.5-3.5l1.4,1.4L13.4,12L16.9,15.5z'}
        fill={fill}
      />
    </svg>
  );
}

DeleteIcon.propTypes = {
  fill: PropTypes.string,
  width: PropTypes.string,
  height: PropTypes.string,
  onClick: PropTypes.func,
};

export default DeleteIcon;
