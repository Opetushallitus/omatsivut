function ApplicationListPage() {
    function visible() {
        return S("#hakemus-list").attr("ng-cloak") == null && api.applications().length > 0
    }

    function preferenceItem(el) {
        return {
            element: function() { return el },
            data: function() { return uiUtil.inputValues(el) },
            arrowDown: function() { return el.find(".sort-arrow-down") },
            arrowUp: function() { return el.find(".sort-arrow-up") },
            number: function() { return el.find(".row-number") },
            deleteBtn: function() { return el.find(".delete-btn") },
            isDisabled: function() { return this.arrowDown().hasClass("disabled") && this.arrowUp().hasClass("disabled") && this.deleteBtn().is(":hidden")}
        }
    }

    function saveButton(el) {
        return {
            element: function() { return el },
            isEnabled: function(isEnabled) { return function() { return el.prop("disabled") != isEnabled } },
            click: function() { el.click() }
        }
    }

    var testHetu = "010101-123N"

    var openListPage = openPage("/omatsivut/", visible)

    function getApplication(index) { return S("#hakemus-list>li").eq(index) }

    function preferencesForApplication(index, filter) {
        var application = getApplication(index)
        return application.find(".preference-list-item")
            .map(function() { return preferenceItem(S(this))}).toArray()
            .filter(filter)
    }

    var api = {
        resetDataAndOpen: function() { return db.resetData().then(openListPage) },

        hetu: function() { return testHetu },

        openPage: openListPage,

        applications: function() {
            return S("#hakemus-list>li")
                .filter(function() { return $(this).find("h2").length > 0 })
                .map(function() { return { applicationSystemName: $(this).find("h2").text().trim() } }).toArray()
        },

        isSavingState: function(applicationIndex, isSaving) {
            return function() { return (getApplication(applicationIndex).find(".ajax-spinner").length > 0) === isSaving }
        },

        preferencesForApplication: function(index) {
            return preferencesForApplication(index,  function(item) { return item.data()["hakutoive.Koulutus"].length > 0 })
        },

        emptyPreferencesForApplication: function(index) {
            return preferencesForApplication(index, function(item) { return item.data()["hakutoive.Koulutus"].length == 0 })
        },

        saveButton: function(applicationIndex) {
            return saveButton(getApplication(applicationIndex).find(".save-btn"))
        }
    }
    return api
}