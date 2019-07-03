import _ from 'lodash';
import React, { PropTypes } from 'react';
import { Link } from 'react-router';
import { connect } from 'react-redux';
import { Drawer, Navigation } from 'react-mdl';
import { companyNavLinks } from 'constants/sideNavigation';
import * as paths from 'constants/paths';
import NavigationLogo from './Logo';
import SideNavigationTeamSection from './TeamSection';
import SideNavigationUserContext from './UserContext';

require('./side-navigation.scss');

function NavigationSide({
  companyId,
  companyName,
  currentPath,
  companyPermissions,
  teams,
  userName,
  userPhotoUrl,
}) {
  return (
    <Drawer>
      <NavigationLogo companyId={companyId} />
      <Navigation>
        {
          _.map(companyNavLinks, (link) => {
            const route = paths.getRoute(link.pathName, { companyId });

            // 'mdl-navigation__link' is automatically added to all links
            const className = (currentPath === route) ? 'active' : '';

            return (
              <Link
                key={link.pathName}
                className={className}
                to={route}
              >
                {link.displayName}
              </Link>
            );
          })
        }
      </Navigation>
      <div className="team-navigation">
        {
          _.map(teams, team =>
            <SideNavigationTeamSection
              key={team.id}
              companyId={companyId}
              teamId={team.id}
              name={team.name}
              color={team.color}
              currentPath={currentPath}
            />
          )
        }
      </div>
      <SideNavigationUserContext
        companyId={companyId}
        companyName={companyName}
        companyPermissions={companyPermissions}
        userName={userName}
        userPhotoUrl={userPhotoUrl}
      />
    </Drawer>
  );
}

function mapStateToProps(state) {
  const admin = _.get(state.whoami.data, 'adminOfList', {});
  const companyPermissions = _.get(admin, 'companies') || [];
  const userData = state.user.data;
  const companyName = state.company.data.name || '';
  let teams = [];

  teams = _.map(state.teams.order, value =>
    state.teams.data[value]
  );

  return {
    currentPath: state.routing.locationBeforeTransitions.pathname,
    userName: _.get(userData, 'name', ''),
    userPhotoUrl: _.get(userData, 'photoUrl', ''),
    companyName,
    companyPermissions,
    teams,
  };
}

NavigationSide.propTypes = {
  companyId: PropTypes.string.isRequired,
  companyName: PropTypes.string.isRequired,
  currentPath: PropTypes.string.isRequired,
  userName: PropTypes.string.isRequired,
  userPhotoUrl: PropTypes.string.isRequired,
  companyPermissions: PropTypes.array.isRequired,
  teams: PropTypes.array.isRequired,
};

export default connect(mapStateToProps)(NavigationSide);
