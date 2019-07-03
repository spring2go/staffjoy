import _ from 'lodash';
import React, { PropTypes } from 'react';

require('./shift-week-table-header.scss');

function ShiftWeekTableHeader({ columns, tableSize }) {
  const columnClass = `table-column col-${tableSize} centered`;

  return (
    <div className="shift-week-table-header table-row">
      {
        _.map(columns, (column) => {
          const divKey = `shift-table-header-div-${column.columnId}`;
          const spanKey = `shift-table-header-span-${column.columnId}`;
          return (
            <div key={divKey} className={columnClass}>
              <span
                key={spanKey}
                className="column-label"
              >
                {column.columnHeader}
              </span>
            </div>
          );
        })
      }
    </div>
  );
}

ShiftWeekTableHeader.propTypes = {
  columns: PropTypes.array.isRequired,
  tableSize: PropTypes.number.isRequired,
};

export default ShiftWeekTableHeader;
