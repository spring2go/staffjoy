import React, { PropTypes } from 'react';
import { Spinner } from 'react-mdl';

require('./loading-screen.scss');
const imgUrl = require(
  '../../../../resources/images/staffjoy.png'
);

function LoadingScreen({ containerProps = {} }) {
  return (
    <div className="loading-container" {...containerProps}>
      <img role="presentation" alt="Staffjoy logo" src={imgUrl} />
      <Spinner singleColor />
    </div>
  );
}

LoadingScreen.propTypes = {
  containerProps: PropTypes.object,
};

export default LoadingScreen;
