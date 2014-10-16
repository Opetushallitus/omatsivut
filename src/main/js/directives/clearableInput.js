module.exports = function(listApp) {
  listApp.directive("clearableInput", function ($parse) {
    return {
      link: function (scope, element, attrs) {
        var wrapper = $("<div/>", { class: "clearable-input" })
        var clearBtn = $("<div/>", { class: "clear-btn" })
        var container = element.wrap(wrapper).parent()
        container.prepend(clearBtn)

        clearBtn.on("click", function() {
          scope.$apply(function() {
            var model = $parse(attrs.ngModel)
            model.assign(scope, "")
          })
        })

        scope.$watch(attrs.ngModel, function(val) {
          clearBtn.toggle(val.length > 0)
        })
      }
    }
  })
}