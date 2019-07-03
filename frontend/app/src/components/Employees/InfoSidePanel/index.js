import React from 'react';

require('./info-side-panel.scss');

function InfoSidePanel() {
  /* eslint-disable */
  const body = 'Manage your team, all in one place. Assign your employees to teams, and set up their preferred method of contact.';
  /* eslint-enable */

  return (
    <div className="employee-side-panel">
      <h3>Staffjoy Employees</h3>
      <p>{body}</p>
    </div>
  );
}

export default InfoSidePanel;
