(function () {
  var page = ApplicationListPage()
  var hakemusNivelKesa2013WithPeruskouluBaseEducationId = "1.2.246.562.11.00000877107"
  var hakemusNivelKesa2013WithPeruskouluBaseEducation = page.getApplication(hakemusNivelKesa2013WithPeruskouluBaseEducationId)
  var hakemusYhteishakuKevat2014WithForeignBaseEducationId = "1.2.246.562.11.00000441368"
  var hakemusYhteishakuKevat2014WithForeignBaseEducation = page.getApplication(hakemusYhteishakuKevat2014WithForeignBaseEducationId)
  var hakemusYhteishakuKevat2013WithForeignBaseEducationId = "1.2.246.562.11.00000441369"
  var hakemusYhteishakuKevat2013WithForeignBaseEducation = page.getApplication(hakemusYhteishakuKevat2013WithForeignBaseEducationId)
  var hakemusYhteishakuKevat2013WithApplicationRoundEndedId = "1.2.246.562.11.00000441370"
  var hakemusYhteishakuKevat2013WithApplicationRoundEnded = page.getApplication(hakemusYhteishakuKevat2013WithApplicationRoundEndedId)
  var hakemusKorkeakouluJatkoHakuId = "1.2.246.562.11.00004590341"
  var hakemusKorkeakouluJatkoHaku = page.getApplication(hakemusKorkeakouluJatkoHakuId)
  var hakemusKorkeakouluYhteishakuSyksy2014Id = "1.2.246.562.11.00000877686"
  var hakemusKorkeakouluYhteishakuSyksy2014 = page.getApplication(hakemusKorkeakouluYhteishakuSyksy2014Id)
  var hakemusErityisopetuksenaId = "1.2.246.562.11.00000877688"
  var hakemusErityisopetuksena = page.getApplication(hakemusErityisopetuksenaId)

  /*
  FYI: tämä on jätetty lähinnä toiminnallisuuksien dokumentointina mahd. myöhempää uusimista varten, nykyisellään näitä ei ajeta
  koska data-alustukset on vanhan hakemuspalvelun pohjalta ja nykyään omillasivuilla on vain ataru-hakemuksia.
   */

  afterEach(function() {
    expect(window.uiError || null).to.be.null
  })

  afterEach(function () {
    if (this.currentTest.state == 'failed') {
      takeScreenshot()
    }
  })

  describe('Tyhjä hakemuslistaus', function () {
    function emptyApplicationPageVisible() {
      return S("#hakemus-list").attr("ng-cloak") == null && page.listStatusInfo().length > 0
    }

    before(session.init("300794-937F","fi"), page.openPage(emptyApplicationPageVisible))

    describe("jos käyttäjällä ei ole hakemuksia", function() {
      it("näytetään ilmoitus", function() {
        expect(page.listStatusInfo()).to.equal('Sinulla ei ole hakemuksia, joita on mahdollista muokata. Etsi koulutukset sanahaulla, ja täytä hakulomake. Tunnistautuneena voit tällä sivulla muokata hakemustasi hakuaikana.' )
      })
    })
  })
  describe('Kun käyttäjän oid puuttuu', function () {
    function infoBoxIsVisible() {
      return S(".info").is(":visible")
    }

    before(session.init("091094-970D","fi"), page.openPage(infoBoxIsVisible))

    it("näytetään ilmoitus", function() {
      expect(S(".info").text()).to.contain("emme löytäneet hakemuksia henkilötunnuksellasi")
    })
  })

  describe("Sivupohjan lokalisointi", function() {
    this.timeout(testTimeoutPageLoad)
    before(
      page.applyFixtureAndOpen({})
    )
    it("kaikki tekstit on lokalisoitu", function() {
      return ApplicationListPage().getNonLocalizedText().then(function(text) {
        expect(text).to.equal("")
      })
    })
  })

  describe('Hakemuslistaus ruotsiksi', function () {
    before(page.applyFixtureAndOpen({lang:"sv"}))

    describe("Hakemuksen tietojen näyttäminen", function() {
      it("otsikko on ruotsiksi", function() {
        expect(S("h1:first").text().trim()).to.equal('Mina ansökningsblanketter')
      })
      it('hakemuslistassa on hakemus henkilölle 010100A939R', function () {
        expect(ApplicationListPage().applications()).to.contain(
          { applicationSystemName: 'Gemensam ansökan till yrkes- och gymnasieutbildning våren 2014' }
        )
        expect(ApplicationListPage().applications()).to.contain(
          { applicationSystemName: "Lisähaku kevään 2014 yhteishaussa vapaaksi jääneille paikoille samma på svenska" }
        )
      })

      it("henkilön 010100A939R hakutoiveet ovat näkyvissä", function () {
        expect(page.getApplication(hakemusNivelKesa2013WithPeruskouluBaseEducationId).preferencesForApplication()).to.deep.equal([
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
  })

  describe("Monikielisyys", function () {

    it("kaikkien kielitiedostojen rakenne on sama", function() {
      return Q.all([
        getJson("/omatsivut/translations?lang=fi"),
        getJson("/omatsivut/translations?lang=en"),
        getJson("/omatsivut/translations?lang=sv")
      ]).then(function(translations) {
        var translations = _(translations).map(function(translation) { return _.keys(util.flattenObject(translation)).sort() })
        diffKeys(translations[0], translations[1]).should.deep.equal([])
        diffKeys(translations[1], translations[2]).should.deep.equal([])
        _(translations[0]).any(function(val) { return _.isEmpty(val) }).should.be.false
        _(translations[1]).any(function(val) { return _.isEmpty(val) }).should.be.false
        _(translations[2]).any(function(val) { return _.isEmpty(val) }).should.be.false
      })
    })
  })

  describe('Hakemuslistaus', function () {
    before(session.init("010100A939R","fi"))

    describe("Hakemuksen tietojen näyttäminen", function() {
      before(page.applyFixtureAndOpen({}))

      it('julkaisemattoman haun hakemus ei näy sivulla', function () {
        expect(ApplicationListPage().applications()).to.not.contain({
          applicationSystemName: 'Ei julkaistu haku (ei pitäisi näkyä omilla sivuilla)'
        })
      })

      it('hakemuslistassa on hakemus henkilölle 010100A939R', function () {
        expect(ApplicationListPage().applications()).to.contain(
          { applicationSystemName: 'Ammatillisen koulutuksen ja lukiokoulutuksen kevään 2014 yhteishaku' }
        )
        expect(ApplicationListPage().applications()).to.contain(
          { applicationSystemName: "Perusopetuksen jälkeisen valmistavan koulutuksen kesän 2014 haku MUOKATTU" }
        )
      })

      it('ensimmäisenä on uusin hakemus', function () {
        expect(ApplicationListPage().applications()[0]).to.deep.equal(
          { applicationSystemName: 'Yhteishaku ammatilliseen ja lukioon, kevät 2016' }
        )
      })

      it("henkilön 010100A939R hakutoiveet ovat näkyvissä", function () {
        expect(hakemusNivelKesa2013WithPeruskouluBaseEducation.preferencesForApplication()).to.deep.equal([
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

      it("hakuaika näkyy", function() {
        hakemusNivelKesa2013WithPeruskouluBaseEducation.applicationPeriods().should.equal("Hakuaika päättyy 1. joulukuuta 2030 klo 07.00 (EET)")
      })

      it.skip("tallennusaikaleima näkyy", function() {
        hakemusNivelKesa2013WithPeruskouluBaseEducation.changesSavedTimestamp().should.match(/Hakemusta muokattu \d+\..*?\d+ klo \d+\.\d+/)
      })

    })

    describe("valintatulokset, kun haku on päättynyt", function() {
      before(page.applyFixtureAndOpen({applicationOid: hakemusYhteishakuKevat2013WithForeignBaseEducationId}))

      var hakuaikatieto = "Opiskelijavalinta on kesken. Tulokset julkaistaan viimeistään 11. kesäkuuta 2014."

      describe("Kun valintatulosten haku epäonnistuu", function() {
        before(page.setValintatulosServiceShouldFail("true"), page.applyValintatulosFixtureAndOpen("hyvaksytty-kesken-julkaistavissa"))
        it("Näytetään virheviesti", function() {
          var error = hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulosError()
          expect(error.visible).to.equal(true)
          expect(error.text).to.equal("Valintatuloksen hakemisessa tapahtui odottamaton virhe. Yritä myöhemmin uudelleen.")
        })
        after(page.setValintatulosServiceShouldFail("false"))
      })

      describe("kun valintatuloksia ei ole julkaistu", function() {
        before(page.applyValintatulosFixtureAndOpen("ei-tuloksia"))
        it("hakemusta ei voi muokata", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length).to.equal(0)
        })

        it("hakuaikatieto näkyy", function() {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal(hakuaikatieto)
        })

        it("valintatulokset näytetään KESKEN-tilassa", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset().length).to.equal(2)
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Opiskelijavalinta kesken')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Opiskelijavalinta kesken')
        })
      })

      describe("kun kk valinta on kesken ja hakija on 2. varasijalla", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-ylempi-varalla", {"ohjausparametrit": "varasijasaannot-ei-viela-voimassa"}))

        it("hakuaikatieto näkyy", function() {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal(hakuaikatieto)
          hakemusYhteishakuKevat2013WithForeignBaseEducation.resultTableTitle().should.equal("Valintatilanteesi")
        })

        it("valintatulokset näytetään", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('2. varasijalla. Varasijoja täytetään 26. elokuuta 2014 asti.')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Hyväksytty (odottaa ylempien hakukohteiden tuloksia)')
        })

        it("paikka ei ole vastaanotettavissa", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).visible()).to.equal(false)
        })
      })

      describe("kun 2. asteen valinta on kesken ja hakija on 2. varasijalla", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-ylempi-varalla", {"haku": "toinen-aste-yhteishaku"}))

        it("valintatulokset näytetään", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('2. varasijalla. Varasijoja täytetään 26. elokuuta 2014 asti.')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].hakukohde).to.equal('Salon lukio Lukio')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Hyväksytty')
        })

        it("ilmoitetaan myönnetystä paikasta", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).title()).to.deep.equal(
            "Sinulle tarjotaan opiskelupaikkaa Salon lukio - Lukio"
          )
        })

        it("paikka on vastaanotettavissa", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).visible()).to.equal(true)
        })
      })

      describe("kun 2. asteen valinta on kesken, mutta tuloksia ei saa vielä julkaista", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-ylempi-varalla", {"haku": "toinen-aste-yhteishaku", "ohjausparametrit": "tuloksia-ei-viela-saa-julkaista"}))

        it("hakuaikatieto näkyy", function() {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal(hakuaikatieto)
        })

        it("valintatulokset näkyvät kesken", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset().length).to.equal(2)
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Opiskelijavalinta kesken')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Opiskelijavalinta kesken')
        })
      })

      describe("Hylätty", function() {
        before(page.applyValintatulosFixtureAndOpen("hylatty-julkaistavissa"))

        it("hakuaikatieto näkyy", function() {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal(hakuaikatieto)
          hakemusYhteishakuKevat2013WithForeignBaseEducation.resultTableTitle().should.equal("Valintatilanteesi")
        })

        it("valintatulokset näytetään", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Et saanut opiskelupaikkaa.')
        })

        it("paikka ei ole vastaanotettavissa", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).visible()).to.equal(false)
        })
      })

      describe("Peruutettu", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-valintatulos-peruutettu"))

        it("hakuaikatieto näkyy", function() {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal(hakuaikatieto)
          hakemusYhteishakuKevat2013WithForeignBaseEducation.resultTableTitle().should.equal("Valintatilanteesi")
        })

        it("valintatulokset näytetään", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].hakukohde).to.equal('Salon lukio Lukio')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruutettu')
        })

        it("paikka ei ole vastaanotettavissa", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).visible()).to.equal(false)
        })
      })

      describe("Perunut", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-valintatulos-perunut"))

        it("hakuaikatieto näkyy", function() {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal(hakuaikatieto)
          hakemusYhteishakuKevat2013WithForeignBaseEducation.resultTableTitle().should.equal("Valintatilanteesi")
        })

        it("valintatulokset näytetään", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].hakukohde).to.equal('Salon lukio Lukio')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruit opiskelupaikan')
        })

        it("paikka ei ole vastaanotettavissa", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).visible()).to.equal(false)
        })
      })

      describe("Hyväksytty varasijalta", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-varasijalta-julkaistavissa"))

        it("valintatulokset näytetään", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Hyväksytty varasijalta')

          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].hakukohde).to.equal('Salon lukio Lukio')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruuntunut')
        })

        it("paikka on vastaanotettavissa", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).visible()).to.equal(true)
        })
      })

      describe("kun lopulliset tulokset on julkaistu ja opiskelija on hyväksytty", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kesken-julkaistavissa"))
        it("ilmoitetaan myönnetystä paikasta", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).title()).to.deep.equal(
            "Sinulle tarjotaan opiskelupaikkaa Kallion lukio - Lukion ilmaisutaitolinja"
          )
        })
      })

      describe("monta hakuaikaa", function() {
        before(
          page.applyFixtureAndOpen({applicationOid: hakemusErityisopetuksenaId, overrideStart: daysFromNow(-73)}),
          page.applyValintatulosFixtureAndOpen("erillishaku-toinen-valmis", {"haku": "toinen-aste-erillishaku"})
        )

        describe("jos jonkun hakutoiveen hakuaika on päättynyt ennen muita ja sen tulokset ovat jo saatavilla", function () {
          it("hakutoiveet ovat näkyvissä", function () {
            expect(hakemusErityisopetuksena.preferencesForApplication().length).to.equal(2)
          })

          it("tuloslistaus on näkyvissä", function() {
            expect(hakemusErityisopetuksena.valintatulokset()[0].tila).to.equal('Opiskelijavalinta kesken')
            expect(hakemusErityisopetuksena.valintatulokset()[1].tila).to.equal('Hyväksytty')
            hakemusErityisopetuksena.resultTableTitle().should.equal("Valintatilanteesi")
          })

          it("paikka on vastaanotettavissa", function() {
            expect(hakemusErityisopetuksena.vastaanotto(0).visible()).to.equal(true)
            expect(hakemusErityisopetuksena.vastaanotto(0).title()).to.equal("Sinulle tarjotaan opiskelupaikkaa Kiipulan ammattiopisto, Lahden toimipaikka - Liiketalouden perustutkinto, er, Kevät 2014, valmis")
          })
        })

        describe("testin tilan siivous", function () {
          after(page.applyFixtureAndOpen({applicationOid: hakemusYhteishakuKevat2013WithForeignBaseEducationId}))
          it("palautetaan vanha", function() {
          })
        })
      })

      describe("hakijalle tarjotaan paikkaa", function() {
        describe("sitova vastaanotto", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kesken-julkaistavissa"))

          describe("ennen valintaa", function() {
            it("vastausaika näkyy", function () {
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).info()).to.deep.equal([
                "Lähetä vastauksesi ennen 10. tammikuuta 2030 klo 12.00 (EET) tai menetät tarjotun opiskelupaikan."
              ])
            })

            it("oikeat vaihtoehdot tulevat näkyviin", function () {
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).vaihtoehdot()).to.deep.equal([
                'Otan opiskelupaikan vastaan',
                'En ota tätä opiskelupaikkaa vastaan'
              ])
            })

            it("nappi on disabloitu", function () {
              hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).confirmButtonEnabled().should.be.false
            })
          })

          describe("valinnan jälkeen", function() {
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VastaanotaSitovasti"))

            it("nappi on enabloitu", function() {
              hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).confirmButtonEnabled().should.be.true
            })

          })

          describe("paikan vastaanottaminen", function() {
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VastaanotaSitovasti"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("vastaanottotieto näkyy", function() {
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Opiskelupaikka vastaanotettu')
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruuntunut')
            })
          })

          describe("paikan hylkääminen", function() {
            before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kesken-julkaistavissa"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("Peru"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("perumistieto näkyy", function() {
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Peruit opiskelupaikan')
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruuntunut')
            })
          })
        })

        describe("vastaanotto varsinaisen vastaanoottoajan jälkeen", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kesken-julkaistavissa", {"ohjausparametrit": "vastaanotto-loppunut-iso-buffer"}))

          describe("ennen vastaanottoa", function() {
            it("vastausaika näkyy", function () {
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).info()).to.deep.equal([
                "Lähetä vastauksesi ennen 11. tammikuuta 2042 klo 12.00 (EET) tai menetät tarjotun opiskelupaikan."
              ])
            })
          })

          describe("paikan vastaanottaminen", function() {
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VastaanotaSitovasti"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("vastaanottotieto näkyy", function() {
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Opiskelupaikka vastaanotettu')
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruuntunut')
            })
          })
        })

        describe("toisen asteen haussa, kun ylempi toive on varalla ja alempi hyväksytty", function() {
          before(page.applyValintatulosFixtureAndOpen("vastaanotettavissa-ehdollisesti", {"haku": "toinen-aste-yhteishaku"}))

          it("voi ottaa paikan vastaan", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).vaihtoehdot()).to.deep.equal([
              'Otan opiskelupaikan vastaan',
              'En ota tätä opiskelupaikkaa vastaan'
            ])
          })

          it("vastausaika näkyy", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).info()).to.deep.equal([
              "Lähetä vastauksesi ennen 10. tammikuuta 2030 klo 12.00 (EET) tai menetät tarjotun opiskelupaikan."
            ])
          })

          describe("paikan vastaanottaminen sitovasti", function() {
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VastaanotaSitovasti"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("vastaanottotieto näkyy", function() {
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('1. varasijalla')
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Opiskelupaikka vastaanotettu')
            })

            it("toinen toive ei peruunnu", function () {
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('1. varasijalla')
            })
          })

          describe("paikan hylkääminen", function() {
            before(page.applyValintatulosFixtureAndOpen("vastaanotettavissa-ehdollisesti", {"haku": "toinen-aste-yhteishaku"}))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("Peru"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("perumistieto näkyy", function() {
              hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal("Opiskelijavalinta on kesken. Tulokset julkaistaan viimeistään 11. kesäkuuta 2014.")
            })
          })
        })

        describe("kk haussa, kun ylempi toive on varalla ja alempi hyväksytty", function() {
          before(
            page.applyValintatulosFixtureAndOpen("korkeakoulu-vastaanotettavissa-ehdollisesti", {"haku": "korkeakoulu-jatkotutkintohaku", "hakuoid" : "1.2.246.562.29.62858726037"}),
            page.applyFixtureAndOpen({applicationOid: hakemusKorkeakouluJatkoHakuId}),
            wait.forMilliseconds(1000)
          )

          it("voi ottaa myös ehdollisesti vastaan", function() {
            expect(hakemusKorkeakouluJatkoHaku.vastaanotto(0).vaihtoehdot()).to.deep.equal([
              'Otan tämän opiskelupaikan vastaan sitovasti',
              'Otan tämän opiskelupaikan vastaan, mutta jään jonottamaan opiskelupaikkaa ylemmillä sijoilla olevista hakutoiveistani.',
              'En ota tätä opiskelupaikkaa vastaan'
            ])
          })

          it("vastausaika näkyy", function() {
            expect(hakemusKorkeakouluJatkoHaku.vastaanotto(0).info()).to.deep.equal([
              "Lähetä vastauksesi ennen 10. tammikuuta 2030 klo 12.00 (EET) tai menetät tarjotun opiskelupaikan."
            ])
          })

          it("varoitus yhden paikan säännöstä näkyy", function() {
            var singleStudyPlaceEnforcement = hakemusKorkeakouluJatkoHaku.vastaanotto(0).singleStudyPlaceEnforcement()
            expect(singleStudyPlaceEnforcement).to.have.length(1)
            expect(singleStudyPlaceEnforcement[0]).to.have.string("Voit ottaa vastaan")
            expect(singleStudyPlaceEnforcement[0]).to.have.string("vain yhden")
            expect(singleStudyPlaceEnforcement[0]).to.have.string("korkeakoulututkintoon johtavan opiskelupaikan samana lukukautena alkavasta koulutuksesta.")
          })

          describe("paikan vastaanottaminen sitovasti", function() {
            before(
              hakemusKorkeakouluJatkoHaku.vastaanotto(0).selectOption("VastaanotaSitovasti"),
              hakemusKorkeakouluJatkoHaku.vastaanotto(0).send
            )

            it("vastaanottotieto näkyy", function() {
              expect(hakemusKorkeakouluJatkoHaku.valintatulokset()[0].tila).to.equal('Peruuntunut')
              expect(hakemusKorkeakouluJatkoHaku.valintatulokset()[1].tila).to.equal('Opiskelupaikka vastaanotettu')
            })
          })

          describe("paikan vastaanottaminen ehdollisesti", function() {
            before(page.applyValintatulosFixtureAndOpen("korkeakoulu-vastaanotettavissa-ehdollisesti", {"haku": "korkeakoulu-jatkotutkintohaku", "hakuoid" : "1.2.246.562.29.62858726037"}))
            before(hakemusKorkeakouluJatkoHaku.vastaanotto(0).selectOption("VastaanotaEhdollisesti"))
            before(hakemusKorkeakouluJatkoHaku.vastaanotto(0).send)

            it("vastaanottotieto näkyy", function() {
              expect(hakemusKorkeakouluJatkoHaku.valintatulokset()[0].tila).to.equal('1. varasijalla')
              expect(hakemusKorkeakouluJatkoHaku.valintatulokset()[1].tila).to.equal('Opiskelupaikka vastaanotettu ja jonotat opiskelupaikkaa ylemmillä sijoilla olevista hakutoiveistasi')
            })
          })

          describe("paikan hylkääminen", function() {
            before(page.applyValintatulosFixtureAndOpen("korkeakoulu-vastaanotettavissa-ehdollisesti", {"haku": "korkeakoulu-jatkotutkintohaku", "hakuoid" : "1.2.246.562.29.62858726037"}))
            before(hakemusKorkeakouluJatkoHaku.vastaanotto(0).selectOption("Peru"))
            before(hakemusKorkeakouluJatkoHaku.vastaanotto(0).send)

            it("perumistieto näkyy", function() {
              expect(hakemusKorkeakouluJatkoHaku.valintatulokset()[0].tila).to.equal('1. varasijalla')
              expect(hakemusKorkeakouluJatkoHaku.valintatulokset()[1].tila).to.equal('Peruit opiskelupaikan')
            })
          })
        })
      })

      describe("kun hakijalle tarjotaan kahta paikkaa kk varsinaisessa haussa, virhetilanne, mutta vastaa lisähaun toimintaa", function() {
        before(page.applyFixtureAndOpen({applicationOid: hakemusYhteishakuKevat2013WithForeignBaseEducationId}),
          page.applyValintatulosFixtureAndOpen("hyvaksytty-kaikkiin", {"haku": "korkeakoulu-yhteishaku"}))

        describe("alkutilassa", function() {
          it("oikeat vaihtoehdot tulevat näkyviin", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotettavia()).to.equal(2)
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).vaihtoehdot()).to.deep.equal([
              'Otan opiskelupaikan vastaan',
              'En ota tätä opiskelupaikkaa vastaan'
            ])
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(1).vaihtoehdot()).to.deep.equal([
              'Otan opiskelupaikan vastaan',
              'En ota tätä opiskelupaikkaa vastaan'
            ])
          })
        })

        describe("ensimmäisen paikan sitova vastaanottaminen", function() {
          before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VastaanotaSitovasti"))
          it("oikea nappi on valittuna", function () {
            var selectedIndex = hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectedIndex();
            expect(selectedIndex).to.equal(0)
          })
        })

        describe("ensimmäisen paikan sitova vastaanottamisen lähetys", function() {
          before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

          it("vastaanottotieto näkyy ja toinen paikka peruuntuu", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Opiskelupaikka vastaanotettu')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruuntunut')
          })

          it("kumpikaan paikka ei ole enää vastaanotettavissa", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotettavia()).to.equal(0)
          })
        })

        describe("toisen paikan sitova vastaanottaminen", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kaikkiin"))
          before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(1).selectOption("VastaanotaSitovasti"))
          before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(1).send)

          it("vastaanottotieto näkyy ja toinen paikka peruuntuu", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Peruuntunut')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Opiskelupaikka vastaanotettu')
          })
        })
      })

      describe("kun hakijalle tarjotaan kahta paikkaa toisen asteen haussa", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kaikkiin", {"haku": "toinen-aste-yhteishaku"}))

        describe("alkutilassa", function() {
          it("oikeat vaihtoehdot tulevat näkyviin", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotettavia()).to.equal(2)
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).vaihtoehdot()).to.deep.equal([
              'Otan opiskelupaikan vastaan',
              'En ota tätä opiskelupaikkaa vastaan'
            ])
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(1).vaihtoehdot()).to.deep.equal([
              'Otan opiskelupaikan vastaan',
              'En ota tätä opiskelupaikkaa vastaan'
            ])
          })
        })

        describe("ensimmäisen paikan vastaanottaminen", function() {
          before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VastaanotaSitovasti"))
          it("oikea nappi on valittuna", function () {
            var selectedIndex = hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectedIndex();
            expect(selectedIndex).to.equal(0)
          })
        })

        describe("ensimmäisen paikan vastaanottamisen lähetys", function() {
          before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

          describe("jälkeen", function() {
            it("toinen paikka on myös vastaanotettavissa", function() {
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).vaihtoehdot()).to.deep.equal([
                'Otan opiskelupaikan vastaan',
                'Otan opiskelupaikan vastaan ja perun samalla alemmat vastaanotetut paikat',
                'En ota tätä opiskelupaikkaa vastaan'
              ])
            })
          })

          describe("toisen paikan vastaanottaminen", function() {
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VastaanotaSitovasti"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("kumpikaan paikka ei ole enää vastaanotettavissa", function() {
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotettavia()).to.equal(0)
            })
          })
        })
      })

      describe("vanhat hakemukset", function() {

        describe("jos ei ole valintatuloksia ja hakukierros on päättynyt", function() {
          before(page.applyFixtureAndOpen({}))
          it("hakemusta ei voi muokata", function () {
            expect(hakemusYhteishakuKevat2013WithApplicationRoundEnded.preferencesForApplication().length).to.equal(0)
          })

          it("hakemusta ei nykyisin suorituskykysyistä näytetä, jos hakukierros on päättynyt", function () {
            hakemusYhteishakuKevat2013WithApplicationRoundEnded.applicationStatus().should.equal("")
          })
        })

        describe("jos ei ole vastaanottotietoa ja ylin hakutoive on hylätty", function() {
          before(page.applyValintatulosFixtureAndOpen("hylatty-julkaistavissa-valmis"))
          it("hakemusta ei voi muokata", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length).to.equal(0)
          })

          it("valintatulokset näytetään", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Et saanut opiskelupaikkaa.')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Et saanut opiskelupaikkaa.')
          })
        })

        describe("jos ei ole vastaanottotietoa ja jos ylin hakutoive on peruttu", function() {
          before(page.applyValintatulosFixtureAndOpen("perunut-julkaistavissa-valmis"))
          it("hakemusta ei voi muokata", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length).to.equal(0)
          })

          it("valintatulokset näytetään", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Peruit opiskelupaikan')
          })
        })

        describe("jos on ottanut paikan vastaan", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty-vastaanottanut"))
          it("hakemusta ei voi muokata", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length).to.equal(0)
          })

          it("valintatulokset näytetään", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Opiskelupaikka vastaanotettu')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruuntunut')
          })
        })

        describe("jos ei ole ottanut paikkaa vastaan määräaikaan mennessä", function() {
          before(page.applyValintatulosFixtureAndOpen("perunut-ei-vastaanottanut-maaraaikana", {"ohjausparametrit": "vastaanotto-loppunut"}))
          it("hakemusta ei voi muokata", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length).to.equal(0)
          })

          it("valintatulokset näytetään", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Et ottanut opiskelupaikkaa vastaan määräaikaan mennessä')
          })
        })

        describe("jos ei ole ottanut paikkaa vastaa paikkaa ja vastaanotto on päättynyt", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kesken-julkaistavissa", {"ohjausparametrit": "vastaanotto-loppunut"}))
          it("hakemusta ei voi muokata", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length).to.equal(0)
          })

          it("valintatulokset näytetään", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Et ottanut opiskelupaikkaa vastaan määräaikaan mennessä')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].hakukohde).to.equal('Salon lukio Lukio')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruuntunut')
          })
        })
      })
    })

    describe("valintatulokset, kun haku on käynnissä", function() {
      before(page.applyFixtureAndOpen({applicationOid: hakemusYhteishakuKevat2013WithForeignBaseEducationId, overrideStart: daysFromNow(0)}))
      describe("jos ei ole vielä tuloksia", function() {
        before(
          page.applyValintatulosFixtureAndOpen("ei-tuloksia", {"haku": "korkeakoulu-erillishaku"})
        )

        it("valintatuloksia ei näytetä", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset().length).to.equal(0)
        })
      })
      describe("jos kaikki on vielä kesken", function() {
        before(
          page.applyValintatulosFixtureAndOpen("julkaisematon-peruuntunut", {"haku": "korkeakoulu-erillishaku"})
        )

        it("valintatuloksia ei näytetä", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset().length).to.equal(0)
        })
      })
      describe("jos on saanut paikan kk erillishaussa", function() {
        before(
          page.applyValintatulosFixtureAndOpen("hyvaksytty-ylempi-sijoittelematon", {"haku": "korkeakoulu-erillishaku-ei-sijoittelua"})
        )

        it("valintatulokset näytetään", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Opiskelijavalinta kesken')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].hakukohde).to.equal('Salon lukio Lukio')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Hyväksytty')
        })

        it("paikka on otettavissa vastaan", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotettavia()).to.equal(1)
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).vaihtoehdot()).to.deep.equal([
            'Otan opiskelupaikan vastaan',
            'En ota tätä opiskelupaikkaa vastaan'
          ])
        })
      })
    })

    describe("Ilmoittautuminen", function() {
      before(page.applyFixtureAndOpen({applicationOid: hakemusKorkeakouluJatkoHakuId}))
      describe("jos on ottanut paikan vastaan yliopistohaussa", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-vastaanottanut-korkeakoulu", {"haku": "korkeakoulu-yhteishaku", "ohjausparametrit": "tulokset-saa-julkaista"}))
        describe("Oili-ilmoittautumislinkki", function () {
          it("Näytetään", function() {
            expect(hakemusKorkeakouluJatkoHaku.valintatulokset()[0].tila).to.equal('Opiskelupaikka vastaanotettu')
            expect(hakemusKorkeakouluJatkoHaku.ilmoittautuminen(0).visible).to.equal(true)
            expect(hakemusKorkeakouluJatkoHaku.ilmoittautuminen(0).linkTarget).to.equal("_blank")
          })
        })
        describe("OhjeetUudelleOpiskelijalle-linkki on hakukohteella", function() {
          it("Näytetään", function() {
            expect(hakemusKorkeakouluJatkoHaku.ilmoittautuminen(0).ohjeetUudelleOpiskelijalleText).to.equal("Tietoa uudelle opiskelijalle")
            expect(hakemusKorkeakouluJatkoHaku.ilmoittautuminen(0).ohjeetUudelleOpiskelijalleUrl).to.equal("https://www.helsinki.fi/fi/opiskelu/ohjeita-hakemuksen-jattaneille-yhteishaku")
            expect(hakemusKorkeakouluJatkoHaku.ilmoittautuminen(0).ohjeetUudelleOpiskelijalleTarget).to.equal("_blank")
          })
        })
      })
      describe("jos on ottanut paikan vastaan yliopistohaussa, mutta ilmoittautuminen on loppunut", function () {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-vastaanottanut-korkeakoulu", {"haku": "korkeakoulu-yhteishaku", "ohjausparametrit": "ilmoittautuminen-loppunut"}))
        describe("Oili-ilmoittautumislinkki", function () {
          it("Piilotetaan", function() {
            expect(hakemusKorkeakouluJatkoHaku.ilmoittautuminen(0).visible).to.equal(false)
          })
        })
      })
      describe("Jos on saanut paikan, muttei vielä ottanut sitä vastaan", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kesken-julkaistavissa-korkeakoulu", {"haku": "korkeakoulu-yhteishaku"}))

        describe("Oili-ilmoittautumislinkki", function () {
          it("Piilotetaan", function() {
            expect(hakemusKorkeakouluJatkoHaku.ilmoittautuminen(0).visible).to.equal(false)
          })
        })

        describe("Kun paikka otetaan vastaan", function() {
          before(hakemusKorkeakouluJatkoHaku.vastaanotto(0).selectOption("VastaanotaSitovasti"))
          before(hakemusKorkeakouluJatkoHaku.vastaanotto(0).send)

          it("Oili-linkki tulee näkyviin", function() {
            expect(hakemusKorkeakouluJatkoHaku.ilmoittautuminen(0).visible).to.equal(true)
          })
        })
      })
      describe("Jos on saanut ehdollisesti paikan, muttei vielä ottanut sitä vastaan", function() {
        before(page.applyFixtureAndOpen({applicationOid: hakemusKorkeakouluJatkoHakuId}))
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-ehdollisesti-kesken-julkaistavissa-korkeakoulu"))

        describe("Ennen vastaanottoa", function () {
          it("Näkyy ehdollisesti hyväksyttynä", function() {
            expect(hakemusKorkeakouluJatkoHaku.valintatulokset()[0].tila).to.equal('Hyväksytty, ehdollinen')
          })
          it("Oili-ilmoittautumislinkki piilotetaan", function() {
            expect(hakemusKorkeakouluJatkoHaku.ilmoittautuminen(0).visible).to.equal(false)
          })
        })

        describe("Kun paikka otetaan vastaan", function() {
          before(hakemusKorkeakouluJatkoHaku.vastaanotto(0).selectOption("VastaanotaSitovasti"))
          before(hakemusKorkeakouluJatkoHaku.vastaanotto(0).send)

          it("vastaanottotieto näkyy", function() {
            expect(hakemusKorkeakouluJatkoHaku.valintatulokset()[0].tila).to.equal('Opiskelupaikka vastaanotettu')
            expect(hakemusKorkeakouluJatkoHaku.valintatulokset()[1].tila).to.equal('Peruuntunut')
          })

          it("näytetään tieto valinnan ehdollisuudesta", function() {
            expect(hakemusKorkeakouluJatkoHaku.ilmoittautuminen(0).title()).to.equal(
              'Opiskelijavalintasi on vielä ehdollinen. Jyväskylän ammattikorkeakoulu, Ammatillinen opettajakorkeakoulu - Ammatillinen erityisopettajankoulutus Tietoa uudelle opiskelijalle')
          })

          it("Oili-linkki ei tule näkyviin", function() {
            expect(hakemusKorkeakouluJatkoHaku.ilmoittautuminen(0).visible).to.equal(false)
          })
        })
      })

      describe("Jos on saanut ehdollisesti paikan, muttei vielä ottanut sitä vastaan näyttää ehdollisen hyvaksymisen syyn", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-ehdollisesti-syy-kesken-julkaistavissa-korkeakoulu"))

        describe("Ennen vastaanottoa suomi", function () {
          it("Näkyy ehdollisesti hyväksyttynä", function () {
            expect(hakemusKorkeakouluJatkoHaku.valintatulokset()[0].tila).to.equal('Hyväksytty, ehdollinen')
          })
        })
      })

      describe("Jos on saanut kaksi paikkaa kk haussa, jossa yhden paikan sääntö ei ole voimassa", function () {
        before(page.applyFixtureAndOpen({applicationOid: hakemusKorkeakouluYhteishakuSyksy2014Id}))
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kaikkiin-korkeakoulu", {"haku": "korkeakoulu-erillishaku-ei-yhden-paikan-saantoa-ei-sijoittelua"}))

        describe("Kun ensimmäinen paikka otetaan vastaan", function () {
          before(hakemusKorkeakouluYhteishakuSyksy2014.vastaanotto(0).selectOption("VastaanotaSitovasti"))
          before(hakemusKorkeakouluYhteishakuSyksy2014.vastaanotto(0).send)

          it("Oili-linkki tulee näkyviin ja toinen paikka on yhä mahdollista vastaanottaa", function () {
            expect(hakemusKorkeakouluYhteishakuSyksy2014.vastaanotto(0).title()).to.equal('Sinulle tarjotaan opiskelupaikkaa ' +
              'Helsingin yliopisto, Matemaattis-luonnontieteellinen tiedekunta - Fysiikka (aineenopettaja), luonnontieteiden kandidaatti ja filosofian maisteri')
            expect(hakemusKorkeakouluYhteishakuSyksy2014.ilmoittautuminen(0).title()).to.equal(
              'Muista tehdä lukuvuosi-ilmoittautuminen korkeakouluun Diakonia-ammattikorkeakoulu, Järvenpään toimipiste - Sosionomi (AMK), monimuotototeutus Tietoa uudelle opiskelijalle')
          })


          describe("Kun toinen paikka otetaan vastaan", function () {
            before(hakemusKorkeakouluYhteishakuSyksy2014.vastaanotto(0).selectOption("VastaanotaSitovasti"))
            before(hakemusKorkeakouluYhteishakuSyksy2014.vastaanotto(0).send)

            it("Näkyy oili linkki molemmille paikoille", function () {
              expect(hakemusKorkeakouluYhteishakuSyksy2014.ilmoittautuminen(0).title()).to.equal(
                'Muista tehdä lukuvuosi-ilmoittautuminen korkeakouluun Diakonia-ammattikorkeakoulu, Järvenpään toimipiste - Sosionomi (AMK), monimuotototeutus Tietoa uudelle opiskelijalle')
              expect(hakemusKorkeakouluYhteishakuSyksy2014.ilmoittautuminen(1).title()).to.equal(
                'Muista tehdä lukuvuosi-ilmoittautuminen korkeakouluun Helsingin yliopisto, Matemaattis-luonnontieteellinen tiedekunta - Fysiikka (aineenopettaja), luonnontieteiden kandidaatti ja filosofian maisteri')
            })
          })
        })
      })

      describe("Jos on ottanut paikan vastaan 2. asteen haussa", function() {
        before(page.applyFixtureAndOpen({applicationOid: hakemusYhteishakuKevat2013WithForeignBaseEducationId}))
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-vastaanottanut", {"haku": "toinen-aste-yhteishaku"}))
        describe("Oili-ilmoittautumislinkki", function () {
          it("Piilotetaan", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.ilmoittautuminen(0).visible).to.equal(false)
          })
        })
      })

      describe("Hakija näkee julkaistun tuloskirjeen", function() {
        before(page.applyFixtureAndOpen({applicationOid: hakemusYhteishakuKevat2013WithForeignBaseEducationId}))
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-vastaanottanut", {"haku": "toinen-aste-yhteishaku"}))
        describe("Tuloskirjeen-latauslinkki", function () {
          it("Näkyy", function() {
            expect(ApplicationListPage().tuloskirjeet()).to.equal('Tuloskirje (14.11.2016)')
          })
        })
      })
    })

    describe("hakemuksen tila", function() {
      describe("passiivinen hakemus", function() {
        before(page.applyFixtureAndOpen({fixtureName:"passiveApplication"}))
        it("hakemus ei näy", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.found().should.be.false
        })
      })

      describe("submitted-tilassa oleva hakemus", function() {
        before(page.applyFixtureAndOpen({fixtureName:"submittedApplication"}))
        it("hakemusta ei voi muokata", function() {
          expect(hakemusYhteishakuKevat2014WithForeignBaseEducation.preferencesForApplication().length).to.equal(0)
        })

        it("seliteteksti näkyy oikein", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.applicationStatus().should.equal("Opiskelijavalinta on kesken. Hakemuksesi on vielä käsiteltävänä. Jos haluat muuttaa hakutoiveitasi, yritä myöhemmin uudelleen.")
        })
      })

      describe("post processing -tilassa oleva hakemus", function() {
        before(page.applyFixtureAndOpen({fixtureName:"postProcessingFailed"}))
        it("hakemusta ei voi muokata", function() {
          expect(hakemusYhteishakuKevat2014WithForeignBaseEducation.preferencesForApplication().length).equal(0)
        })

        it("seliteteksti näkyy oikein", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.applicationStatus().should.equal("Opiskelijavalinta on kesken. Hakemuksesi on vielä käsiteltävänä. Jos haluat muuttaa hakutoiveitasi, yritä myöhemmin uudelleen.")
        })
      })

    })

    describe("Virheidenkäsittely", function() {
      before(
        page.applyFixtureAndOpen({}),
        mockAjax.init
      )

      describe("Lisäkysymykset", function() {
        before(function() { mockAjax.respondOnce("POST", "/omatsivut/secure/applications/validate/1.2.246.562.11.00000877107", 400, "") })

        it("haun epäonnistuminen näytetään käyttäjälle", function() {
          // In current implementation move will trigger validation API call
          return hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).moveDown()
            .then(wait.until(function() { return hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().length > 0 }))
            .then(function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("Tietojen haku epäonnistui. Yritä myöhemmin uudelleen.")
            })
        })
      })

      describe("Hakutoiveen vastaanotto", function() {

        before(
          page.applyValintatulosFixtureAndOpen("hyvaksytty-kesken-julkaistavissa"),
          mockAjax.init
        )

        describe("kun server ei vastaa", function() {
          before(
            function() { mockAjax.respondOnce("POST", "/omatsivut/secure/applications/vastaanota/1.2.246.562.11.00000441369/hakukohde/1.2.246.562.5.72607738902", 400, "") },
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VastaanotaSitovasti"),
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send
          )
          it("virhe näytetään", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).errorText().should.equal("Hakemuksen lähettäminen epäonnistui. Yritä myöhemmin uudelleen.")
          })
          it("nappi on enabloitu", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).confirmButtonEnabled().should.be.true
          })
        })

        describe("kun session on vanhentunut", function() {
          before(
            function() { mockAjax.respondOnce("POST", "/omatsivut/secure/applications/vastaanota/1.2.246.562.11.00000441369/hakukohde/1.2.246.562.5.72607738902", 401, "") },
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VastaanotaSitovasti"),
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send
          )
          it("virhe näytetään", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).errorText().should.equal("Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.")
          })
        })

        describe("kun serveriltä tulee odottamaton virhe", function() {
          before(
            function() { mockAjax.respondOnce("POST", "/omatsivut/secure/applications/vastaanota/1.2.246.562.11.00000441369/hakukohde/1.2.246.562.5.72607738902", 500, "") },
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VastaanotaSitovasti"),
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send
          )
          it("virhe näytetään", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).errorText().should.equal("Odottamaton virhe. Ota yhteyttä ylläpitoon.")
          })
        })
      })

      describe("Tallennus", function() {
        describe("kun tapahtuu palvelinvirhe", function() {
          before(function() { mockAjax.respondOnce("PUT", "/omatsivut/secure/applications/1.2.246.562.11.00000877107", 400, "") })

          it("virheilmoitus näkyy oikein", function() {
            return hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).moveDown()
              .then(hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitError)
              .then(function() {
                console.log('saveError()='+hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError())
                expect(hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError()).to.equal("Odottamaton virhe. Ota yhteyttä ylläpitoon.")
              })
          })

          it("tallennus toimii uudella yrittämällä", function() {
            return hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitSuccess().then(function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("")
              hakemusNivelKesa2013WithPeruskouluBaseEducation.statusMessage().should.equal("Kaikki muutokset lähetetty. Tarkista sähköpostivahvistus.")
            })
          })
        })

        describe("kun istunto on vanhentunut", function() {
          before(function() { mockAjax.respondOnce("PUT", "/omatsivut/secure/applications/1.2.246.562.11.00000877107", 401, "") })

          it("virheilmoitus näkyy oikein", function() {
            return hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).moveDown()
              .then(hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitError)
              .then(function() {
                expect(hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError()).to.equal("Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.")
              })
          })
        })

      })
    })

    describe("Käyttö näppäimistöllä", function() {
      before(page.applyFixtureAndOpen({applicationOid: hakemusYhteishakuKevat2014WithForeignBaseEducationId}))
      it("tab-nappi toimii oletusjärjestyksessä", function() {
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).arrowUp().isRealButton().should.be.true
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).arrowDown().isRealButton().should.be.true
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).removeButton().isRealButton().should.be.true
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).arrowUp().isFocusableBefore(hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).arrowDown()).should.be.true
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).arrowDown().isFocusableBefore(hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).removeButton()).should.be.true
      })
    })

    describe("Kun hakijalla on ulkomaalainen pohjakoulutus", function() {
      before(
        page.applyFixtureAndOpen({applicationOid: hakemusYhteishakuKevat2014WithForeignBaseEducationId}),
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).remove,
        hakemusYhteishakuKevat2014WithForeignBaseEducation.saveWaitSuccess,
        replacePreference(hakemusYhteishakuKevat2014WithForeignBaseEducation, 1, "Kallion")
      )

      describe("näyttäminen", function() {
        it("ok", function() {
        })
      })

    })

  });

  function replacePreference(hakemus, index, searchString, koulutusIndex) {
    koulutusIndex = koulutusIndex || 1;
    return function() {
      const pref = hakemus.getPreference(index);
      return pref.remove()
        .then(pref.selectOpetusPiste(searchString))
        .then(pref.selectKoulutus(koulutusIndex))
        .then(wait.forAngular);
    }
  }

  function diffKeys(arr1, arr2) {
    return _.flatten([_.difference(arr1, arr2), _.difference(arr2, arr1)])
  }

})()
