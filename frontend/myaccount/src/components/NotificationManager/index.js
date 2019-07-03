import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as actions from '../../actions';
// import CheckboxField from '../CheckboxField';

function NotificationManager() {
  return (
    <div className="notifications">
      <h2>Notifications</h2>
      <p>Staffjoy sends notifications over email and text message.</p>
    </div>
  );

  /*
    TODO add these back in once app supports customizable notifications
      <CheckboxField
        description="Send email alerts"
        id="enable_email_notifications"
        attribute="enable_email_notifications"
        checked={props.enableEmailNotifications}
        onChange={props.modifyUserAttribute}
      />
      <CheckboxField
        description="Send text message alerts"
        id="enable_sms_notifications"
        attribute="enable_sms_notifications"
        checked={props.enableSmsNotifications}
        onChange={props.modifyUserAttribute}
      />
      <CheckboxField
        description="Send reminders"
        id="enable_reminders"
        attribute="enable_reminders"
        checked={props.enableReminders}
        onChange={props.modifyUserAttribute}
      />
  */
}

function mapDispatchToProps(dispatch) {
  return {
    modifyUserAttribute: bindActionCreators(
      actions.modifyUserAttribute,
      dispatch
    ),
  };
}

NotificationManager.propTypes = {
  enableEmailNotifications: PropTypes.bool.isRequired,
  enableSmsNotifications: PropTypes.bool.isRequired,
  enableReminders: PropTypes.bool.isRequired,
  modifyUserAttribute: PropTypes.func,
};

export default connect(
  null,
  mapDispatchToProps
)(NotificationManager);
