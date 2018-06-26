export default function(app) {
  app.directive("formattedTime", [function () {
    return {
      restrict: "A",
      template: "",
      scope: {
        formattedTime: "=formattedTime"
      },
      link: function (scope, element, attrs) {
        scope.$watch("formattedTime", function(dt) {
          if (dt == null) {
            return ""
          } else {
            var m = moment(dt)
            element.attr("datetime", m.format())
            element.text(m.format(attrs.format || 'LLLL').replace(/,/g, ""))
          }
        })
      }
    }
  }])
}
