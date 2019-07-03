import React from 'react';
import { Spinner } from 'react-mdl';

require('./loading-screen.scss');
const imgUrl = require(
  '../../../../resources/images/staffjoy.png'
);

function LoadingScreen() {
  return (
    <div className="loading-container">
      <img role="presentation" alt="Staffjoy logo" src={imgUrl} />
      <Spinner singleColor />
    </div>
  );
}

export default LoadingScreen;
