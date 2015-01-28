module.exports = function(listApp) {
  listApp.directive("localizedLink", ["$sce", "localization", function ($sce, localization) {
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
        element.append(link)
      }
    }
  }])
}