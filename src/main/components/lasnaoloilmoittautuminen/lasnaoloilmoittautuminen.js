module.exports = function(app) {
    app.directive("lasnaoloilmoittautuminen", ["localization", "restResources", function (localization, restResources) {
        return {
            restrict: 'E',
            scope: {
                application: '=application',
                tulos: '=tulos'
            },
            templateUrl: 'lasnaoloilmoittautuminen.html',

            link: function ($scope, element, attrs) {
                $scope.localization = localization;
                $scope.states = {
                    // Spring hakus
                    semester: 'LASNA_KOKO_LUKUVUOSI',
                    autumn: 'LASNA_SYKSY',

                    // Autumn hakus
                    spring: 'LASNA'
                };
                $scope.reserveStates = {
                    LASNA_KOKO_LUKUVUOSI: 'semester',
                    LASNA_SYKSY: 'autumn',
                    LASNA: 'spring'
                };

                $scope.init = function() {
                    var hakutoive = $scope.application.hakutoiveet.find(function(element) {
                        return element.data['Koulutus-id'] === $scope.tulos.hakukohdeOid;
                    });

                    $scope.tulos.koulutuksenAlkaminen = hakutoive.koulutuksenAlkaminen;

                    $scope.ilmoittautuminen = $scope.reserveStates[$scope.tulos.ilmoittautumistila.ilmoittautumistila];
                    $scope.ilmoittautunut = !!$scope.ilmoittautuminen;
                }();

                $scope.postLasnaoloilmoittautuminen = function() {
                    $scope.error = false;
                    var body = {
                        hakukohdeOid: $scope.tulos.hakukohdeOid,
                        muokkaaja: '',
                        tila: $scope.states[$scope.ilmoittautuminen],
                        selite: 'Omien sivujen läsnäoloilmoittautuminen'
                    };
                    restResources.lasnaoloilmoittautuminen.save({ hakuOid: $scope.application.haku.oid , hakemusOid: $scope.application.oid }, JSON.stringify(body), onSuccess, onError);
                };

                $scope.getStateTranslation = function() {
                    return localization('lasnaoloilmoittautuminen.' + $scope.ilmoittautuminen);
                };

                $scope.getErrorTranslation = function() {
                    return localization('lasnaoloilmoittautuminen.error.' + $scope.error);
                };

                $scope.schoolStartsInSpring = function () {
                    return $scope.tulos.koulutuksenAlkaminen.kausiUri === 'kausi_k#1'
                };

                $scope.schoolStartsInAutumn = function () {
                    return $scope.tulos.koulutuksenAlkaminen.kausiUri === 'kausi_s#1'
                };

                function onSuccess() {
                    $scope.tulos.ilmoittautumistila.ilmoittautumisaika.tehty = new Date();
                    $scope.ilmoittautunut = true;
                }

                function onError(err) {
                    console.log(err);
                    switch (err.status) {
                        case 401:
                            document.location.replace(window.url("omatsivut.login"));
                            break;
                        case 404:
                            $scope.error = 'notFound';
                            break;
                        default:
                            $scope.error = 'other';
                    }
                }

                $scope.getEnrolmentMessageKeys = function() {
                    var date = $scope.tulos.ilmoittautumistila.ilmoittautumisaika.tehty;
                    // Workaround till I get this from valinta-tulos-service
                    date = date ? date : new Date();
                    return {date: getEnrolmentDate(date), time: getEnrolmentTime(date)};
                };

                function getEnrolmentDate(date) {
                    var day = date.getDate();
                    var month = date.getMonth() + 1;
                    var year = date.getFullYear();

                    return day + '.' + month + '.' + year;
                }

                function getEnrolmentTime(date) {
                    var hours = date.getHours();
                    var minutes = date.getMinutes();

                    return hours + ':' + ("0" + minutes).slice(-2);
                }
            }

        }
    }])
};