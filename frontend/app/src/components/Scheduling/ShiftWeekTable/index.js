import _ from 'lodash';
import moment from 'moment';
import React, { PropTypes } from 'react';
import {
  MOMENT_DAY_DATE_DISPLAY,
  MOMENT_ISO_DATE,
} from 'constants/config';
import {
  UNASSIGNED_SHIFTS,
} from 'constants/constants';
import LoadingScreen from 'components/LoadingScreen';
import ShiftWeekTableHeader from './Header';
import ShiftWeekTableSection from './Section';


const unassignedShiftPhoto = require(
  '../../../../../resources/images/unassigned_shift_icon.png'
);

require('./shift-week-table.scss');


class ShiftWeekTable extends React.Component {

  organizeShiftsIntoSections() {
    const { viewBy } = this.props;

    if (viewBy === 'employee') {
      return this.shiftsByEmployee();
    } else if (viewBy === 'job') {
      return this.shiftsByJob();
    }

    return [];
  }

  shiftsByEmployee() {
    const { employees, shifts, filters } = this.props;
    const searchQuery = _.get(filters, 'searchQuery', '');
    const response = {};

    _.forEach(employees, (employee, id) => {
      if (employee.name.toLowerCase().includes(searchQuery) ||
          employee.email.includes(searchQuery)
      ) {
        response[id] = _.extend({}, employee, {
          id,
          shifts: [],
        });
      }
    });

    _.forEach(shifts, (shift) => {
      // unassigned shifts will be ignored here
      if (_.has(response, shift.userId)) {
        response[shift.userId].shifts.push(shift);
      }
    });

    return _.sortBy(_.values(response), 'name');
  }

  shiftsByJob() {
    const { jobs, shifts, filters } = this.props;
    const activeJobs = _.pickBy(jobs, job => !job.archived);
    const searchQuery = _.get(filters, 'searchQuery', '');
    const response = {};

    _.forEach(activeJobs, (job, id) => {
      if (job.name.toLowerCase().includes(searchQuery)) {
        response[id] = _.extend({}, job, { shifts: [] });
      }
    });

    _.forEach(shifts, (shift) => {
      // unassigned shifts will be included here
      if (_.has(response, shift.jobId)) {
        response[shift.jobId].shifts.push(shift);
      }
    });

    return _.sortBy(_.values(response), 'name');
  }

  buildColumns() {
    const { startDate, tableSize } = this.props;
    const startMoment = moment(startDate);

    return _.map(_.range(tableSize), (i) => {
      const calDate = startMoment.clone().add(i, 'days');
      return {
        columnId: calDate.format(MOMENT_ISO_DATE),
        columnHeader: calDate.format(MOMENT_DAY_DATE_DISPLAY),
      };
    });
  }

