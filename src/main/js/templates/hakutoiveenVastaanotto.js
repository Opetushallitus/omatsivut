; module.exports = function(listApp) {
  listApp.directive("hakutoiveenVastaanotto", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        hakutoiveet: '&hakutoiveet',
        confirmCallback: '=callback'
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
          scope.confirmCallback({ hakutoive: hakutoive, vastaanottotila: this.vastaanottotila }, failedCallback)

          function failedCallback(errorText) {
            scope.error = errorText
            scope.ajaxPending = false
          }
        }
      }
    }
  }])
}