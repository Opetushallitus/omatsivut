var util = require("../util")

module.exports = function(listApp) {
  listApp.directive("valintatulos", ["localization", "restResources", "settings", function (localization, restResources, settings) {
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

        $scope.$on("hakutoive-vastaanotettu", function(e, hakutoive) {
          var item = $(_(element.find("tbody tr")).find(function(tr) {
            return angular.element(tr).scope().tulos.koulutus.oid === hakutoive.koulutus.oid
          }))

          item.css({ "opacity": 0 })

          window.setTimeout(function() {
            item.animate({"opacity": 1}, settings.uiTransitionTime)
          }, settings.uiTransitionTime*2)
        })

        $scope.valintatulosText = function(valintatulos) {
          var key
          if (!_.isEmpty(valintatulos.tilankuvaus)) {
            return valintatulos.tilankuvaus
          } else if (["VASTAANOTTANUT", "EI_VASTAANOTETTU_MAARA_AIKANA", "EHDOLLISESTI_VASTAANOTTANUT"].indexOf(valintatulos.vastaanottotila) >= 0) {
            key = util.underscoreToCamelCase(valintatulos.vastaanottotila)
            return localization("label.resultState." + key)
          } else if (valintatulos.tila === "VARALLA" && valintatulos.varasijojaTaytetaanAsti != null) {
            key = util.underscoreToCamelCase(valintatulos.tila)
            return localization("label.resultState.VarallaPvm", {
              varasija: valintatulos.varasijanumero,
              varasijaPvm: $scope.formatDate(valintatulos.varasijojaTaytetaanAsti)
            })
          } else {
            key = util.underscoreToCamelCase(valintatulos.tila)
            return localization("label.resultState." + key, {
              varasija: valintatulos.varasijanumero
            })
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