  render() {
    const { employees, jobs, tableSize, viewBy, timezone, modalOpen,
      droppedSchedulingCard, toggleSchedulingModal, editTeamShift,
      deleteTeamShift, startDate, updateSchedulingModalFormData,
      clearSchedulingModalFormData, createTeamShift, modalFormData,
      isSaving, shifts, companyId, teamId } = this.props;

    const columns = this.buildColumns();
    let unassignedSection = null;

    if (viewBy === 'employee') {
      unassignedSection = (
        <ShiftWeekTableSection
          key="unassigned-shifts-section"
          droppedSchedulingCard={droppedSchedulingCard}
          columns={columns}
          tableSize={tableSize}
          shifts={shifts.filter(shift => shift.userId === '')}
          name="Unassigned Shifts"
          sectionType={viewBy}
          sectionId={UNASSIGNED_SHIFTS}
          timezone={timezone}
          viewBy={viewBy}
          photoUrl={unassignedShiftPhoto}
          employees={employees}
          jobs={jobs}
          deleteTeamShift={deleteTeamShift}
          toggleSchedulingModal={toggleSchedulingModal}
          modalOpen={modalOpen}
          modalFormData={modalFormData}
          createTeamShift={createTeamShift}
          editTeamShift={editTeamShift}
          startDate={startDate}
          updateSchedulingModalFormData={updateSchedulingModalFormData}
          clearSchedulingModalFormData={clearSchedulingModalFormData}
          onCardZAxisChange={this.props.onCardZAxisChange}
          companyId={companyId}
          teamId={teamId}
        />
      );
    } else if (viewBy === 'job') {
      unassignedSection = (
        <ShiftWeekTableSection
          key="unassigned-jobs-section"
          droppedSchedulingCard={droppedSchedulingCard}
          columns={columns}
          tableSize={tableSize}
          shifts={shifts.filter(shift => shift.jobId === '')}
          name="No Job"
          sectionType={viewBy}
          sectionId={UNASSIGNED_SHIFTS}
          timezone={timezone}
          viewBy={viewBy}
          photoUrl={unassignedShiftPhoto}
          employees={employees}
          jobs={jobs}
          deleteTeamShift={deleteTeamShift}
          toggleSchedulingModal={toggleSchedulingModal}
          modalOpen={modalOpen}
          modalFormData={modalFormData}
          createTeamShift={createTeamShift}
          editTeamShift={editTeamShift}
          startDate={startDate}
          updateSchedulingModalFormData={updateSchedulingModalFormData}
          clearSchedulingModalFormData={clearSchedulingModalFormData}
          onCardZAxisChange={this.props.onCardZAxisChange}
        />
      );
    }

    return (
      <div className="shift-week-table">
        {isSaving &&
          <LoadingScreen
            containerProps={{
              style: {
                position: 'absolute',
                top: '0',
                bottom: '0',
                right: '0',
                left: '0',
                zIndex: '99',
                backgroundColor: 'rgba(255, 255, 255, 0.75)',
                marginTop: '0',
                paddingTop: '115px',
                opacity: '1',
              },
            }}
          />}
        <div className="scrolling-panel">
          <ShiftWeekTableHeader
            columns={columns}
            tableSize={tableSize}
          />
          {unassignedSection}
          {
            // TODO add unassigned shifts row if it's viewType employees
            _.map(this.organizeShiftsIntoSections(), (group) => {
              const sectionKey = `shift-table-section-${group.id}`;
              return (
                <ShiftWeekTableSection
                  columns={columns}
                  tableSize={tableSize}
                  shifts={group.shifts}
                  name={group.name}
                  sectionType={viewBy}
                  sectionId={group.id}
                  timezone={timezone}
                  viewBy={viewBy}
                  photoUrl={_.get(group, 'photoUrl', '')}
                  employees={employees}
                  jobs={jobs}
                  droppedSchedulingCard={droppedSchedulingCard}
                  key={sectionKey}
                  deleteTeamShift={deleteTeamShift}
                  toggleSchedulingModal={toggleSchedulingModal}
                  modalOpen={modalOpen}
                  modalFormData={modalFormData}
                  createTeamShift={createTeamShift}
                  editTeamShift={editTeamShift}
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

ShiftWeekTable.propTypes = {
  tableSize: PropTypes.number.isRequired,
  startDate: PropTypes.string.isRequired,
  employees: PropTypes.object.isRequired,
  jobs: PropTypes.object.isRequired,
  viewBy: PropTypes.string.isRequired,
  shifts: PropTypes.arrayOf(PropTypes.object).isRequired,
  filters: PropTypes.object.isRequired,
  timezone: PropTypes.string.isRequired,
  droppedSchedulingCard: PropTypes.func.isRequired,
  deleteTeamShift: PropTypes.func.isRequired,
  toggleSchedulingModal: PropTypes.func.isRequired,
  editTeamShift: PropTypes.func.isRequired,
  createTeamShift: PropTypes.func.isRequired,
  modalOpen: PropTypes.bool.isRequired,
  modalFormData: PropTypes.object.isRequired,
  updateSchedulingModalFormData: PropTypes.func.isRequired,
  clearSchedulingModalFormData: PropTypes.func.isRequired,
  onCardZAxisChange: PropTypes.func.isRequired,
  isSaving: PropTypes.bool.isRequired,
  companyId: PropTypes.string.isRequired,
  teamId: PropTypes.string.isRequired,
};

export default ShiftWeekTable;
