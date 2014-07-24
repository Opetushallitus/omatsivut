function AdditionalQuestion(question, validationErrors) {
  this.question = question
  this.validationMessage = validationErrors.join(", ")
}

AdditionalQuestion.prototype = {
  defaultValue: function() {
    var defaultOption = _(this.question.options).find(function(option) { return option.default })
    return defaultOption == null ? "" : defaultOption.value
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