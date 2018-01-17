module.exports = function(app) {
  app.directive("localizedLink", ["$sce", "localization", function ($sce, localization) {
    return {
      restrict: "E",
      template: "",
      link: function (scope, element, attrs) {
        var linkString = localization(attrs["key"])
        var link = $("<span>" + linkString + "</span>")
          .find("a")
          .attr("href", attrs["href"])
          .attr("target", attrs["target"])
          .end()
        // angular re-evaluates the original href expression and attrs.href needs to be watched
        // <localized-link key="message.rememberSendAttachments"
        //     href="{{url('omatsivut.applications.preview', application.oid) + '#liitteet'}}" target="_blank">
        // link function is called with: attrs.href == "#liitteet"
        // watched value for attrs.href becomes: "/omatsivut/secure/applications/preview/1.2.246.562.11.00000877699#liitteet"
        // angular, WTF?
        scope.$watch("attrs.href", function(value) {
          link.find("a").attr("href", attrs.href)
        });
        element.append(link)
      }
    }
  }])
}