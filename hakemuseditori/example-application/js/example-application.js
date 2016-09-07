require("angular");
require('ng-resource')(window, angular);
// copy-paste
require("../lib/ui-bootstrap-custom-tpls-0.10.0.min.js");
require('../lib/angular-debounce')
require('angular-module-sanitize')
angular.module("templates", [])
require("../../dev/hakemuseditori-templates")
require('./recursionHelper')
_ = require("underscore");
window.moment = require("moment");


var exampleApp = angular.module('exampleApp', ["ngResource", "debounce", "ngSanitize", "templates", "RecursionHelper", "ui.bootstrap.typeahead", "template/typeahead/typeahead-popup.html", "template/typeahead/typeahead-match.html"])

// example data controllers
require('./example-hakemus')(exampleApp)

// more copy-paste
var staticResources = require('./staticResources')
require('./localization')(exampleApp, staticResources)
require('./rest-resources')(exampleApp)
require('./settings')(exampleApp)

require('./hakemuseditori')(exampleApp)


staticResources.init(function() {
  angular.bootstrap(document, ['exampleApp'])
})


console.log("example initialized")
