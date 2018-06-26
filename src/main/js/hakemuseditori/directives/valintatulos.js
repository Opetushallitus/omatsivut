import { VASTAANOTTOTILA, VASTAANOTTO_ACTION } from '../../constants';
import { getLanguage } from '../../staticResources';
import localize from '../../localization';

const _ = require('underscore');
const util = require("../util");

export default function(app) {
  app.directive('ignoreDirty', [function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, elm, attrs, ctrl) {
        ctrl.$pristine = false;
      }
    }
  }]);
  app.directive("valintatulos", ["restResources", "settings", function (restResources, settings) {
    return {
      restrict: 'E',
      scope: {
        valintatulos: '&data',
        application: '=application',
        isFinal: '&final',
        callback: '=callback'
      },
      template: require('./valintatulos.html'),
      link: function ($scope, element, attrs) {
        $scope.localization = localize;
        $scope.VASTAANOTTOTILA = VASTAANOTTOTILA;
        $scope.error = false
        $scope.language = getLanguage();

        $scope.formatDate = function(dt) {
          if (dt == null)
            return ""
          else
            return moment(dt).format('LL').replace(/,/g, "")
        }

        $scope.$watch("isFinal()", function(value) {
          $scope.status = value ? localize("label.resultsFinal") : localize("label.resultsPending")
        })

        $scope.getError = function() {
          if ($scope.error) {
            return localize($scope.error)
          }
        }
        $scope.isHyvaksyttyKesken = function(valintatulos, valintatulokset) {
          if(valintatulos.valintatila === "HYVAKSYTTY") {
            var firstKeskenIndex = _.findIndex(valintatulokset, function(v) { return v.valintatila === "KESKEN" || v.valintatila === "VARALLA"})
            if(firstKeskenIndex != -1) {
              var valintatulosIndex = _.findIndex(valintatulokset, function(v) { return v.hakukohdeOid === valintatulos.hakukohdeOid})
              // If hyvaksytty hakemus is not vastaanotettavissa it belongs to kk haku with sijoittelu
              return firstKeskenIndex < valintatulosIndex && valintatulos.vastaanotettavuustila === "EI_VASTAANOTETTAVISSA";
            }
          }
          return false;
        }

        $scope.valintatulosText = function(valintatulos, valintatulokset) {
          var isHyvaksyttyKesken = $scope.isHyvaksyttyKesken(valintatulos, valintatulokset);
          var key = isHyvaksyttyKesken ? "HyvaksyttyKesken" : util.underscoreToCamelCase(valintatulos.valintatila);
          var ehdollisenHyvaksymisenKenttaEhto = localize("label.resultState.EhdollisenHyvaksymisenEhdonKentanNimi");

          if ([VASTAANOTTOTILA.VASTAANOTTANUT_SITOVASTI, VASTAANOTTOTILA.EI_VASTAANOTETTU_MAARA_AIKANA, VASTAANOTTOTILA.EHDOLLISESTI_VASTAANOTTANUT].indexOf(valintatulos.vastaanottotila) >= 0) {
            key = util.underscoreToCamelCase(valintatulos.vastaanottotila);
            return localize("label.resultState." + key)
          } else if (!_.isEmpty(tilanKuvaus(valintatulos))) {
            if(valintatulos.valintatila === "PERUNUT"){
              return localize("label.resultState." + key)
            } else if(valintatulos.valintatila === "HYLATTY"){
              return localize("label.resultState." + key) + " " + tilanKuvaus(valintatulos)
            } else if(hyvaksytty(valintatulos) && valintatulos.ehdollisestiHyvaksyttavissa) {
              if (valintatulos.ehdollisenHyvaksymisenEhtoKoodi !== undefined && valintatulos.ehdollisenHyvaksymisenEhtoKoodi != null) {
                  return localize("label.resultState." + key) + ' (' + valintatulos[ehdollisenHyvaksymisenKenttaEhto] + ')';
              }
              return localize("label.resultState." + key) + localize("label.resultState.EhdollinenPostfix")
            } else {
              return tilanKuvaus(valintatulos)
            }
          } else if (valintatulos.valintatila === "VARALLA" && valintatulos.varasijojaTaytetaanAsti != null) {
            return localize("label.resultState.VarallaPvm", {
              varasija: valintatulos.varasijanumero ? valintatulos.varasijanumero + "." : "",
              varasijaPvm: $scope.formatDate(valintatulos.varasijojaTaytetaanAsti)
            })
          } else if(hyvaksytty(valintatulos) && valintatulos.ehdollisestiHyvaksyttavissa) {
            if (valintatulos.ehdollisenHyvaksymisenEhtoKoodi !== undefined && valintatulos.ehdollisenHyvaksymisenEhtoKoodi != null) {
              return localize("label.resultState." + key) + ' (' + valintatulos[ehdollisenHyvaksymisenKenttaEhto] + ')';
            }
            return localize("label.resultState." + key) + localize("label.resultState.EhdollinenPostfix")
          } else {
            return localize("label.resultState." + key, {
              varasija: valintatulos.varasijanumero ? valintatulos.varasijanumero + "." : ""
            })
          }
        };

        $scope.vastaanotaSitovasti = function(application, hakukohde) {
          var email = ''
          try {
            email = application.henkilotiedot['Sähköposti'].answer
          }
          catch (e) {
            email = ''
          }
          $scope.ajaxPending = true

          restResources.vastaanota.post({hakemusOid: application.oid, hakukohdeOid: hakukohde.hakukohdeOid}, {
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
          return valintatulos.tilanKuvaukset[localize("languageId").toUpperCase()]
        }
      }
    }
  }])
}
