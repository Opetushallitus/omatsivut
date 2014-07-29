var hakutoiveErrorRegexp = /^(preference\d)$|^(preference\d)-Koulutus$/
var util = require('./util')

module.exports = {
  isHakutoiveError: function(questionId) {
    return hakutoiveErrorRegexp.test(questionId)
  },

  hasHakutoiveErrors: function(errorsJson) {
    var errorMap = util.mapArray(errorsJson, "key", "message");
    var self = this
    return _(errorMap).any(function(val, key) {
      return self.isHakutoiveError(key) && val.length > 0
    })
  },

  questionMap: function(questions) {
    questions = util.flattenTree(questions, "questionNodes")
    return util.indexBy(questions, function(node) { return node.question.id.questionId })
  },

  hakutoiveMap: function(hakutoiveet) {
    return util.indexBy(hakutoiveet, function(hakutoive, index) { return "preference" + (index+1) })
  },

  questionIdToHakutoiveId: function(questionId) {
    return _.chain(hakutoiveErrorRegexp.exec(questionId)).rest().without(undefined).first().value()
  }
}