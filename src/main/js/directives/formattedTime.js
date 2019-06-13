export default function () {
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
          var m = window.moment(dt);
          var format = m.format();
          element.attr("datetime", format);

          var text = m.format(attrs.format || 'LLLL Z');
          text = text.replace(/,/g, "").replace(/\+02:00/, "(EET)").replace(/\+03:00/, "(EEST)");
          element.text(text);
        }
      })
    }
  }
}
