; module.exports = function(listApp) {
  listApp.directive("hakutoiveenVastaanotto", ["localization", "restResources", function (localization, restResources) {
    return {
      restrict: 'E',
      scope: {
        hakutoiveet: '&hakutoiveet',
        applicationOid: '=applicationOid',
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

          restResources.valitseOpetuspiste.put({applicationId: scope.applicationOid }, {
            opetuspiste: hakutoive.opetuspiste.oid,
            vastaanottotila: this.vastaanottotila
          }, onSuccess, onError)

          function onSuccess() {

          }

          function onError() {
            scope.error = "virhe"
            scope.ajaxPending = false
          }
        }
      }
    }
  }])
}