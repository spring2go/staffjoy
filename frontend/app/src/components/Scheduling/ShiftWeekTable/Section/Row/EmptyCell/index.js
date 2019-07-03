import React, { PropTypes } from 'react';
import { DropTarget as dropTarget } from 'react-dnd';
import classNames from 'classnames';
import CreateShiftModal from '../../../../CreateShiftModal';

require('./shift-week-table-empty-cell.scss');

class ShiftWeekTableEmptyCell extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      isHovering: false,
    };
    this.handleMouseOver = this.handleMouseOver.bind(this);
    this.handleMouseOut = this.handleMouseOut.bind(this);
  }

  // relying on css :hover state presented issues, so safer way is to track
  // hover status via react state
  handleMouseOver() {
    this.setState({ isHovering: true });
  }

  handleMouseOut() {
    this.setState({ isHovering: false });
  }

  render() {
    const { tableSize, startDate, isOver, connectDropTarget,
      timezone, toggleSchedulingModal, columnId, viewBy, employees, jobs,
      sectionId, createTeamShift, updateSchedulingModalFormData,
      clearSchedulingModalFormData, modalFormData } = this.props;
    const { isHovering } = this.state;

    const classes = classNames({
      'shift-week-table-empty-cell': true,
      'card-is-over': isOver,
      isHovering,
    });

    return connectDropTarget(
      <div
        onMouseOver={this.handleMouseOver}
        onMouseOut={this.handleMouseOut}
      >
        <CreateShiftModal
          tableSize={tableSize}
          startDate={startDate}
          timezone={timezone}
          modalCallbackToggle={toggleSchedulingModal}
          containerComponent="div"
          containerProps={{ className: classes }}
          selectedDate={columnId}
          viewBy={viewBy}
          selectedRow={sectionId}
          employees={employees}
          jobs={jobs}
          onSave={createTeamShift}
          modalFormData={modalFormData}
          updateSchedulingModalFormData={updateSchedulingModalFormData}
          clearSchedulingModalFormData={clearSchedulingModalFormData}
          sectionId={sectionId}
        />
      </div>
    );
  }
}

ShiftWeekTableEmptyCell.propTypes = {
  isOver: PropTypes.bool.isRequired,
  connectDropTarget: PropTypes.func.isRequired,
  columnId: PropTypes.string.isRequired,
  sectionId: PropTypes.string.isRequired,
  timezone: PropTypes.string.isRequired,
  toggleSchedulingModal: PropTypes.func.isRequired,
  startDate: PropTypes.string.isRequired,
  tableSize: PropTypes.number.isRequired,
  viewBy: PropTypes.string.isRequired,
  employees: PropTypes.object.isRequired,
  jobs: PropTypes.object.isRequired,
  modalFormData: PropTypes.object.isRequired,
  updateSchedulingModalFormData: PropTypes.func.isRequired,
  clearSchedulingModalFormData: PropTypes.func.isRequired,
  createTeamShift: PropTypes.func.isRequired,
};

/*
  There are some props needed for the drag and drop wrappers, but they aren't
  used from inside the actual component. We will just list them here for
  reference.

  * droppedSchedulingCard: PropTypes.func.isRequired
*/

const cardDropSpec = {
  drop(props, monitor) {
    const { columnId, sectionId, droppedSchedulingCard } = props;
    const { shiftId, oldColumnId } = monitor.getItem();

    // trigger our drag action/prop
    droppedSchedulingCard(shiftId, oldColumnId, sectionId, columnId);
  },
};

function collectDrop(connect, monitor) {
  return {
    connectDropTarget: connect.dropTarget(),
    isOver: monitor.isOver(),
    canDrop: monitor.canDrop(),
  };
}

export default dropTarget(
  'card',
  cardDropSpec,
  collectDrop
)(ShiftWeekTableEmptyCell);
