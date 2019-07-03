$(document).ready(function () {
  document.getElementById('twitter-share').href   = "https://twitter.com/home?status=" + document.URL;
  document.getElementById('facebook-share').href  = "https://www.facebook.com/sharer/sharer.php?u=" + document.URL;
  var title = document.getElementById('linkedin-share').getAttribute("data-title").split(' ').join("%20");
  document.getElementById('linkedin-share').href  = "https://www.linkedin.com/shareArticle?mini=true&url=" + document.URL + "&title=" + title + "&summary=&source=";
});
