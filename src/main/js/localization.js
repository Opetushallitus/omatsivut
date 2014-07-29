module.exports = function(listApp) {
  listApp.factory("localization", ["settings", function(settings) {
    var translations = {
      fi: {
        loadingApplications: "Hakemuksia ladataan...",
        loadingFailed: "Tietojen lataus epäonnistui. Yritä myöhemmin uudelleen.",
        loadingFailed_notLoggedIn: "Tietojen lataus epäonnistui: et ole kirjautunut sisään.",
        timestamp_applicationUpdated: "Hakemusta muokattu",
        timestamp_applicatonReceived: "Hakemus jätetty",
        validationFailed: "Täytä kaikki tiedot",
        sessionExpired: "Istunto on vanhentunut. Kirjaudu uudestaan sisään",
        validationFailed_httpError: "Tietojen haku epäonnistui. Yritä myöhemmin uudelleen.",
        changesSaved: "Kaikki muutokset tallennettu",

        saveFailed: "Tallentaminen epäonnistui. Yritä myöhemmin uudelleen.",
        saveFailed_sessionExpired: "Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.",
        saveFailed_validationError: "Ei tallennettu - vastaa ensin kaikkiin lisäkysymyksiin",

        serverError: "Odottamaton virhe. Ota yhteyttä ylläpitoon."
      }
    }

    return function(key) {
      var lang = translations[settings.language]
      if (!lang)
        throw new Error("Language not found")

      var val = lang[key]
      if (val)
        return val
      else
        throw new Error("Translation not found for: " + key)
    }
  }])
}