function ApplicationListPage() {
    var api = {
        visible: function() {
            return api.applications().length > 0
        },
        applications: function() {
            return S("#hakemus-list>li")

                .filter(function() { return $(this).find("h2").length > 0 })
                .map(function() { return { applicationSystemName: $(this).find("h2").text().trim() } }).toArray()
        }
    }
    return api
}