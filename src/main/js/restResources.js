module.exports = function(listApp) {
  listApp.factory("restResources", ["$resource", function($resource) {
    return {
      applications: $resource("/omatsivut/api/applications", null, {
          "update": {
            method: "PUT",
            url: "/omatsivut/api/applications/:id"
          }
        }),

      koulutukset: $resource("/omatsivut/koulutusinformaatio/koulutukset/:asId/:opetuspisteId"),
      opetuspisteet: $resource("/omatsivut/koulutusinformaatio/opetuspisteet/:query")
    }
  }])
}