import _ from 'lodash';
import React, { PropTypes } from 'react';

import * as rowTypes from './rowTypes';
import TableContactInfo from './ContactInfo';
import TablePhotoName from './PhotoName';
import TableBooleanLabel from './BooleanLabel';

class TableRow extends React.Component {

  constructor(props) {
    super(props);
    this.onRowClick = this.onRowClick.bind(this);
  }

  onRowClick(event) {
    const { onClick, rowId } = this.props;

    if (_.isFunction(onClick)) {
      return onClick(event, rowId);
    }

    return {};
  }

  getRowComponent(column) {
    const { rowId } = this.props;
    const tdKey = `table-row-${rowId}-col-${column.columnId}`;

    switch (column.component) {
      case rowTypes.PHOTO_NAME:
        return (
          <TablePhotoName
            key={tdKey}
            {...this.extractPropData(column.propDataFields)}
          />
        );

      case rowTypes.CONTACT_INFO:
        return (
          <TableContactInfo
            key={tdKey}
            {...this.extractPropData(column.propDataFields)}
          />
        );

      case rowTypes.INFO_LIST:
        return (
          <td
            key={tdKey}
            className="mdl-data-table__cell--non-numeric"
          >
            Team
          </td>
        );

      case rowTypes.BOOLEAN_LABEL:
        return (
          <TableBooleanLabel
            key={tdKey}
            {...this.extractPropData(column.propDataFields)}
            callback={column.callback}
          />
        );

      default:
        return {};
    }
  }

  extractPropData(propDataFields) {
    /*
      returns an object containing the needed props mapped by componentProps
    */

    const { rowData } = this.props;
    const response = {};

    _.forEach(propDataFields, (value, key) => {
      response[key] = _.get(rowData, value);
    });

    return response;
  }


  render() {
    const { columns } = this.props;

    return (
      <tr onClick={this.onRowClick}>
        {
          _.map(columns, column =>
            this.getRowComponent(column)
          )
        }
      </tr>
    );
  }
}

TableRow.propTypes = {
  columns: PropTypes.arrayOf(PropTypes.object).isRequired,
  rowData: PropTypes.object.isRequired,
  rowId: PropTypes.string.isRequired,
  onClick: PropTypes.func,
};

export default TableRow;
