var util = require("../util")

module.exports = function(app) {
  app.directive("valintatulos", ["localization", "restResources", "settings", function (localization, restResources, settings) {
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
          var key = util.underscoreToCamelCase(valintatulos.valintatila)

          if (["VASTAANOTTANUT_SITOVASTI", "EI_VASTAANOTETTU_MAARA_AIKANA", "EHDOLLISESTI_VASTAANOTTANUT"].indexOf(valintatulos.vastaanottotila) >= 0) {
            key = util.underscoreToCamelCase(valintatulos.vastaanottotila)
            return localization("label.resultState." + key)
          } else if (!_.isEmpty(tilanKuvaus(valintatulos))) {
            if(valintatulos.valintatila === "HYLATTY"){
              return localization("label.resultState." + key) + " " + tilanKuvaus(valintatulos)
            } else if(hyvaksytty(valintatulos) && valintatulos.ehdollisestiHyvaksyttavissa) {
              return localization("label.resultState." + key) + localization("label.resultState.EhdollinenPostfix")
            } else {
              return tilanKuvaus(valintatulos)
            }
          } else if (valintatulos.valintatila === "VARALLA" && valintatulos.varasijojaTaytetaanAsti != null) {
            return localization("label.resultState.VarallaPvm", {
              varasija: valintatulos.varasijanumero ? valintatulos.varasijanumero + "." : "",
              varasijaPvm: $scope.formatDate(valintatulos.varasijojaTaytetaanAsti)
            })
          } else if(hyvaksytty(valintatulos) && valintatulos.ehdollisestiHyvaksyttavissa) {
            return localization("label.resultState." + key) + localization("label.resultState.EhdollinenPostfix")
          } else {
            return localization("label.resultState." + key, {
              varasija: valintatulos.varasijanumero ? valintatulos.varasijanumero + "." : ""
            })
          }
        }

        $scope.valintatulosStyle = function(valintatulos) {
          if (hyvaksytty(valintatulos))
            return "accepted"
        }

        function hyvaksytty(valintatulos) {
          return valintatulos.valintatila == "HYVAKSYTTY" || valintatulos.valintatila == "HARKINNANVARAISESTI_HYVAKSYTTY" || valintatulos.valintatila == "VARASIJALTA_HYVAKSYTTY"
        }

        function tilanKuvaus(valintatulos) {
          return valintatulos.tilanKuvaukset[$scope.localization("languageId").toUpperCase()]
        }
      }
    }
  }])
}
