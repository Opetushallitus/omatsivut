function AdditionalQuestion(question, validationErrors) {
  this.question = question
  this.errors = validationErrors
  this.answer = initialValue(this)
}

AdditionalQuestion.prototype = {
  defaultValue: function() {
    var defaultOption = _(this.question.options).find(function(option) { return option.default })
    return defaultOption == null ? "" : defaultOption.value
  },

  setErrors: function(errors) {
    this.errors = errors || []
  },

  appendErrors: function(errors) {
    this.errors = this.errors.concat(errors)
  }
}

function initialValue(questionNode) {
  var question = questionNode.question
  if (question.options != null) { // Aseta default-arvo vain monivalinnoille
    if (question.questionType == "Checkbox") {
      return _(question.options).chain().map(function(option) {
        return [option.value, false]
      }).object().value()
    } else {
      return questionNode.defaultValue()
    }
  }
}

function AdditionalQuestionGroup(title) {
  this.title = title
  this.questionNodes = []
}

module.exports = {
  AdditionalQuestion: AdditionalQuestion,
  AdditionalQuestionGroup: AdditionalQuestionGroup
}