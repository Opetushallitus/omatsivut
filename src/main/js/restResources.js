module.exports = function(listApp) {
  listApp.factory("restResources", ["$resource", function($resource) {
    return {
      applications: $resource("/omatsivut/secure/applications", null, {
          "update": {
            method: "PUT",
            url: "/omatsivut/secure/applications/:id"
          }
        }),

      vastaanota: $resource("/omatsivut/secure/applications/vastaanota", null, {
        "post": {
          method: "POST",
          url: "/omatsivut/secure/applications/vastaanota/:hakuOid/:applicationOid"
        }
      }),

      koulutukset: $resource("/omatsivut/koulutusinformaatio/koulutukset/:asId/:opetuspisteId"),
      opetuspisteet: $resource("/omatsivut/koulutusinformaatio/opetuspisteet/:query")
    }
  }])
}