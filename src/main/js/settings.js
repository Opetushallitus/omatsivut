import { isTestMode } from './util';

export default class Settings {
  constructor($animate) {
    if (isTestMode()) {
      $animate.enabled(false);
      this.uiTransitionTime = 0;
      this.modelDebounce = 0;
      this.uiIndicatorDebounce = 0;
    } else {
      this.uiTransitionTime = 500;
      this.modelDebounce = 300;
      this.uiIndicatorDebounce = 500;
    }
  }
}

Settings.$inject = ['$animate'];
