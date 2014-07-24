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

    describe("Virheidenkäsittely", function() {
      before(ApplicationListPage().resetDataAndOpen)
      before(mockAjax.init)

      describe("Lisäkysymykset", function() {
        before(function() { mockAjax.respondOnce("POST", "/omatsivut/api/applications/validate/1.2.246.562.11.00000877107", 400, "") })

        it("haun epäonnistuminen näytetään käyttäjälle", function() {
          // In current implementation move will trigger validation API call
          return page.getPreference(0).moveDown()
            .then(wait.until(function() { return page.saveError().length > 0 }))
            .then(function() {
              page.saveError().should.equal("Tietojen haku epäonnistui. Yritä myöhemmin uudelleen.")
            })
        })
      })

      describe("Tallennus", function() {
        describe("kun tapahtuu palvelinvirhe", function() {
          before(function() { mockAjax.respondOnce("PUT", "/omatsivut/api/applications/1.2.246.562.11.00000877107", 400, "") })

          it("virheilmoitus näkyy oikein", function() {
            return page.getPreference(0).moveDown()
              .then(page.saveWaitError)
              .then(function() {
                page.saveError().should.equal("Tallentaminen epäonnistui")
              })
          })

          it("tallennus toimii uudella yrittämällä", function() {
            return page.save().then(function() {
              page.saveError().should.equal("")
              page.statusMessage().should.equal("Kaikki muutokset tallennettu")
            })
          })
        })

        describe("kun istunto on vanhentunut", function() {
          before(function() { mockAjax.respondOnce("PUT", "/omatsivut/api/applications/1.2.246.562.11.00000877107", 401, "") })

          it("virheilmoitus näkyy oikein", function() {
            return page.getPreference(0).moveDown()
              .then(page.saveWaitError)
              .then(function() {
                page.saveError().should.equal("Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.")
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
          page.getPreference(0).isDisabled().should.be.true
          page.getPreference(1).isDisabled().should.be.true
          page.getPreference(2).isDisabled().should.be.true
        })

        it("vain ensimmäinen tyhjä rivi on muokattavissa", function () {
          page.getPreference(0).isEditable().should.be.false
          page.getPreference(1).isEditable().should.be.true
          page.getPreference(2).isEditable().should.be.false
        })

        it("ainoaa hakutoivetta ei voi poistaa", function() {
          ApplicationListPage().getPreference(0).canRemove().should.be.false
        })
      })


      describe("kun lisätään hakukohde", function() {
        before(
          page.getPreference(1).selectOpetusPiste("Ahl"),
          page.getPreference(1).selectKoulutus(0)
        )

        it("seuraava hakukohde tulee muokattavaksi", function() {
          page.getPreference(2).isEditable().should.be.true
        })

        it("lisätty hakutoive on edelleen muokattavissa", function() {
          page.getPreference(1).isEditable().should.be.true
        })

        it("lomake on tallennettavissa", function() {
          page.isValidationErrorVisible().should.be.false
        })
      })

      describe("kun hakutoiveen valinta on kesken", function() {
        it("lomaketta ei voi tallentaa", function() {
          var pref = page.getPreference(1)
          return pref.selectOpetusPiste("Ahl")()
            .then(wait.untilFalse(page.saveButton(0).isEnabled))
            .then(function() { page.isValidationErrorVisible().should.be.true })
        })
      })
      
      describe("kun valinta jätetään kesken ja siirrytään vaihtamaan toista hakukohdetta", function() {
        before(
          page.getPreference(1).selectOpetusPiste("Ahl"),
          page.getPreference(1).selectKoulutus(0),
          page.getPreference(2).selectOpetusPiste("Turun Kristillinen"),
          page.getPreference(1).selectOpetusPiste("Turun Kristillinen")
        )
        it("keskeneräinen valinta pysyy muokattavassa tilassa", function() {
          page.getPreference(1).isEditable().should.be.true
          page.getPreference(2).isEditable().should.be.true
        })
        it("lomaketta ei voi tallentaa", function() {
          page.isValidationErrorVisible().should.be.true
        })
      })

      describe("jos kaksi hakutoivetta on identtisiä", function() {
        before(replacePreference(1, "Turun Kristillinen"))
        describe("käyttöliittymän tila", function() {
          it("näytetään validointivirhe", function() {
            page.getPreference(1).errorMessage().should.equal("Et voi syöttää samaa hakutoivetta useaan kertaan.")
          })

          it("lomaketta ei voi tallentaa", function() {
            page.saveButton(0).isEnabled().should.be.false
          })
        })

        describe("kun valitaan eri hakukohde", function() {
          before(replacePreference(1, "Etelä-Savon ammattiopisto"))

          it("lomakkeen voi tallentaa", function() {
            page.saveButton(0).isEnabled().should.be.true
          })

          it("poistetaan validointivirhe", function() {
            page.getPreference(1).errorMessage().should.equal("")
          })
        })
      })
    })

    describe("Lisäkysymykset", function() {
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
          replacePreference(2, "Etelä-Savon ammattiopisto"),
          function() {
            timestamp = page.changesSavedTimestamp()
          }
        )

        describe("Aluksi", function() {
          it("kysymykset näytetään", function() {
            ApplicationListPage().questionsForApplication(0).count().should.equal(11)
            page.questionsForApplication(0).getAnswer(0).should.equal("")
          })
          it("pakolliset kentät korostetaan", function() {
            ApplicationListPage().questionsForApplication(0).validationMessages()[0].should.equal("*")
          })
        })

        describe("Kun tallennetaan vastaamatta pakollisiin kysymyksiin", function() {
          before(page.save)
          it("näytetään tallennusvirhe", function() {
            page.saveError().should.equal("Ei tallennettu - vastaa ensin kaikkiin lisäkysymyksiin")
          })

          it("näytetään kaikki validaatiovirheet", function() {
            page.questionsForApplication(0).validationMessageCount().should.equal(11)
          })

          it("näytetään checkboxin minmax-validaatiovirhe", function() {
            page.questionsForApplication(0).validationMessages()[2].should.equal("Virheellinen arvo")
          })

          it("näytetään required-validaatiovirhe", function() {
            page.questionsForApplication(0).validationMessages()[0].should.equal("Pakollinen tieto.")
          })
        })

        describe("Kun tallennuksessa esiintyy validointivirhe, joka ei liity lisäkysymyksiin", function() {
          before(answerAllQuestions)
          before(page.questionsForApplication(0).modifyAnswers(function(answers) {
            answers.hakutoiveet.dummyAnswer = "tämä aiheuttaa epämääräisen validointivirheen"
          }))
          before(page.save)
          it.skip("näytetään tallennusvirhe", function() { // test broken
            // TODO: näytä eri viesti, koska tapahtui käsittelemätön validointivirhe
            page.saveError().should.equal("Ei tallennettu - vastaa ensin kaikkiin lisäkysymyksiin")
          })
        })

        describe("Onnistuneen tallennuksen jälkeen", function() {
          before(page.questionsForApplication(0).modifyAnswers(function(answers) {
            delete answers.hakutoiveet.dummyAnswer
          }))
          before(answerAllQuestions)

          describe("Tietokanta", function() {
            it("sisältää tallennetut tiedot", function() {
              return db.getApplications().then(function(data) {
                var answers = data[0].answers
                var questions = page.questionsForApplication(0).data()

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
              ApplicationListPage().questionsForApplication(0).count().should.equal(11)
            })

            it("validaatiovirheitä ei ole", function() {
              _.all(page.questionsForApplication(0).validationMessages(), function(item) {
                return item == ""
              }).should.be.true
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
              page.questionsForApplication(0).getAnswer(0).should.equal("tekstivastaus 1")
            })
          })

          describe("Kun ladataan sivu uudelleen", function() {
            before(page.openPage)
            it("valitut hakutoiveet näytetään", function() {
              page.getPreference(2).opetuspiste().should.equal("Etelä-Savon ammattiopisto,  Otavankatu 4")
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
              replacePreference(2, "Etelä-Savon ammattiopisto")
            )
            it("hakutoiveeseen liittyvien lisäkysymysten aiemmat vastaukset hävitetään", function() {
              ApplicationListPage().questionsForApplication(0).count().should.equal(11)
              page.questionsForApplication(0).getAnswer(0).should.equal("")
            })
          })
        })

        describe("Kun poistetaan lisätty hakutoive, jolla lisäkysymyksiä", function() {
          before(
            page.getPreference(2).remove,
            page.save
          )

          it("Tallennus onnistuu", function() {
            page.saveError().should.equal("")
            page.statusMessage().should.equal("Kaikki muutokset tallennettu")
          })
        })

        function answerAllQuestions() {
          page.questionsForApplication(0).enterAnswer(0, "tekstivastaus 1")
          page.questionsForApplication(0).enterAnswer(1, "Vaihtoehto x 1")
          page.questionsForApplication(0).enterAnswer(2, "Vaihtoehto 1")
          page.questionsForApplication(0).enterAnswer(2, "Vaihtoehto 2")
          page.questionsForApplication(0).enterAnswer(3, "Isokyrö")
          page.questionsForApplication(0).enterAnswer(4, "textarea-vastaus")
          page.questionsForApplication(0).enterAnswer(5, "Vaihtoehto yyy 1")
          page.questionsForApplication(0).enterAnswer(5, "Vaihtoehto yyy 2")
          page.questionsForApplication(0).enterAnswer(6, "Vaihtoehto arvosanat 1")
          page.questionsForApplication(0).enterAnswer(7, "tekstivastaus 2")
          page.questionsForApplication(0).enterAnswer(8, "tekstivastaus 3")
          page.questionsForApplication(0).enterAnswer(9, "Vaihtoehto zzzz 1")
          page.questionsForApplication(0).enterAnswer(10, "Vaihttoehto yksi")

          return Q.all([page.save(), page.waitForTimestampUpdate()])
        }
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
    }
  }

  function leaveOnlyOnePreference() {
    return ApplicationListPage().getPreference(0).remove()
      .then(function() { return ApplicationListPage().getPreference(0).remove() })
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