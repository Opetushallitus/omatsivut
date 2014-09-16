module.exports = function(listApp) {
  listApp.factory("restResources", ["$resource", function($resource) {
    return {
      applications: $resource("/omatsivut/api/applications", null, {
          "update": {
            method: "PUT",
            url: "/omatsivut/api/applications/:id"
          }
        }),

      valitseOpetuspiste: $resource("/omatsivut/api/valitseOpetuspiste", null, {
        "put": {
          method: "PUT",
          url: "/omatsivut/api/valitseOpetuspiste/:id"
        }
      }),

      koulutukset: $resource("/omatsivut/koulutusinformaatio/koulutukset/:asId/:opetuspisteId"),
      opetuspisteet: $resource("/omatsivut/koulutusinformaatio/opetuspisteet/:query")
    }
  }])
}