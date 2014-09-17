; module.exports = function(listApp) {
  listApp.directive("hakutoiveenVastaanotto", ["localization", "restResources", function (localization, restResources) {
    return {
      restrict: 'E',
      scope: {
        application: '=application',
        updateApplication: '=updateApplication'
      },
      templateUrl: 'templates/hakutoiveenVastaanotto.html',
      link: function (scope, element, attrs) {
        scope.vastaanottotila = ""
        scope.localization = localization
        scope.ajaxPending = false
        scope.error = ""

        scope.formatTimestamp = function(dt) {
            return moment(dt).format('LLL').replace(/,/g, "")
        }

        scope.vastaanotaHakutoive = function(hakutoive) {
          var scope = this
          scope.error = ""
          scope.ajaxPending = true

          restResources.vastaanota.post({applicationOid: scope.application.oid, hakuOid: scope.application.haku.oid}, {
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