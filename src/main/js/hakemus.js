var Hakutoive = require('./hakutoive')
var AdditionalQuestion = require('./additionalQuestion').AdditionalQuestion
var util = require('./util')

function Hakemus(json) {
  _.extend(this, json)
  this.hakutoiveet = _(this.hakutoiveet).map(function(hakutoive) { return new Hakutoive(hakutoive) })
  this.additionalQuestions = null
}

function updatePreferenceQuestionIds(manipulationF) {
  var newIndexes = (function getNewIndexes() {
    var arr = _.range(1, this.hakutoiveet.length+1)
    manipulationF(arr)
    var indexes = _(arr).map(function(val, index) { return [val, index+1] })
    return _.object(indexes)
  }).call(this)

  _(AdditionalQuestion.questionMap(this.additionalQuestions)).each(function(question, id) {
    var questionIdParts = /^(preference)(\d+)([-_].+)/.exec(id)
    if (questionIdParts != null) {
      var newId = questionIdParts[1] + newIndexes[questionIdParts[2]] + questionIdParts[3]
      question.question.id.questionId = newId
    }
  })
}

Hakemus.prototype = {
  removePreference: function(index) {
    this.hakutoiveet.splice(index, 1)
    updatePreferenceQuestionIds.call(this, function(arr) {
      arr.splice(index, 1)
    })
  },

  addPreference: function(hakutoive) {
    this.hakutoiveet.push(hakutoive)
  },

  hasPreference: function(index) {
    return index >= 0 && index <= this.hakutoiveet.length-1 && this.hakutoiveet[index].hasData()
  },

  movePreference: function(from, to) {
    this.hakutoiveet[from].setAsModified()
    this.hakutoiveet[to].setAsModified()
    this.hakutoiveet.splice(to, 0, this.hakutoiveet.splice(from, 1)[0])

    updatePreferenceQuestionIds.call(this,
      function(arr) {
        arr.splice(to, 0, arr.splice(from, 1)[0])
      }
    )
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

  mergeSavedApplication: function(savedApplication) {
    this.updated = savedApplication.updated
    this.haku.applicationPeriods = savedApplication.haku.applicationPeriods

    for (var i=0; i<this.hakutoiveet.length && i<savedApplication.hakutoiveet.length; i++) {
      hakutoive = this.hakutoiveet[i]
      hakutoive.importJson(savedApplication.hakutoiveet[i])
      if (hakutoive.hasData())
        hakutoive.setAsSaved()
    }
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
      return {
        "Koulutus": hakutoive.data["Koulutus"],
        "Koulutus-id": hakutoive.data["Koulutus-id"],
        "Opetuspiste": hakutoive.data["Opetuspiste"],
        "Opetuspiste-id": hakutoive.data["Opetuspiste-id"]
      }
    })
  },

  getAnswerWatchCollection: function() {
    return _(AdditionalQuestion.questionMap(this.additionalQuestions)).map(function(item, key) { return item.answer })
  },

  getOptionAnswerWatchCollection: function() {
    return _(AdditionalQuestion.questionMap(this.additionalQuestions)).filter(function(item) {return item.question.options != null}).map(function(item, key) { return item.answer })
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