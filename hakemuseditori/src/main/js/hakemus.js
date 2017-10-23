var Hakutoive = require('./hakutoive')
var Question = require('./question').Question
var util = require('./util')

function Hakemus(json) {
  try {
    this.oid = json.hakemus.oid
    this.updated = json.hakemus.updated
    this.haku = copy(json.hakemus.haku)
    this.state = copy(json.hakemus.state)
    this.hasForm = json.hakemus.hasForm
    this.educationBackground = copy(json.hakemus.educationBackground)
    this.notifications = json.hakemus.notifications
    this.hakutoiveet = convertHakutoiveet(json.hakemus.hakutoiveet)
    this.henkilotiedot = convertHenkilotiedot(json.hakemus.answers.henkilotiedot)
    this.persistedAnswers = json.hakemus.answers
    this.additionalQuestions = Question.getQuestions(json.questions, this)
    this.tuloskirje = copy(formatTuloskirje(json.hakemus.tuloskirje))
    this.tulosOk = json.tulosOk
    this.requiredPaymentState = json.hakemus.requiredPaymentState
    this.calculatedValues = {
      postOffice: json.hakemus.postOffice
    }
    this.oiliJwt = null
    this.hakemusSource = json.hakemusSource
    if (this.hakemusSource === 'Ataru') this.ataruHakijaUrl = json.ataruHakijaUrl
    this.secret = json.hakemus.secret
  } catch (e) {
    throw e;
  }
}

function copy(json) { return $.extend(true, {}, json) }

function formatTuloskirje(tuloskirje) {
  if(tuloskirje) {
    var date = new Date(tuloskirje.created)
    var yyyy = date.getFullYear()
    var mm = date.getMonth() + 1
    var dd  = date.getDate()
    tuloskirje.createdDate = dd + "." + mm + "." + yyyy
  }
  return tuloskirje;
}

