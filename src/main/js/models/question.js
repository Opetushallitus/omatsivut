import { flattenTree, indexBy } from '../util';
const _ = require('underscore');

const questionDefaults = {
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

export default class Question {
  constructor(question, answer, validationErrors) {
    _.extend(this, params(question))
    this.answer = answer
    this.errors = validationErrors || []
  }

  setErrors(errors) {
    this.errors = errors || []
  }

  appendErrors(errors) {
    this.errors = this.errors.concat(errors)
  }

  static fromJson(json, application) {
    return new Question(json, initialValue(json, application), json.required ? ["*"] : [])
  }

  static getQuestions(jsonQuestions, application) {
    return convertToItems(jsonQuestions, new QuestionGroup())

    function convertToItems(questions, results) {
      _(questions).each(function (questionNode) {
        if (questionNode.questions != null) {
          results.questionNodes.push(convertToItems(questionNode.questions, new QuestionGroup(questionNode.title)))
        } else {
          results.questionNodes.push(Question.fromJson(questionNode, application))
        }
      })
      return results
    }
  }

  static questionMap(questions) {
    questions = flattenTree(questions, "questionNodes")
    return indexBy(questions, function(node) { return node.id.questionId })
  }

}

function initialValue(question, application) {
  function defaultValue() {
    var defaultOption = _(question.options).find(function(option) { return option.default })
    return defaultOption == null ? "" : defaultOption.value
  }
  function getOldValue(questionId) {
    var questionIdParts = /^(preference)(\d+)([-_].+)/.exec(questionId)
    if (questionIdParts != null && application.hakutoiveet[questionIdParts[2] - 1] != null && application.hakutoiveet[questionIdParts[2] - 1].isNew) {
      return null
    }
    var phaseAnswers = application.persistedAnswers[question.id.phaseId]
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

class QuestionGroup {
  constructor(title) {
    this.title = title
    this.questionNodes = []
  }
}
