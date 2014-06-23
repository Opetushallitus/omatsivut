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

    var api = {
        openPage: openPage('/?hetu=010101-123N', visible),

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