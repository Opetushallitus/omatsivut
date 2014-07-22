(function () {
  describe('Hakemuslistaus', function () {
    var page = ApplicationListPage();

    before(function (done) {
      session.init("010101-123N").then(page.resetDataAndOpen).done(done)
    })

    describe("Hakemuksen tietojen näyttäminen", function() {
      it('hakemuslistassa on hakemus henkilölle 010101-123N', function () {
        expect(ApplicationListPage().applications()).to.deep.equal([
          {
            applicationSystemName: "Perusopetuksen jälkeisen valmistavan koulutuksen kesän 2014 haku MUOKATTU"
          }
        ])
      })

      it("henkilön 010101-123N hakutoiveet ovat näkyvissä", function () {
        expect(ApplicationListPage().preferencesForApplication(0)).to.deep.equal([
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
        ])
      })
    })

    describe("Hakutoiveiden validaatio", function() {
      before(ApplicationListPage().resetDataAndOpen)

      beforeEach(function() {
        return page.openPage().then(leaveOnlyOnePreference)
      })

      function leaveOnlyOnePreference() {
        return ApplicationListPage().getPreference(0).remove()
          .then(function() { return ApplicationListPage().getPreference(0).remove() })
      }

      it("vain yksi hakutoive voi olla muokattavana kerrallaan", function() {
        var pref = page.getPreference(1)
        var pref2 = page.getPreference(2)
        return pref.selectOpetusPiste("Ahl")()
          .then(function() { pref2.isEditable().should.be.false } )
          .then(pref.selectKoulutus(0))
          .then(function() { pref2.isEditable().should.be.true } )
      })

      it("tyhjiä rivejä ei voi muokata", function () {
        _.each(ApplicationListPage().emptyPreferencesForApplication(0), function (row) {
          row.isDisabled().should.be.true
        })
      })

      it("ainoaa hakutoivetta ei voi poistaa", function() {
        ApplicationListPage().getPreference(0).canRemove().should.be.false
      })

      it("lomaketta ei voi tallentaa, jos hakutoive on epätäydellinen", function() {
        var pref = page.getPreference(1)
        return pref.selectOpetusPiste("Ahl")()
          .then(wait.untilFalse(page.saveButton(0).isEnabled))
          .then(function() { page.isValidationErrorVisible().should.be.true })
          .then(pref.selectKoulutus(0))
          .then(wait.until(page.saveButton(0).isEnabled))
      })
    })

    describe("Lisäkysymykset", function() {
      describe("Lisäkysymyksien näyttäminen", function() {
        var questions1 = [
          'Testikysymys, avaoin vastaus kenttä (pakollinen)?',
          'Valitse kahdesta vaihtoehdosta paremmin itsellesi sopiva?',
          'Mikä tai mitkä ovat mielestäsi parhaiten soveltuvat vastausket?',
          'Valitse parhaat vaihtoehdot valittavista vaihtoehdoista?',
          'Testivalintakysymys arvosanat',
          'Testikysymys arvosanat, avoin vastaus',
          'Testikysymys lupatiedot-kohta avoin vastaus',
          'Testikysymys valitse vaihtoehdoista paras tai parhaat',
          'Testikysymys valitse toinen vaihtoehdoista' ]

        var questions2 = [
          'Miksi haet kymppiluokalle?',
          'Haen ensisijaisesti kielitukikympille?',
          'Päättötodistuksen kaikkien oppiaineiden keskiarvo?',
          'Päättötodistukseni on' ]

        before(
          ApplicationListPage().resetDataAndOpen,
          ApplicationListPage().getPreference(1).remove,
          ApplicationListPage().getPreference(1).remove,
          ApplicationListPage().save
        )

        describe("tallennetut hakutoiveet, joilla on lisäkysymyksiä", function() {
          it("lisäkysymyksiä ei näytetä", function() {
            expect(ApplicationListPage().questionsForApplication(0).data()).to.deep.equal([])
          })
        })

        describe("lisätty hakutoive, jolla on lisäkysymyksiä", function() {
          before(replacePreference(1, "Etelä-Savon ammattiopisto"))

          it("lisäkysymykset näytetään", function() {
            var questionTitles = ApplicationListPage().questionsForApplication(0).titles()
            expect(questionTitles).to.deep.equal(questions1)
          })
        })

        describe("lisätty kaksi hakutoivetta, jolla on lisäkysymyksiä", function() {
          before(replacePreference(1, "Etelä-Savon ammattiopisto"))
          before(replacePreference(2, "Turun Kristillinen"))

          it("molempien lisäkysymykset näytetään", function() {
            var questionTitles = ApplicationListPage().questionsForApplication(0).titles()
            expect(questionTitles).to.deep.equal(questions1.concat(questions2))
          })
        })
      })
      describe("Lisäkysymyksiin vastaaminen", function() {
        var timestamp;

        before(
          ApplicationListPage().resetDataAndOpen,
          page.getPreference(2).remove,
          page.save,
          replacePreference(2, "Omnian ammattiopisto"),
          function() {
            timestamp = page.changesSavedTimestamp()
          }
        )

        describe("Aluksi", function() {
          it("kysymykset näytetään", function() {
            ApplicationListPage().questionsForApplication(0).count().should.equal(1)
            page.questionsForApplication(0).getAnswer(0).should.equal("")
          })
          it("pakolliset kentät korostetaan", function() {
            ApplicationListPage().questionsForApplication(0).validationMessages()[0].should.equal("*")
          })
        })

        describe("Kun tallennetaan vastaamatta pakollisiin kysymyksiin", function() {
          before(page.save)
          it("näytetään validaatiovirheet oikein", function() {
            page.saveError().should.equal("Ei tallennettu - vastaa ensin kaikkiin lisäkysymyksiin")
            page.questionsForApplication(0).validationMessages()[0].should.equal("Pakollinen tieto.")
          })
        })

        describe("Onnistuneen tallennuksen jälkeen", function() {
          before(function() {
            page.questionsForApplication(0).enterAnswer(0, "testivastaus")
            return Q.all([page.save(), page.waitForTimestampUpdate])
          })

          describe("Käyttöliittymän tila", function() {
            it("kysymykset näytetään edelleen", function() {
              ApplicationListPage().questionsForApplication(0).count().should.equal(1)
            })

            it("validaatiovirheitä ei ole", function() {
              page.questionsForApplication(0).validationMessages()[0].should.equal("")
            })

            it("aikaleima päivittyy", function() {
              page.changesSavedTimestamp().should.not.be.empty
            })

            it("tallennusnappi disabloituu", function() {
              page.saveButton(0).isEnabled().should.be.false
            })

            it("tallennusviesti näytetään", function() {
              page.saveError().should.equal("")
              page.statusMessage().should.equal("Kaikki muutokset tallennettu")
            })
            it("syötetty vastaus näytetään", function() {
              page.questionsForApplication(0).getAnswer(0).should.equal("testivastaus")
            })
          })
          describe("Kun ladataan sivu uudelleen", function() {
            before(page.openPage)
            it("valitut hakutoiveet näytetään", function() {
              page.getPreference(2).opetuspiste().should.equal("Omnian ammattiopisto, Espoon keskus, Lehtimäentie")
            })
            it("vastauksia ei näytetä", function() {
              page.questionsForApplication(0).count().should.equal(0)
            })
          })
          describe("Kun poistetaan hakutoive, tallennetaan ja lisätään se uudelleen", function() {
            before(
              page.getPreference(2).remove,
              page.save,
              page.openPage,
              replacePreference(2, "Omnian ammattiopisto")
            )
            it("hakutoiveeseen liittyvien lisäkysymysten aiemmat vastaukset hävitetään", function() {
              ApplicationListPage().questionsForApplication(0).count().should.equal(1)
              page.questionsForApplication(0).getAnswer(0).should.equal("")
            })
          })
        })
      })
    })

    describe("Hakemuslistauksen muokkaus", function () {
      endToEndTest("järjestys", "järjestys muuttuu nuolta klikkaamalla", function () {
        return page.getPreference(1).moveDown()
      }, function (dbStart, dbEnd) {
        dbStart.hakutoiveet[1].should.deep.equal(dbEnd.hakutoiveet[2])
        dbStart.hakutoiveet[2].should.deep.equal(dbEnd.hakutoiveet[1])
      })
      endToEndTest("poisto", "hakutoiveen voi poistaa", function () {
        return page.getPreference(0).remove()
      }, function (dbStart, dbEnd) {
        dbEnd.hakutoiveet.should.deep.equal(_.flatten([_.rest(dbStart.hakutoiveet), {}]))
      })
      endToEndTest("lisäys", "hakutoiveen voi lisätä", replacePreference(2, "Ahl"), function(dbStart, dbEnd) {
        var newOne = { 'Opetuspiste-id': '1.2.246.562.10.60222091211',
          Opetuspiste: 'Ahlmanin ammattiopisto',
          Koulutus: 'Ammattistartti',
          'Koulutus-id-kaksoistutkinto': 'false',
          'Koulutus-id-sora': 'false',
          'Koulutus-id-vocational': 'true',
          'Koulutus-id-lang': '',
          'Koulutus-id-aoIdentifier': '028',
          'Koulutus-id-athlete': 'false',
          'Koulutus-educationDegree': '32',
          'Koulutus-id': '1.2.246.562.14.2014040912353139913320',
          'Opetuspiste-id-parents': '',
          'Koulutus-id-educationcode': 'koulutus_039996' }
        dbEnd.hakutoiveet.should.deep.equal(dbStart.hakutoiveet.slice(0, 2).concat(newOne))
      })
    })
  })

  function replacePreference(index, searchString) {
    return function() {
      var pref = ApplicationListPage().getPreference(index)
      return pref.remove()
        .then(pref.selectOpetusPiste(searchString))
        .then(pref.selectKoulutus(0))
        .then(wait.forAngular)
    }
  }

  function endToEndTest(descName, testName, manipulationFunction, dbCheckFunction) {
    describe(descName, function() {
      var applicationsBefore, applicationsAfter;
      before(
        ApplicationListPage().resetDataAndOpen,
        function() {
          return db.getApplications().then(function(apps) {
            applicationsBefore = apps
          })
        },
        manipulationFunction,
        ApplicationListPage().save,
        function(done) {
          db.getApplications().then(function(apps) {
            applicationsAfter = apps
            done()
          })
        }
      )
      it(testName, function() {
        dbCheckFunction(applicationsBefore[0], applicationsAfter[0])
      })
    })
  }
})()