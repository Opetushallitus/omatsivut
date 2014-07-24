var Hakutoive = require('./hakutoive')

function Hakemus(json) {
  _.extend(this, json)
  this.hakutoiveet = _(this.hakutoiveet).map(function(hakutoive) { return new Hakutoive(hakutoive) })
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
    return this.hasPreference(from) && this.hasPreference(to);
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
    return _(this.hakutoiveet).every(function(hakutoive) {
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

  setDefaultAnswers: function(questionNode) {
    var self = this

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
}

module.exports = Hakemus