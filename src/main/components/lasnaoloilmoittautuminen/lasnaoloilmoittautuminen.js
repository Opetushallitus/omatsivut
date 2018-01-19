module.exports = function(app) {
    app.directive("lasnaoloilmoittautuminen", ["localization", function (localization) {
        return {
            restrict: 'E',
            scope: {},
            templateUrl: 'lasnaoloilmoittautuminen.html',

            link: function ($scope, element, attrs) {
                $scope.localization = localization;
            }
        }
    }])
};