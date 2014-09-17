; module.exports = function(listApp) {
  listApp.directive("hakutoiveenVastaanotto", ["localization", "restResources", function (localization, restResources) {
    return {
      restrict: 'E',
      scope: {
        hakutoiveet: '&hakutoiveet',
        applicationOid: '=applicationOid',
        hakuOid: '=hakuOid',
        updateApplication: '=updateApplication'
      },
      templateUrl: 'templates/hakutoiveenVastaanotto.html',
      link: function (scope, element, attrs) {
        scope.vastaanottotila = ""
        scope.localization = localization
        scope.ajaxPending = false
        scope.error = ""

        scope.vastaanotaHakutoive = function(hakutoive) {
          var scope = this
          scope.error = ""
          scope.ajaxPending = true

          restResources.vastaanota.post({applicationOid: scope.applicationOid, hakuOid: scope.hakuOid }, {
            hakukohdeOid: hakutoive.koulutus.oid,
            tila: this.vastaanottotila
          }, onSuccess, onError)

          function onSuccess(updatedApplication) {
            scope.ajaxPending = false
            scope.error = ""
            scope.updateApplication(updatedApplication)
          }

          function onError() {
            scope.error = "virhe" // TODO: localize
            scope.ajaxPending = false
          }
        }
      }
    }
  }])
}