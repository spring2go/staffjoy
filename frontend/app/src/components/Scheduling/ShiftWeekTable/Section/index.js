import _ from 'lodash';
import moment from 'moment';
import 'moment-timezone';
import React, { PropTypes } from 'react';

import TableSectionPhotoName from './PhotoName';
import SectionSummaryInfo from './SummaryInfo';
import ShiftWeekTableRow from './Row';
import { MOMENT_ISO_DATE } from '../../../../constants/config';
import { arrayToObjectBucket } from '../../../../utility';

require('./shift-week-table-section.scss');

class ShiftWeekTableSection extends React.Component {
  /*
    - A TableSection constitutes one database id, i.e. a job or an employee
    - A TableSection can have multiple rows, such as if there are more than
      one record to show in a specific column
    - TableSection determines how many rows it needs, and then dispatches the
      data to be rendered in rows and the corresponding cards
  */

  newRow(rowCount) {
    const { columns } = this.props;
    return _.map(columns, column =>
      _.extend({}, column, {
        shift: {},
        rowNumber: rowCount,
      })
    );
  }

  generateRows() {
    /*
      this might be the most important function for this whole page!

      - At this point we have the prop shifts that is just an array of all the
        shifts that this section is responsible for
      - If there are ever multiple shifts on the same day, then the section
        needs to make one row for each shift to be rendered in

      This function:
      - Returns an array of arrays that contains properly constructed row
        objects
        - 1st level: is a whole row
        - 2nd level: Array of objects, each objects represents 1 column
          - which will either be a card or an empty cell

        Strategy:
        1) organize shifts into days
        2) you need as many rows as the day with the most shifts
        3) create a response of the needed amount of rows
        4) map shifts over by day into the rows
    */

    const { columns, timezone, shifts } = this.props;
    const dayMap = arrayToObjectBucket(_.map(columns, 'columnId'));
    const response = [];

    // arrange shifts into days
    _.forEach(shifts, (shift) => {
      const shiftStartMoment = moment.utc(shift.start).tz(timezone);
      const shiftColumnId = shiftStartMoment.format(MOMENT_ISO_DATE);

      if (_.has(dayMap, shiftColumnId)) {
        dayMap[shiftColumnId].push(shift);
      }
    });

    // sort each day (it's faster now that they are separated)
    _.forEach(dayMap, (day) => {
      day.sort((a, b) => {
        const aStart = moment.utc(a.start);
        const aStop = moment.utc(a.stop);
        const bStart = moment.utc(b.start);
        const bStop = moment.utc(b.stop);

        if (aStart.isSame(bStart)) {
          return aStop.isAfter(bStop);
        }

        return aStart.isAfter(bStart);
      });
    });

    // create a map of columnId to array indicies
    const dateToIndex = {};
    _.forEach(columns, (column, index) => {
      dateToIndex[column.columnId] = index;
    });

    // figure out how many rows are needed
    // there should be 1 row for each shift in a day => need the max
    // if no shifts, need to default to 1 row
    const rowsNeeded = _.max(_.map(dayMap, day => day.length)) || 1;

    // now build out the row structure
    _.forEach(_.range(rowsNeeded), (i) => {
      response.push(this.newRow(i));
    });


    // now transfer into the rows
    _.forEach(dayMap, (dayShifts, key) => {
      _.forEach(dayShifts, (shift, index) => {
        response[index][dateToIndex[key]].shift = shift;
      });
    });

    return response;
  }

  render() {
    const { employees, name, photoUrl, tableSize, timezone, jobs,
      viewBy, sectionId, droppedSchedulingCard, shifts, deleteTeamShift,
      toggleSchedulingModal, modalOpen, editTeamShift, startDate,
      updateSchedulingModalFormData, createTeamShift, modalFormData,
      clearSchedulingModalFormData, companyId, teamId } = this.props;

    return (
      <div className="shift-week-table-section">
        <div className="section-info">
          <TableSectionPhotoName name={name} photoUrl={photoUrl} />
          <SectionSummaryInfo shifts={shifts} timezone={timezone} />
        </div>
        <div className="shift-rows">
          {
            _.map(this.generateRows(), (rowObj, index) => {
              const rowKey = `shift-table-row-key-${sectionId}-${index}`;
              return (
                <ShiftWeekTableRow
                  key={rowKey}
                  shiftColumns={rowObj}
                  rowNumber={index}
                  tableSize={tableSize}
                  timezone={timezone}
                  employees={employees}
                  jobs={jobs}
                  viewBy={viewBy}
                  sectionId={sectionId}
                  droppedSchedulingCard={droppedSchedulingCard}
                  deleteTeamShift={deleteTeamShift}
                  toggleSchedulingModal={toggleSchedulingModal}
                  modalOpen={modalOpen}
                  modalFormData={modalFormData}
                  editTeamShift={editTeamShift}
                  createTeamShift={createTeamShift}
                  startDate={startDate}
                  updateSchedulingModalFormData={updateSchedulingModalFormData}
                  clearSchedulingModalFormData={clearSchedulingModalFormData}
                  onCardZAxisChange={this.props.onCardZAxisChange}
                  companyId={companyId}
                  teamId={teamId}
                />
              );
            })
          }
        </div>
      </div>
    );
  }
}

ShiftWeekTableSection.propTypes = {
  shifts: PropTypes.array.isRequired,
  name: PropTypes.string.isRequired,
  sectionId: PropTypes.string.isRequired,
  columns: PropTypes.array.isRequired,
  tableSize: PropTypes.number.isRequired,
  timezone: PropTypes.string.isRequired,
  viewBy: PropTypes.string.isRequired,
  employees: PropTypes.object.isRequired,
  jobs: PropTypes.object.isRequired,
  droppedSchedulingCard: PropTypes.func.isRequired,
  photoUrl: PropTypes.string,
  deleteTeamShift: PropTypes.func.isRequired,
  toggleSchedulingModal: PropTypes.func.isRequired,
  editTeamShift: PropTypes.func.isRequired,
  createTeamShift: PropTypes.func.isRequired,
  modalOpen: PropTypes.bool.isRequired,
  modalFormData: PropTypes.object.isRequired,
  startDate: PropTypes.string.isRequired,
  updateSchedulingModalFormData: PropTypes.func.isRequired,
  clearSchedulingModalFormData: PropTypes.func.isRequired,
  onCardZAxisChange: PropTypes.func.isRequired,
  companyId: PropTypes.string,
  teamId: PropTypes.string,
};

export default ShiftWeekTableSection;
