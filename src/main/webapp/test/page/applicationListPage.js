function ApplicationListPage() {
  var testHetu = "010101-123N"

  var api = {
    openPage: openPage("/omatsivut/", applicationPageVisible),

    resetDataAndOpen: function () {
      return db.resetData().then(function() { return session.init(testHetu)} ).then(api.openPage)
    },

    hetu: function () {
      return testHetu
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
          .then(wait.until(function() { return timestamp().text() != "01.01.1970 02:00:00" })) // tallennus-aikaleima päivittyy

        function timestamp() { return getApplicationElement().find(".timestamp time") }
      },

      saveWaitError: function() {
        var status = api.statusMessage()
        api.saveButton().click()
        return wait.until(function() { return api.statusMessage() != status && api.saveError().length > 0 })()
      },

      isSavingState: function (isSaving) {
        return function () {
          return (getApplicationElement().find(".ajax-spinner").length > 0) === isSaving
        }
      },

      saveButton: function () {
        return saveButton(getApplicationElement().find(".save-btn"))

        function saveButton(el) {
          return {
            isEnabled: function () {
              return !el.prop("disabled")
            },
            click: function () {
              el.click()
            }
          }
        }
      },

      getPreference: function (index) {
        return PreferenceItem(function () {
          return getApplicationElement().find(".preference-list-item").eq(index)
        })
      },

      preferencesForApplication: function () {
        return preferencesForApplication(function (item) {
          return item.data()["hakutoive.Koulutus"].length > 0
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
        return testFrame.angular.element(getApplicationElement()).scope()
      }
    }

    function getApplicationElement() {
      return S("#hakemus-list>li").eq(applicationIndex)
    }
  }

  function Questions(el) {
    return {
      data: function() {
        return el().find(".question").map(function() {
          return {
            title: $(this).find(".title").text(),
            validationMessage: $(this).find(".validation-message").text(),
            id: testFrame.angular.element($(this).parent()).scope().questionNode.question.id.questionId

          }
        }).toArray()
      },
      titles: function() {
        return _.pluck(this.data(), "title")
      },
      validationMessages: function() {
        return _.pluck(this.data(), "validationMessage")
      },
      validationMessageCount: function() {
        return _(this.validationMessages()).filter(function(msg) { return msg.length > 0 }).length
      },
      enterAnswer: function(index, answer) {
        function inputType(el) {
          if (el.prop("tagName") == "SELECT" || el.prop("tagName") == "TEXTAREA")
            return el.prop("tagName")
          else
            return el.prop("type").toUpperCase()
        }
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
        //input.val(answer).change()
      },
      getAnswer: function(index) {
        return el().find(".question").eq(index).find("input").val()
      },
      count: function() {
        return this.data().length
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
        return waitForChange(function() {
          arrowUp().click()
        })
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
            return el().find(".koulutus select option").length > 1
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
      })().then(wait.forAngular)
    }
  }
}