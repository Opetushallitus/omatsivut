function AdditionalQuestion(question, validationErrors) {
  this.question = question
  this.errors = validationErrors
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

function AdditionalQuestionGroup(title) {
  this.title = title
  this.questionNodes = []
}

module.exports = {
  AdditionalQuestion: AdditionalQuestion,
  AdditionalQuestionGroup: AdditionalQuestionGroup
}