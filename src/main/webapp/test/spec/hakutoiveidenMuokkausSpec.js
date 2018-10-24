(function () {
  var page = HakutoiveidenMuokkausPage()

  afterEach(function() {
    expect(window.uiError || null).to.be.null
  })

  afterEach(function () {
    if (this.currentTest.state == 'failed') {
      takeScreenshot()
    }
  })

  describe("Hakutoiveiden muokkaus väärällä tokenilla", function() {
    before(
      page.openPage("väärä")
    )

    it("näytetään virhe", function() {
      expect(page.alertMsg()).to.equal('Hakemustasi ei voida näyttää koska linkki on virheellinen.' )
    })
  })

  describe("Hakutoiveiden muokkaus vanhentuneella tokenilla", function() {
    before(
        page.openPage("expiredToken")
    )

    it("näytetään infoviesti", function() {
      expect(page.alertMsg()).to.contain('Hakuaika on päättynyt etkä voi enää tehdä muutoksia hakutoiveisiisi')
    })
  })

  describe('Hakutoiveiden muokkaus, kun hakemusta ei löydy', function () {
    before(
      page.openPage("1.2.246.562.11.0")
    )

    it("näytetään ilmoitus", function() {
      expect(page.alertMsg()).to.equal('Hakemuksesi ei ole enää aktiivinen.' )
    })
  })

  describe('Hakutoiveiden muokkaus "Korkeakoulujen yhteishaku kevät 2015"', function () {
    before(
      page.applyFixtureAndOpen({token: hakemusKorkeakouluKevatWithJazzId})
    )

    describe("Linkin avaamisen jälkeen", function() {
      it('näkyy oikea hakemus', function () {
        expect(page.getApplication().name()).to.equal('Korkeakoulujen yhteishaku kevät 2015')
      })
    })
  })

  describe('Hakutoiveiden muokkaus "Yhteishaku ammatilliseen ja lukioon, kevät 2016", kun valittuna ammatillisia', function () {
    before(
        page.applyFixtureAndOpen({token: hakemusYhteishakuKevat2016Ammatillisia, applicationOid: hakemusYhteishakuKevat2016Ammatillisia, overrideStart: daysFromNow(0)})
    )

    describe("Linkin avaamisen jälkeen", function() {
      it('näkyy oikea hakemus', function () {
        expect(page.getApplication().name()).to.equal('Yhteishaku ammatilliseen ja lukioon, kevät 2016')
      })
    })

    describe("kun lisätään hakukohde", function() {
      before(
          page.getApplication().getPreference(3).selectOpetusPiste("Lap"),
          page.getApplication().getPreference(3).selectKoulutus(3)
      )

      describe("lisäämisen jälkeen", function() {
        it("seuraava hakukohde tulee muokattavaksi", function() {
          page.getApplication().getPreference(4).isEditable().should.be.true
        })

        it("lisätty hakutoive on edelleen muokattavissa", function() {
          page.getApplication().getPreference(3).isEditable().should.be.true
        })

        it("näytetään uudet kysymykset", function() {
          var questionTitles = page.getApplication().questionsForApplication().titles()
          expect(questionTitles).to.deep.equal([ 'Haetko koulutukseen harkintaan perustuvassa valinnassa?',
            'Haetko urheilijan ammatilliseen koulutukseen?',
            'Aiemmat opinnot',
            'Liikunnanopettajan nimi',
            'Lukuaineiden keskiarvo tai arvio siitä',
            'Pakollisen liikunnan arvosana tai arvio siitä',
            'Urheilu',
            'Urheilulaji nro 1, jolla haet urheiluoppilaitokseen',
            'Lajiliitto',
            'Urheilulaji nro 2, jolla haet urheiluoppilaitokseen',
            'Lajiliitto',
            'Urheilusaavutukset',
            'Urheilusaavutukset',
            'Valmentajan yhteystiedot',
            'Nimi',
            'Puhelinnumero',
            'Sähköpostiosoite',
            'Valmennusryhmä',
            'Maajoukkue/Lajiliitto',
            'Alue/Piiri',
            'Urheiluseura ja sen valmennusryhmä tai joukkue',
            'Haetko koulutukseen harkintaan perustuvassa valinnassa?',
            'Tällä alalla on terveydentilavaatimuksia, jotka voivat olla opiskelijaksi ottamisen esteenä. Onko sinulla terveydellisiä tekijöitä, jotka voivat olla opiskelijaksi ottamisen esteenä?',
            'Tässä koulutuksessa opiskelijaksi ottamisen esteenä voi olla aiempi päätös opiskeluoikeuden peruuttamisessa. Onko opiskeluoikeutesi aiemmin peruutettu terveydentilasi tai muiden henkilöiden turvallisuuden vaarantamisen takia?',
            'Haluaisitko suorittaa ammatillisen perustutkinnon ohella lukio-opintoja/ylioppilastutkinnon?',
            'Tällä alalla on terveydentilavaatimuksia, jotka voivat olla opiskelijaksi ottamisen esteenä. Onko sinulla terveydellisiä tekijöitä, jotka voivat olla opiskelijaksi ottamisen esteenä?',
            'Tässä koulutuksessa opiskelijaksi ottamisen esteenä voi olla aiempi päätös opiskeluoikeuden peruuttamisessa. Onko opiskeluoikeutesi aiemmin peruutettu terveydentilasi tai muiden henkilöiden turvallisuuden vaarantamisen takia?',
            'Haetko urheilijan ammatilliseen koulutukseen?',
            'Oppisopimuskoulutus',
            'Työkokemus kuukausina' ])
        })

        it("lomake ei ole vielä tallennettavissa", function() {
          page.getApplication().isValidationErrorVisible().should.be.true
        })

        it("sovellus ei ole virhetilassa", function() {
          page.getApplication().saveError().should.equal("Täytä kaikki tiedot")
        })
      })

      describe("vastattaessa kysymyksiin", function() {
        before(
            function() { page.getApplication().questionsForApplication().enterAnswer(0, "Ei"); },
            function() { page.getApplication().questionsForApplication().enterAnswer(1, "Ei"); },
            function() { page.getApplication().questionsForApplication().enterAnswer(21, "Ei"); },
            function() { page.getApplication().questionsForApplication().enterAnswer(22, "Ei"); },
            function() { page.getApplication().questionsForApplication().enterAnswer(23, "Ei"); },
            function() { page.getApplication().questionsForApplication().enterAnswer(24, "Ei"); },
            function() { page.getApplication().questionsForApplication().enterAnswer(25, "Ei"); }, // Tällä alalla on terveydentilavaatimuksia...
            function() { page.getApplication().questionsForApplication().enterAnswer(26, "Ei"); }, // Tässä koulutuksessa opiskelijaksi ottamisen esteenä...
            function() { page.getApplication().questionsForApplication().enterAnswer(27, "Ei"); }, // Haetko urheilijan ammatilliseen koulutukseen?
            wait.forAngular
        )

        describe("vastaamisen jälkeen", function() {
          it("lomake on tallennettavissa", function() {
            page.getApplication().isValidationErrorVisible().should.be.false
          })
        })

        describe("siirrettäessä lisätty hakutoive ylemmäksi", function() {
          before(
              page.getApplication().getPreference(3).moveUp
          )

          describe("siirtämisen jälkeen", function() {
            it("lomake on tallennettavissa", function() {
              page.getApplication().isValidationErrorVisible().should.be.false
            })

            it("näytetään yhä uudet kysymykset (paitsi urheilijan lisäkysymykset)", function() {
              var questionTitles = page.getApplication().questionsForApplication().titles()
              expect(questionTitles).to.deep.equal([ 'Haetko koulutukseen harkintaan perustuvassa valinnassa?',
                'Haetko urheilijan ammatilliseen koulutukseen?',
                'Haetko koulutukseen harkintaan perustuvassa valinnassa?',
                'Tällä alalla on terveydentilavaatimuksia, jotka voivat olla opiskelijaksi ottamisen esteenä. Onko sinulla terveydellisiä tekijöitä, jotka voivat olla opiskelijaksi ottamisen esteenä?',
                'Tässä koulutuksessa opiskelijaksi ottamisen esteenä voi olla aiempi päätös opiskeluoikeuden peruuttamisessa. Onko opiskeluoikeutesi aiemmin peruutettu terveydentilasi tai muiden henkilöiden turvallisuuden vaarantamisen takia?',
                'Haluaisitko suorittaa ammatillisen perustutkinnon ohella lukio-opintoja/ylioppilastutkinnon?',
                'Tällä alalla on terveydentilavaatimuksia, jotka voivat olla opiskelijaksi ottamisen esteenä. Onko sinulla terveydellisiä tekijöitä, jotka voivat olla opiskelijaksi ottamisen esteenä?',
                'Tässä koulutuksessa opiskelijaksi ottamisen esteenä voi olla aiempi päätös opiskeluoikeuden peruuttamisessa. Onko opiskeluoikeutesi aiemmin peruutettu terveydentilasi tai muiden henkilöiden turvallisuuden vaarantamisen takia?',
                'Haetko urheilijan ammatilliseen koulutukseen?',
                'Oppisopimuskoulutus',
                'Työkokemus kuukausina' ])
            })
          })

          describe("tallentaminen", function() {
            before(
                page.getApplication().saveWaitSuccess
            )

            it("onnistuu", function() {

            })
          })
        })
      })
    })
  })

  describe('Hakutoiveiden muokkaus "Yhteishaku ammatilliseen ja lukioon, kevät 2016", kun valittuna pelkkä lukio', function () {
    before(
        page.applyFixtureAndOpen({token: hakemusYhteishakuKevat2016PelkkaLukio, applicationOid: hakemusYhteishakuKevat2016PelkkaLukio, overrideStart: daysFromNow(0)})
    )

    describe("Linkin avaamisen jälkeen", function() {
      it('näkyy oikea hakemus', function () {
        expect(page.getApplication().name()).to.equal('Yhteishaku ammatilliseen ja lukioon, kevät 2016')
      })
    })

    describe("kun lisätään ammatillinen hakukohde", function() {
      before(
          page.getApplication().getPreference(1).selectOpetusPiste("Helsingin Diakoniaopisto"),
          page.getApplication().getPreference(1).selectKoulutus(3)
      )

      it("seuraava hakukohde tulee muokattavaksi", function() {
        page.getApplication().getPreference(1).isEditable().should.be.true
      })

      it("lisätty hakutoive on edelleen muokattavissa", function() {
        page.getApplication().getPreference(1).isEditable().should.be.true
      })

      it("näytetään uudet kysymykset", function() {
        var questionTitles = page.getApplication().questionsForApplication().titles()
        expect(questionTitles).to.deep.equal([
          'Haetko koulutukseen harkintaan perustuvassa valinnassa?',
          'Oppisopimuskoulutus',
          'Työkokemus kuukausina'
        ])
      })
    })

    describe("kun lisätään lukion urheilulinja hakukohde", function() {
      before(
          page.getApplication().getPreference(1).selectOpetusPiste("Mäkelänrinteen lukio"),
          page.getApplication().getPreference(1).selectKoulutus(2)
      )

      it("seuraava hakukohde tulee muokattavaksi", function() {
        page.getApplication().getPreference(1).isEditable().should.be.true
      })

      it("lisätty hakutoive on edelleen muokattavissa", function() {
        page.getApplication().getPreference(1).isEditable().should.be.true
      })

      it("näytetään urheilijan lisäkysymykset", function() {
        var questionTitles = page.getApplication().questionsForApplication().titles()
        expect(questionTitles).to.deep.equal([
          'Aiemmat opinnot',
          'Liikunnanopettajan nimi',
          'Lukuaineiden keskiarvo tai arvio siitä',
          'Pakollisen liikunnan arvosana tai arvio siitä',
          'Urheilu',
          'Urheilulaji nro 1, jolla haet urheiluoppilaitokseen',
          'Lajiliitto',
          'Urheilulaji nro 2, jolla haet urheiluoppilaitokseen',
          'Lajiliitto',
          'Urheilusaavutukset',
          'Urheilusaavutukset',
          'Valmentajan yhteystiedot',
          'Nimi',
          'Puhelinnumero',
          'Sähköpostiosoite',
          'Valmennusryhmä',
          'Maajoukkue/Lajiliitto',
          'Alue/Piiri',
          'Urheiluseura ja sen valmennusryhmä tai joukkue'
        ])
      })
    })
  })
})()
