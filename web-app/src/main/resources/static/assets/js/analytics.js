function getEnvironmentSlug() {
  var env = 'DEV';
  var url = window.location.href.toLowerCase();
  var domain = url.split('/')[2];

  if (domain.endsWith('.staffjoy.xyz')) {
    env = 'PROD';
  } else if (domain.endsWith('.staffjoystaging.xyz')) {
    env = 'STAGE';
  }

  return env;
}

function initializeGoogleAnalytics(env) {
  env = env || 'DEV';
  var googleAnalyticsMap = {
    PROD: {
      key: 'UA-57208929-1',
      domains: ['*.staffjoy.xyz'],
    },
    STAGE: {
      key: 'UA-57208929-7',
      domains: ['*.staffjoystaging.xyz'],
    },
  };

  if (env === 'PROD' || env === 'STAGE') {
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

    ga('create', googleAnalyticsMap[env].key, 'auto', {'allowLinker': true});
    ga('require', 'linker');
    ga('send', 'pageview');
    ga('linker:autoLink', googleAnalyticsMap[env].domains);
  }
}

function intercomLauncher(env, customLauncher, launcherId) {
  var whoamiEndpoint;
  env = env || 'DEV';
  customLauncher = !!customLauncher;
  launcherId = launcherId || '';

  var intercomMap = {
    PROD: 'u0h29085',
    STAGE: 'x4atsrz6',
    DEV: 'x4atsrz6',
  };
  
  switch(env) {
    case 'DEV':
      return;
    case 'STAGE':
      whoamiEndpoint = "https://whoami.staffjoystaging.xyz/intercom/";
      break;
    case 'PROD':
      whoamiEndpoint = "https://whoami.staffjoy.xyz/intercom/";
        break;
    default:
        return;
  }

  $.ajax({
      url: whoamiEndpoint,
      cache: false,
      dataType: 'json',
      type: 'GET',
      xhrFields: {
        withCredentials: true
      },
      success: function(data, statusText, xhr) {
        window.intercomSettings = data
        if (customLauncher) {
          window.intercomSettings['custom_launcher_selector'] = launcherId;
          //window.intercomSettings['hide_default_launcher'] = false;
        }
        (function() {
          var w = window;
          var ic = w.Intercom;
          if (typeof ic === "function") {
            ic('reattach_activator');
            ic('update', intercomSettings);
          } else {
            var d = document;
            var i = function() {
                i.c(arguments)
            };
            i.q = [];
            i.c = function(args) {
                i.q.push(args)
            }
            ;
            w.Intercom = i;

            var s = d.createElement('script');
            s.type = 'text/javascript';
            s.async = true;
            s.src = 'https://widget.intercom.io/widget/' + data.app_id;
            var x = d.getElementsByTagName('script')[0];
            x.parentNode.insertBefore(s, x);
          }
      })();

      },
      error: function(jqXHR, textStatus, errorThrown) {
        window.intercomSettings = {
          app_id: jqXHR.responseJSON.app_id,
        };
        if (customLauncher) {
          window.intercomSettings['custom_launcher_selector'] = launcherId;
          //window.intercomSettings['hide_default_launcher'] = true;
        }
        (function() {
          var w = window;
          var ic = w.Intercom;
          if (typeof ic === "function") {
              ic('reattach_activator');
              ic('update', intercomSettings);
          } else {
              var d = document;
              var i = function() {
                  i.c(arguments)
              };
              i.q = [];
              i.c = function(args) {
                  i.q.push(args)
              }
              ;
              w.Intercom = i;
              function l() {
                  var s = d.createElement('script');
                  s.type = 'text/javascript';
                  s.async = true;
                  s.src = 'https://widget.intercom.io/widget/' + window.intercomSettings.app_id;
                  var x = d.getElementsByTagName('script')[0];
                  x.parentNode.insertBefore(s, x);
              }
              l()
              if (w.attachEvent) {
                  w.attachEvent('onload', l);
              } else {
                  w.addEventListener('load', l, false);
              }
          }
      })();

      }
  });
};