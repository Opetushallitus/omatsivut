module.exports = function(app) {
  require('./applicationValidator')(app);
  require('./angularBacon')(app);
  require('./constants')(app);
  require('./directives/confirm')(app);
  require('./directives/question')(app);
  require('./directives/localizedLink')(app);
  require('./directives/formattedTime')(app);
  require('./directives/sortable')(app);
  require('./directives/disableClickFocus')(app);
  require('./directives/application')(app);
  require('./directives/hakutoiveenVastaanotto')(app);
  require('./directives/ilmoittautuminen')(app);
  require('./directives/kela')(app);
  require('./directives/hakutoiveet')(app);
  require('./directives/valintatulos')(app);
  require('./directives/henkilotiedot')(app);
  require('./directives/applicationPeriods')(app);
  require('./directives/clearableInput')(app);
  require('./directives/callout')(app);
  require('../../components/lasnaoloilmoittautuminen/lasnaoloilmoittautuminen')(app);
};

module.exports.Hakemus = require("./hakemus");