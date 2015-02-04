module.exports = function(listApp) {
  listApp.factory("restResources", ["$resource", "$http", function($resource, $http) {
    return {
      applications: $resource("/omatsivut/secure/applications", null, {
        "update": {
          method: "PUT",
          url: "/omatsivut/secure/applications/:id"
        }
      }),

      validate: function(application) {
        return $http.post("/omatsivut/secure/applications/validate/" + application.oid, application.toJson())
      },

      vastaanota: $resource("/omatsivut/secure/applications/vastaanota", null, {
        "post": {
          method: "POST",
          url: "/omatsivut/secure/applications/vastaanota/:hakuOid/:applicationOid"
        }
      }),

      postOffice: $resource("/omatsivut/koodisto/postitoimipaikka/:postalCode"),
      koulutukset: $resource("/omatsivut/koulutusinformaatio/koulutukset/:asId/:opetuspisteId"),
      opetuspisteet: $resource("/omatsivut/koulutusinformaatio/opetuspisteet/:query")
    }
  }])
}