var prevLang = "fi"

function ApplicationListPage() {
  var testHetu = "010101-123N"

  var api = {
    openPage: function(pageLoadedCheck) {
      if (!pageLoadedCheck) {
        pageLoadedCheck = applicationPageVisible
      }
      return openPage("/omatsivut/#skipRaamit", pageLoadedCheck)
    },

    reloadPage: function() {
      return function() {
        if (!api.isVisible()) {
          return api.openPage()()
        } else {
          testFrame().angular.element(testFrame().jQuery("application-list").get(0)).scope().loadApplications()
          return wait.forAngular()
        }
      }
    },

    applyFixtureAndOpen: function(params) {
      return function() {
        return fixtures.applyFixture(params.fixtureName, params.applicationOid)
          .then(function() {
            if (prevLang != params.lang) {
              // Force frame reload
              $(testFrame().document).find("html").html("")
              prevLang = params.lang
            }
            return session.init(testHetu, params.lang)()
          })
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
          .then(api.reloadPage())
      }
    },

    applyValintatulosFixtureAndOpen: function(fixtureName, otherFixtures) {
      return function() {
        return fixtures.applyValintatulos(fixtureName, otherFixtures).then(api.reloadPage())
      }
    },

    applyErillishakuFixtureAndOpen: function(hyvaksytty) {
      return function(){
        return fixtures.applyErillishaku(hyvaksytty).then(api.reloadPage())
      }
    },

    setValintatulosServiceShouldFail: function(fail) {
      return function() {
        return fixtures.setValintatulosServiceFailure(fail)
      }
    },

    setApplicationStartAndOpen: function(applicationId, startTime) {
      return function() {
        return fixtures.setApplicationStart(applicationId, startTime).then(api.reloadPage())
      }
    },

    isVisible: function() {
      return S("#hakemus-list").is(":visible")
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

  function applicationPageVisible() {
    return S("#hakemus-list").attr("ng-cloak") == null && api.applications().length > 0
  }

  function getTemplateTexts() {
    return Q.all([
      Q($.get("/omatsivut/index.html")),
      Q($.ajax({type: "get", url:"/omatsivut/bundle.js", dataType:"text"})) // explicitly set datatype to prevent script execution
    ]).then(function(arr) {
      var indexHtml = arr[0]
      var templateIds = _.uniq(arr[1].match(/templates\/.*?html/g))
      var templates = _(templateIds).map(function(id) {
        var html = testFrame().angular.element(S("body")).injector().get("$templateCache").get(id)
        return $(html).text()
      })

      return _.flatten([$(indexHtml).text(), templates])
    })
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
        return wait.until(function() { return api.statusMessage() == "Muista lähettää muutokset" })()
      },

      isSavingState: function (isSaving) {
        return function () {
          return (getApplicationElement().find("form[name='applicationForm'] .ajax-spinner").length > 0) === isSaving
        }
      },

      saveButton: function () {
        return Button(function() { return getApplicationElement().find(".save-btn").first() }) 
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

      valintatulokset: function () {
        var application = getApplicationElement(applicationIndex)
        var nbsp = /\u00A0/g
        return application.find(application.find(".result-list tr[ng-repeat]"))
          .map(function () {
            var el = $(this)
            return {
              hakukohde: el.find("[ng-bind='tulos.tarjoajaNimi']").text() + " " + el.find("[ng-bind='tulos.hakukohdeNimi']").text(),
              tila: el.find("[ng-bind='valintatulosText(tulos)']").text().trim().replace(nbsp, " ")
            }
          }).toArray()
      },

      valintatulosError: function () {
        var el = getApplicationElement(applicationIndex).find(".application-valintatulos-error")
        return {
          visible: el.is(":visible"),
          text: el.find("span").text()
        }
      },

      ilmoittautuminen: function(index) {
        var el = getApplicationElement(applicationIndex).find(".ilmoittautuminen-item").eq(index)
        return {
          visible: el.is(":visible"),
          linkUrl: el.find("a").attr("href"),
          title: function() {
            return removeSpaces(el.find("header").text())
          }
        }
      },

      yhteystiedot: function() {
        var application = getApplicationElement(applicationIndex)
        return {
          getRow: function(id) {
            function element() {
              var item = S(_(application.find("henkilotiedot label")).find(function (item) {
                return S(item).text().indexOf(id) >= 0
              }))
              return item.find("input")
            }
            return YhteystietoRow(element)
          },

          postitoimipaikka: function() {
            return application.find("[ng-bind='application.calculatedValues.postOffice']").text()
          },

          isVisible: function() {
            return application.find(".henkilotiedot").is(":visible")
          },

          saveButton: function() {
            function element() {
              return application.find("henkilotiedot").parent().find("button")
            }
            return Button(element)
          }
        }
      },

      vastaanotettavia: function() {
        return getApplicationElement(applicationIndex).find(".hakutoiveenVastaanotto").length
      },

      vastaanotto: function(index) {
        function vastaanottoElement() {
          return getApplicationElement(applicationIndex).find(".hakutoiveenVastaanotto").eq(index)
        }
        return {
          visible: function() {
            return vastaanottoElement().is(":visible")
          },

          title: function() {
            return removeSpaces(vastaanottoElement().find("header").text())
          },

          info: function() {
            return vastaanottoElement().find("[ng-if='hakutoive.vastaanottoDeadline']").map(function() {
              return $(this).text().trim()
            }).toArray()
          },

          vaihtoehdot: function() {
            return vastaanottoElement().find(".hakutoive-options label:visible").map(function() {
              return $(this).text().trim()
            }).toArray()
          },

          selectOption: function(id) {
            return function() {
              vastaanottoElement().find("input[value='" + id + "']").click().click() // Angular hack
              return wait.forAngular()
            }
          },

          confirmButtonEnabled: function() {
            return !vastaanottoElement().find(".vastaanota-btn").prop("disabled")
          },

          send: function() {
            vastaanottoElement().find(".vastaanota-btn").click() // confirm
            return wait.forAngular()
          },

          errorText: function() {
            return vastaanottoElement().find(".status-message.error").first().text()
          }
        }
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
        return Questions(function() { return getApplicationElement().find(".questions") })
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
      var application = getApplicationElement(applicationIndex)
      return application.find(".preference-list-item:visible")
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
      var el = doGetApplicationElement()
      //if (el.get(0)) el.get(0).scrollIntoView()
      return el
    }

    function doGetApplicationElement() {
      if (typeof applicationIndex == "number") {
        return S("#hakemus-list>li").eq(applicationIndex)
      } else {
        return S('#hakemus-list>li[data-oid="' + applicationIndex + '"]').add('#hakemus-list>li:contains('+applicationIndex+')')
      }
    }
  }

  function YhteystietoRow(el) {
    return {
      clear: function() {
        el().prev(".clear-btn").click()
        return wait.forAngular()
      },

      val: function(newValue) {
        if (newValue == null)
          return el().val()
        else
          el().val(newValue).change()
      },

      error: function() {
        return el().closest(".value-column").find(".validation-message").text()
      }
    }
  }

  function Question(el) {
    return {
      title: function() {
        return el.find('.title').text()
      },
      inputs: function() {
        return el.find("input, textarea, select").map(function() {
          var o = {
            input: $(this),
          }
          if ($(this).parent().is('label')) {
            o.label = $(this).parent().text().trim()
          }
          return o
        }).toArray()
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
            id: testFrame().angular.element($(this).parent()).scope().questionNode.id.questionId
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
      getQuestionsByTitle: function(title) {
        return el().find(".title")
          .filter(function() { return $(this).text() === title })
          .closest('.question')
          .map(function() {
            return Question($(this))
          })
          .toArray()
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
              .filter(function(item) {
                return $(item).val() == $(item).filter(':checked').val()
              })
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
        return this.numberElement().text()
      },
      numberElement: function () {
        return el().find(".row-number")
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
      isNotPrioritized: function () {
        return !this.arrowDown().isVisible() && !this.arrowUp().isVisible() && !this.numberElement().is(":visible")
      },
      isEditable: function() {
        return el().find("input").is(":visible")
      },
      isLocked: function() {
        return !this.canRemove() && !this.isMovable()
      },
      isMovable: function() {
        return this.arrowDown().isEnabled() || this.arrowUp().isEnabled()
      },
      isLoadingHakukohde: function() {
        return el().find(".ajax-spinner-small").is(":visible") && el().find(".koulutus select").is(":not(:visible)")
      },
      hakukohdeItems: function() {
        return el().find(".koulutus option").map(function() { return $(this).text() }).toArray()
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
      hakuaika: function () {
        return el().find(".hakuaika").text().trim()
      },
      paymentNotificationIsShown: function() {
        return el().find(".aoPaymentNotification:visible").length === 1
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
      selectOpetusPiste: function (query, waitForResult) {
        waitForResult = waitForResult == null ? true : waitForResult

        return function() {
          return api.searchOpetusPiste(query)().then(function () {
            opetusPisteListView().find("li:contains('" + query + "')").eq(0).find("a").click()
          }).then(wait.until(function() {
            return !waitForResult || _(el().find(".koulutus select option")).any(function(el) { return $(el).text().length > 0 })
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
      isVisible: function() {
        return el().is(":visible")
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

  function removeSpaces(text) {
    return text.replace(/(\r\n|\n|\r)/gm,"").replace(/\s+/g," ").trim()
  }

  function trimText() {
    return removeSpaces($(this).text())
  }
}
