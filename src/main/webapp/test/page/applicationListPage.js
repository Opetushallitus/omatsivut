function ApplicationListPage() {
    function visible() {
        return S("#hakemus-list").attr("ng-cloak") == null && api.applications().length > 0
    }

    var api = {
        openPage: openPage('/?hetu=010101-123N', visible),

        applications: function() {
            return S("#hakemus-list>li")
                .filter(function() { return $(this).find("h2").length > 0 })
                .map(function() { return { applicationSystemName: $(this).find("h2").text().trim() } }).toArray()
        }
    }
    return api
}