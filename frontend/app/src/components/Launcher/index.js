import _ from 'lodash';
import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import { Link, hashHistory } from 'react-router';
import LoadingScreen from 'components/LoadingScreen';
import * as actions from 'actions';
import {
  COMPANY_BASE,
  getRoute,
  routeToMicroservice,
} from 'constants/paths';

require('./launcher.scss');

class Launcher extends React.Component {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch(actions.getWhoAmI());
  }

  componentDidUpdate() {
    const { companies, teams, isFetching } = this.props;

    if (!isFetching) {
      // redirect if not an admin
      if (companies.length === 0) {
        /* eslint-disable */
        // route to new company sign up if no privileges
        if (teams.length === 0) {
          window.location = routeToMicroservice('www', '/new_company/');

        // route to myaccount if a worker
        } else {
          window.location = routeToMicroservice('myaccount');
        }
        /* eslint-enable */
      }
      // if only a member of 1 organization, route them directly
      if (companies.length === 1) {
        const company = companies.pop();
        hashHistory.push(
          getRoute(COMPANY_BASE, { companyId: company.id })
        );
      }
    }
  }

  render() {
    const { companies, isFetching } = this.props;

    if (isFetching) {
      return (
        <LoadingScreen />
      );
    }

    return (
      <ul className="company-launcher mdl-list">
        {_.map(companies, (company) => {
          const route = getRoute(COMPANY_BASE, { companyId: company.id });
          const liKey = `launcher-li-${company.id}`;
          const linkKey = `launcher-a-${company.id}`;
          return (
            <li className="mdl-list__item" key={liKey}>
              <Link
                key={linkKey}
                className="mdl-list__item-primary-content company"
                to={route}
              >
                {company.name}
              </Link>
            </li>
          );
        })}
      </ul>
    );
  }
}

Launcher.propTypes = {
  dispatch: PropTypes.func.isRequired,
  companies: PropTypes.array.isRequired,
  teams: PropTypes.array.isRequired,
  isFetching: PropTypes.bool.isRequired,
};

function mapStateToProps(state) {
  const { data, isFetching } = state.whoami;
  const admin = _.get(data, 'adminOfList', {});
  const companies = _.get(admin, 'companies') || [];
  const worker = _.get(data, 'workerOfList', {});
  const teams = _.get(worker, 'teams') || [];

  return {
    isFetching: isFetching || _.isEmpty(data),
    companies,
    teams,
  };
}

export default connect(mapStateToProps)(Launcher);
