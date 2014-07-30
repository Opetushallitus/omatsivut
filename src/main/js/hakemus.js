var Hakutoive = require('./hakutoive')
var AdditionalQuestion = require('./additionalQuestion').AdditionalQuestion
var util = require('./util')

function Hakemus(json) {
  _.extend(this, json)
  this.hakutoiveet = _(this.hakutoiveet).map(function(hakutoive) { return new Hakutoive(hakutoive) })
  this.additionalQuestions = null
}

Hakemus.prototype = {
  removePreference: function(index) {
    var row = this.hakutoiveet.splice(index, 1)[0]
  },

  addPreference: function(hakutoive) {
    this.hakutoiveet.push(hakutoive)
  },

  hasPreference: function(index) {
    return index >= 0 && index <= this.hakutoiveet.length-1 && this.hakutoiveet[index].hasData()
  },

  canMoveTo: function(from, to) {
    var lastFilledItem = (function getLastFilled(hakutoiveet) {
      for (var i=hakutoiveet.length-1; i>=0; i--)
        if (hakutoiveet[i].hasData())
          return i
      return -1
    })(this.hakutoiveet)

    return this.hakutoiveet[from].hasData() && from >= 0 && to <= lastFilledItem && to >= 0
  },

  toJson: function() {
    var self = this
    return _.extend({}, this, {
      hakutoiveet: _(this.hakutoiveet).map(function(hakutoive) { return hakutoive.toJson() }),
      answers: removeFalseBooleans(getAnswers())
    })

    function getAnswers() {
      var answers = {};
      _(AdditionalQuestion.questionMap(self.additionalQuestions)).each(function(questionNode, key) {
        answers[questionNode.question.id.phaseId] = answers[questionNode.question.id.phaseId] || {}
        var answer = questionNode.answer
        if (_.isObject(answer)) {
          _(answer).each(function(val, key) {
            answers[questionNode.question.id.phaseId][key] = val
          })
        } else {
          answers[questionNode.question.id.phaseId][questionNode.question.id.questionId] = answer
        }
      })

      return answers
    }

    function removeFalseBooleans(obj) {
      _.each(obj, function(val, key) {
        if (_.isBoolean(val) && val === false)
          delete obj[key]
        else if (_.isObject(val))
          removeFalseBooleans(val)
      })
      return obj;
    }
  },

  setAsSaved: function(savedApplication) {
    this.updated = savedApplication.updated

    _(this.hakutoiveet).each(function(hakutoive) {
        if (hakutoive.hasData()) hakutoive.setAsSaved() }
    )
  },

  validatePreferences: function() {
    return (this.hakutoiveet.length > 0 && this.hakutoiveet[0].hasData()) &&
      _(this.hakutoiveet).every(function(hakutoive) {
        return hakutoive.isValid()
      }) && !_(this.hakutoiveet.slice(0, this.lastIndexWithData() + 1)).any(function(hakutoive) {
        return !hakutoive.hasData()
      })
  },

  getHakutoiveWatchCollection: function() {
    return _(this.hakutoiveet).map(function(hakutoive) {
      return hakutoive.data
    })
  },

  getAnswerWatchCollection: function() {
    return _(AdditionalQuestion.questionMap(this.additionalQuestions)).map(function(item, key) { return item.answer })
  },

  moveHakutoive: function(from, to) {
    this.hakutoiveet[from].setAsModified()
    this.hakutoiveet[to].setAsModified()
    this.hakutoiveet.splice(to, 0, this.hakutoiveet.splice(from, 1)[0])
  },

  getChangedItems: function() {
    return _(this.hakutoiveet).chain()
      .map(function(hakutoive, index) { return hakutoive.isModified ? index : null })
      .without(null)
      .value()
  },

  isEditable: function(index) {
    return index <= this.lastIndexWithData() + 1
  },

  lastIndexWithData: function() {
    for (var i = this.hakutoiveet.length - 1; i >= 0; i--) {
      if (this.hakutoiveet[i].hasData()) return i
    }
    return -1
  },

  importQuestions: function(questions) {
    this.additionalQuestions = (function mergeOldAnswers(old, questions) {
      var oldQuestions = AdditionalQuestion.questionMap(old)
      _(AdditionalQuestion.questionMap(questions)).each(function(newQuestion, id) {
        if (oldQuestions[id] != null)
          newQuestion.answer = oldQuestions[id].answer
      })
      return questions
    })(this.additionalQuestions, questions)
  },

  updateValidationMessages: function(errors, skipQuestions) {
    var errorMap = util.mapArray(errors, "key", "message")
    var questionMap = AdditionalQuestion.questionMap(this.additionalQuestions)
    var hakutoiveMap = Hakutoive.hakutoiveMap(this.hakutoiveet)
    var unhandled = []

    clearErrors()

    _(errorMap).each(function(errorList, key) {
      if (!updateErrors(key, errorList))
        unhandled.push({questionId: key, errors: errorList})
    })

    return unhandled

    function clearErrors() {
      _(hakutoiveMap).each(function(item) { item.setErrors() })
      if (!skipQuestions)
        _(questionMap).each(function(item) { item.setErrors() })
    }

    function updateErrors(questionId, errors) {
      if (questionMap[questionId] != null) {
        if (!skipQuestions)
          questionMap[questionId].appendErrors(errors)
        return true
      } else if (Hakutoive.isHakutoiveError(questionId)) {
        hakutoiveMap[Hakutoive.questionIdToHakutoiveId(questionId)].appendErrors(errors)
        return true
      } else {
        return false
      }
    }
  }
}

module.exports = Hakemus