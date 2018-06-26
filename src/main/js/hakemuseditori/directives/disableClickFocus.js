export default function(app) {
  app.directive("disableClickFocus", function() {
    return {
      link: function (scope, element) {
        element.on("mousedown", function(event) {
          event.preventDefault()
        })
      }
    }
  })
}
