var Hakutoive = require('./hakutoive')

function Hakemus(json) {
  _.extend(this, json)
  this.hakutoiveet = _(this.hakutoiveet).map(function(hakutoive) { return new Hakutoive(hakutoive) })
  this.answers = this.answers || {}
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
    return _.extend({}, this, { hakutoiveet: _(this.hakutoiveet).map(function(hakutoive) { return hakutoive.toJson() })})
  },

  setAsSaved: function() {
    _(this.hakutoiveet).each(function(hakutoive) {
        if (hakutoive.hasData()) hakutoive.setAsSaved() }
    )
  },

  isValid: function() {
    return _(this.hakutoiveet).every(function(hakutoive) { return hakutoive.isValid() })
  },

  getHakutoiveWatchCollection: function() {
    return _(this.hakutoiveet).map(function(hakutoive) { return hakutoive.data })
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
    var firstEditableIndex = _(this.hakutoiveet).filter(function(hakutoive) { return hakutoive.hasData() && hakutoive.isValid() }).length
    return index >= 0 && index < this.hakutoiveet.length && index <= firstEditableIndex
  },

  getAnswers: function(phaseId) {
    if (!this.answers[phaseId])
      this.answers[phaseId] = {}
    return this.answers[phaseId]
  },

  setDefaultAnswers: function(questions) {
    var self = this
    function defaultValue(options) {
      var defaultOption = _(options).find(function(option) { return option.default })
      return defaultOption == null ? "" : defaultOption.value
    }

    function setDefaultAnswer(item) {
      var phaseAnswers = self.getAnswers(item.question.id.phaseId)
      function setValueIfEmpty(key, val) { phaseAnswers[key] = phaseAnswers[key] || val }

      if (item.question.questionType == "Checkbox") {
        _(item.question.options).each(function(option) {
          setValueIfEmpty(option.value, false)
        })
      } else {
        setValueIfEmpty(item.question.id.questionId, defaultValue(item.question.options))
      }
    }

    return questions
    _(questions).chain()
      .filter(function(item) { return item.questionNodes == null && item.question.options != null })
      .each(setDefaultAnswer)
  }
}

module.exports = Hakemus