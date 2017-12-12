module.exports = function(listApp) {
  listApp.factory("restResources", ["$resource", "$http", function($resource, $http) {
    return {
      applications: $resource(window.url("omatsivut.applications"), null, {
        get: {
          method: "GET",
          isArray: false
        },
        "update": {
          method: "PUT",
          url: window.url("omatsivut.applications.update")
        }
      }),

      validate: function(application) {
        return $http.post(window.url( "omatsivut.applications.validate", application.oid), application.toJson())
      },

      vastaanota: $resource(window.url("omatsivut.applications.vastaanota"), null, {
        "post": {
          method: "POST",
          url: window.url("omatsivut.applications.vastaanota.post")
        }
      }),

      postOffice: $resource(window.url("omatsivut.postitoimipaikka")),
      koulutukset: $resource(window.url("omatsivut.koulutukset")),
      opetuspisteet: $resource(window.url("omatsivut.opetuspisteet"))
    }
  }])
}