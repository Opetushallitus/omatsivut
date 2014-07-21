function ApplicationListPage() {
  var testHetu = "010101-123N"
  var openListPage = openPage("/omatsivut/", visible)

  var api = {
    resetDataAndOpen: function () {
      return db.resetData().then(function() { return session.init(testHetu)} ).then(openListPage)
    },

    save: function() {
      return wait.until(api.saveButton(0).isEnabled(true))()
        .then(api.saveButton(0).click)
        .then(wait.until(api.saveButton(0).isEnabled(false))) // Tallennus on joko alkanut tai valmis
        .then(wait.until(api.isSavingState(0, false))) // Tallennus ei ole kesken
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

    saveButton: function (applicationIndex) {
      return saveButton(getApplication(applicationIndex).find(".save-btn"))
    },

    getPreference: function (index) {
      return PreferenceItem(function () {
        var applicationElement = getApplication(0)
        return applicationElement.find(".preference-list-item").eq(index)
      })
    },

    preferencesForApplication: function (index) {
      return preferencesForApplication(index, function (item) {
        return item.data()["hakutoive.Koulutus"].length > 0
      }).map(function (item) {
        return item.data()
      })
    },

    isValidationErrorVisible: function() {
      return getApplication(0).find(".status-message.error").is(":visible")
    },

    statusMessage: function() {
      return getApplication(0).find(".status-message").text()
    },

    saveError: function() {
      return getApplication(0).find(".status-message.error").text()
    },

    emptyPreferencesForApplication: function (index) {
      return preferencesForApplication(index, function (item) {
        return _.isEmpty(item.data()["hakutoive.Koulutus"])
      })
    },

    questionsForApplication: function (index) {
      return Questions(function() { return getApplication(index).find(".questions") })
    }
  }
  return api

  function Questions(el) {
    return {
      data: function() {
        return el().find(".question").map(function() {
          return {
            title: $(this).find(".title").text(),
            validationMessage: $(this).find(".validation-message").text()

          }
        }).toArray()
      },
      titles: function() {
        return _.pluck(this.data(), "title")
      },
      validationMessages: function() {
        return _.pluck(this.data(), "validationMessage")
      },
      enterAnswer: function(index, answer) {
        el().find(".question").eq(index).find("input").val(answer).change()
      }
    }
  }

  function PreferenceItem(el) {
    var api = {
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
        var parent = el().parent()
        var itemCount = parent.children().length
        var element = el()
        element.find(".delete-btn").click().click()

        return wait.until(function() { return element.parent().length === 0 })()
          .then(wait.until(function() { return parent.children().length === itemCount })) // wait until a new element has been inserted
      },
      canRemove: function() {
        return el().find(".delete-btn").is(":visible")
      },
      isNew: function () {
        return el().find("input:visible").length > 0
      },
      isDisabled: function () {
        return arrowDown().hasClass("disabled") && arrowUp().hasClass("disabled") && !api.canRemove()
      },
      isEditable: function() {
        return el().find("input").is(":visible")
      },
      opetuspiste: function () {
        return el().find(".opetuspiste input").val()
      },
      koulutus: function () {
        return el().find(".koulutus [ng-bind='hakutoive.data.Koulutus']").text()
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
            inputField.next().find("li:contains('" + query + "')").eq(0).find("a").click()
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