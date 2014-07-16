function QuestionItem(question, validationErrors) {
  this.question = question
  this.validationMessage = validationErrors.join(", ")
}

module.exports = QuestionItem