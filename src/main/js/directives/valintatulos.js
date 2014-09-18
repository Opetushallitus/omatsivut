var util = require("../util")

module.exports = function(listApp) {
  listApp.directive("valintatulos", ["localization", "restResources", function (localization, restResources) {
    return {
      restrict: 'E',
      scope: {
        valintatulos: '&data'
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

        $scope.valintatulosText = function(valintatulos) {
          var tila = util.underscoreToCamelCase(valintatulos.tila)
          var localizationString = (tila=== "Varalla" && valintatulos.varasijojaTaytetaanAsti != null) ? "label.resultState.VarallaPvm" : "label.resultState." + tila
          return localization(localizationString, {
            varasija: valintatulos.varasijanumero,
            varasijaPvm: $scope.formatDate(valintatulos.varasijojaTaytetaanAsti)
          })
        }

        $scope.valintatulosColor = function(valintatulos) {
          var tila = util.underscoreToCamelCase(valintatulos.tila)
          if (tila == "Hyvaksytty" || tila == "HarkinnanvaraisestiHyvaksytty")
            return "green"
          else if (tila == "Hylatty" || tila == "Perunut" || tila == "Peruutettu")
            return "gray"
          else if (tila == "Kesken" || tila == "Varalla")
            return "blue"
          else
            return "transparent lighter italic"
        }
      }
    }
  }])
}