import { mapArray, indexBy } from '../util';

const _ = require('underscore');
const hakutoiveErrorRegexp = /^(preference\d)$|^(preference\d)-Koulutus$/;

export default class Hakutoive {
  constructor(json) {
    this.importJson(json)
    this.isModified = false
    this.isNew = _.isEmpty(json)
    this.addedDuringCurrentSession = _.isEmpty(json)
    this.errors = []
  }

  toJson() {
    return this.data
  }

  importJson(json) {
    this.data = json.hakemusData || {};
    this.hakuaikaId = json.hakuaikaId;
    this.kohdekohtainenHakuaika = json.kohdekohtainenHakuaika;
    this.koulutuksenAlkaminen = json.koulutuksenAlkaminen;
    this.yhdenPaikanSaanto = json.yhdenPaikanSaanto;
  }

  clear() {
    this.data = {}
    this.isNew = true
    this.isModified = false
  }

  hasData() {
    return !_.isEmpty(this.data)
  }

  setOpetuspiste(id, name) {
    this.data["Opetuspiste"] = name
    this.data["Opetuspiste-id"] = id
    this.isModified = true
    this.setErrors([])
  }

  setKoulutus(koulutus) {
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
    this.data["Koulutus-id-discretionary"] = toString(koulutus.kysytaanHarkinnanvaraiset)
    this.data["Koulutus-id-attachments"] = toString(koulutus.attachments != null && koulutus.attachments.length > 0)
    this.data["Koulutus-requiredBaseEducations"] = (koulutus.requiredBaseEducations || []).join(",")
    this.addGroupInfo(koulutus)
    this.isModified = true
    this.setErrors([])
    function toString(x) {
      return (x==null) ? "" : x.toString()
    }
  }

  addGroupInfo(koulutus) {
    var attachmentGroups = [];
    var aoGroups = [];
    if (koulutus.organizationGroups instanceof Array) {
      for (var i = 0; i < koulutus.organizationGroups.length; i++) {
        var group = koulutus.organizationGroups[i];
        if(group.groupTypes.indexOf("hakukohde") >= 0) {
          aoGroups.push(group.oid);
          if(group.usageGroups.indexOf("hakukohde_liiteosoite") >= 0) {
            attachmentGroups.push(group.oid);
          }
        }
      }
    }
    if(aoGroups.length > 0) {
      this.data["Koulutus-id-ao-groups"] = aoGroups.join(",")
    }
    if(attachmentGroups.length > 0) {
      this.data["Koulutus-id-attachmentgroups"] = attachmentGroups.join(",")
    }
  }

  hasOpetuspiste() {
    return !_.isEmpty(this.data["Opetuspiste-id"])
  }

  removeOpetuspisteData() {
    var self = this
    _.each(this.data, function(value, key) {
      if (key.indexOf("$")!==0 && key != "Opetuspiste")
        delete self.data[key]
    })
    delete this.kohdekohtainenHakuaika
  }

  isValid() {
    return (_.isEmpty(this.data["Opetuspiste"]) || !_.isEmpty(this.data["Koulutus-id"]))
  }

  setErrors(errors) {
    this.errors = errors || []
  }

  appendErrors(errors) {
    this.errors = this.errors.concat(errors)
  }

  setAsSaved() {
    this.isNew = false
    this.isModified = false
  }

  setAsModified() {
    this.isModified = true
  }

  static isHakutoiveError(questionId) {
    return hakutoiveErrorRegexp.test(questionId)
  }

  static parseHakutoiveIndex(questionId) {
    var result = /^preference(\d+)/.exec(questionId)
    if (result)
      return Number(result[1])
    else
      return null
  }

  static hasHakutoiveErrors(errorsJson) {
    var errorMap = mapArray(errorsJson, "key", "message");
    var self = this
    return _(errorMap).any(function(val, key) {
      return self.isHakutoiveError(key) && val.length > 0
    })
  }

  static hakutoiveMap(hakutoiveet) {
    return indexBy(hakutoiveet, function(hakutoive, index) { return "preference" + (index+1) })
  }

  static questionIdToHakutoiveId(questionId) {
    return _.chain(hakutoiveErrorRegexp.exec(questionId)).rest().without(undefined).first().value()
  }

}
