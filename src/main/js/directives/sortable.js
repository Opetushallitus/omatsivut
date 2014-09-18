module.exports = function(listApp) {
  listApp.directive('sortable', ["settings", function(settings) {
    return function($scope, $element, attrs) {
      var slide = function(el, offset) {
        el.css("transition", "all 0.5s")
        el.css("transform", "translate3d(0px, " + offset + "px, 0px)")
      }

      var moveDown = function(el) {
        slide(el, el.outerHeight())
      }

      var moveUp = function(el) {
        slide(el, -el.outerHeight())
      }

      var resetSlide = function(el) {
        el.css({
          "transition": "",
          "transform": ""
        })
      }

      var switchPlaces = function(element1, element2) {
        if (element1.index() < element2.index()) {
          moveDown(element1)
          moveUp(element2)
        } else {
          moveUp(element1)
          moveDown(element2)
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