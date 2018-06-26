module.exports = function(app) {
  app.directive("hakutoiveenVastaanotto", ["localization", "restResources", "$timeout", "VASTAANOTTO_ACTION",
    function (localization, restResources, $timeout, VASTAANOTTO_ACTION) {
    return {
      restrict: 'E',
      scope: {
        applicationOid: '&applicationOid',
        haku: '&haku',
        hakutoiveet: '&hakutoiveet',
        callback: '=callback'
      },
      template: require('./hakutoiveenVastaanotto.html'),
      link: function (scope, element, attrs) {
        scope.vastaanottoAction = ""
        scope.localization = localization
        scope.ajaxPending = false
        scope.error = ""
        scope.VASTAANOTTO_ACTION = VASTAANOTTO_ACTION
        try {
          scope.email = scope.$parent.$parent.application.henkilotiedot['Sähköposti'].answer
        }
        catch (e) {
          scope.email = '';
        }

        scope.formatTimestamp = function(dt) {
            return moment(dt).format('LLL').replace(/,/g, "")
        }

        scope.inputIsDisabled = function() {
          return this.ajaxPending || this.vastaanottoSentSuccessfully
        }

        scope.flashSiirtohakuNotification = function() {
          scope.siirtohakuClass = 'siirtohaku-fade-out'
          $timeout(function() {
            scope.siirtohakuClass = 'siirtohaku-fade-in'
          }, 50)
        }

        scope.vastaanotaHakutoive = function(hakutoive) {
          var scope = this
          scope.error = ""
          scope.ajaxPending = true

          restResources.vastaanota.post({hakemusOid: scope.applicationOid(), hakukohdeOid: hakutoive.hakukohdeOid}, {
            vastaanottoAction: { action:scope.vastaanottoAction },
            email: scope.email,
            hakukohdeNimi: hakutoive.hakukohdeNimi,
            tarjoajaNimi: hakutoive.tarjoajaNimi
          }, onSuccess, onError)

          function onSuccess(updatedApplication) {
            scope.ajaxPending = false
            scope.error = ""
            scope.vastaanottoSentSuccessfully = true
            $timeout(function() {
              scope.callback(hakutoive, updatedApplication)
            }, 3500);
          }

          function onError(err) {
            var saveError = (function() {
             if (err.status == 401)
                return "error.saveFailed_sessionExpired"
              else if (err.status == 500)
                return "error.serverError"
              else if (err.status == 403)
                return "error.priorAcceptance"
              else
                return "error.saveFailed"
            })()
            scope.error = localization(saveError)
            scope.ajaxPending = false
          }
        }
      }
    }
  }])
}
