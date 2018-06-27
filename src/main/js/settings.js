import { isTestMode } from './util';

export default ["$animate", function($animate) {
  if (isTestMode()) $animate.enabled(false);
  return {
    uiTransitionTime: isTestMode() ? 0 : 500,
    modelDebounce: isTestMode() ? 0 : 300,
    uiIndicatorDebounce: isTestMode() ? 0: 500
  };
}]
