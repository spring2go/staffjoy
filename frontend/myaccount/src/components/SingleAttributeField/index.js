import React, { PropTypes } from 'react';

require('./single-attribute-field.scss');

function SingleAttributeField({
  id,
  title,
  fieldValue,
  attribute = '',
  type = 'text',
  onBlur,
  onChange,
}) {
  return (
    <div className="single-attribute-field">
      <label htmlFor={id}>{title}</label>
      <input
        id={id}
        type={type}
        data-model-attribute={attribute}
        value={fieldValue}
        onBlur={onBlur}
        onChange={onChange}
      />
    </div>
  );
}
SingleAttributeField.propTypes = {
  id: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  fieldValue: PropTypes.string.isRequired,
  attribute: PropTypes.string,
  onBlur: PropTypes.func,
  onChange: PropTypes.func,
  type: PropTypes.string,
};

export default SingleAttributeField;
