var util = require('./util')

var questionDefaults = {
  help: "",
  verboseHelp: "",
  required: false,
  questionType: "",
  id: null,
  title: ""
}

function params(obj) {
  var questionParameters = _.extend({}, questionDefaults, obj)
  _(questionParameters).each(function(val, key) {
    if (val == null)
      if(key == "title") {
        questionParameters.title = "?"
      }
      else {
        throw new Error("question parameter " + key + " null")
      }
  })
  return questionParameters
}

function Question(question, answer, validationErrors) {
  _.extend(this, params(question))
  this.answer = answer
  this.errors = validationErrors || []
}

Question.fromJson = function(json, persistedAnswers) {
  return new Question(json, initialValue(json, persistedAnswers), json.required ? ["*"] : [])
}

Question.prototype = {
  setErrors: function(errors) {
    this.errors = errors || []
  },

  appendErrors: function(errors) {
    this.errors = this.errors.concat(errors)
  }
}

Question.questionMap = function(questions) {
  questions = util.flattenTree(questions, "questionNodes")
  return util.indexBy(questions, function(node) { return node.id.questionId })
}

function initialValue(question, persistedAnswers) {
  function defaultValue() {
    var defaultOption = _(question.options).find(function(option) { return option.default })
    return defaultOption == null ? "" : defaultOption.value
  }
  function getOldValue(questionId) {
    var phaseAnswers = persistedAnswers[question.id.phaseId]
    if(phaseAnswers == null) {
      return null
    }
    return phaseAnswers[questionId]
  }

  var oldAnswer = getOldValue(question.id.questionId)
  if (question.options != null) {
    if (question.questionType == "Checkbox") {
      return _(question.options).chain().map(function(option) {
        oldAnswer = getOldValue(option.value)
        return [option.value, oldAnswer == null ? false : Boolean(oldAnswer)]
      }).object().value()
    } else {
      // Aseta default-arvo vain monivalinnoille
      return oldAnswer == null ? defaultValue() : oldAnswer
    }
  }
  if(oldAnswer != null) {
    return  oldAnswer
  }
}

function QuestionGroup(title) {
  this.title = title
  this.questionNodes = []
}

module.exports = {
  Question: Question,
  QuestionGroup: QuestionGroup
}