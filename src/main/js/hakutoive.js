var util = require('./util')

function Hakutoive(json) {
  this.data = json
  this.isModified = false
  this.isNew = _.isEmpty(json)
  this.addedDuringCurrentSession = _.isEmpty(json)
  this.errors = []
}

Hakutoive.prototype = {
  toJson: function() {
    return this.data
  },

  importJson: function(json) {
    this.data = json
  },

  clear: function() {
    this.data = {}
    this.isNew = true
    this.isModified = false
  },

  hasData: function() {
    return !_.isEmpty(this.data)
  },

  setOpetuspiste: function(id, name) {
    this.data["Opetuspiste"] = name
    this.data["Opetuspiste-id"] = id
    this.isModified = true
    this.setErrors([])
  },

  setKoulutus: function(koulutus) {
    this.data["Koulutus"] = toString(koulutus.name)
    this.data["Koulutus-id"] = toString(koulutus.id)
    this.data["Koulutus-educationDegree"] = toString(koulutus.educationDegree)
    this.data["Koulutus-id-lang"] = toString(koulutus.teachingLanguages[0])
    this.data["Koulutus-id-sora"] = toString(koulutus.sora)
    this.data["Koulutus-id-aoIdentifier"] = toString(koulutus.aoIdentifier)
    this.data["Koulutus-id-kaksoistutkinto"] = toString(koulutus.kaksoistutkinto)
    this.data["Koulutus-id-vocational"] = toString(koulutus.vocational)
    this.data["Koulutus-id-educationcode"] = toString(koulutus.educationCodeUri)
    this.data["Koulutus-id-athlete"] = toString(koulutus.athleteEducation)
    this.data["Koulutus-id-attachmentgroups"] = this.getAttachmentGroups(koulutus)
    this.isModified = true
    this.setErrors([])
    function toString(x) {
      return (x==null) ? "" : x.toString()
    }
  },

  getAttachmentGroups: function(koulutus) {
    var attachmentGroups = [];
    if (koulutus.organizationGroups instanceof Array) {
      for (var i = 0; i < koulutus.organizationGroups.length; i++) {
        var group = koulutus.organizationGroups[i];
        if(group.groupTypes.indexOf("hakukohde") >= 0 && group.usageGroups.indexOf("hakukohde_liiteosoite") >= 0) {
          attachmentGroups.push(group.oid);
        }
      }
    }
    return attachmentGroups.join(",")
  },

  hasOpetuspiste: function() {
    return !_.isEmpty(this.data["Opetuspiste-id"])
  },

  removeOpetuspisteData: function() {
    var self = this
    _.each(this.data, function(value, key) {
      if (key.indexOf("$")!==0 && key != "Opetuspiste")
        delete self.data[key]
    })
  },

  isValid: function() {
    return (_.isEmpty(this.data["Opetuspiste"]) || !_.isEmpty(this.data["Koulutus-id"]))
  },

  setErrors: function(errors) {
    this.errors = errors || []
  },

  appendErrors: function(errors) {
    this.errors = this.errors.concat(errors)
  },

  setAsSaved: function() {
    this.isNew = false
    this.isModified = false
  },

  setAsModified: function() {
    this.isModified = true
  }
}

var hakutoiveErrorRegexp = /^(preference\d)$|^(preference\d)-Koulutus$/
Hakutoive.isHakutoiveError = function(questionId) {
  return hakutoiveErrorRegexp.test(questionId)
}

Hakutoive.parseHakutoiveIndex = function(questionId) {
  var result = /^preference(\d+)/.exec(questionId)
  if (result)
    return Number(result[1])
  else
    return null
}

Hakutoive.hasHakutoiveErrors = function(errorsJson) {
  var errorMap = util.mapArray(errorsJson, "key", "message");
  var self = this
  return _(errorMap).any(function(val, key) {
    return self.isHakutoiveError(key) && val.length > 0
  })
}

Hakutoive.hakutoiveMap = function(hakutoiveet) {
  return util.indexBy(hakutoiveet, function(hakutoive, index) { return "preference" + (index+1) })
}
Hakutoive.questionIdToHakutoiveId = function(questionId) {
  return _.chain(hakutoiveErrorRegexp.exec(questionId)).rest().without(undefined).first().value()
}
module.exports = Hakutoive