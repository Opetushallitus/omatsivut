import localize from '../localization';

export default ["restResources", "debounce", "settings", "angularBacon", function (restResources, debounce, settings, angularBacon) {
  return {
    restrict: 'E',
    scope: {
      application: '=application'
    },
    template: require('./henkilotiedot.html'),
    link: function ($scope, element, attrs) {
      $scope.localization = localize;
      $scope.yhteystiedot = $scope.application.henkilotiedot

      var postalCode = angularBacon.watch($scope, "yhteystiedot['Postinumero'].answer").debounce(settings.modelDebounce).skipDuplicates().map(".trim")
      var query = angularBacon.resource(restResources.postOffice.get)

      function length(len) { return function(str) { return str.length === len } }
      function not(f) { return function(val) { return !f(val) }}

      var responses = postalCode.filter(length(5))
        .flatMapLatest(function(code) {
          return query({postalCode: code })
        })

      responses.onValue(function(response) {
        $scope.application.calculatedValues.postOffice = response.postOffice
        $scope.yhteystiedot.Postinumero.setErrors([])
      })

      responses.onError(function() {
        $scope.application.calculatedValues.postOffice = ""
      })

      postalCode.filter(not(length(5))).onValue(function() {
        $scope.$apply(function() {
          $scope.application.calculatedValues.postOffice = ""
        })
      })
    }
  }
}]
