(function () {
  var page = ApplicationListPage()
  var hakemus1nimi = "Perusopetuksen jälkeisen valmistavan koulutuksen kesän 2014 haku MUOKATTU"
  var hakemus1 = page.getApplication(hakemus1nimi)
  var hakemus2nimi = "Ammatillisen koulutuksen ja lukiokoulutuksen kevään 2014 yhteishaku"
  var hakemus2 = page.getApplication(hakemus2nimi)
  var hakemus3nimi = "Lisähaku kevään 2014 yhteishaussa vapaaksi jääneille paikoille"
  var hakemus3 = page.getApplication(hakemus3nimi)

  describe('Hakemuslistaus', function () {

    before(function (done) {
      session.init("010101-123N").then(page.resetDataAndOpen).done(done)
    })

    describe("Hakemuksen tietojen näyttäminen", function() {
      it('hakemuslistassa on hakemus henkilölle 010101-123N', function () {
        expect(ApplicationListPage().applications()).to.contain(
          { applicationSystemName: 'Ammatillisen koulutuksen ja lukiokoulutuksen kevään 2014 yhteishaku' }
        )
        expect(ApplicationListPage().applications()).to.contain(
          { applicationSystemName: "Perusopetuksen jälkeisen valmistavan koulutuksen kesän 2014 haku MUOKATTU" }
        )
      })

      it("henkilön 010101-123N hakutoiveet ovat näkyvissä", function () {
        expect(hakemus1.preferencesForApplication()).to.deep.equal([
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

    describe("Virheidenkäsittely", function() {
      before(ApplicationListPage().resetDataAndOpen)
      before(mockAjax.init)

      describe("Lisäkysymykset", function() {
        before(function() { mockAjax.respondOnce("POST", "/omatsivut/api/applications/validate/1.2.246.562.11.00000877107", 400, "") })

        it("haun epäonnistuminen näytetään käyttäjälle", function() {
          // In current implementation move will trigger validation API call
          return hakemus1.getPreference(0).moveDown()
            .then(wait.until(function() { return hakemus1.saveError().length > 0 }))
            .then(function() {
              hakemus1.saveError().should.equal("Tietojen haku epäonnistui. Yritä myöhemmin uudelleen.")
            })
        })
      })

      describe("Tallennus", function() {
        describe("kun tapahtuu palvelinvirhe", function() {
          before(function() { mockAjax.respondOnce("PUT", "/omatsivut/api/applications/1.2.246.562.11.00000877107", 400, "") })

          it("virheilmoitus näkyy oikein", function() {
            return hakemus1.getPreference(0).moveDown()
              .then(hakemus1.saveWaitError)
              .then(function() {
                hakemus1.saveError().should.equal("Tallentaminen epäonnistui")
              })
          })

          it("tallennus toimii uudella yrittämällä", function() {
            return hakemus1.saveWaitSuccess().then(function() {
              hakemus1.saveError().should.equal("")
              hakemus1.statusMessage().should.equal("Kaikki muutokset tallennettu")
            })
          })
        })

        describe("kun istunto on vanhentunut", function() {
          before(function() { mockAjax.respondOnce("PUT", "/omatsivut/api/applications/1.2.246.562.11.00000877107", 401, "") })

          it("virheilmoitus näkyy oikein", function() {
            return hakemus1.getPreference(0).moveDown()
              .then(hakemus1.saveWaitError)
              .then(function() {
                hakemus1.saveError().should.equal("Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.")
              })
          })
        })

        describe("Kun tallennuksessa esiintyy odottamaton validointivirhe", function() {
          before(function () {
            mockAjax.respondOnce("PUT", "/omatsivut/api/applications/1.2.246.562.11.00000877107", 400, '[{"key":"asdfqwer", "message": "something went wrong"}]')
          })

          it("virheilmoitus näkyy oikein", function () {
            return hakemus1.getPreference(0).moveDown()
              .then(hakemus1.saveWaitError)
              .then(function () {
                hakemus1.saveError().should.equal("Odottamaton virhe. Ota yhteyttä ylläpitoon.")
              })
          })
        })
      })
    })

    describe("Hakutoiveiden validaatio", function() {
      before(
        ApplicationListPage().resetDataAndOpen,
        leaveOnlyOnePreference
      )

      describe("Kun yksi hakukohde valittu", function() {
        it("rivejä ei voi siirtää", function () {
          hakemus1.getPreference(0).isMovable().should.be.false
          hakemus1.getPreference(1).isMovable().should.be.false
          hakemus1.getPreference(2).isMovable().should.be.false
        })

        it("vain ensimmäinen tyhjä rivi on muokattavissa", function () {
          hakemus1.getPreference(0).isEditable().should.be.false
          hakemus1.getPreference(1).isEditable().should.be.true
          hakemus1.getPreference(2).isEditable().should.be.false
        })
      })

      describe("kun lisätään hakukohde", function() {
        before(
          hakemus1.getPreference(1).selectOpetusPiste("Ahl"),
          hakemus1.getPreference(1).selectKoulutus(0)
        )

        it("seuraava hakukohde tulee muokattavaksi", function() {
          hakemus1.getPreference(2).isEditable().should.be.true
        })

        it("lisätty hakutoive on edelleen muokattavissa", function() {
          hakemus1.getPreference(1).isEditable().should.be.true
        })

        it("lomake on tallennettavissa", function() {
          hakemus1.isValidationErrorVisible().should.be.false
        })
      })

      describe("kun vain opetuspiste on valittu", function() {
        it("lomaketta ei voi tallentaa", function() {
          var pref = hakemus1.getPreference(1)
          return pref.selectOpetusPiste("Ahl")()
            .then(wait.untilFalse(hakemus1.saveButton().isEnabled))
            .then(function() { hakemus1.isValidationErrorVisible().should.be.true })
        })
      })

      describe("kun valitun opetuspisteen syöttökenttä tyhjennetään", function() {
        before(
          hakemus1.getPreference(1).selectOpetusPiste("Ahl"),
          hakemus1.getPreference(1).searchOpetusPiste("")
        )
        it("lomakkeen voi tallentaa", function() {
          hakemus1.isValidationErrorVisible().should.be.false
          hakemus1.saveButton().isEnabled().should.be.true
        })
      })

      describe("kun valittu opetuspiste siirretään ylöspäin ja syöttökenttä tyhjennetään", function() {
        before(
          hakemus1.getPreference(1).selectOpetusPiste("Ahl"),
          hakemus1.getPreference(1).moveUp,
          hakemus1.getPreference(0).searchOpetusPiste("")
        )
        it("lomaketta ei voi tallentaa", function() {
          hakemus1.isValidationErrorVisible().should.be.true
        })
        it("hakukohde säilyy muokattavana", function() {
          hakemus1.getPreference(0).isEditable().should.be.true
        })
      })

      describe("kun valinta jätetään kesken ja siirrytään vaihtamaan toista hakukohdetta", function() {
        before(
          page.resetDataAndOpen,
          leaveOnlyOnePreference, // first two steps to undo previous test case
          hakemus1.getPreference(1).selectOpetusPiste("Ahl"),
          hakemus1.getPreference(1).selectKoulutus(0),
          hakemus1.getPreference(2).selectOpetusPiste("Turun Kristillinen"),
          hakemus1.getPreference(1).selectOpetusPiste("Turun Kristillinen")
        )
        it("keskeneräinen valinta pysyy muokattavassa tilassa", function() {
          hakemus1.getPreference(1).isEditable().should.be.true
          hakemus1.getPreference(2).isEditable().should.be.true
        })
        it("lomaketta ei voi tallentaa", function() {
          hakemus1.isValidationErrorVisible().should.be.true
        })
      })

      describe("jos kaksi hakutoivetta on identtisiä", function() {
        before(replacePreference(hakemus1, 1, "Turun Kristillinen"))
        describe("käyttöliittymän tila", function() {
          it("näytetään validointivirhe", function() {
            hakemus1.getPreference(1).errorMessage().should.equal("Et voi syöttää samaa hakutoivetta useaan kertaan.")
          })

          it("lomaketta ei voi tallentaa", function() {
            hakemus1.saveButton(0).isEnabled().should.be.false
          })
        })

        describe("kun valitaan eri hakukohde", function() {
          before(replacePreference(hakemus1, 1, "Etelä-Savon ammattiopisto"))

          it("lomakkeen voi tallentaa", function() {
            hakemus1.saveButton(0).isEnabled().should.be.true
          })

          it("poistetaan validointivirhe", function() {
            hakemus1.getPreference(1).errorMessage().should.equal("")
          })
        })
      })
    })

    describe("Kun hakijalla on ulkomaalainen pohjakoulutus", function() {
      before(
        page.resetDataAndOpen,
        hakemus2.getPreference(0).remove,
        hakemus2.saveWaitSuccess,
        replacePreference(hakemus2, 1, "Kallion")
      )

      describe("näyttäminen", function() {
        it("ok", function() {
        })
      })

      describe("tallentaminen", function() {
        before(
          hakemus2.saveWaitSuccess
        )

        it("onnistuu", function() {

        })
      })
    })

    describe("Kun hakijalla on koulutus, joka edellyttää harkinnanvaraisuuskysymyksiin vastausta", function() {
      before(
        page.applyFixtureAndOpen("peruskoulu"),
        hakemus2.getPreference(0).remove,
        hakemus2.saveWaitSuccess,
        replacePreference(hakemus2, 1, "Ahlman")
      )

      it("kysymykset näytetään", function() {
        var questionTitles = hakemus2.questionsForApplication().titles()
        expect(questionTitles).to.deep.equal([
          'Haetko koulutukseen harkintaan perustuvassa valinnassa?',
          'Työkokemus kuukausina' ])
      })

      /*

      TODO: vastaaminen epäonnistuu, koska follow-up -kysymystä ei osata näyttää

      describe("vastaaminen", function() {
        before(
          answerQuestions,
          hakemus2.saveWaitSuccess
        )

        it("onnistuu", function() {

        })

        function answerQuestions() {
          hakemus2.questionsForApplication().enterAnswer(0, "Kyllä")
          hakemus2.questionsForApplication().enterAnswer(1, "24")
        }
      })
      */
    })

    describe("Lisäkysymykset", function() {
      describe("Kysymysten suodatus koulutuksen kielen perustella", function() {
        before(
          page.resetDataAndOpen,
          hakemus3.getPreference(0).remove,
          replacePreference(hakemus3, 0, "Ammattiopisto Livia, fiskeri")
        )

        it("epäoleellisia kysymyksiä ei näytetä", function() {
          var questionTitles = hakemus3.questionsForApplication().titles()
          expect(questionTitles).to.deep.equal([
            'Oletko suorittanut yleisten kielitutkintojen ruotsin kielen tutkinnon kaikki osakokeet vähintään taitotasolla 3?',
            'Oletko suorittanut Valtionhallinnon kielitutkintojen ruotsin kielen suullisen ja kirjallisen tutkinnon vähintään taitotasolla tyydyttävä?' ])
        })

        it("suodatetun vastausjoukon tallentaminen onnistuu", function() {
          hakemus3.questionsForApplication().enterAnswer(0, "Kyllä")
          hakemus3.questionsForApplication().enterAnswer(1, "Ei")
          return hakemus3.saveWaitSuccess()
        })
      })

      describe("Lisäkysymyksien näyttäminen", function() {
        var questions1 = [
          'Testikysymys, avaoin vastaus kenttä (pakollinen)?',
          'Valitse kahdesta vaihtoehdosta paremmin itsellesi sopiva?',
          'Mikä tai mitkä ovat mielestäsi parhaiten soveltuvat vastausket?',
          'Kotikunta',
          'Minkä koulutuksen olet suorittanut ulkomailla?',
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
          page.resetDataAndOpen,
          hakemus1.getPreference(1).remove,
          hakemus1.getPreference(1).remove,
          hakemus1.saveWaitSuccess
        )

        describe("tallennetut hakutoiveet, joilla on lisäkysymyksiä", function() {
          it("lisäkysymyksiä ei näytetä", function() {
            expect(hakemus1.questionsForApplication().data()).to.deep.equal([])
          })
        })

        describe("lisätty hakutoive, jolla on lisäkysymyksiä", function() {
          before(replacePreference(hakemus1, 1, "Etelä-Savon ammattiopisto"))

          it("lisäkysymykset näytetään", function() {
            var questionTitles = hakemus1.questionsForApplication().titles()
            expect(questionTitles).to.deep.equal(questions1)
          })
        })

        describe("lisätty kaksi hakutoivetta, jolla on lisäkysymyksiä", function() {
          before(replacePreference(hakemus1, 1, "Etelä-Savon ammattiopisto"))
          before(replacePreference(hakemus1, 2, "Turun Kristillinen"))

          it("molempien lisäkysymykset näytetään", function() {
            var questionTitles = hakemus1.questionsForApplication().titles()
            expect(questionTitles).to.deep.equal(questions1.concat(questions2))
          })
        })
      })

      describe("Lisäkysymyksiin vastaaminen", function() {
        before(
          page.resetDataAndOpen,
          replacePreference(hakemus1, 2, "Etelä-Savon ammattiopisto")
        )

        describe("Aluksi", function() {
          it("kysymykset näytetään", function() {
            hakemus1.questionsForApplication().count().should.equal(11)
            hakemus1.questionsForApplication().getAnswer(0).should.equal("")
          })
          it("pakolliset kentät korostetaan", function() {
            hakemus1.questionsForApplication().validationMessages()[0].should.equal("*")
          })
        })

        describe("Kun tallennetaan vastaamatta pakollisiin kysymyksiin", function() {
          before(hakemus1.saveWaitError)
          it("näytetään tallennusvirhe", function() {
            hakemus1.saveError().should.equal("Ei tallennettu - vastaa ensin kaikkiin lisäkysymyksiin")
          })

          it("näytetään kaikki validaatiovirheet", function() {
            hakemus1.questionsForApplication().validationMessageCount().should.equal(11)
          })

          it("näytetään checkboxin minmax-validaatiovirhe", function() {
            hakemus1.questionsForApplication().validationMessages()[2].should.equal("Virheellinen arvo")
          })

          it("näytetään required-validaatiovirhe", function() {
            hakemus1.questionsForApplication().validationMessages()[0].should.equal("Pakollinen tieto.")
          })
        })

        describe("Onnistuneen tallennuksen jälkeen", function() {
          before(answerAllQuestions, hakemus1.saveWaitSuccess)

          describe("Tietokanta", function() {
            it("sisältää tallennetut tiedot", function() {
              return db.getApplications().then(function(data) {
                var answers = findByHaku(data, hakemus1nimi).answers
                var questions = hakemus1.questionsForApplication().data()

                answers.hakutoiveet[questions[0].id].should.equal("tekstivastaus 1")
                answers.hakutoiveet[questions[1].id].should.equal("option_0")
                answers.hakutoiveet[questions[2].id + "-option_0"].should.equal("true")
                answers.hakutoiveet[questions[2].id + "-option_1"].should.equal("true")
                answers.osaaminen[questions[3].id].should.equal("152")
                answers.osaaminen[questions[4].id].should.equal("textarea-vastaus")
                answers.osaaminen[questions[5].id + "-option_0"].should.equal("true")
                answers.osaaminen[questions[5].id + "-option_1"].should.equal("true")
                answers.osaaminen[questions[6].id].should.equal("option_0")
                answers.osaaminen[questions[7].id].should.equal("tekstivastaus 2")
                answers.lisatiedot[questions[8].id].should.equal("tekstivastaus 3")
                answers.lisatiedot[questions[9].id + "-option_0"].should.equal("true")
                answers.lisatiedot[questions[10].id].should.equal("option_0")
              })
            })
          })

          describe("Käyttöliittymän tila", function() {
            it("kysymykset näytetään edelleen", function() {
              hakemus1.questionsForApplication().count().should.equal(11)
            })

            it("validaatiovirheitä ei ole", function() {
              _.all(hakemus1.questionsForApplication().validationMessages(), function(item) {
                return item == ""
              }).should.be.true
            })

            it("aikaleima päivittyy", function() {
              hakemus1.changesSavedTimestamp().should.not.be.empty
            })

            it("tallennusnappi disabloituu", function() {
              hakemus1.saveButton().isEnabled().should.be.false
            })

            it("tallennusviesti näytetään", function() {
              hakemus1.saveError().should.equal("")
              hakemus1.statusMessage().should.equal("Kaikki muutokset tallennettu")
            })
            it("syötetty vastaus näytetään", function() {
              hakemus1.questionsForApplication().getAnswer(0).should.equal("tekstivastaus 1")
            })
          })

          describe("Kun ladataan sivu uudelleen", function() {
            before(page.openPage)
            it("valitut hakutoiveet näytetään", function() {
              hakemus1.getPreference(2).opetuspiste().should.equal("Etelä-Savon ammattiopisto,  Otavankatu 4")
            })
            it("vastauksia ei näytetä", function() {
              hakemus1.questionsForApplication().count().should.equal(0)
            })
          })
          describe("Kun poistetaan hakutoive, tallennetaan ja lisätään se uudelleen", function() {
            before(
              hakemus1.getPreference(2).remove,
              hakemus1.saveWaitSuccess,
              page.openPage,
              replacePreference(hakemus1, 2, "Etelä-Savon ammattiopisto")
            )
            it("hakutoiveeseen liittyvien lisäkysymysten aiemmat vastaukset hävitetään", function() {
              hakemus1.questionsForApplication().count().should.equal(11)
              hakemus1.questionsForApplication().getAnswer(0).should.equal("")
            })
          })
        })

        describe("Kun poistetaan lisätty hakutoive, jolla lisäkysymyksiä, joihin ei vastattu", function() {
          before(
            hakemus1.getPreference(2).remove,
            hakemus1.saveWaitSuccess
          )

          it("Tallennus onnistuu", function() {
            hakemus1.saveError().should.equal("")
            hakemus1.statusMessage().should.equal("Kaikki muutokset tallennettu")
          })
        })

        describe("Kun poistetaan lisätty hakutoive, jolla lisäkysymyksiä, joihin vastattiin", function() {
          before(
            page.resetDataAndOpen,
            replacePreference(hakemus1, 2, "Etelä-Savon ammattiopisto"),
            answerAllQuestions,
            hakemus1.getPreference(2).remove,
            hakemus1.saveWaitSuccess
          )

          it("Tallennus onnistuu", function() {
            hakemus1.saveError().should.equal("")
            hakemus1.statusMessage().should.equal("Kaikki muutokset tallennettu")
          })
        })

        function answerAllQuestions() {
          hakemus1.questionsForApplication().enterAnswer(0, "tekstivastaus 1")
          hakemus1.questionsForApplication().enterAnswer(1, "Vaihtoehto x 1")
          hakemus1.questionsForApplication().enterAnswer(2, "Vaihtoehto 1")
          hakemus1.questionsForApplication().enterAnswer(2, "Vaihtoehto 2")
          hakemus1.questionsForApplication().enterAnswer(3, "Isokyrö")
          hakemus1.questionsForApplication().enterAnswer(4, "textarea-vastaus")
          hakemus1.questionsForApplication().enterAnswer(5, "Vaihtoehto yyy 1")
          hakemus1.questionsForApplication().enterAnswer(5, "Vaihtoehto yyy 2")
          hakemus1.questionsForApplication().enterAnswer(6, "Vaihtoehto arvosanat 1")
          hakemus1.questionsForApplication().enterAnswer(7, "tekstivastaus 2")
          hakemus1.questionsForApplication().enterAnswer(8, "tekstivastaus 3")
          hakemus1.questionsForApplication().enterAnswer(9, "Vaihtoehto zzzz 1")
          hakemus1.questionsForApplication().enterAnswer(10, "Vaihttoehto yksi")
        }
      })
    })

    describe("Hakemuslistauksen muokkaus", function () {
      endToEndTest("järjestys", "järjestys muuttuu nuolta klikkaamalla", function () {
        return hakemus1.getPreference(1).moveDown()
      }, function (dbStart, dbEnd) {
        dbStart.hakutoiveet[1].should.deep.equal(dbEnd.hakutoiveet[2])
        dbStart.hakutoiveet[2].should.deep.equal(dbEnd.hakutoiveet[1])
      })
      endToEndTest("poisto", "hakutoiveen voi poistaa", function () {
        return hakemus1.getPreference(0).remove()
      }, function (dbStart, dbEnd) {
        dbEnd.hakutoiveet.should.deep.equal(_.flatten([_.rest(dbStart.hakutoiveet), {}]))
      })
      endToEndTest("lisäys", "hakutoiveen voi lisätä", replacePreference(hakemus1, 2, "Ahl"), function(dbStart, dbEnd) {
        var newOne = { 'Opetuspiste-id': '1.2.246.562.10.60222091211',
          Opetuspiste: 'Ahlmanin ammattiopisto',
          Koulutus: 'Ammattistartti',
          'Koulutus-id-kaksoistutkinto': 'false',
          'Koulutus-id-sora': 'false',
          'Koulutus-id-vocational': 'true',
          'Koulutus-id-lang': 'FI',
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

  function replacePreference(hakemus, index, searchString) {
    return function() {
      var pref = hakemus.getPreference(index)
      return pref.remove()
        .then(pref.selectOpetusPiste(searchString))
        .then(pref.selectKoulutus(0))
    }
  }

  function leaveOnlyOnePreference() {
    return hakemus1.getPreference(0).remove()
      .then(function() { return hakemus1.getPreference(0).remove() })
  }

  function endToEndTest(descName, testName, manipulationFunction, dbCheckFunction) {
    describe(descName, function() {
      var applicationsBefore, applicationsAfter;
      before(
        page.resetDataAndOpen,
        function() {
          return db.getApplications().then(function(apps) {
            applicationsBefore = apps
          })
        },
        manipulationFunction,
        hakemus1.saveWaitSuccess,
        function(done) {
          db.getApplications().then(function(apps) {
            applicationsAfter = apps
            done()
          })
        }
      )
      it(testName, function() {
        dbCheckFunction(findByHaku(applicationsBefore, hakemus1nimi), findByHaku(applicationsAfter, hakemus1nimi))
      })
    })
  }

  function findByHaku(applications, haku) {
    return _.find(applications, function(a) { return a.haku.name == haku })
  }
})()