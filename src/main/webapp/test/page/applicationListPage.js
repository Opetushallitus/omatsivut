function ApplicationListPage() {
    function visible() {
        return S("#hakemus-list").attr("ng-cloak") == null && api.applications().length > 0
    }

    function preferenceItem(el) {
        return {
            element: function() { return el },
            data: function() { return uiUtil.inputValues(el) },
            arrowDown: function() { return el.find(".sort-arrow-down") },
            number: function() { return el.find(".row-number") },
            deleteBtn: function() { return el.find(".delete-btn") }
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

    var openListPage = openPage("/?hetu=" + testHetu, visible)

    function getApplication(index) { return S("#hakemus-list>li").eq(index) }

    var api = {
        resetDataAndOpen: function() { return db.resetData().then(openListPage) },

        hetu: function() { return testHetu },

        openPage: openListPage,

        applications: function() {
            return S("#hakemus-list>li")
                .filter(function() { return $(this).find("h2").length > 0 })
                .map(function() { return { applicationSystemName: $(this).find("h2").text().trim() } }).toArray()
        },

        preferencesForApplication: function(index) {
            var application = getApplication(index)
            return application.find(".preference-list-item")
                .map(function() { return preferenceItem(S(this))}).toArray()
                .filter(function(item) { return item.data()["hakutoive.Koulutus"].length > 0 })
        },

        saveButton: function(applicationIndex) {
            return saveButton(getApplication(applicationIndex).find(".save-btn"))
        }
    }
    return api
}