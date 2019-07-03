// Copy and pasted from react-intercom: https://github.com/nhagen/react-intercom

// We wanted to make a change to the library to poll Intercom
// every 20 seconds. This is required to support our onboarding chat feature.
// Since the file is so small, we decided to copy & paste the code rather
// than forking react-intercom.

import React, { Component, PropTypes } from 'react';
const canUseDOM = !!(
  (typeof window !== 'undefined' &&
  window.document && window.document.createElement)
);

export const IntercomAPI = (...args) => {
  if (canUseDOM && window.Intercom) {
    window.Intercom.apply(null, args);
  } else {
    console.warn('Intercom not initialized yet');
  }
};

export default class Intercom extends Component {
  static propTypes = {
    appID: PropTypes.string,
    app_id: PropTypes.string
  };

  static displayName = 'Intercom';

  constructor(props) {
    super(props);

    const {
      appID,
      ...otherProps,
    } = props;

    if (!appID || !canUseDOM) {
      return;
    }

    if (!window.Intercom) {
      (function(w, d, id, s, x) {
        function i() {
            i.c(arguments);
        }
        i.q = [];
        i.c = function(args) {
            i.q.push(args);
        };
        w.Intercom = i;
        s = d.createElement('script');
        s.async = 1;
        s.src = 'https://widget.intercom.io/widget/' + id;
        x = d.getElementsByTagName('script')[0];
        x.parentNode.insertBefore(s, x);
      })(window, document, appID);
    }

    window.intercomSettings = { ...otherProps, app_id: appID };

    if (window.Intercom) {
      window.Intercom('boot', otherProps);
    }
  }

  componentWillMount() {
    if (!canUseDOM) return;

    // poll Intercom every 20 seconds for changes
    this.pollIntercomId = setInterval(() => window.Intercom('update'), 20000);
  }

  componentWillReceiveProps(nextProps) {
    const {
      appID,
      ...otherProps,
    } = nextProps;

    if (!canUseDOM) return;

    window.intercomSettings = { ...otherProps, app_id: appID };
    window.Intercom('update', otherProps);
  }

  shouldComponentUpdate() {
    return false;
  }

  componentWillUnmount() {
    if (!canUseDOM) return false;
    clearInterval(this.pollIntercomId);

    window.Intercom('shutdown');

    delete window.Intercom;
  }

  render() {
    return false;
  }
}
