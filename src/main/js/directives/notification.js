export default class Notification {
  constructor() {
    this.restrict = 'E';
    this.scope = {
      message: '@'
    };
    this.template = require('./notification.html');
  }

  link(scope) {
    scope.visible = true;
    scope.close = () => scope.visible = false;
  }
}
