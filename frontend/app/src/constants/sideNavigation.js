import * as paths from './paths';

export const companyNavLinks = [
  {
    displayName: 'Employees',
    pathName: paths.COMPANY_EMPLOYEES,
  },
  /*{
    displayName: 'History',
    pathName: paths.COMPANY_HISTORY,
  },*/
];

// these links will be available for each team that exists
export const teamNavLinks = [
  {
    displayName: 'Scheduler',
    pathName: paths.TEAM_SCHEDULING,
  },
  {
    displayName: 'Settings',
    pathName: paths.TEAM_SETTINGS,
  },
  /*{
    displayName: 'Shift Board',
    pathName: paths.TEAM_SHIFT_BOARD,
  },*/
];
