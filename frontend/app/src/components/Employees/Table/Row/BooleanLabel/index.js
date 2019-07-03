import React, { PropTypes } from 'react';
import classNames from 'classnames';

require('./table-boolean-label.scss');

function TableBooleanLabel({ booleanField, callback }) {
  const labelClasses = classNames({
    'mdl-data-table__cell--non-numeric': true,
    'table-boolean-label': true,
    invalid: !booleanField,
  });

  return (
    <td className={labelClasses} >
      {callback(booleanField)}
    </td>
  );
}

TableBooleanLabel.propTypes = {
  booleanField: PropTypes.bool.isRequired,
  callback: PropTypes.func.isRequired,
};

export default TableBooleanLabel;
