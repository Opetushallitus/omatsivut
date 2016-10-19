module.exports = function (app) {
    app.directive("notification", function () {
        return {
            restrict: 'E',
            scope: {
                message: '@'
            },
            templateUrl: 'templates/notification.html',
            link: function (scope) {
                scope.visible = true
                scope.close = function () {
                    scope.visible = false
                }
            }
        }
    })
}