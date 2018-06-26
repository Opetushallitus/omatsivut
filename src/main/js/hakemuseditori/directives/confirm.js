export default function(app) {
  app.directive("confirm", function () {
    return {
      scope: {
        callback: '&confirmAction'
      },
      link: function (scope, element, attrs) {
        var originalText = ""

        function cancel() {
          element.removeClass("confirm")
          element.text(originalText)
          element.off(".cancelConfirm")
          $("body").off(".cancelConfirm")
        }

        element.on("click", function () {
          if (element.hasClass("confirm")) {
            scope.$apply(scope.callback)
            cancel()
          } else {
            element.hide()
            element.addClass("confirm")
            originalText = element.text()
            element.text(attrs.confirmText)
            $("body").one("click.cancelConfirm", cancel)
            element.fadeIn(100)
          }
          return false
        })
      }
    }
  })
}
