import _ from 'lodash';
import $ from 'npm-zepto';
import React, { PropTypes } from 'react';
import ModalListSelectableItem from './SelectableItem';

require('./selectable-modal-list.scss');

class SelectableModalList extends React.Component {

  constructor(props) {
    super(props);
    this.selectElement = this.selectElement.bind(this);
    this.state = {
      selections: {},
    };
  }

  componentWillMount() {
    const { records, selectedId, formField, formCallback,
    idKey } = this.props;
    const selections = {};
    _.forEach(records, (record) => {
      selections[record[idKey]] = false;
    });

    if (_.has(selections, selectedId)) {
      selections[selectedId] = true;
    }

    this.setState({ selections });
    formCallback({ [formField]: selections });
  }

  selectElement(event) {
    const { formField, formCallback } = this.props;
    const newId = $(event.target)
                      .closest('.modal-list-selectable-item')
                      .data('id');
    const selections = _.extend({}, this.state.selections);
    selections[newId] = !selections[newId];
    this.setState({ selections });
    formCallback({ [formField]: selections });
  }

  render() {
    const { error, records, displayByProperty, idKey } = this.props;
    const { selections } = this.state;

    let errorMessage;
    if (error) {
      errorMessage = (
        <div className="error-message">
          {error}
        </div>
      );
    }

    return (
      <div className="modal-selectable-list">
        {errorMessage}
        {
          _.map(records, (record) => {
            const selectorKey = `modal-list-selectable-item-${record[idKey]}`;

            return (
              <ModalListSelectableItem
                key={selectorKey}
                selected={selections[record[idKey]]}
                changeFunction={this.selectElement}
                id={record[idKey]}
                name={record[displayByProperty]}
              />
            );
          })
        }
      </div>
    );
  }
}

SelectableModalList.propTypes = {
  error: PropTypes.string,
  records: PropTypes.arrayOf(React.PropTypes.object),
  displayByProperty: PropTypes.string.isRequired,
  selectedId: PropTypes.string,
  formCallback: PropTypes.func.isRequired,
  formField: PropTypes.string.isRequired,
  idKey: PropTypes.string.isRequired,
};

export default SelectableModalList;
