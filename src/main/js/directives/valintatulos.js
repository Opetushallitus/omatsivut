var util = require("../util")

module.exports = function(listApp) {
  listApp.directive("valintatulos", ["localization", "restResources", function (localization, restResources) {
    return {
      restrict: 'E',
      scope: {
        valintatulos: '&data',
        isFinal: '&final'
      },
      templateUrl: 'templates/valintatulos.html',
      link: function ($scope, element, attrs) {
        $scope.localization = localization

        $scope.formatDate = function(dt) {
          if (dt == null)
            return ""
          else
            return moment(dt).format('LL').replace(/,/g, "")
        }

        $scope.$watch("isFinal()", function(value) {
          $scope.status = value ? localization("label.resultsFinal") : localization("label.resultsPending")
        })

        $scope.valintatulosText = function(valintatulos) {
          if (_.isEmpty(valintatulos.tilankuvaus)) {
            var tila = util.underscoreToCamelCase(valintatulos.tila)
            var localizationString = (tila === "Varalla" && valintatulos.varasijojaTaytetaanAsti != null) ? "label.resultState.VarallaPvm" : "label.resultState." + tila
            return localization(localizationString, {
              varasija: valintatulos.varasijanumero,
              varasijaPvm: $scope.formatDate(valintatulos.varasijojaTaytetaanAsti)
            })
          } else
          {
            return valintatulos.tilankuvaus
          }
        }

        $scope.valintatulosColor = function(valintatulos) {
          if (valintatulos.tila == "PERUUNTUNUT")
            return "canceled"
        }
      }
    }
  }])
}