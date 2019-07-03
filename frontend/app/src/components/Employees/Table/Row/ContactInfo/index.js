import React, { PropTypes } from 'react';
import classNames from 'classnames';
import { formatPhoneNumber } from 'utility';

require('./contact-info.scss');

function TableContactInfo({ email, phoneNumber }) {
  const nullValue = String.fromCharCode('8212');
  const value = formatPhoneNumber(phoneNumber) || email || nullValue;

  const labelClasses = classNames({
    'mdl-data-table__cell--non-numeric': true,
    'table-contact-info-label': true,
    empty: value === nullValue,
  });

  return (
    <td className={labelClasses} >{value}</td>
  );
}

TableContactInfo.propTypes = {
  email: PropTypes.string,
  phoneNumber: PropTypes.string,
};

export default TableContactInfo;
