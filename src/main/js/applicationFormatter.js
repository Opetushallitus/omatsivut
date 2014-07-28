module.exports = function(listApp) {
  listApp.factory("applicationFormatter", function() {
    return function(application) {
      var json = application.toJson()
      var answers = {};

      (function appendOnlyRelatedAnswers(node) {
        if (node != null) {
          if (node.questionNodes == null) {
            answers[node.question.id.phaseId] = answers[node.question.id.phaseId] || {}
            _(relatedAnswers(node.question)).each(function(answer) {
              answers[node.question.id.phaseId][answer.key] = answer.value
            })
          } else {
            _(node.questionNodes).each(appendOnlyRelatedAnswers)
          }
        }
      })(application.additionalQuestions)

      return _.extend({}, json, { answers: removeFalseBooleans(answers)})

      function relatedAnswers(question) {
        var answers = application.answers[question.id.phaseId]

        function answerForQuestion(question) {
          return function(key) {
            return question.id.questionId == key || key.indexOf(question.id.questionId + "-option") === 0
          }
        }

        return _(answers).chain().keys().filter(answerForQuestion(question)).map(function(key) {
          return {
            key: key,
            value: answers[key]
          }
        }).value()
      }

      function removeFalseBooleans(obj) {
        _.each(obj, function(val, key) {
          if (_.isBoolean(val) && val === false)
            delete obj[key]
          else if (_.isObject(val))
            removeFalseBooleans(val)
        })
        return obj;
      }
    }
  })
}