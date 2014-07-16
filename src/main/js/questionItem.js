function QuestionItem(question, answer, validationErrors) {
  this.question = question
  this.answer = answer
  this.validationMessage = validationErrors.join(", ")
}

module.exports = QuestionItem