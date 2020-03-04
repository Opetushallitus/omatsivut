export default ["$resource", "$http", '$cookies', function($resource, $http, $cookies) {
  return {
    applications: $resource(window.url("omatsivut.applications"), null, {
      get: {
        method: "GET",
        isArray: false
      },
      "update": {
        method: "PUT",
        url: window.url("omatsivut.applications.update"),
      }
    }),

    validate: function(application) {
      const request = {
        method: 'POST',
        url: window.url("omatsivut.applications.validate"),
        headers: {
          'CSRF': $cookies['CSRF']
        },
        data: application.toJson()
      };
      return $http(request);
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
