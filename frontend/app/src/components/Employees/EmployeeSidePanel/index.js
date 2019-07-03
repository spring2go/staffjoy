import _ from 'lodash';
import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import { Field, reduxForm } from 'redux-form';
import * as actions from 'actions';
import EmployeePanelPhotoName from './PhotoName';
import EmployeeFormField from './FormField';

require('./employee-side-panel.scss');

class EmployeeSidePanel extends React.Component {

  constructor(props) {
    super(props);
    this.handleFieldBlur = this.handleFieldBlur.bind(this);
  }

  componentDidMount() {
    const { dispatch, companyId, employeeId } = this.props;

    // get the employees for the whole company
    dispatch(actions.initializeEmployeeSidePanel(companyId, employeeId));
  }

  componentWillReceiveProps(nextProps) {
    const { dispatch, companyId, employeeId } = this.props;
    const newEmployeeId = nextProps.employeeId;

    // there are a lot of updates that will happen, but only need to fetch
    // if its because of a route change
    if (newEmployeeId !== employeeId) {
      dispatch(
        actions.initializeEmployeeSidePanel(companyId, newEmployeeId)
      );
    }
  }

  handleFieldBlur(event) {
    const { name } = event.target;
    const {
      companyId,
      dispatch,
      employeeId,
      updateEmployeeField,
    } = this.props;

    dispatch(updateEmployeeField(companyId, employeeId, name));
  }

  render() {
    const { employee, updatingFields } = this.props;

    return (
      <div className="employee-side-panel">
        <form>
          <EmployeePanelPhotoName
            name={employee.name}
            photoUrl={employee.photoUrl}
          />
          <div className="info-section" id="contact-information">
            <h4 className="info-section-title">Contact Information</h4>
            <div>
              <Field
                component={EmployeeFormField}
                iconKey="phone"
                name="phoneNumber"
                updateStatus={updatingFields && updatingFields.phoneNumber}
                onBlur={this.handleFieldBlur}
              />
            </div>
            <div>
              <Field
                component={EmployeeFormField}
                iconKey="mail_outline"
                name="email"
                updateStatus={updatingFields && updatingFields.email}
                onBlur={this.handleFieldBlur}
              />
            </div>
          </div>
          <div className="info-section" id="work-information">
            <h4 className="info-section-title">Work Information</h4>
            <p>teams</p>
            <p>jobs</p>
            <p>status</p>
          </div>
          <div className="info-section" id="other-information">
            <h4 className="info-section-title">Other Information</h4>
            <p>note</p>
            <p>wage</p>
          </div>
        </form>
      </div>
    );
  }
}

EmployeeSidePanel.propTypes = {
  dispatch: PropTypes.func.isRequired,
  companyId: PropTypes.string.isRequired,
  employeeId: PropTypes.string.isRequired,
  employee: PropTypes.object.isRequired,
  updateEmployeeField: PropTypes.func.isRequired,
  updatingFields: PropTypes.object.isRequired,
};

function mapStateToProps(state, ownProps) {
  const employeeId = ownProps.routeParams.employeeId;
  const employee = _.get(state.employees.data, employeeId, {});
  const updatingFields = _.get(
    state.employees.updatingFields,
    employeeId,
    {}
  );
  const initialValues = employee;

  return {
    companyId: ownProps.routeParams.companyId,
    employee,
    employeeId,
    initialValues,
    updatingFields,
  };
}

const mapDispatchToProps = dispatch => ({
  updateEmployeeField: actions.updateEmployeeField,
  dispatch,
});

const Form = reduxForm({
  enableReinitialize: true,
  form: 'employee-side-panel',
})(EmployeeSidePanel);
const Container = connect(mapStateToProps, mapDispatchToProps)(Form);
export default Container;
