var prevLang = "fi"

function ApplicationListPage() {
  var testHetu = "010100A939R"
  var applicationApi = ApplicationApi()

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
            else {
              fixtures.resetApplicationStart(params.applicationOid)
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

    loadInFrame: function(src) {
        return function() {
            $('#testframe')
                .attr('src', src)
                .attr('width', 1024)
                .attr('height', 800)
                .on('error', function (err) {
                    console.error(err);
                    window.uiError = err;
                });
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

    tuloskirjeet: function () {
      return S(".tuloskirje").text().trim()
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

  function getTemplateTexts() { //FIXME: we don't use template cache
    return Q.all([
      Q($.get("/omatsivut/index.html")),
      Q($.ajax({type: "get", url:"/omatsivut/index.bundle.js", dataType:"text"})) // explicitly set datatype to prevent script execution
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
        return applicationApi.Button(function() { return getApplicationElement().find(".save-btn").last() }) 
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

      valintatulokset: function () {
        var application = getApplicationElement(applicationIndex);
        var nbsp = /\u00A0/g;
        return application.find(".result-list-wrap.ng-scope")
          .map(function () {
            var el = $(this)
            return {
              hakukohde: el.find("[ng-bind='tulos.tarjoajaNimi']").text() + " " + el.find("[ng-bind='tulos.hakukohdeNimi']").text(),
              tila: el.find("span.valintatulos-otsake").text().trim().replace(nbsp, " ")
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
          visible: el.find("a").is(":visible"),
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

          singleStudyPlaceEnforcement: function() {
            return vastaanottoElement().find(".hakutoive-vain-yksi-valittavissa").map(function() {
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

          selectedIndex: function() {
              var checked = vastaanottoElement().find("[type=radio]:checked");
              return checked.index()
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
        return getApplicationElement().find("[name='applicationForm'] .status-message:visible").first().text()
      },

      saveError: function() {
        return getApplicationElement().find("[name='applicationForm'] .status-message.error:visible").first().text()
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
        return getApplicationElement().find(".timestamp-row a:nth-child(2)")
      },

      resultTableTitle: function() {
        return getApplicationElement().find(".result-list-header span.ng-binding").text().trim()
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

      calloutLink: function() {
        var callout = getApplicationElement().find("div[callout]:visible")
        return callout.find("a").attr("href")
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
          return applicationApi.PreferenceItem(function () {
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
}
