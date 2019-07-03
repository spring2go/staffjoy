import React from 'react';

require('./table-header.scss');

function TableHeader() {
  return (
    <thead>
      <tr className="job-settings-header">
        <th
          className="mdl-data-table__cell--non-numeric col-4"
        >
          Jobs
        </th>
        <th
          className="mdl-data-table__cell--non-numeric col-1 job-color-header"
        >
          Color
        </th>
        <th className="mdl-data-table__cell--non-numeric col-1" />
      </tr>
    </thead>
  );
}

export default TableHeader;
