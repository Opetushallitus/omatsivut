function Hakutoive(json) {
  this.data = json
  this.isModified = false
  this.isNew = _.isEmpty(json)
  this.errors = []
}

Hakutoive.prototype = {
  toJson: function() {
    return this.data
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
    this.isModified = true
    this.setErrors([])
    function toString(x) {
      return (x==null) ? "" : x.toString()
    }
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
    return (!this.errors.length) && (_.isEmpty(this.data["Opetuspiste"]) || !_.isEmpty(this.data["Koulutus-id"]))
  },

  setErrors: function(errors) {
    this.errors = errors || []
  },

  setAsSaved: function() {
    this.isNew = false
    this.isModified = false
  },

  setAsModified: function() {
    this.isModified = true
  }
}

module.exports = Hakutoive