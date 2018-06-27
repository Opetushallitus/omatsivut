export default function() {
  return {
    link: function (scope, element) {
      element.on("mousedown", function(event) {
        event.preventDefault()
      })
    }
  }
}
