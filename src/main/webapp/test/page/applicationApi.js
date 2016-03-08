var hakemusKorkeakouluKevatWithJazzId = "1.2.246.562.11.00000000178"
var hakemusYhteishakuKevat2016Ammatillisia = "1.2.246.562.11.00004883029"
var hakemusYhteishakuKevat2016PelkkaLukio = "1.2.246.562.11.00004883579"

function ApplicationApi() {
  var applicationApi = {
    Question: function(el) {
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
    },

    Questions: function(el) {
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
                return applicationApi.Question($(this))
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
    },

    PreferenceItem: function(el) {
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
          return applicationApi.Button(function() { return el().find(".sort-arrow-down") })
        },
        arrowUp: function() {
          return applicationApi.Button(function() { return el().find(".sort-arrow-up") })
        },
        removeButton: function() {
          return applicationApi.Button(function() { return el().find(".delete-btn") })
        },
        number: function () {
          return this.numberElement().text()
        },
        numberElement: function () {
          return el().find(".row-number")
        },
        el: function() {
          return el()
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
    },

    Button: function(el) {
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
  }
  return applicationApi
}
