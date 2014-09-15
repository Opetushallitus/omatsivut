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

  listApp.directive("callout", function() {
    return {
      link: function (scope, element, attrs) {
        element.addClass("callout")
        element.prepend($("<div/>", {class: "callout-close"}))
        element.on("click", ".callout-close", function() {
          element.fadeOut("fast")
        })
        scope.$on("show-callout", function(evt, calloutId, toggle) {
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

  listApp.directive("confirm", function () {
    return {
      scope: {
        callback : '&confirmAction'
      },
      link: function (scope, element, attrs) {
        function cancel() {
          element.removeClass("confirm")
          element.text(originalText)
          element.off(".cancelConfirm")
          $("body").off(".cancelConfirm")
        }

        var originalText = element.text()

        element.on("click", function() {
          if (element.hasClass("confirm")) {
            scope.$apply(scope.callback)
            cancel()
          } else {
            element.hide()
            element.addClass("confirm")
            element.text(attrs.confirmText)
            $("body").one("click.cancelConfirm", cancel)
            element.one("mouseout.cancelConfirm", function() { element.blur() })
            element.one("blur.cancelConfirm", cancel)
            element.fadeIn(100)
          }
          return false
        })
      }
    }
  })

  listApp.directive("localizedLink", ["$sce", "localization", function($sce, localization) {
    return {
      restrict: "E",
      template: "",
      link: function(scope, element, attrs) {
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