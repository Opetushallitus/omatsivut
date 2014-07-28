var hakutoiveErrorRegexp = /^(preference\d)$|^(preference\d)-Koulutus$/
var util = require('./util')

module.exports = {
  isHakutoiveError: function(questionId) {
    return hakutoiveErrorRegexp.test(questionId)
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