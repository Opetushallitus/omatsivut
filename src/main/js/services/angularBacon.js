import Bacon from 'baconjs';

export default function () {
  return {
    watch: function(scope, expression) {
      let stream = new Bacon.Bus();
      scope.$watch(expression, function(val, prevVal) { if (val !== prevVal) stream.push(val) });
      return stream
    },

    resource: function(resource) {
      return function(queryParams, body) {
        return Bacon.fromNodeCallback(function (callback) {
          resource(queryParams, body, success, error);

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
}
