(function () {
  describe('hakemuslistaus', function () {
    before(function (done) {
      session.init("010101-123N").then(ApplicationListPage().resetDataAndOpen).done(done)
    })

    describe("näyttäminen", function() {
      it('hakemuslistassa on hakemus henkilölle 010101-123N', function () {
        expect(ApplicationListPage().applications()).to.deep.equal([
          {
            applicationSystemName: "Perusopetuksen jälkeisen valmistavan koulutuksen kesän 2014 haku MUOKATTU"
          }
        ])
      })

      it("henkilön 010101-123N hakutoiveet ovat näkyvissä", function () {
        expect(preferencesAsText()).to.deep.equal([
          {
            "hakutoive.Opetuspiste": "Amiedu, Valimotie 8",
            "hakutoive.Koulutus": "Maahanmuuttajien ammatilliseen peruskoulutukseen valmistava koulutus"
          },
          {
            "hakutoive.Opetuspiste": "Ammatti-instituutti Iisakki",
            "hakutoive.Koulutus": "Kymppiluokka"
          },
          {
            "hakutoive.Opetuspiste": "Turun Kristillinen opisto",
            "hakutoive.Koulutus": "Kymppiluokka"
          }
        ]);
      })

      it("tyhjiä rivejä ei voi muokata", function () {
        _.each(ApplicationListPage().emptyPreferencesForApplication(0), function (row) {
          row.isDisabled().should.be.true
        })
      })
    })

    describe("hakemuslistauksen muokkaus", function () {
      endToEndTest("järjestys", "järjestys muuttuu nuolta klikkaamalla", function () {
        var pref2 = getPreference(1)
        var pref3 = getPreference(2)
        pref2.arrowDown().click()
        return wait.until(function () {
          return pref2.element().index() === 2 && pref3.element().index() === 1 && pref2.number().text() === "3."
        })()
      }, function (dbStart, dbEnd) {
        dbStart.hakutoiveet[1].should.deep.equal(dbEnd.hakutoiveet[2])
        dbStart.hakutoiveet[2].should.deep.equal(dbEnd.hakutoiveet[1])
      })
      endToEndTest("poisto", "hakutoiveen voi poistaa", function () {
        var pref1 = getPreference(0)
        var pref2 = getPreference(1)
        pref1.deleteBtn().click().click()
        return wait.until(function () {
          return pref2.element().index() === 0 && pref1.element().parent().length === 0
        })()
      }, function (dbStart, dbEnd) {
        dbEnd.hakutoiveet.should.deep.equal(_.flatten([_.rest(dbStart.hakutoiveet), {}]))
      })
    })
  })

  function endToEndTest(descName, testName, manipulationFunction, dbCheckFunction) {
    describe(descName, function() {
      var applicationsBefore, applicationsAfter;
      before(function() {
        return db.getApplications().then(function(apps) {
          applicationsBefore = apps
        })
      })
      before(manipulationFunction)
      before(save)
      before(function(done) {
        db.getApplications().then(function(apps) {
          applicationsAfter = apps
          done()
        })
      })
      it(testName, function() {
        dbCheckFunction(applicationsBefore[0], applicationsAfter[0])
      })
    })

    function save() {
      return wait.until(ApplicationListPage().saveButton(0).isEnabled(true))()
        .then(ApplicationListPage().saveButton(0).click)
        .then(wait.until(ApplicationListPage().saveButton(0).isEnabled(false))) // Tallennus on joko alkanut tai valmis
        .then(wait.until(ApplicationListPage().isSavingState(0, false))) // Tallennus ei ole kesken
    }
  }

  function preferencesAsText() {
    return ApplicationListPage().preferencesForApplication(0).map(function (item) {
      return item.data()
    })
  }

  function getPreference(index) {
    return ApplicationListPage().preferencesForApplication(0)[index]
  };
})()