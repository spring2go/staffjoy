$(document).ready(function () {

  /*
  $('.sign-up-submit').click(function(e) {
    var $button = $(e.target);
    var $input = $button.closest(".sign-up-form").find(".mdl-textfield__input");
    var $warning = $button.closest(".sign-up-form").find(".warning-message");
    buttonAnimate($button, $input, $warning);
  });

  $('.sign-up-form .mdl-textfield__input').keypress(function(e) {
    if (e.which === 13) {
      var $input = $(e.target);
      var $button = $input.closest(".sign-up-form").find(".mdl-button");
      var $warning = $button.closest(".sign-up-form").find(".warning-message");
      buttonAnimate($button, $input, $warning);
    }
  });
  */

  var envSlug = getEnvironmentSlug();
  intercomLauncher(envSlug, true, '#intercom-launcher');
  initializeGoogleAnalytics(envSlug);
});

// returns true if email contains an @ that is not at either end of the string
function simpleEmailCheck(email) {
  var atSignLocation = email.indexOf("@");
  return (atSignLocation > 0) && (atSignLocation < (email.length - 1));
}

function buttonAnimate($button, $input, $warning) {
  var email = $input.val();
  var $area = $input.closest(".sign-up-area");

  if (simpleEmailCheck(email)) {

    $warning.css("visibility", "hidden");
    $area.removeClass("warning");
    $button.animate({
      opacity: 0.7,
    });
    $button.attr("disabled", "disabled");

    $input.animate({
      opacity: 0.5,
    });
    $input.attr("disabled", "disabled");

    setTimeout(function() {
      $button.text("Email Sent!");
    }, 200);
  } else {
    $warning.css("visibility", "visible");
    $area.addClass("warning");
  }
}