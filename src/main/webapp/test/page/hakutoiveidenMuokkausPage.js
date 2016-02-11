function HakutoiveidenMuokkausPage() {
  var applicationApi = ApplicationApi()

  var api = {

    isVisible: function() {
      return S("h1").text().trim() === "Hakutoiveiden muokkaus" && (api.alertMsg().length > 0 || api.getApplication().name().length > 0)
    },

    openPage: function(token, pageLoadedCheck) {
      if (!pageLoadedCheck) {
        pageLoadedCheck = hakutoiveidenMuokkausPageIsReady
      }
      return openPage("/omatsivut/hakutoiveidenMuokkaus.html#/token/" + token, pageLoadedCheck)
    },

    applyFixtureAndOpen: function(params) {
      return function() {
        return fixtures.applyFixture(params.fixtureName, params.applicationOid)
            .then(function() {
              if (params.overrideStart != null) {
                return fixtures.setApplicationStart(params.applicationOid, params.overrideStart)
              }
            })
            .then(function () {
              if(params.invertPriority) {
                return fixtures.setInvertedPriority(params.applicationOid)
              }
            })
            .then(api.openPage(params.token))
      }
    },

    getApplication: function() {
      return Application()
    },

    alertMsg: function() {
      return S(".alert").text().trim()
    },

    getNonLocalizedText: function() {
      return getTemplateTexts().then(function(templates) {
        function nonLocalizedText(text) {
          text = text.replace(/\{\{[\s\S]*?\}\}/g, "")
          return text.match(/\w+/)
        }

        var texts = _(templates).map(function(template) { return nonLocalizedText(template) })
        return _(texts).chain().compact().flatten().value().join("")
      })

    }
  }

  function hakutoiveidenMuokkausPageIsReady() {
    return api.isVisible()
  }

  return api

  function Application() {
    var api = {
      saveWaitSuccess: function() {
        return wait.until(api.saveButton().isEnabled)()
          .then(api.saveButton().click)
          .then(wait.untilFalse(api.saveButton().isEnabled)) // Tallennus on joko alkanut tai valmis
          .then(wait.until(api.isSavingState(false))) // Tallennus ei ole kesken
          .then(wait.until(function() { return api.saveError() == "" }))
          .then(wait.until(function() { return timestamp().text() != "01.01.1970 02:00:00" })) // tallennus-aikaleima päivittyy

        function timestamp() { return getApplicationElement().find(".timestamp time") }
      },

      name: function() {
        return S("h2").text().trim()
      },

      saveWaitError: function() {
        var status = api.statusMessage()
        api.saveButton().click()
        return wait.until(function() { return api.statusMessage() != status && api.saveError().length > 0 })()
      },

      waitValidationErrorForRequiredQuestion: function() {
        return wait.until(function() { return getApplicationElement().find(".validation-message.error").text() == "Pakollinen tieto." })()
      },

      waitValidationOk: function() {
        return wait.until(function() { return api.statusMessage() == "Muista lähettää muutokset" })()
      },

      isSavingState: function (isSaving) {
        return function () {
          return (getApplicationElement().find("form[name='applicationForm'] .ajax-spinner").length > 0) === isSaving
        }
      },

      saveButton: function () {
        return applicationApi.Button(function() { return getApplicationElement().find(".save-btn").first() }) 
      },

      getPreference: function (index) {
        return applicationApi.PreferenceItem(function () {
          return getApplicationElement().find(".preference-list-item").eq(index)
        })
      },

      preferencesForApplication: function () {
        return preferencesForApplication(function (item) {
          return (item.data()["hakutoive.Koulutus"] || []).length > 0
        }).map(function (item) {
          return item.data()
        })
      },

      isValidationErrorVisible: function() {
        return getApplicationElement().find("[name='applicationForm'] .status-message.error").first().is(":visible")
      },

      statusMessage: function() {
        return getApplicationElement().find("[name='applicationForm'] .status-message").first().text()
      },

      saveError: function() {
        return getApplicationElement().find("[name='applicationForm'] .status-message.error").first().text()
      },

      emptyPreferencesForApplication: function () {
        return preferencesForApplication(function (item) {
          return _.isEmpty(item.data()["hakutoive.Koulutus"])
        })
      },

      questionsForApplication: function () {
        return applicationApi.Questions(function() { return getApplicationElement().find(".questions") })
      },

      helpTextsForQuestions: function () {
        var helpTexts = []
        getApplicationElement().find(".helptext").each(function() { helpTexts.push($(this).text()) })
        return helpTexts
      },

      verboseHelpTextsForQuestions: function () {
        var verboseHelpTexts = [];
        getApplicationElement().find(".verboseHelp").each(function() { verboseHelpTexts.push($(this).attr("title")) });
        return verboseHelpTexts
      },

      changesSavedTimestamp: function () {
        return getApplicationElement().find(".timestamp").text().trim()
      },

      applicationStatus: function() {
        return getApplicationElement().find(".application-status").map(trimText).toArray().join(" ")
      },

      applicationPeriods: function() {
        return getApplicationElement().find("application-periods").map(trimText).toArray().join(" ")
      },

      previewLink: function() {
        return getApplicationElement().find(".preview")
      },

      resultTableTitle: function() {
        return getApplicationElement().find(".result-list th.ng-binding").text().trim()
      },

      labels: function() {
        return getApplicationElement().find("label").map(function() {
          return $(this).text().split(":")[0].trim() }
        )
      },

      found: function() {
        return getApplicationElement().length > 0
      },

      calloutText: function() {
        var callout = getApplicationElement().find("div[callout]:visible")
        return callout.text().trim()
      },

      isEditable: function() {
        return this.saveButton().isVisible() && _(preferencesForApplication()).any(function(preference) { return preference.isEditable() })
      }
    }
    return api

    function preferencesForApplication(filter) {
      filter = filter || function() { return true }
      var application = getApplicationElement()
      return application.find(".preference-list-item:visible")
        .map(function () {
          var el = this
          return applicationApi.PreferenceItem(function () {
            return S(el)
          })
        }).toArray()
        .filter(filter)
    }

    function getApplicationElement() {
      var el = doGetApplicationElement()
      //if (el.get(0)) el.get(0).scrollIntoView()
      return el
    }

    function doGetApplicationElement() {
      return S("application")
    }
  }
}
