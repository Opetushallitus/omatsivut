module.exports = function(application) {
  application.factory("restResources", ["$resource", function($resource) {
    return {
      applications: $resource("/api/applications", null, {
        "update": {
          method: "PUT",
          url: "/omatsivut/secure/applications/:id"
        }
      }),
      postOffice: $resource("/api/postitoimipaikka/:postalCode"),
      koulutukset: $resource("/api/koulutukset/:asId/:opetuspisteId"),
      opetuspisteet: $resource("/api/opetuspisteet/:query")
    }
  }])
}