function convertHenkilotiedot(json) {
  if (!_.isUndefined(json)) {
    var fields = ["Sähköposti", "matkapuhelinnumero1", "asuinmaa", "lahiosoite", "Postinumero"]
    return _(fields).reduce(function (memo, key) {
      memo[key] = new Question({id: key}, json[key])
      return memo
    }, {})
  }
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

    return !this.preferenceLocked(from) && this.hakutoiveet[from].hasData() && from >= 0 && to <= lastFilledItem && to >= 0
  },

  preferenceLocked: function(index) {
    var hakutoive = this.hakutoiveet[index]
    var hakuaikaId = hakutoive.hakuaikaId
    var self = this

    function isPeriodActive(applicationPeriodId) {
      var period = _(self.haku.applicationPeriods).find(function(period) { return period.id === applicationPeriodId })
      return period !== undefined ? period.active : self.haku.active
    }

    if (hakutoive.addedDuringCurrentSession) {
      return false
    } else if (!_.isEmpty(hakutoive.kohdekohtainenHakuaika)) {
      return !hakutoive.kohdekohtainenHakuaika.active
    } else if (!_.isEmpty(hakuaikaId)) {
      return !isPeriodActive(hakuaikaId)
    } else {
      return !this.haku.active
    }
  },

  allResultsAvailable: function() {
    return !this.hasResultState(["KESKEN", "VARALLA"]) && this.valintatulosHakutoiveet().length > 0
  },

  resultFetchedSuccessfully: function() {
    return this.tulosOk
  },

  hasSomeResults: function() {
    return this.valintatulosHakutoiveet().length > 0
  },

  hasSomeNonKeskenResults: function() {
    return this.hasSomeResults() && _(this.valintatulosHakutoiveet()).some(function(hakutoive) { return hakutoive.valintatila != "KESKEN" })
  },

  valintatulosHakutoiveet: function() {
    return this.state && this.state.valintatulos ? this.state.valintatulos.hakutoiveet : []
  },

  applicationPeriodsInactive: function() {
    return _(this.haku.applicationPeriods).every(function(period) { return !period.active })
  },

  editHakutoiveetEnabled: function() {
    return this.state && (this.state.id === 'ACTIVE' || this.state.id === 'INCOMPLETE') && this.hakemusSource !== 'Ataru'
  },

  editHenkilotiedotEnabled: function() {
    return this.editHakutoiveetEnabled() || (this.state && this.state.id === "HAKUKAUSIPAATTYNYT" && this.hakemusSource !== 'Ataru')
  },

  vastaanotettavatHakutoiveet: function() {
    return _(this.valintatulosHakutoiveet()).filter(function(hakutoive) {
      return (hakutoive.vastaanotettavuustila === "VASTAANOTETTAVISSA_SITOVASTI" || hakutoive.vastaanotettavuustila === "VASTAANOTETTAVISSA_EHDOLLISESTI") && hakutoive.vastaanottotila == "KESKEN"
    })
  },

  ilmoittautumisLinkit: function() {
    return _(this.valintatulosHakutoiveet()).filter(function(tulos) {
      return tulos.ilmoittautumistila != null &&
        tulos.ilmoittautumistila.ilmoittauduttavissa &&
        tulos.ilmoittautumistila.ilmoittautumistapa != null && tulos.ilmoittautumistila.ilmoittautumistapa.url
    })
  },

  kelaURL: function() {
    return _.chain(this.valintatulosHakutoiveet()).map(function(tulos) {
      return tulos.kelaURL
    }).filter(function(k) {return k}).head().value()
  },

  oiliUrl: function() {
    var oiliJwt = this.oiliJwt;
    return _.chain(this.valintatulosHakutoiveet()).map(function(tulos) {
        var baseUrl = tulos.ilmoittautumistila.ilmoittautumistapa.url;
        return baseUrl.endsWith('/oili/') ? baseUrl : baseUrl + '?token=' + oiliJwt;
    }).filter(function(k) {return k}).head().value()
  },

  hasResultState: function(resultStates) {
    if (!_.isArray(resultStates))
      resultStates = [resultStates]

    return _(this.valintatulosHakutoiveet()).any(function(hakutoive) {
      return _(resultStates).contains(hakutoive.valintatila)}
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
          obj[key] = ""
        else if (_.isObject(val))
          removeFalseBooleans(val)
      })
      return obj;
    }
  },

  mergeSavedApplication: function(savedApplication) {
    this.updated = savedApplication.updated
    this.requiredPaymentState = savedApplication.requiredPaymentState
    this.notifications = savedApplication.notifications

    if (!_.isEqual(util.withoutAngularFields(this.state), savedApplication.state))
      this.state = $.extend(true, {}, savedApplication.state)

    for (var i=0; i<this.hakutoiveet.length && i<savedApplication.hakutoiveet.length; i++) {
      hakutoive = this.hakutoiveet[i]
      hakutoive.importJson(savedApplication.hakutoiveet[i])
      if (hakutoive.hasData())
        hakutoive.setAsSaved()
    }
  },

  importHakuajat: function(hakukohteet) {
    if (hakukohteet != null) {
      for (var i = 0; i < this.hakutoiveet.length && i < hakukohteet.length; i++) {
        this.hakutoiveet[i].kohdekohtainenHakuaika = hakukohteet[i].kohdekohtainenHakuaika
      }
    }
  },

  importPaymentInfo: function(paymentInfo) {
    var hakemus = this
    paymentInfo = paymentInfo || {}
    this.hakutoiveet.forEach(function(hakutoive) {
      var hakemusPaid = hakemus.requiredPaymentState === 'OK'
      hakutoive.showPaymentNotification = paymentInfo[hakutoive.data['Koulutus-id']] && !hakemusPaid
    });
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
    return !this.applicationPeriodsInactive() &&  index <= this.lastIndexWithData() + 1
  },

  lastIndexWithData: function() {
    for (var i = this.hakutoiveet.length - 1; i >= 0; i--) {
      if (this.hakutoiveet[i].hasData()) return i
    }
    return -1
  },

  compareQuestionMapsIgnoring: function (oldQuestions, newQuestions, ignored) {
    var areEqual = false;

    if (_.keys(oldQuestions).length === _.keys(newQuestions).length) {
      areEqual = _.every(newQuestions, function (question, id) {
        if (oldQuestions[id]) {
          var oldQ = _.omit(oldQuestions[id], ignored);
          var Q = _.omit(question, ignored);
          return angular.equals(oldQ, Q);
        } else {
          return false;
        }
      });
    } else {
      areEqual = false;
    }

    return areEqual;
  },

  importQuestions: function(questions) {
    var currentQuestions = Question.questionMap(this.additionalQuestions);
    var newQuestions = Question.questionMap(questions);
    var equalQuestions = this.compareQuestionMapsIgnoring(currentQuestions, newQuestions, ['answer', 'errors']);

    if (equalQuestions) {
      this.additionalQuestions = (function mergeErrors(old, questions) {
        var oldQuestions = Question.questionMap(old);
        _(oldQuestions).each(function(oldQuestion, id) {
          if (questions[id] != null)
            oldQuestion.errors = questions[id].errors;
        });
        return old;
      })(this.additionalQuestions, questions);
      return this.additionalQuestions;
    } else {
      this.additionalQuestions = (function mergeOldAnswers(old, questions) {
        var oldQuestions = Question.questionMap(old);
        _(Question.questionMap(questions)).each(function(newQuestion, id) {
          if (oldQuestions[id] != null)
            newQuestion.answer = oldQuestions[id].answer;
        });
        return questions;
      })(this.additionalQuestions, questions);

      return this.additionalQuestions;
    }
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
      _(hakutoiveMap).each(function(item) {
        item.setErrors()
      });
      if (!skipQuestions) {
        _(questionMap).each(function(item) {
          try {
            item.setErrors()
          } catch (e) {
            console.log("For some unfathomable reason item.setErrors() is not defined here although it should be! Should be fixed hakemus.js#updateValidationMessages().");
          }
        })
      }
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
