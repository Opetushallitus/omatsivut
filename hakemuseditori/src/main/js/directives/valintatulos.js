var util = require("../util")

module.exports = function(app) {
  app.directive('ignoreDirty', [function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, elm, attrs, ctrl) {
        ctrl.$pristine = false;
      }
    }
  }]);
  app.directive("valintatulos", ["localization", "restResources", "settings", "VASTAANOTTOTILA", "VASTAANOTTO_ACTION", function (localization, restResources, settings, VASTAANOTTOTILA, VASTAANOTTO_ACTION) {
    return {
      restrict: 'E',
      scope: {
        valintatulos: '&data',
        hakemus: '=application',
        isFinal: '&final',
        callback: '=callback'
      },
      templateUrl: 'templates/valintatulos.html',
      link: function ($scope, element, attrs) {
        $scope.localization = localization
        $scope.VASTAANOTTOTILA = VASTAANOTTOTILA
        $scope.error = false
        $scope.formatDate = function(dt) {
          if (dt == null)
            return ""
          else
            return moment(dt).format('LL').replace(/,/g, "")
        }

        $scope.$watch("isFinal()", function(value) {
          $scope.status = value ? localization("label.resultsFinal") : localization("label.resultsPending")
        })

        $scope.getError = function() {
          if ($scope.error) {
            return localization($scope.error)
          }
        }
        $scope.isHyvaksyttyKesken = function(valintatulos, valintatulokset) {
          if(valintatulos.valintatila === "HYVAKSYTTY") {
            var firstKeskenIndex = _.findIndex(valintatulokset, function(v) { return v.valintatila === "KESKEN" })
            if(firstKeskenIndex != -1) {
              var valintatulosIndex = _.findIndex(valintatulokset, function(v) { return v.hakukohdeOid === valintatulos.hakukohdeOid})
              return firstKeskenIndex < valintatulosIndex;
            }
          }
          return false;
        }
        $scope.valintatulosText = function(valintatulos, valintatulokset) {
          var isHyvaksyttyKesken = $scope.isHyvaksyttyKesken(valintatulos, valintatulokset)
          var key = isHyvaksyttyKesken ? "HyvaksyttyKesken" : util.underscoreToCamelCase(valintatulos.valintatila)

          if ([VASTAANOTTOTILA.VASTAANOTTANUT_SITOVASTI, VASTAANOTTOTILA.EI_VASTAANOTETTU_MAARA_AIKANA, VASTAANOTTOTILA.EHDOLLISESTI_VASTAANOTTANUT].indexOf(valintatulos.vastaanottotila) >= 0) {
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

        $scope.vastaanotaSitovasti = function(hakemus, hakukohde) {
          var email = ''
          try {
            email = hakemus.henkilotiedot['Sähköposti'].answer
          }
          catch (e) {
            email = ''
          }
          $scope.ajaxPending = true

          restResources.vastaanota.post({hakemusOid: hakemus.oid, hakukohdeOid: hakukohde.hakukohdeOid}, {
            vastaanottoAction: { action: VASTAANOTTO_ACTION.VASTAANOTA_SITOVASTI },
            email: email,
            hakukohdeNimi: hakukohde.hakukohdeNimi,
            tarjoajaNimi: hakukohde.tarjoajaNimi
          }, onSuccess, onError)

          function onSuccess(updatedApplication) {
            $scope.ajaxPending = false
            $scope.error = ""
            $scope.vastaanottoSentSuccessfully = true
            $scope.callback(hakukohde, updatedApplication)
          }

          function onError(err) {
            $scope.ajaxPending = false
            $scope.error = (function () {
              if (err.status == 401)
                return "error.saveFailed_sessionExpired"
              else if (err.status == 500)
                return "error.serverError"
              else if (err.status == 403)
                return "error.priorAcceptance"
              else
                return "error.saveFailed"
            })()
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
