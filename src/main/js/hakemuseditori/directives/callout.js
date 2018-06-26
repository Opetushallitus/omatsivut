export default function(app) {
  app.directive("callout", function () {
    return {
      link: function (scope, element, attrs) {
        element.addClass("callout")
        element.prepend($("<div/>", {class: "callout-close"}))
        element.on("click", ".callout-close", function () {
          element.fadeOut("fast")
        })
        scope.$on("show-callout", function (evt, calloutId, toggle) {
          if (attrs.callout == calloutId) {
            if (toggle)
              element.fadeIn("fast")
            else
              element.fadeOut("fast")
          }
        })
      }
    }
  })
}
