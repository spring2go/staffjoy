import React, { PropTypes } from 'react';

require('./checkbox-field.scss');

function CheckboxField({ id, description, attribute, checked, onChange }) {
  return (
    <div className="checkbox-field">
      <input
        name={id}
        id={id}
        type="checkbox"
        data-model-attribute={attribute}
        checked={checked}
        onChange={onChange}
      />
      <span>{description}</span>
    </div>
  );
}
CheckboxField.propTypes = {
  attribute: PropTypes.string,
  checked: React.PropTypes.bool,
  description: PropTypes.string.isRequired,
  id: PropTypes.string.isRequired,
  onChange: React.PropTypes.func,
};

export default CheckboxField;
