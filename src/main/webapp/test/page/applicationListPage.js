function ApplicationListPage() {
  var testHetu = "010101-123N"
  var openListPage = openPage("/omatsivut/", visible)

  var api = {
    resetDataAndOpen: function () {
      return db.resetData().then(openListPage)
    },

    hetu: function () {
      return testHetu
    },

    openPage: openListPage,

    applications: function () {
      return S("#hakemus-list>li")
        .filter(function () {
          return $(this).find("h2").length > 0
        })
        .map(function () {
          return { applicationSystemName: $(this).find("h2").text().trim() }
        }).toArray()
    },

    isSavingState: function (applicationIndex, isSaving) {
      return function () {
        return (getApplication(applicationIndex).find(".ajax-spinner").length > 0) === isSaving
      }
    },

    preferencesForApplication: function (index) {
      return preferencesForApplication(index, function (item) {
        return item.data()["hakutoive.Koulutus"].length > 0
      })
    },

    emptyPreferencesForApplication: function (index) {
      return preferencesForApplication(index, function (item) {
        return item.data()["hakutoive.Koulutus"].length == 0
      })
    },

    saveButton: function (applicationIndex) {
      return saveButton(getApplication(applicationIndex).find(".save-btn"))
    },

    getPreference: function (index) {
      return PreferenceItem(function () {
        var applicationElement = getApplication(0)
        return applicationElement.find(".preference-list-item").eq(index)
      })
    }
  }
  return api

  function PreferenceItem(el) {
    api = {
      data: function () {
        return uiUtil.inputValues(el())
      },
      moveDown: function () {
        return waitForChange(function() {
          arrowDown().click()
        })
      },
      moveUp: function () {
        arrowUp().click()
      },

      number: function () {
        return el().find(".row-number").text()
      },
      remove: function () {
        return waitForChange(function() {
          el().find(".delete-btn").click().click()
        })
      },
      isNew: function () {
        return el().find("input:visible").length > 0
      },
      isDisabled: function () {
        return arrowDown().hasClass("disabled") && arrowUp().hasClass("disabled") && api.deleteBtn().is(":hidden")
      },
      opetuspiste: function () {
        return el().find(".opetuspiste input").val()
      },
      koulutus: function () {
        return el().find(".koulutus input").val()
      },
      toString: function() {
        return api.opetuspiste() + " " + api.koulutus()
      },
      selectOpetusPiste: function (query) {
        return function() {
          var inputField = el().find(".opetuspiste input");
          inputField.val(query).change()
          return wait.until(function () {
            return inputField.next().find("li").eq(0).find("a").length > 0
          })().then(function () {
            inputField.next().find("li").eq(0).find("a").click()
          }).then(wait.until(function() {
            return el().find(".koulutus select option").length > 1
          }))
        }
      },
      selectKoulutus: function (index) {
        return function() {
          var selectElement = el().find(".koulutus select")
          selectElement.val(index).change()
        }
      }
    }
    return api

    function arrowDown() {
      return el().find(".sort-arrow-down")
    }

    function arrowUp() {
      return el().find(".sort-arrow-up")
    }

    function waitForChange(modification) {
      var description = api.toString()
      modification()
      return wait.until(function() {
        return description != api.toString()
      })()
    }
  }

  function visible() {
    return S("#hakemus-list").attr("ng-cloak") == null && api.applications().length > 0
  }

  function saveButton(el) {
    return {
      isEnabled: function (isEnabled) {
        return function () {
          return el.prop("disabled") != isEnabled
        }
      },
      click: function () {
        el.click()
      }
    }
  }

  function getApplication(index) {
    return S("#hakemus-list>li").eq(index)
  }

  function preferencesForApplication(index, filter) {
    var application = getApplication(index)
    return application.find(".preference-list-item")
      .map(function () {
        var el = this
        return PreferenceItem(function () {
          return S(el)
        })
      }).toArray()
      .filter(filter)
  }
}