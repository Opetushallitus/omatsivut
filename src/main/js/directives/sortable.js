module.exports = function(listApp) {
  listApp.directive('sortable', ["settings", function(settings) {
    return function($scope, $element, attrs) {
      var slide = function(el, offset) {
        el.css("transition", "all 0.5s")
        el.css("transform", "translate3d(0px, " + offset + "px, 0px)")
      }

      var resetSlide = function(el) {
        el.css({
          "transition": "",
          "transform": ""
        })
      }

      var switchPlaces = function(element1, element2) {
        var diffY = Math.abs(element1.offset().top - element2.offset().top)

        if (element1.index() < element2.index()) {
          slide(element1, element2.outerHeight() + diffY-element1.outerHeight())
          slide(element2, -diffY)
        } else {
          slide(element2, element1.outerHeight() + diffY-element2.outerHeight())
          slide(element1, -diffY)
        }

        setTimeout(function() {
          $scope.$apply(function(self) {
            var items = $element.find(attrs.sortableItem)
            self[attrs.sortableMoved](items.index(element1), items.index(element2))
            resetSlide(element1)
            resetSlide(element2)
          })
        }, settings.uiTransitionTime)
      }

      var arrowClicked = function(elementF) {
        return function(evt) {
          var btn = $(evt.target)
          if (!btn.hasClass("disabled")) {
            var element1 = btn.closest(attrs.sortableItem)
            var element2 = element1[elementF]()
            switchPlaces(element1, element2)
          }
        }
      }

      $element.on("click", ".sort-arrow-down", arrowClicked("next"))
      $element.on("click", ".sort-arrow-up", arrowClicked("prev"))
    }
  }])
}