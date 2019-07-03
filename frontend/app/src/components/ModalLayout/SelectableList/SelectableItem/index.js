import React, { PropTypes } from 'react';

require('./modal-list-selectable-item.scss');

function ModalListSelectableItem({
  name,
  selected,
  changeFunction,
  id,
}) {
  return (
    <div className="modal-list-selectable-item" data-id={id}>
      <input
        className="modal-checkbox"
        type="checkbox"
        checked={selected}
        onChange={changeFunction}
      />
      <span className="name-label" onClick={changeFunction}>{name}</span>
    </div>
  );
}

ModalListSelectableItem.propTypes = {
  id: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  selected: PropTypes.bool.isRequired,
  changeFunction: PropTypes.func.isRequired,
};

export default ModalListSelectableItem;
