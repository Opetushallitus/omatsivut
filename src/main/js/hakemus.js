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
    function removeFalseBooleans(obj) {
      _.each(obj, function(val, key) {
        if (_.isBoolean(val) && val === false)
          delete obj[key]
        else if (_.isObject(val))
          removeFalseBooleans(val)
      })
      return obj;
    }

    return _.extend({}, this,
      { hakutoiveet: _(this.hakutoiveet).map(function(hakutoive) { return hakutoive.toJson() })},
      { answers: removeFalseBooleans($.extend(true, {}, this.answers))})

    return _.extend({}, this, { hakutoiveet: _(this.hakutoiveet).map(function(hakutoive) { return hakutoive.toJson() })})
  },

  setAsSaved: function(savedApplication) {
    this.updated = savedApplication.updated

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

  setDefaultAnswers: function(questionNode) {
    var self = this

    if (questionNode != null) {
      _(questionNode.questionNodes).each(function(node) {
        if (node.questionNodes != null)
          self.setDefaultAnswers(node)
        else
          setDefaultAnswer(node.question)
      })
    }

    function setDefaultAnswer(question) {
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
          setValueIfEmpty(question.id.questionId, defaultValue(question.options))
        }
      }
    }

    function defaultValue(options) {
      var defaultOption = _(options).find(function(option) { return option.default })
      return defaultOption == null ? "" : defaultOption.value
    }
  }
}

module.exports = Hakemus