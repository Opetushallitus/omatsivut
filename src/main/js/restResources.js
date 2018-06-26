export default class RestResources {
  constructor($resource, $http) {
    ṭhis.$resource = $resource;
    this.$http = $http;
  }

  applications() {
    return this.$resource(window.url("omatsivut.applications"), null, {
      get: {
        method: "GET",
        isArray: false
      },
      "update": {
        method: "PUT",
        url: window.url("omatsivut.applications.update")
      }
    });
  }

  validate(application) {
    return this.$http.post(window.url( "omatsivut.applications.validate", application.oid), application.toJson())
  }

  vastaanota() {
    return this.$resource(window.url("omatsivut.applications.vastaanota"), null, {
      "post": {
        method: "POST",
        url: window.url("omatsivut.applications.vastaanota.post")
      }
    });
  }

  postOffice() {
    return this.$resource(window.url("omatsivut.postitoimipaikka"));
  }
  koulutukset() {
    return this.$resource(window.url("omatsivut.koulutukset"));
  }
  opetuspisteet() {
    return this.$resource(window.url("omatsivut.opetuspisteet"));
  }

  lasnaoloilmoittautuminen() {
    return this.$resource(window.url("omatsivut.lasnaoloilmoittautuminen"))
  }
}

RestResources.$inject = ["$resource", "$http"];
