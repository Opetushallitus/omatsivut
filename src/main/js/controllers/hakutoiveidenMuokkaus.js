module.exports = function(app, staticResources) {
  app.controller('HakutoiveidenMuokkausController', function($scope) {
    $scope.lang = staticResources.translations.languageId
  })
}
