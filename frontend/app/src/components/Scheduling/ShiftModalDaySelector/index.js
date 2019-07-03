import _ from 'lodash';
import $ from 'npm-zepto';
import moment from 'moment';
import React, { PropTypes } from 'react';
import {
  MOMENT_DATE_DISPLAY,
  MOMENT_ISO_DATE,
} from 'constants/config';
import { getLetterFromDayName } from 'utility';
import ShiftModalDayCell from './DayCell';

require('./shift-modal-day-selector.scss');

class ShiftModalDaySelector extends React.Component {

  constructor(props) {
    super(props);

    const { tableSize, startDate } = this.props;
    const startMoment = moment(startDate);
    const cells = _.map(_.range(tableSize), (i) => {
      const calDate = startMoment.clone().add(i, 'days');
      return {
        dayLetter: getLetterFromDayName(calDate.format('dddd')),
        displayDate: calDate.format(MOMENT_DATE_DISPLAY),
        cellId: calDate.format(MOMENT_ISO_DATE),
      };
    });

    this.selectedDay = this.selectedDay.bind(this);
    this.state = {
      selected: {},
    };
    this.cells = cells;
  }

  componentWillMount() {
    const selectedState = {};
    const { selectedDate, formCallback } = this.props;
    _.forEach(this.cells, (cell) => {
      selectedState[cell.cellId] = false;
    });

    if (_.has(selectedState, selectedDate)) {
      selectedState[selectedDate] = true;
    }

    this.setState({ selected: selectedState });
    formCallback({ selectedDays: selectedState });
  }

  selectedDay(event) {
    const cellId = $(event.target).data('cellid');
    const { selected } = this.state;
    const { formCallback } = this.props;
    const currentValue = selected[cellId];
    const selectedDays = _.extend({}, selected, { [cellId]: !currentValue });

    this.setState({ selected: selectedDays });
    formCallback({ selectedDays });
  }

  render() {
    const { selected } = this.state;

    return (
      <div className="shift-modal-day-selector">
        {
          _.map(this.cells, (cell) => {
            const cellKey = `${cell.cellId}-modal-day-cell`;
            return (
              <ShiftModalDayCell
                {...cell}
                selected={selected[cell.cellId]}
                onClick={this.selectedDay}
                key={cellKey}
              />
            );
          })
        }
      </div>
    );
  }
}

ShiftModalDaySelector.propTypes = {
  tableSize: PropTypes.number.isRequired,
  startDate: PropTypes.string.isRequired,
  selectedDate: PropTypes.string,
  formCallback: PropTypes.func.isRequired,
};

export default ShiftModalDaySelector;
