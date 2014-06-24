function ApplicationListPage() {
    function visible() {
        return S("#hakemus-list").attr("ng-cloak") == null && api.applications().length > 0
    }

    function preferenceItem(el) {
        return {
            element: function() { return el },
            data: function() { return uiUtil.inputValues(el) },
            arrowDown: function() { return el.find(".sort-arrow-down") },
            number: function() { return el.find(".row-number") }
        }
    }

    function resetData(callback) {
        $.ajax("/fixtures/apply", { type: "PUT" }).done(callback)
    }

    function compose(f1, f2) {
        return function(done) {
            f1(function() { f2(done) })
        }
    }

    var openListPage = openPage('/?hetu=010101-123N', visible)

    var api = {
        resetDataAndOpen: compose(resetData, openListPage),

        openPage: openListPage,

        applications: function() {
            return S("#hakemus-list>li")
                .filter(function() { return $(this).find("h2").length > 0 })
                .map(function() { return { applicationSystemName: $(this).find("h2").text().trim() } }).toArray()
        },

        preferencesForApplication: function(index) {
            var application = S("#hakemus-list>li").eq(index)
            return application.find(".preference-list-item")
                .map(function() { return preferenceItem(S(this))}).toArray()
                .filter(function(item) { return item.data()["hakutoive.Koulutus"].length > 0 })
        }
    }
    return api
}