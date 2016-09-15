module.exports = function(listApp, isTestMode) {
  listApp.factory("settings", ["$animate", function($animate) {
    if (isTestMode) $animate.enabled(false)
    return {
      uiTransitionTime: isTestMode ? 0 : 500,
      modelDebounce: isTestMode ? 0 : 300,
      uiIndicatorDebounce: isTestMode ? 0: 500
    };
  }]);
}