import React, { PropTypes } from 'react';
import { ScaleModal } from 'boron';

require('./confirmation-modal.scss');

export default class ConfirmationModal extends React.Component {

  showModal() {
    this.modal.show();
  }

  hideModal() {
    this.modal.hide();
  }

  render() {
    const {
      modalProps,
      titleProps,
      title,
      contentProps,
      content,
      buttons,
    } = this.props;

    return (
      <ScaleModal
        contentStyle={{
          /* due to chrome css bug */
          animation: 'none',
          borderRadius: '3px',
        }}
        modalStyle={{ width: '300px' }}
        className="confirmation-modal"
        ref={(modal) => { this.modal = modal; }}
        onHide={this.onModalClose}
        {...modalProps}
      >
        <div
          className="confirmation-modal-title"
          {...titleProps}
        >
          <span>
            {title}
          </span>
        </div>
        <div
          className="confirmation-modal-content"
          {...contentProps}
        >
          <span>
            {content}
          </span>
        </div>
        {buttons}
      </ScaleModal>
    );
  }
}

ConfirmationModal.propTypes = {
  modalProps: PropTypes.object,
  titleProps: PropTypes.object,
  title: PropTypes.string,
  contentProps: PropTypes.object,
  content: PropTypes.string,
  buttons: PropTypes.arrayOf(PropTypes.node),
};

ConfirmationModal.defaultProps = {
  modalProps: {},
  titleProps: {},
  title: '',
  contentProps: {},
  content: '',
  buttons: [],
};
