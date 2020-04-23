import { VASTAANOTTOTILA, VASTAANOTTO_ACTION } from '../constants';
import { underscoreToCamelCase } from '../util';
import { getLanguage } from '../staticResources';
import localize from '../localization';

const _ = require('underscore');

export default ["restResources", function(restResources) {
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
        if(valintatulos && valintatulos.valintatila === "HYVAKSYTTY") {
          var firstKeskenIndex = _.findIndex(valintatulokset, function(v) { return v.valintatila === "KESKEN" || v.valintatila === "VARALLA"})
          if(firstKeskenIndex != -1) {
            var valintatulosIndex = _.findIndex(valintatulokset, function(v) { return v.hakukohdeOid === valintatulos.hakukohdeOid})
            // If hyvaksytty hakemus is not vastaanotettavissa it belongs to kk haku with sijoittelu
            return firstKeskenIndex < valintatulosIndex && valintatulos.vastaanotettavuustila === "EI_VASTAANOTETTAVISSA";
          }
        }
        return false;
      }

      $scope.hakutoiveenValintatulosText = function(valintatulos, valintatulokset) {
        var isHyvaksyttyKesken = $scope.isHyvaksyttyKesken(valintatulos, valintatulokset);
        var key = isHyvaksyttyKesken ? "HyvaksyttyKesken" : underscoreToCamelCase(valintatulos.valintatila);

        if ([VASTAANOTTOTILA.VASTAANOTTANUT_SITOVASTI, VASTAANOTTOTILA.EI_VASTAANOTETTU_MAARA_AIKANA, VASTAANOTTOTILA.EHDOLLISESTI_VASTAANOTTANUT].indexOf(valintatulos.vastaanottotila) >= 0) {
          key = underscoreToCamelCase(valintatulos.vastaanottotila);
          return localize("label.resultState." + key)
        } else if (!_.isEmpty(tilanKuvaus(valintatulos))) {
          if(valintatulos.valintatila === "PERUNUT"){
            return localize("label.resultState." + key)
          } else if(valintatulos.valintatila === "HYLATTY"){
            return localize("label.resultState." + key)
          } else if(hyvaksytty(valintatulos) && valintatulos.ehdollisestiHyvaksyttavissa) {
            return localize("label.resultState." + key) + localize("label.resultState.EhdollinenPostfix")
          }
        } else if (valintatulos.valintatila === "VARALLA" && valintatulos.varasijojaTaytetaanAsti != null) {
          return localize("label.resultState.VarallaPvm", {
            varasija: valintatulos.varasijanumero ? valintatulos.varasijanumero + "." : "",
            varasijaPvm: $scope.formatDate(valintatulos.varasijojaTaytetaanAsti)
          })
        } else if(hyvaksytty(valintatulos) && valintatulos.ehdollisestiHyvaksyttavissa) {
          return localize("label.resultState." + key) + localize("label.resultState.EhdollinenPostfix")
        }
        return localize("label.resultState." + key, {
          varasija: valintatulos.varasijanumero ? valintatulos.varasijanumero + "." : ""
        })
      };

      $scope.hakutoiveenValintatilanKuvaus = function(valintatulos) {
        if (
          [
            VASTAANOTTOTILA.VASTAANOTTANUT_SITOVASTI,
            VASTAANOTTOTILA.EI_VASTAANOTETTU_MAARA_AIKANA,
            VASTAANOTTOTILA.EHDOLLISESTI_VASTAANOTTANUT,
          ].indexOf(valintatulos.vastaanottotila) === -1
        ) {
          if (
            hyvaksytty(valintatulos) &&
            valintatulos.ehdollisestiHyvaksyttavissa
          ) {
            return valintatulos[
              localize(
                'label.resultState.EhdollisenHyvaksymisenEhdonKentanNimi'
              )
            ]
          }
          return tilanKuvaus(valintatulos)
        }
      }

      $scope.capitalize = function(str) {
        return str ? `${str.charAt(0).toUpperCase()}${str.slice(1).toLowerCase()}` : ''
      }

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

      $scope.toggleJonokohtaisetTulostiedotVisibility = function(hakutoive) {
        if (!$scope.jonokohtaisetTulostiedotVisibility) {
          $scope.jonokohtaisetTulostiedotVisibility = {
            [hakutoive.hakukohdeOid]: true
          }
          return
        }
        $scope.jonokohtaisetTulostiedotVisibility[hakutoive.hakukohdeOid] = !$scope.jonokohtaisetTulostiedotVisibility[hakutoive.hakukohdeOid]
      }

      $scope.isJonokohtaisetTulostiedotVisible = function(hakutoive) {
        return $scope.jonokohtaisetTulostiedotVisibility
          && $scope.jonokohtaisetTulostiedotVisibility[hakutoive.hakukohdeOid]
          && $scope.hasJonokohtaisetTulostiedot(hakutoive)
      }

      $scope.hasJonokohtaisetTulostiedot = function(hakutoive) {
        return hakutoive.jonokohtaisetTulostiedot && hakutoive.jonokohtaisetTulostiedot.length !== 0
      }

      $scope.sortJonokohtaisetTulostiedot = function(jonokohtaisetTulostiedot) {
        return jonokohtaisetTulostiedot
          .map(x => x)
          .sort((a, b) => {
            if (!a.hasOwnProperty('valintatapajonoPrioriteetti') || !b.hasOwnProperty('valintatapajonoPrioriteetti')) {
              return 0
            }
            return a.valintatapajonoPrioriteetti - b.valintatapajonoPrioriteetti
          })
      }

      $scope.getValintatilanKuvaus = function(jonokohtainenTulostieto) {
        const { tilanKuvaukset, valintatila } = jonokohtainenTulostieto
        return getLocalizedDisclaimer(tilanKuvaukset, valintatila)
      }

      $scope.getEhdollisenHyvaksymisenEhto = function(jonokohtainenTulostieto) {
        const {
          ehdollisenHyvaksymisenEhto,
          valintatila,
        } = jonokohtainenTulostieto
        return getLocalizedDisclaimer(ehdollisenHyvaksymisenEhto, valintatila)
      }

      $scope.hakutoiveValintatilaStateClass = function(hakutoive) {
        return hakutoive.valintatila === 'HYVAKSYTTY' || hakutoive.valintatila === 'VARASIJALTA_HYVAKSYTTY'
          ? 'hakutoive--hyvaksytty'
          : 'hakutoive--ei-hyvaksytty'
      }

      function getLocalizedDisclaimer(disclaimer, valintatila) {
        const language = getLanguage().toUpperCase()
        const localizedDisclaimer = disclaimer
          ? disclaimer[language] ||
            disclaimer['FI'] ||
            disclaimer['EN'] ||
            disclaimer['SV']
          : undefined
        const localizedValintatila = valintatila
          ? localize(
              'label.jonokohtaisetTulostiedot.valintatilat.' + valintatila
            )
          : undefined
        return localizedDisclaimer &&
          (!localizedValintatila ||
            localizedDisclaimer !== localizedValintatila)
          ? localizedDisclaimer
          : undefined
      }

      $scope.getVarasijaDisclaimer = function(jonokohtainenTulostieto) {
        return jonokohtainenTulostieto.valintatila === 'VARALLA' &&
          (jonokohtainenTulostieto.eiVarasijatayttoa ||
            (jonokohtainenTulostieto.varasijat &&
              parseInt(jonokohtainenTulostieto.varasijat, 10) > 0))
          ? localize('label.varasijaDisclaimer')
          : undefined
      }

      function hyvaksytty(valintatulos) {
        return valintatulos.valintatila == "HYVAKSYTTY" || valintatulos.valintatila == "HARKINNANVARAISESTI_HYVAKSYTTY" || valintatulos.valintatila == "VARASIJALTA_HYVAKSYTTY"
      }

      function tilanKuvaus(valintatulos) {
        return valintatulos.tilanKuvaukset[getLanguage().toUpperCase()]
      }
    }
  }
}]
