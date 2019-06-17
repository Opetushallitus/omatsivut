import localize from '../localization';
import moment from '../moment';

export default class HakutoiveenVastaanotto {
  constructor() {
    this.restrict = 'E';
    this.bindToController = {
      applicationOid: '&',
      haku: '&',
      hakutoiveet: '&',
      callback: '='
    };
    this.template = require('./hakutoiveenVastaanotto.html');
    this.controller = HakutoiveenVastaanottoController;
    this.controllerAs = 'ctrl';
  }

  link(scope, element, attrs) {
    scope.localization = localize;
  }
}

class HakutoiveenVastaanottoController {
  constructor($timeout, restResources, $scope) {
    this.$timeout = $timeout;
    this.restResources = restResources;
    this.selectedHakukohde = null;

    try {
      this.email = $scope.$parent.$parent.application.henkilotiedot['Sähköposti'].answer
    } catch (e) {
      this.email = '';
    }
  }

  formatTimestamp(dt) {
    return moment(dt).tz('Europe/Helsinki')
      .format('LLL Z')
      .replace(/,/g, "")
      .replace(/\+02:00/, "(EET)")
      .replace(/\+03:00/, "(EEST)");
  };

  isVastaanottoKesken() {
    return this.ajaxPending || this.vastaanottoSentSuccessfully;
  };

  isNotVastaanotettavissa(hakukohdeOid) {
    return !(this.vastaanottoAction && this.vastaanottoAction[hakukohdeOid] && this.vastaanottoAction[hakukohdeOid].length !== 0)
      || (this.selectedHakukohde != hakukohdeOid)
      || this.isVastaanottoKesken()
      || (this.isRejectSelected(hakukohdeOid) && !this.confirmCancelAction && this.isKkHaku());
  }

  isRejectSelected(hakukohdeOid) {
    return this.vastaanottoAction && this.vastaanottoAction[hakukohdeOid] === 'Peru';
  }

  isKkHaku() {
    return !this.haku().toisenasteenhaku;
  }

  flashSiirtohakuNotification() {
    this.siirtohakuClass = 'siirtohaku-fade-out';
    this.$timeout(() => this.siirtohakuClass = 'siirtohaku-fade-in', 50)
  };

  selectHakukohde(hakukohdeOid) {
    this.selectedHakukohde = hakukohdeOid;
  }

  vastaanotaHakutoive(hakutoive) {
    this.ajaxPending = true;

    const pathParams = {
      hakemusOid: this.applicationOid(),
      hakukohdeOid: hakutoive.hakukohdeOid
    };

    const data = {
      vastaanottoAction: {action: this.vastaanottoAction[hakutoive.hakukohdeOid]},
      email: this.email,
      hakukohdeNimi: hakutoive.hakukohdeNimi,
      tarjoajaNimi: hakutoive.tarjoajaNimi
    };

    var promise = this.restResources.vastaanota.post(pathParams, data).$promise;
    promise.then(this.onSuccess(hakutoive), this.onError());
  }

  onSuccess(hakutoive) {
    var self = this;
    return function(updatedApplication) {
      self.ajaxPending = false;
      self.error = "";
      self.vastaanottoSentSuccessfully = true;
      self.$timeout(() => self.callback(hakutoive, updatedApplication), 3500);
    }
  }

  onError() {
    var self = this;
    return function(err) {
      var errorKey = (function () {
        if (err.status == 401)
          return "error.saveFailed_sessionExpired";
        else if (err.status == 500)
          return "error.serverError";
        else if (err.status == 403)
          return "error.priorAcceptance";
        else
          return "error.saveFailed";
      })();
      self.error = localize(errorKey);
      self.ajaxPending = false;
    }
  }
}

HakutoiveenVastaanottoController.$inject = ['$timeout', 'restResources', '$scope'];
