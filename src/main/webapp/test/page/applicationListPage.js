function ApplicationListPage() {
  var testHetu = "010101-123N"

  var api = {
    openPage: function(pageLoadedCheck) {
      if (!pageLoadedCheck) {
        pageLoadedCheck = applicationPageVisible
      }
      return openPage("/omatsivut/#skipRaamit", pageLoadedCheck)
    },

    resetDataAndOpen: function () {
      return db.applyFixture().then(function() { return session.init(testHetu)} ).then(api.openPage())
    },

    applyFixtureAndOpen: function(fixtureName) {
      return function() {
        return db.applyFixture(fixtureName).then(function() { return session.init(testHetu)} ).then(api.openPage())
      }
    },

    hetu: function () {
      return testHetu
    },

    listStatusInfo: function () {
      return S(".application-list-status.info").text().trim()
    },

    applications: function () {
      return S("#hakemus-list>li")
        .filter(function () {
          return $(this).find("h2").length > 0
        })
        .map(function () {
          return { applicationSystemName: $(this).find("h2").text().trim() }
        }).toArray()
    },

    getApplication: function(applicationIndex) {
      return Application(applicationIndex)
    }
  }

  function applicationPageVisible() {
    return S("#hakemus-list").attr("ng-cloak") == null && api.applications().length > 0
  }
  return api

  function Application(applicationIndex) {
    var api = {

      saveWaitSuccess: function() {
        modifyApplicationScope(function(scope) { scope.application.updated = 0 })
        return wait.until(api.saveButton().isEnabled)()
          .then(api.saveButton().click)
          .then(wait.untilFalse(api.saveButton().isEnabled)) // Tallennus on joko alkanut tai valmis
          .then(wait.until(api.isSavingState(false))) // Tallennus ei ole kesken
          .then(wait.until(function() { return api.saveError() == "" }))
          .then(wait.until(function() { return timestamp().text() != "01.01.1970 02:00:00" })) // tallennus-aikaleima päivittyy

        function timestamp() { return getApplicationElement().find(".timestamp time") }
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
        return wait.until(function() { return api.statusMessage() == "Muista tallentaa muutokset" })()
      },

      isSavingState: function (isSaving) {
        return function () {
          return (getApplicationElement().find(".ajax-spinner").length > 0) === isSaving
        }
      },

      saveButton: function () {
        return Button(function() { return getApplicationElement().find(".save-btn") }) 
      },

      getPreference: function (index) {
        return PreferenceItem(function () {
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
        return getApplicationElement().find(".status-message.error").is(":visible")
      },

      statusMessage: function() {
        return getApplicationElement().find(".status-message").text()
      },

      saveError: function() {
        return getApplicationElement().find(".status-message.error").text()
      },

      emptyPreferencesForApplication: function () {
        return preferencesForApplication(function (item) {
          return _.isEmpty(item.data()["hakutoive.Koulutus"])
        })
      },

      questionsForApplication: function () {
        return Questions(function() { return getApplicationElement().find(".questions") })
      },

      changesSavedTimestamp: function () {
        return getApplicationElement().find(".timestamp").text()
      },

      applicationPeriod: function() {
        return getApplicationElement().find(".application-period").children().map(function() {
          return $(this).text().replace(/(\r\n|\n|\r)/gm,"").replace(/\s+/g," ").trim() }
        ).toArray().join(" ")
      },

      previewLink: function() {
        return getApplicationElement().find(".preview")
      },

      convertToKorkeakouluhaku: function() {
        modifyApplicationScope(function(scope) {
          scope.application.haku.korkeakouluhaku = true
        })
      },

      labels: function() {
        return getApplicationElement().find("label").map(function() {
          return $(this).text().split(":")[0].trim() }
        )
      },

      found: function() {
        return getApplicationElement().length > 0
      },

      applicationState: function() {
        return getApplicationElement().find(".application-state-message").text().trim()
      },

      calloutText: function() {
        var callout = getApplicationElement().find("div[callout]:visible")
        return callout.text().trim()
      }
    }
    return api

    function preferencesForApplication(filter) {
      var application = getApplicationElement(applicationIndex)
      return application.find(".preference-list-item")
        .map(function () {
          var el = this
          return PreferenceItem(function () {
            return S(el)
          })
        }).toArray()
        .filter(filter)
    }

    function modifyApplicationScope(manipulationFunction) {
      var scope = getApplicationScope(applicationIndex)
      scope.$apply(function() { manipulationFunction(scope) })

      function getApplicationScope() {
        return testFrame().angular.element(getApplicationElement()).scope()
      }
    }

    function getApplicationElement() {
      if (typeof applicationIndex == "number") {
        return S("#hakemus-list>li").eq(applicationIndex)
      } else {
        return S('#hakemus-list>li[data-oid="' + applicationIndex + '"]')
      }
    }
  }

  function Questions(el) {
    return {
      data: function() {
        return el().find(".question").map(function() {
          return {
            title: $(this).find(".title").text(),
            validationMessage: $(this).find(".validation-message").text(),
            id: testFrame().angular.element($(this).parent()).scope().questionNode.question.id.questionId

          }
        }).toArray()
      },
      titles: function() {
        return _.pluck(this.data(), "title")
      },
      groupTitles: function() {
        var titles = _(el().find(".application-section-heading")).map(function(el) { return $(el).text().replace(/\s+/g, " ").trim() })
        return titles
      },
      validationMessages: function() {
        return _.pluck(this.data(), "validationMessage")
      },
      validationMessageCount: function() {
        return _(this.validationMessages()).filter(function(msg) { return msg.length > 0 }).length
      },
      enterAnswer: function(index, answer) {
        var input = el().find(".question").eq(index).find("input, textarea, select")
        switch (inputType(input)) {
          case "TEXT":
          case "TEXTAREA":
            input.val(answer).change(); break;
          case "CHECKBOX":
            var option = _(input).find(function(item) { return $(item).parent().text().trim() == answer })
            $(option).click()
            break;
          case "RADIO":
            var option = _(input).find(function(item) { return $(item).parent().text().trim() == answer })
            $(option).click()
            break;
          case "SELECT":
            var option = _(input.children()).find(function(item) { return $(item).text().trim() == answer })
            input.val($(option).attr("value")).change()
            break;

        }
      },
      getAnswer: function(index) {
        var input = el().find(".question").eq(index).find("input, textarea, select")
        switch (inputType(input)) {
          case "TEXT":
          case "TEXTAREA":
            return input.val(); break;
          case "CHECKBOX":
            throw new Error("todo")
            break;
          case "RADIO":
            return _(input).chain()
              .filter(function(item) { return $(item).val() == "true" })
              .map(function(item) { return $(item) })
              .first().value().closest("label").text().trim()
          case "SELECT":
            return $(_(input.children()).find(function(opt) { return opt.value == input.val() })).text()
          default:
            throw new Error("todo")
        }
      },
      count: function() {
        return this.data().length
      }
    }

    function inputType(el) {
      if (el.prop("tagName") == "SELECT" || el.prop("tagName") == "TEXTAREA")
        return el.prop("tagName")
      else
        return el.prop("type").toUpperCase()
    }
  }

  function PreferenceItem(el) {
    var api = {
      data: function () {
        return uiUtil.inputValues(el())
      },
      moveDown: function () {
        return waitForChange(function() {
          api.arrowDown().click()
        })
      },
      moveUp: function () {
        return waitForChange(function() {
          api.arrowUp().click()
        })
      },
      arrowDown: function() {
        return Button(function() { return el().find(".sort-arrow-down") })
      },
      arrowUp: function() {
        return Button(function() { return el().find(".sort-arrow-up") })
      },
      removeButton: function() {
        return Button(function() { return el().find(".delete-btn") })
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
          .then(wait.forAngular)
      },
      canRemove: function() {
        return el().find(".delete-btn").is(":visible")
      },
      isNew: function () {
        return el().find("input:visible").length > 0
      },
      isDisabled: function () {
        return !this.arrowDown().isEnabled() && !this.arrowUp().isEnabled() && !api.canRemove()
      },
      isEditable: function() {
        return el().find("input").is(":visible")
      },
      isMovable: function() {
        return this.arrowDown().isEnabled() && this.arrowUp().isEnabled()
      },
      errorMessage: function() {
        return el().find(".error").text()
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
      searchOpetusPiste: function (query) {
        return function() {
          opetusPisteInputField().val(query).change()
          return wait.forAngular()
        }
      },
      selectOpetusPiste: function (query) {
        return function() {
          return api.searchOpetusPiste(query)().then(function () {
            opetusPisteListView().find("li:contains('" + query + "')").eq(0).find("a").click()
          }).then(wait.until(function() {
            return _(el().find(".koulutus select option")).any(function(el) { return $(el).text().length > 0 })
          })).then(wait.forAngular)
        }
      },

      selectKoulutus: function (index) {
        return function() {
          var selectElement = el().find(".koulutus select")
          selectElement.val(index).change()
          return wait.forAngular()
        }
      }
    }
    return api

    function opetusPisteInputField() {
      return el().find(".opetuspiste input")
    }

    function opetusPisteListView() {
      return opetusPisteInputField().next()
    }

    function waitForChange(modification) {
      var description = api.toString()
      modification()
      return wait.until(function() {
        return description != api.toString()
      })().then(wait.forAngular)
    }
  }

  function Button(el) {
    return {
      element: function() {
        return el()
      },
      isEnabled: function () {
        return !el().prop("disabled")
      },
      click: function () {
        el().click()
      },
      isRealButton: function() {
        return el().prop("tagName") == "BUTTON"
      },
      hasTabIndex: function() {
        return el().prop("tabIndex") > 0
      },
      isFocusableBefore: function(button) {
        return !this.hasTabIndex() && !button.hasTabIndex() && compareDOMIndex(this.element(), button.element()) < 0
      }
    }
  }

  function compareDOMIndex(el1, el2) {
    function indexInTree(element, indexes) {
      indexes = indexes || []
      indexes.unshift(element.index())
      if (element.parent().length)
        indexInTree(element.parent(), indexes)
      return indexes
    }

    function compareTrees(tree1, tree2) {
      for (var i=0; i<tree1.length && i < tree2.length; i++) {
        if (tree1[i] < tree2[i])
          return -1
        else if (tree1[i] > tree2[i])
          return 1
      }
      return 0
    }

    return compareTrees(indexInTree(el1), indexInTree(el2))
  }
}