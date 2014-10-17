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
      throw new Error("question parameter " + key + " null")
  })
  return questionParameters
}

function Question(question, answer, validationErrors) {
  _.extend(this, params(question))
  this.answer = answer != null ? answer : initialValue(this)
  this.errors = validationErrors || []
}

Question.fromJson = function(json) {
  return new Question(json, null, json.required ? ["*"] : [])
}

Question.prototype = {
  defaultValue: function() {
    var defaultOption = _(this.options).find(function(option) { return option.default })
    return defaultOption == null ? "" : defaultOption.value
  },

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

function initialValue(question) {
  if (question.options != null) { // Aseta default-arvo vain monivalinnoille
    if (question.questionType == "Checkbox") {
      return _(question.options).chain().map(function(option) {
        return [option.value, false]
      }).object().value()
    } else {
      return question.defaultValue()
    }
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