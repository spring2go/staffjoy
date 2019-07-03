$(document).ready(function () {
  
  // Detects user scroll position and adds a class to the
  // features as needed. This triggers the CSS animations.
  $(window).scroll(function () {
    
    var scroll = $(this).scrollTop();
    var middle = $(window).height() / 2;

    if ( scroll > $('.feature--schedule').offset().top - middle ) {
      $('.feature--schedule').addClass('is-active');
    }
    if ( scroll > $('.feature--message').offset().top - middle ) {
      $('.feature--message').addClass('is-active');
    }
    if ( scroll > $('.feature--broadcast').offset().top - middle ) {
      $('.feature--broadcast').addClass('is-active');
    }
  });
  

  // This 'types' the headline in the hero. On complete a class
  // is added that triggers the fade in of the Sign Up input
  // the ^700 is a delay before the heart icon appears.
  
  $("#typed").css('height', $("#typed").height()).text("").css("color", "#fff").typed({
    strings: ["Share schedules in less time <i class='fa fa-commenting-o' aria-hidden='true'></i>"],
    typeSpeed: 10,
    showCursor: false,
    callback: complete
  });
  
  function complete() {
    $(".lead").addClass("is-complete");
  }  
});
