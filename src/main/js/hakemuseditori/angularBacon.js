var Bacon = require("baconjs")

module.exports = function(app, isTestMode) {
  app.factory("angularBacon", function () {
    return {
      watch: function(scope, expression) {
        var stream = new Bacon.Bus()
        scope.$watch(expression, function(val, prevVal) { if (val !== prevVal) stream.push(val) })
        return stream
      },

      resource: function(resource) {
        return function(queryParams, body) {
          return Bacon.fromNodeCallback(function (callback) {
            resource(queryParams, body, success, error)

            function success(value) {
              callback(null, value)
            }

            function error(response) {
              callback(response)
            }
          })
        }
      }
    }
  })
}
