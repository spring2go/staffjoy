import _ from 'lodash';
import React, { PropTypes } from 'react';

require('./modal-layout.scss');

function ModalLayoutRightSideColumn({
  children,
  buttons,
  title,
  panelTitle,
  panelContent,
}) {
  let buttonContent = null;
  let titleElement = null;
  let panelTitleElement = null;

  if (!_.isEmpty(buttons)) {
    buttonContent = (
      <div className="modal-main-button-panel">
        {
          _.map(buttons, button =>
            button
          )
        }
      </div>
    );
  }

  if (!_.isEmpty(title)) {
    titleElement = <h4 className="modal-title">{title}</h4>;
  }

  if (!_.isEmpty(panelTitle)) {
    panelTitleElement = <h4 className="side-panel-title">{panelTitle}</h4>;
  }

  return (
    <div className="staffjoy-modal-layout right-side-column">
      <div className="modal-side-panel">
        {panelTitleElement}
        <div className="scrolling-panel">
          {panelContent}
        </div>
      </div>
      <div className="modal-main">
        <div className="modal-main-content">
          {titleElement}
          {children}
        </div>
        {buttonContent}
      </div>

    </div>
  );
}

ModalLayoutRightSideColumn.propTypes = {
  children: PropTypes.oneOfType([PropTypes.array, PropTypes.element]),
  buttons: PropTypes.array,
  title: PropTypes.string,
  panelTitle: PropTypes.string,
  panelContent: PropTypes.element,
};

export default ModalLayoutRightSideColumn;
