var Hakutoive = require('./hakutoive')
var util = require('./util')
var domainUtil = require('./domainUtil')

function Hakemus(json) {
  _.extend(this, json)
  this.hakutoiveet = _(this.hakutoiveet).map(function(hakutoive) { return new Hakutoive(hakutoive) })
  this.additionalQuestions = null
  this.answers = {}
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
    return _.extend({}, this,
      { hakutoiveet: _(this.hakutoiveet).map(function(hakutoive) { return hakutoive.toJson() })})
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
    return this.answers
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

  getAnswers: function(phaseId) {
    if (!this.answers[phaseId])
      this.answers[phaseId] = {}
    return this.answers[phaseId]
  },

  importQuestions: function(questions) {
    this.additionalQuestions = questions
    setDefaultAnswers(questions)

    var self = this

    function setDefaultAnswers(questionNode) {
      if (questionNode != null) {
        _(questionNode.questionNodes).each(function(node) {
          if (node.questionNodes != null)
            self.setDefaultAnswers(node)
          else
            setDefaultAnswer(node)
        })
      }

      function setDefaultAnswer(questionNode) {
        var question = questionNode.question
        if (question.options != null) { // Aseta default-arvo vain monivalinnoille
          var phaseAnswers = self.getAnswers(question.id.phaseId)

          var setValueIfEmpty = function(key, val) {
            phaseAnswers[key] = phaseAnswers[key] || val
          }

          if (question.questionType == "Checkbox") {
            _(question.options).each(function(option) {
              setValueIfEmpty(option.value, false)
            })
          } else {
            setValueIfEmpty(question.id.questionId, questionNode.defaultValue())
          }
        }
      }
    }
  },

  updateValidationMessages: function(errors, skipQuestions) {
    var errorMap = util.mapArray(errors, "key", "message")
    var questionMap = domainUtil.questionMap(this.additionalQuestions)
    var hakutoiveMap = domainUtil.hakutoiveMap(this.hakutoiveet)
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
      } else if (domainUtil.isHakutoiveError(questionId)) {
        hakutoiveMap[domainUtil.questionIdToHakutoiveId(questionId)].appendErrors(errors)
        return true
      } else {
        return false
      }
    }
  }
}

module.exports = Hakemus