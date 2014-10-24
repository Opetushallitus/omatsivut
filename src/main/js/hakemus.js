var Hakutoive = require('./hakutoive')
var Question = require('./question').Question
var util = require('./util')

function Hakemus(json) {
  this.oid = json.oid
  this.updated = json.updated
  this.haku = copy(json.haku)
  this.state = copy(json.state)
  this.educationBackground = copy(json.educationBackground)

  this.hakutoiveet = convertHakutoiveet(json.hakutoiveet)
  this.henkilotiedot = convertHenkilotiedot(json.answers.henkilotiedot)
  this.additionalQuestions = { questionNodes: [] }
  this.calculatedValues = {
    postOffice: json.postOffice
  }
}

function copy(json) { return $.extend(true, {}, json) }

function convertHenkilotiedot(json) {
  var fields = ["Sähköposti", "matkapuhelinnumero1", "lahiosoite", "Postinumero"]
  return _(fields).reduce(function(memo, key) {
    memo[key] = new Question({ id: key }, json[key])
    return memo
  } , {})
}

function convertHakutoiveet(hakutoiveet) {
  return _(hakutoiveet).map(function(hakutoive) { return new Hakutoive(hakutoive) })
}

function updatePreferenceQuestionIds(manipulationF) {
  var newIndexes = (function getNewIndexes() {
    var arr = _.range(1, this.hakutoiveet.length+1)
    manipulationF(arr)
    var indexes = _(arr).map(function(val, index) { return [val, index+1] })
    return _.object(indexes)
  }).call(this)

  _(Question.questionMap(this.additionalQuestions)).each(function(question, id) {
    var questionIdParts = /^(preference)(\d+)([-_].+)/.exec(id)
    if (questionIdParts != null) {
      var newId = questionIdParts[1] + newIndexes[questionIdParts[2]] + questionIdParts[3]
      question.id.questionId = newId
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

  canMovePreference: function(from, to) {
    var lastFilledItem = (function getLastFilled(hakutoiveet) {
      for (var i=hakutoiveet.length-1; i>=0; i--)
        if (hakutoiveet[i].hasData())
          return i
      return -1
    })(this.hakutoiveet)

    return this.hakutoiveet[from].hasData() && from >= 0 && to <= lastFilledItem && to >= 0
  },

  isPeriodActive: function() {
    return this.haku.applicationPeriods[0].active
  },

  allResultsAvailable: function() {
    return !this.hasResultState(["KESKEN", "VARALLA"]) && this.valintatulosHakutoiveet().length > 0
  },

  hasSomeResults: function() {
    var hakutoiveet = this.valintatulosHakutoiveet()
    return hakutoiveet.length > 0 && _(hakutoiveet).some(function(hakutoive) { return hakutoive.tila != "KESKEN" })
  },

  valintatulosHakutoiveet: function() {
    return this.state && this.state.valintatulos ? this.state.valintatulos.hakutoiveet : []
  },

  editHakutoiveetEnabled: function() {
    return this.state && (this.state.id == 'ACTIVE' || this.state.id == 'INCOMPLETE')
  },

  editHenkilotiedotEnabled: function() {
    return this.editHakutoiveetEnabled() || (this.state && this.state.id == "HAKUKAUSIPAATTYNYT")
  },

  vastaanotettavatHakutoiveet: function() {
    return _(this.valintatulosHakutoiveet()).filter(function(hakutoive) {
      return hakutoive.vastaanotettavuustila === "VASTAANOTETTAVISSA_SITOVASTI" || hakutoive.vastaanotettavuustila === "VASTAANOTETTAVISSA_EHDOLLISESTI"
    })
  },

  hasResultState: function(resultStates) {
    if (!_.isArray(resultStates))
      resultStates = [resultStates]

    return _(this.valintatulosHakutoiveet()).any(function(hakutoive) {
      return _(resultStates).contains(hakutoive.tila)}
    )
  },

  toJson: function() {
    var self = this

    return {
      oid: self.oid,
      hakuOid: self.haku.oid,
      hakutoiveet: _(this.hakutoiveet).map(function(hakutoive) { return hakutoive.toJson() }),
      answers: removeFalseBooleans(getAnswers())
    }

    function getAnswers() {
      var contactDetails = _(self.henkilotiedot).reduce(function(answers, question, id) {
        answers.henkilotiedot[id] = question.answer
        return answers
      }, { henkilotiedot: {}})

      var additionalQuestionAnswers = _(Question.questionMap(self.additionalQuestions)).reduce(function(answers, questionNode) {
        answers[questionNode.id.phaseId] = answers[questionNode.id.phaseId] || {}
        var answer = questionNode.answer
        if (_.isObject(answer)) {
          _(answer).each(function(val, key) {
            answers[questionNode.id.phaseId][key] = val
          })
        } else {
          answers[questionNode.id.phaseId][questionNode.id.questionId] = answer
        }
        return answers
      }, {})

      return _.extend({}, contactDetails, additionalQuestionAnswers)
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

    if (!_.isEqual(util.withoutAngularFields(this.state), savedApplication.state))
      this.state = $.extend(true, {}, savedApplication.state)

    for (var i=0; i<this.hakutoiveet.length && i<savedApplication.hakutoiveet.length; i++) {
      hakutoive = this.hakutoiveet[i]
      hakutoive.importJson(savedApplication.hakutoiveet[i])
      if (hakutoive.hasData())
        hakutoive.setAsSaved()
    }
  },

  mergeValidationResult: function(validationResult) {
    this.importQuestions(validationResult.questions)
    this.haku.applicationPeriods = validationResult.applicationPeriods
  },

  validatePreferences: function() {
    return (this.hakutoiveet.length > 0 && this.hakutoiveet[0].hasData()) &&
      _(this.hakutoiveet).every(function(hakutoive) {
        return hakutoive.isValid()
      }) && !_(this.hakutoiveet.slice(0, this.lastIndexWithData() + 1)).any(function(hakutoive) {
        return !hakutoive.hasData()
      })
  },

  getChangedPreferences: function() {
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
      var oldQuestions = Question.questionMap(old)
      _(Question.questionMap(questions)).each(function(newQuestion, id) {
        if (oldQuestions[id] != null)
          newQuestion.answer = oldQuestions[id].answer
      })
      return questions
    })(this.additionalQuestions, questions)
  },

  updateValidationMessages: function(errors, skipQuestions) {
    var errorMap = util.mapArray(errors, "key", "message")
    var hakutoiveMap = Hakutoive.hakutoiveMap(this.hakutoiveet)
    var questionMap = _.extend({}, Question.questionMap(this.additionalQuestions), this.henkilotiedot)
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