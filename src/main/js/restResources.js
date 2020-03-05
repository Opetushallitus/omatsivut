export default ["$resource", "$http", '$injector', function($resource, $http, $injector) {
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
      console.log('setting cookie: ' + $injector.get('$cookies').get('CSRF'));
      const request = {
        method: 'POST',
        url: window.url("omatsivut.applications.validate", application.oid),
        headers: {
          'CSRF': $injector.get('$cookies').get('CSRF')
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
