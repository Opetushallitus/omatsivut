export default ["$resource", "$http", "$cookies", function($resource, $http, $cookies) {
  return {
    applications: $resource(window.url("omatsivut.applications"), null, {
      get: {
        method: "GET",
        isArray: false
      },
      "update": {
        method: "PUT",
        url: window.url("omatsivut.applications.update"),
        headers: {
          'CSRF': $cookies['CSRF']
        },
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
    opetuspisteet: $resource(window.url("omatsivut.opetuspisteet")),
    lasnaoloilmoittautuminen: $resource(window.url("omatsivut.lasnaoloilmoittautuminen"))
  }
}];
