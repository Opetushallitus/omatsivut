module.exports = function(listApp) {
  listApp.factory("localization", ["$http", "settings", function($http, settings) {
    var translations = {}
    $http.get('/omatsivut/translations/' + settings.language + '.json')
        .then(function(data){
            translations = data.data
        }, function(reason){
            throw new Error("Language not found: " + reason)
        })

    return function(key) {
        // TODO: this should be more safe:
        // If http promise is not already resolved, returns undefined for every key.
        // This already works for UI texts, but probably because of scope watches resolving new values after
        // promise has resolved.
        return translations[key]
    }
  }])
}