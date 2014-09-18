module.exports = function(listApp) {
  listApp.directive("highlightSave", function() {
    return function($scope, $element) {
      $scope.$on("highlight-save", function(event, indexes) {
        var items = $element.find(".preference-list-item")

        _.each(indexes, function(index) {
          items.eq(index).addClass("saved")
        })

        $element.find(".timestamp-row").addClass("saved")

        window.setTimeout(function() {
          items.removeClass("saved")
          $(".timestamp-row").removeClass("saved")
        }, 3000)
      })
    }
  })

  listApp.directive("disableClickFocus", function() {
    return {
      link: function (scope, element) {
        element.on("mousedown", function(event) {
          event.preventDefault()
        })
      }
    }
  })
}