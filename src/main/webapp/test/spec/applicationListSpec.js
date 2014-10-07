(function () {
  var page = ApplicationListPage()
  var hakemusNivelKesa2013WithPeruskouluBaseEducationId = "1.2.246.562.11.00000877107"
  var hakemusNivelKesa2013WithPeruskouluBaseEducation = page.getApplication(hakemusNivelKesa2013WithPeruskouluBaseEducationId)
  var hakemusYhteishakuKevat2014WithForeignBaseEducationId = "1.2.246.562.11.00000441368"
  var hakemusYhteishakuKevat2014WithForeignBaseEducation = page.getApplication(hakemusYhteishakuKevat2014WithForeignBaseEducationId)
  var hakemusLisaKevat2014WithForeignBaseEducationId = "1.2.246.562.11.00000441371"
  var hakemusLisaKevat2014WithForeignBaseEducation = page.getApplication(hakemusLisaKevat2014WithForeignBaseEducationId)
  var hakemusYhteishakuKevat2013WithForeignBaseEducationId = "1.2.246.562.11.00000441369"
  var hakemusYhteishakuKevat2013WithForeignBaseEducation = page.getApplication(hakemusYhteishakuKevat2013WithForeignBaseEducationId)
  var hakemusIncompleteId = "1.2.246.562.11.00000855417"
  var hakemusIncomplete = page.getApplication(hakemusIncompleteId)
  var hakemusKorkeakouluId = "1.2.246.562.11.00000877699"
  var hakemusKorkeakoulu = page.getApplication(hakemusKorkeakouluId)

  afterEach(function() {
    expect(window.uiError || null).to.be.null
  })

  describe("Kun käyttäjä ei ole kirjautunut sisään", function() {
    before(
      session.logout,
      page.openPage(loginVisible)
    )

    it("näytetään sisäänkirjautumissivu", function() {

    })

    function loginVisible() {
      return $(testFrame().document).find(".fake-vetuma").is(":visible")
    }
  })

  describe('Tyhjä hakemuslistaus', function () {
    var emptyPage = ApplicationListPage()
    function emptyApplicationPageVisible() {
      return S("#hakemus-list").attr("ng-cloak") == null && emptyPage.listStatusInfo().length > 0
    }

    before(function (done) {
      session.init("300794-937F","fi").then(emptyPage.openPage(emptyApplicationPageVisible)).done(done)
    })

    describe("jos käyttäjällä ei ole hakemuksia", function() {
      it("näytetään ilmoitus", function() {
        expect(page.listStatusInfo()).to.equal('Sinulla ei ole hakemuksia, joita on mahdollista muokata. Etsi koulutukset sanahaulla, ja täytä hakulomake. Tunnistautuneena voit tällä sivulla muokata hakemustasi hakuaikana.' )
      })
    })
  })

  describe("Sivupohjan lokalisointi", function() {
    before(page.resetDataAndOpen)
    it("kaikki tekstit on lokalisoitu", function() {
      return ApplicationListPage().getNonLocalizedText().then(function(text) {
        expect(text).to.equal("")
      })
    })
  })

  describe('Hakemuslistaus ruotsiksi', function () {
    before(page.resetDataAndOpenWithLang("sv"))

    describe("Hakemuksen tietojen näyttäminen", function() {
      it("otsikko on ruotsiksi", function() {
        expect(S("h1:first").text().trim()).to.equal('Mina ansökningsblanketter')
      })
      it('hakemuslistassa on hakemus henkilölle 010101-123N', function () {
        expect(ApplicationListPage().applications()).to.contain(
          { applicationSystemName: 'Gemensam ansökan till yrkes- och gymnasieutbildning våren 2014' }
        )
        expect(ApplicationListPage().applications()).to.contain(
          { applicationSystemName: "Lisähaku kevään 2014 yhteishaussa vapaaksi jääneille paikoille samma på svenska" }
        )
      })

      it("henkilön 010101-123N hakutoiveet ovat näkyvissä", function () {
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

    before(function (done) {
      session.init("010101-123N","fi").then(page.resetDataAndOpen).done(done)
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
      
      it('ensimmäisenä on uusin hakemus', function () {
        expect(ApplicationListPage().applications()[0]).to.deep.equal(
          { applicationSystemName: 'Korkeakoulujen yhteishaku syksy 2014' }
        )
      })

      it("henkilön 010101-123N hakutoiveet ovat näkyvissä", function () {
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

      describe("haun tyyppi", function() {
        before(hakemusNivelKesa2013WithPeruskouluBaseEducation.convertToKorkeakouluhaku)
        it ("korkeakouluhaussa käytetään eri tekstejä", function() {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.labels()[0].should.equal("Korkeakoulu")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.labels()[1].should.equal("Hakukohde")
          hakemusYhteishakuKevat2014WithForeignBaseEducation.labels()[0].should.equal("Opetuspiste")
          hakemusYhteishakuKevat2014WithForeignBaseEducation.labels()[1].should.equal("Hakukohde")
        })
      })
    })

    describe("lisähaku", function() {
      before(page.resetDataAndOpen)

      it("hakuaika haetaan hakukohteelta", function() {
        hakemusLisaKevat2014WithForeignBaseEducation.applicationStatus().should.equal("Hakuaika päättyy perjantaina 28. elokuuta 2054 klo 14.19")
      })

      describe("hakuajan päivittyminen", function() {
        before(
          replacePreference(hakemusLisaKevat2014WithForeignBaseEducation, 0, "Ahlman")
        )

        it("hakuaika päivittyy kun hakutoive muuttuu", function() {
          hakemusLisaKevat2014WithForeignBaseEducation.applicationStatus().should.equal("Hakuaika päättyy keskiviikkona 1. joulukuuta 2100 klo 07.00")
        })
      })

      describe("lisähaun muokkaus hakuajan jälkeen", function() {
        before(page.applyFixtureAndOpen("lisahakuEnded"))
        it("ei ole mahdollista", function() {
          hakemusLisaKevat2014WithForeignBaseEducation.preferencesForApplication().length.should.equal(0)
        })

        it("ohjeteksti päivittyy", function() {
          hakemusLisaKevat2014WithForeignBaseEducation.applicationStatus().should.equal("Hakuaika on päättynyt. Opiskelijavalinta on kesken")
        })
      })
    })

    describe("valintatulokset", function() {
      var hakuaikatieto = "Hakuaika on päättynyt. Haun tulokset julkaistaan viimeistään 11. kesäkuuta 2014."

      describe("kun valintatuloksia ei ole julkaistu", function() {
        before(page.applyValintatulosFixtureAndOpen("ei-tuloksia"))
        it("hakemusta ei voi muokata", function () {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length.should.equal(0)
        })

        it("hakuaikatieto näkyy", function() {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal(hakuaikatieto + " Opiskelijavalinta on kesken")
        })

        it("valintatulokset näytetään tilassa \"Opiskelijavalinta kesken\"", function () {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset().length.should.equal(2)
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Opiskelijavalinta kesken')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Opiskelijavalinta kesken')
        })
      })

      describe("kun valinta on kesken ja hakija on 2. varasijalla", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-ylempi-varalla"))

        it("hakemusta ei voi muokata", function () {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length.should.equal(0)
        })

        it("hakuaikatieto näkyy", function() {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal(hakuaikatieto + " Opiskelijavalinta on kesken")
        })

        it("valintatulokset näytetään", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('2. varasijalla. Varasijoja täytetään 26. elokuuta 2014 asti.')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Hyväksytty')
        })

        it("paikka ei ole vastaanotettavissa", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).visible()).to.equal(false)
        })

        describe.skip("hakija on ottanut paikan vastaan ehdollisesti, mutta valinta on kesken", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksynyt_ehdollisesti"))
          it("tieto näytetään oikein", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.match(/^Hakuaika on päättynyt. Haun tulokset julkaistaan viimeistään 11. kesäkuuta 2014. Olet ottanut ehdollisesti vastaan opiskelupaikan \d+\. \w+ 20\d\d\ klo \d+\.\d\d: Salon lukio - Lukio./)
          })
        })

        describe.skip("hakija ei ole ottanut paikkaa vastaan ehdollisesti, mutta valinta on kesken", function() {
          before(page.applyValintatulosFixtureAndOpen("perunut_ehdollisen_vastaanoton"))
          it("tieto näytetään oikein", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.match(/^Hakuaika on päättynyt. Haun tulokset julkaistaan viimeistään 11. kesäkuuta 2014. Opiskelijavalinta on kesken/)
          })
        })
      })

      describe("Hylätty", function() {
        before(page.applyValintatulosFixtureAndOpen("hylatty-julkaistavissa"))

        it("hakuaikatieto näkyy", function() {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal(hakuaikatieto + " Opiskelijavalinta on kesken")
        })

        it("valintatulokset näytetään", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Hylätty')
        })

        it("paikka ei ole vastaanotettavissa", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).visible()).to.equal(false)
        })
      })

      describe("Peruutettu", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-valintatulos-peruutettu"))

        it("hakuaikatieto näkyy", function() {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal(hakuaikatieto + " Opiskelijavalinta on kesken")
        })

        it("valintatulokset näytetään", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].hakukohde).to.equal('Kallion lukio Lukio')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruutettu')
        })

        it("paikka ei ole vastaanotettavissa", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).visible()).to.equal(false)
        })
      })

      describe("Perunut", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-valintatulos-perunut")) // TODO voiko fikstuurin ensimmäinen hakukohde olla "Kesken" tässä tapauksessa?

        it("hakuaikatieto näkyy", function() {
          hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal(hakuaikatieto + " Opiskelijavalinta on kesken")
        })

        it("valintatulokset näytetään", function () {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].hakukohde).to.equal('Kallion lukio Lukio')
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

          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].hakukohde).to.equal('Kallion lukio Lukio')
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruuntunut')
        })

        it("paikka on vastaanotettavissa", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).visible()).to.equal(true)
        })
      })

      describe("kun lopulliset tulokset on julkaistu ja opiskelija on hyväksytty", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kesken-julkaistavissa"))
        it("ilmoitetaan myönnetystä paikasta", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).title()).to.deep.equal([
                "Opiskelupaikka myönnetty: Kallion lukio - Lukion ilmaisutaitolinja"
          ])
        })
      })

      describe("hakijalle tarjotaan paikkaa", function() {
        describe("sitova vastaanotto", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kesken-julkaistavissa"))

          it("vastausaika näkyy", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).info()).to.deep.equal([
                  "Vastaa sitovasti viimeistään 10. tammikuuta 2100 klo 12.00"
            ])
          })

          it("oikeat vaihtoehdot tulevat näkyviin", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).vaihtoehdot()).to.deep.equal([
                  'Otan myönnetyn opiskelupaikan vastaan',
                  'En ota opiskelupaikkaa vastaan'
            ])
          })

          it("nappi on disabloitu", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).confirmButtonEnabled().should.be.false
          })

          describe("valinnan jälkeen", function() {
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VASTAANOTTANUT"))

            it("nappi on enabloitu", function() {
              hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).confirmButtonEnabled().should.be.true
            })

          })

          describe("paikan vastaanottaminen", function() {
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VASTAANOTTANUT"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("vastaanottotieto näkyy", function() {
              hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.match(/^Olet ottanut opiskelupaikan vastaan \d+\. \w+ 20\d\d\ klo \d+\.\d\d: Kallion lukio - Lukion ilmaisutaitolinja./)
            })
          })

          describe("paikan hylkääminen", function() {
            before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kesken-julkaistavissa"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("PERUNUT"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("perumistieto näkyy", function() {
              hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.match(/^Olet perunut hakemuksen \d+\. \w+ 20\d\d\ klo \d+\.\d\d./)
            })
          })
        })

        describe.skip("vastaanotto varsinaisen vastaanootttoajan jälkeen", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty_ilmoitettu_myohaan"))

          it("vastausaika näkyy", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).info()).to.deep.equal([
                  "Vastaa sitovasti viimeistään 2. tammikuuta 2113 klo 17.52"
            ])
          })

          describe("paikan vastaanottaminen", function() {
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VASTAANOTTANUT"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("vastaanottotieto näkyy", function() {
              hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.match(/^Olet ottanut opiskelupaikan vastaan \d+\. \w+ 20\d\d\ klo \d+\.\d\d: Kallion lukio - Lukion ilmaisutaitolinja./)
            })
          })
        })

        describe.skip("vastaanotto varsinaisen vastaanoottoajan jälkeen", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty_ilmoitettu_myohaan"))

          it("vastausaika näkyy", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).info()).to.deep.equal([
                  "Vastaa sitovasti viimeistään 2. tammikuuta 2113 klo 17.52"
            ])
          })

          describe("paikan vastaanottaminen", function() {
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VASTAANOTTANUT"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("vastaanottotieto näkyy", function() {
              hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.match(/^Olet ottanut opiskelupaikan vastaan \d+\. \w+ 20\d\d\ klo \d+\.\d\d: Kallion lukio - Lukion ilmaisutaitolinja./)
            })
          })
        })

        describe.skip("vastaanotto vastaanoottoajan jälkeen", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty_ilmoitettu_myohaan"))

          it("vastausaika näkyy", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).info()).to.deep.equal([
                  "Vastaa sitovasti viimeistään 2. tammikuuta 2113 klo 17.52"
            ])
          })

          describe("paikan vastaanottaminen", function() {
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VASTAANOTTANUT"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("vastaanottotieto näkyy", function() {
              hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.match(/^Olet ottanut opiskelupaikan vastaan \d+\. \w+ 20\d\d\ klo \d+\.\d\d: Kallion lukio - Lukion ilmaisutaitolinja./)
            })
          })
        })

        describe("ehdollinen vastaanotto", function() {
          before(page.applyValintatulosFixtureAndOpen("vastaanotettavissa-ehdollisesti"))
          it("vastausaika näkyy", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).info()).to.deep.equal([
                  "Vastaa sitovasti viimeistään 10. tammikuuta 2100 klo 12.00"
            ])
          })
          it("oikeat vaihtoehdot tulevat näkyviin", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).vaihtoehdot()).to.deep.equal([
                  'Otan myönnetyn opiskelupaikan vastaan',
                  'Otan myönnetyn opiskelupaikan vastaan, jos en saa paikkaa mistään mieluisammasta hakukohteestani',
                  'En ota opiskelupaikkaa vastaan'
            ])
          })

          describe("paikan vastaanottaminen", function() {
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VASTAANOTTANUT"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("vastaanottotieto näkyy", function() {
              expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus()).to.match(/^Olet ottanut opiskelupaikan vastaan \d+\. \w+ 20\d\d\ klo \d+\.\d\d: Kallion lukio - Lukio./)
            })
          })

          describe("paikan vastaanottaminen ehdollisesti", function() {
            before(page.applyValintatulosFixtureAndOpen("vastaanotettavissa-ehdollisesti"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("EHDOLLISESTI_VASTAANOTTANUT"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("vastaanottotieto näkyy", function() {
              hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.match(/^Hakuaika on päättynyt. Haun tulokset julkaistaan viimeistään 11. kesäkuuta 2014. Olet ottanut ehdollisesti vastaan opiskelupaikan \d+\. \w+ 20\d\d\ klo \d+\.\d\d: Kallion lukio - Lukio./)
            })
          })

          describe("paikan hylkääminen", function() {
            before(page.applyValintatulosFixtureAndOpen("vastaanotettavissa-ehdollisesti"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("PERUNUT"))
            before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

            it("perumistieto näkyy", function() {
              hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal("Hakuaika on päättynyt. Haun tulokset julkaistaan viimeistään 11. kesäkuuta 2014. Opiskelijavalinta on kesken")
            })
          })
        })
      })

      describe("hakijalle tarjotaan kahta paikkaa", function() {
        before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kaikkiin"))

        it("oikeat vaihtoehdot tulevat näkyviin", function() {
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotettavia()).to.equal(2)
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).vaihtoehdot()).to.deep.equal([
            'Otan myönnetyn opiskelupaikan vastaan',
            'En ota opiskelupaikkaa vastaan'
          ])
          expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(1).vaihtoehdot()).to.deep.equal([
            'Otan myönnetyn opiskelupaikan vastaan',
            'En ota opiskelupaikkaa vastaan'
          ])
        })

        describe("ensimmäisen paikan vastaanottaminen", function() {
          before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VASTAANOTTANUT"))
          before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send)

          it("vastaanottotieto näkyy", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.match(/^Olet ottanut opiskelupaikan vastaan \d+\. \w+ 20\d\d\ klo \d+\.\d\d: Kallion lukio - Lukion ilmaisutaitolinja./)
          })

          it("kumpikaan paikka ei ole enää vastaanotettavissa", function() {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotettavia()).to.equal(0)
          })
        })

        describe("toisen paikan vastaanottaminen", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty-kaikkiin"))
          before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(1).selectOption("VASTAANOTTANUT"))
          before(hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(1).send)

          it("vastaanottotieto näkyy", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.match(/^Olet ottanut opiskelupaikan vastaan \d+\. \w+ 20\d\d\ klo \d+\.\d\d: Kallion lukio - Lukio./)
          })
        })
      })

      describe("vanhat hakemukset", function() {

        describe("jos ei ole vastaanottotietoa ja ylin hakutoive on hylätty", function() {
          before(page.applyValintatulosFixtureAndOpen("hylatty-julkaistavissa-valmis"))
          it("hakemusta ei voi muokata", function () {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length.should.equal(0)
          })

          it("et saanut paikkaa näkyy", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal("Et saanut opiskelupaikkaa.")
          })

          it("valintatulokset näytetään", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Hylätty')
          })
        })

        describe("jos ei ole vastaanottotietoa ja jos ylin hakutoive on peruttu", function() {
          before(page.applyValintatulosFixtureAndOpen("perunut-julkaistavissa-valmis"))
          it("hakemusta ei voi muokata", function () {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length.should.equal(0)
          })

          it("olet perunut hakemuksen näkyy", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal("Olet perunut hakemuksen.")
          })

          it("valintatulokset näytetään", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Peruit opiskelupaikan')
          })
        })

        describe("jos on ottanut paikan vastaan", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty-vastaanottanut"))
          it("hakemusta ei voi muokata", function () {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length.should.equal(0)
          })

          it("vastaanottotieto näkyy", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal("Olet ottanut opiskelupaikan vastaan 26. elokuuta 2014 klo 19.05: Kallion lukio - Lukion ilmaisutaitolinja.")
          })

          it("valintatulokset näytetään", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].hakukohde).to.equal('Kallion lukio Lukion ilmaisutaitolinja')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[0].tila).to.equal('Hyväksytty')
          })
        })

        describe("jos ei ole ottanut paikkaa vastaan määräaikaan mennessä", function() {
          before(page.applyValintatulosFixtureAndOpen("hyvaksytty-valintatulos-ei-vastaanottanut-maaraaikana"))
          it("hakemusta ei voi muokata", function () {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.preferencesForApplication().length.should.equal(0)
          })

          it("vastaanottotieto näkyy", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.applicationStatus().should.equal("Et ottanut opiskelupaikkaa vastaan määräaikaan mennessä: Kallion lukio - Lukio.")
          })

          it("valintatulokset näytetään", function () {
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].hakukohde).to.equal('Kallion lukio Lukio')
            expect(hakemusYhteishakuKevat2013WithForeignBaseEducation.valintatulokset()[1].tila).to.equal('Peruuntunut')
          })
        })
      })
    })

    describe("hakemuksen tila", function() {
      describe("passiivinen hakemus", function() {
        before(page.applyFixtureAndOpen("passiveApplication"))
        it("hakemus ei näy", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.found().should.be.false
        })
      })

      describe("submitted-tilassa oleva hakemus", function() {
        before(page.applyFixtureAndOpen("submittedApplication"))
        it("hakemusta ei voi muokata", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.preferencesForApplication().length.should.equal(0)
        })

        it("seliteteksti näkyy oikein", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.applicationState().should.equal("Hakemuksesi on vielä käsiteltävänä. Jos haluat muuttaa hakutoiveitasi, yritä myöhemmin uudelleen.")
        })
      })

      describe("post processing -tilassa oleva hakemus", function() {
        before(page.applyFixtureAndOpen("postProcessingFailed"))
        it("hakemusta ei voi muokata", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.preferencesForApplication().length.should.equal(0)
        })

        it("seliteteksti näkyy oikein", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.applicationState().should.equal("Hakemuksesi on vielä käsiteltävänä. Jos haluat muuttaa hakutoiveitasi, yritä myöhemmin uudelleen.")
        })
      })

      describe("incomplete-tilassa oleva hakemus", function() {
        before(ApplicationListPage().resetDataAndOpen)
        it("hakemusta voi muokata", function() {
          hakemusIncomplete.preferencesForApplication().length.should.not.equal(0)
        })

        it("muokkaus ei aiheuta validaatiovirhettä", function() {
          return hakemusIncomplete.getPreference(0).moveDown()
            .then(function() {
              hakemusIncomplete.statusMessage().should.equal("Muista tallentaa muutokset")
            })
        })

        it("tallennus ei aiheuta virhettä", function() {
          return hakemusIncomplete.saveWaitSuccess()
        })
      })
    })

    describe("Virheidenkäsittely", function() {
      before(
        ApplicationListPage().resetDataAndOpen,
        mockAjax.init
      )

      describe("Lisäkysymykset", function() {
        before(function() { mockAjax.respondOnce("POST", "/omatsivut/api/applications/validate/1.2.246.562.11.00000877107", 400, "") })

        it("haun epäonnistuminen näytetään käyttäjälle", function() {
          // In current implementation move will trigger validation API call
          return hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).moveDown()
            .then(wait.until(function() { return hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().length > 0 }))
            .then(function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("Tietojen haku epäonnistui. Yritä myöhemmin uudelleen.")
            })
        })
      })

      describe("Jos tulee validaatiovirhe hakutoiveesta", function() {
        before(function() { mockAjax.respondOnce("POST", "/omatsivut/api/applications/validate/1.2.246.562.11.00000877107", 200, '{"errors":[{"key":"preference1-Koulutus","message":"Testi virhe."}],"questions":[{"title":"Etelä-Savon ammattiopisto,  Otavankatu 4 - Ammattistartti","questions":[{"title":"Hakutoiveet - Hakutoiveet","questions":[{"id":{"phaseId":"hakutoiveet","questionId":"539ecf15e4b09a485311aac9"},"title":"Testikysymys, avaoin vastaus kenttä (pakollinen)?","help":"100 merkkiä max (kolmella kielellä kysymys)","required":true,"maxlength":100,"questionType":"Text"}]}]}],"applicationPeriods":[{"start":1404190831839,"end":4131320431839,"active":true}]}')})
        before(
            hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).moveDown,
            function() { return hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().length > 0 }
        )

        it("näytetään virhe", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).errorMessage().should.equal("Testi virhe.")
        })

        it("uusia kysymyksiä ei näytetä käyttäjälle", function() {
            var questionTitles = hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().titles()
            expect(questionTitles).to.deep.equal([])
        })
      })

      describe("Hakukohteen valinta", function() {

        describe("vanhat hakukohteet katoavat näkyvistä, jos koulutusinformaatio-API ei vastaa", function() {
          before(replacePreference(hakemusKorkeakoulu, 1, "Ahlman"))
          before(function() { mockAjax.respondOnce("GET", "/omatsivut/koulutusinformaatio/koulutukset/1.2.246.562.29.173465377510/1.2.246.562.10.60222091211?uiLang=fi&vocational=true", 400, "")})
          before(function() { return hakemusKorkeakoulu.getPreference(1).selectOpetusPiste("Ahlman", false)() })
          it("näyttää ilmoituksen", function() {
            hakemusKorkeakoulu.getPreference(1).isLoadingHakukohde().should.be.true
          })
          it("lista tyhjennetään", function() {
            expect(hakemusKorkeakoulu.getPreference(1).hakukohdeItems()).to.deep.equal([""])
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
              function() { mockAjax.respondOnce("POST", "/omatsivut/api/applications/vastaanota/1.2.246.562.5.2013080813081926341928/1.2.246.562.11.00000441369", 400, "") },
              hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VASTAANOTTANUT"),
              hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send
            )
          it("virhe näytetään", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).errorText().should.equal("Tallentaminen epäonnistui. Yritä myöhemmin uudelleen.")
          })
          it("nappi on enabloitu", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).confirmButtonEnabled().should.be.true
          })
        })

        describe("kun session on vanhentunut", function() {
          before(
            function() { mockAjax.respondOnce("POST", "/omatsivut/api/applications/vastaanota/1.2.246.562.5.2013080813081926341928/1.2.246.562.11.00000441369", 401, "") },
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VASTAANOTTANUT"),
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send
          )
          it("virhe näytetään", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).errorText().should.equal("Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.")
          })
        })

        describe("kun serveriltä tulee odottamaton virhe", function() {
          before(
            function() { mockAjax.respondOnce("POST", "/omatsivut/api/applications/vastaanota/1.2.246.562.5.2013080813081926341928/1.2.246.562.11.00000441369", 500, "") },
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).selectOption("VASTAANOTTANUT"),
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).send
          )
          it("virhe näytetään", function() {
            hakemusYhteishakuKevat2013WithForeignBaseEducation.vastaanotto(0).errorText().should.equal("Odottamaton virhe. Ota yhteyttä ylläpitoon.")
          })
        })
      })

      describe("Tallennus", function() {
        describe("kun tapahtuu palvelinvirhe", function() {
          before(function() { mockAjax.respondOnce("PUT", "/omatsivut/api/applications/1.2.246.562.11.00000877107", 400, "") })

          it("virheilmoitus näkyy oikein", function() {
            return hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).moveDown()
              .then(hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitError)
              .then(function() {
                hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("Odottamaton virhe. Ota yhteyttä ylläpitoon.")
              })
          })

          it("tallennus toimii uudella yrittämällä", function() {
            return hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitSuccess().then(function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("")
              hakemusNivelKesa2013WithPeruskouluBaseEducation.statusMessage().should.equal("Kaikki muutokset tallennettu")
            })
          })
        })

        describe("kun istunto on vanhentunut", function() {
          before(function() { mockAjax.respondOnce("PUT", "/omatsivut/api/applications/1.2.246.562.11.00000877107", 401, "") })

          it("virheilmoitus näkyy oikein", function() {
            return hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).moveDown()
              .then(hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitError)
              .then(function() {
                hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.")
              })
          })
        })

        describe("Kun tallennuksessa esiintyy odottamaton validointivirhe", function() {
          before(function () {
            mockAjax.respondOnce("PUT", "/omatsivut/api/applications/1.2.246.562.11.00000877107", 400, '[{"key":"asdfqwer", "message": "something went wrong"}]')
          })

          it("virheilmoitus näkyy oikein", function () {
            return hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).moveDown()
              .then(hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitError)
              .then(function () {
                hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("Odottamaton virhe. Ota yhteyttä ylläpitoon.")
              })
          })
        })
      })
    })

    describe("Käyttö näppäimistöllä", function() {
      before(ApplicationListPage().resetDataAndOpen)
      it("tab-nappi toimii oletusjärjestyksessä", function() {
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).arrowUp().isRealButton().should.be.true
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).arrowDown().isRealButton().should.be.true
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).removeButton().isRealButton().should.be.true
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).arrowUp().isFocusableBefore(hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).arrowDown()).should.be.true
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).arrowDown().isFocusableBefore(hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).removeButton()).should.be.true
      })
    })

    describe("Hakutoiveiden validaatio", function() {
      before(
        ApplicationListPage().resetDataAndOpen,
        leaveOnlyOnePreference
      )

      describe("Kun yksi hakukohde valittu", function() {
        it("rivejä ei voi siirtää", function () {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).isMovable().should.be.false
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).isMovable().should.be.false
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(2).isMovable().should.be.false
        })

        it("vain ensimmäinen tyhjä rivi on muokattavissa", function () {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).isEditable().should.be.false
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).isEditable().should.be.true
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(2).isEditable().should.be.false
        })
      })

      describe("kun lisätään hakukohde", function() {
        before(
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).selectOpetusPiste("Ahl"),
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).selectKoulutus(0)
        )

        it("seuraava hakukohde tulee muokattavaksi", function() {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(2).isEditable().should.be.true
        })

        it("lisätty hakutoive on edelleen muokattavissa", function() {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).isEditable().should.be.true
        })

        it("lomake on tallennettavissa", function() {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.isValidationErrorVisible().should.be.false
        })
      })

      describe("kun vain opetuspiste on valittu", function() {
        it("lomaketta ei voi tallentaa", function() {
          var pref = hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1)
          return pref.selectOpetusPiste("Ahl")()
            .then(wait.untilFalse(hakemusNivelKesa2013WithPeruskouluBaseEducation.saveButton().isEnabled))
            .then(function() { hakemusNivelKesa2013WithPeruskouluBaseEducation.isValidationErrorVisible().should.be.true })
        })
      })

      describe("kun valitun opetuspisteen syöttökenttä tyhjennetään", function() {
        before(
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).selectOpetusPiste("Ahl"),
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).searchOpetusPiste("")
        )
        it("lomakkeen voi tallentaa", function() {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.isValidationErrorVisible().should.be.false
          hakemusNivelKesa2013WithPeruskouluBaseEducation.saveButton().isEnabled().should.be.true
        })
      })

      describe("kun valittu opetuspiste siirretään ylöspäin ja syöttökenttä tyhjennetään", function() {
        before(
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).selectOpetusPiste("Ahl"),
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).moveUp,
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).searchOpetusPiste("")
        )
        it("lomaketta ei voi tallentaa", function() {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.isValidationErrorVisible().should.be.true
        })
        it("hakukohde säilyy muokattavana", function() {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).isEditable().should.be.true
        })
      })

      describe("kun valinta jätetään kesken ja siirrytään vaihtamaan toista hakukohdetta", function() {
        before(
          page.resetDataAndOpen,
          leaveOnlyOnePreference, // first two steps to undo previous test case
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).selectOpetusPiste("Ahl"),
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(2).selectOpetusPiste("Turun Kristillinen"),
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).selectOpetusPiste("Turun Kristillinen")
        )
        it("keskeneräinen valinta pysyy muokattavassa tilassa", function() {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).isEditable().should.be.true
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(2).isEditable().should.be.true
        })
        it("lomaketta ei voi tallentaa", function() {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.isValidationErrorVisible().should.be.true
        })
      })

      describe("jos kaksi hakutoivetta on identtisiä", function() {
        before(replacePreference(hakemusNivelKesa2013WithPeruskouluBaseEducation, 1, "Turun Kristillinen"))
        describe("käyttöliittymän tila", function() {
          it("näytetään validointivirhe", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).errorMessage().should.equal("Et voi syöttää samaa hakutoivetta useaan kertaan.")
          })

          it("lomaketta ei voi tallentaa", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.saveButton(0).isEnabled().should.be.false
          })

          it("sovellus ei ole virhetilassa", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("Täytä kaikki tiedot")
          })
        })

        describe("kun ensimmäinen hakukohde poistetaan", function() {
          before(hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).remove)

          it("lomakkeen voi tallentaa", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.saveButton(0).isEnabled().should.be.true
          })

          it("poistetaan validointivirhe", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).errorMessage().should.equal("")
          })
        })
      })
    })

    describe("Kun hakijalla on ulkomaalainen pohjakoulutus", function() {
      before(
        page.resetDataAndOpen,
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).remove,
        hakemusYhteishakuKevat2014WithForeignBaseEducation.saveWaitSuccess,
        replacePreference(hakemusYhteishakuKevat2014WithForeignBaseEducation, 1, "Kallion")
      )

      describe("näyttäminen", function() {
        it("ok", function() {
        })
      })

      describe("tallentaminen", function() {
        before(
          hakemusYhteishakuKevat2014WithForeignBaseEducation.saveWaitSuccess
        )

        it("onnistuu", function() {

        })
      })
    })

    describe("Kun hakijalla on koulutus, joka edellyttää harkinnanvaraisuuskysymyksiin vastausta", function() {
      before(
        page.applyFixtureAndOpen("peruskoulu"),
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).remove,
        hakemusYhteishakuKevat2014WithForeignBaseEducation.saveWaitSuccess,
        replacePreference(hakemusYhteishakuKevat2014WithForeignBaseEducation, 1, "Ahlman", 0)
      )

      it("kysymykset näytetään", function() {
        var questionTitles = hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().titles()
        expect(questionTitles).to.deep.equal([
          'Haetko koulutukseen harkintaan perustuvassa valinnassa?',
          'Työkokemus kuukausina' ])
      })

      describe("Vastaaminen kun haetaan harkintaan perustuvassa valinnassa", function() {
        before(answerDiscretionaryQuestions)

        it("onnistuu", function() {
        })

        describe("Tallentamattoman harkinnanvaraisuustietoja sisältävän rivin siirto", function() {
          before(hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(1).moveUp)
          it("onnistuu", function() {
            hakemusYhteishakuKevat2014WithForeignBaseEducation.saveError().should.equal("")
          })

          it("siirretyn rivin tallentaminen onnistuu", function() {
            return hakemusYhteishakuKevat2014WithForeignBaseEducation.saveWaitSuccess()
          })

          it("tallennetun rivin siirtäminen onnistuu", function() {
            return hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).moveDown().then(function() {
              hakemusYhteishakuKevat2014WithForeignBaseEducation.saveWaitSuccess()
            })
          })

          it("vastaukset siirtyvät listan muokkauksen mukana", function() {
            hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().getAnswer(0).should.equal("Kyllä")
            hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().getAnswer(1).should.equal("Oppimisvaikeudet")
          })
        })
      })

      describe("Hakutoiveen poiston jälkeen", function() {
        before(
          answerDiscretionaryQuestions,
          hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).remove
        )
        it("harkinnanvaraisuustietoja sisältävät vastaukset näkyvät oikein", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().getAnswer(0).should.equal("Kyllä")
          hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().getAnswer(1).should.equal("Oppimisvaikeudet")
        })
      })

      describe("kysymysten ryhmittely", function() {
        before(replacePreference(hakemusYhteishakuKevat2014WithForeignBaseEducation, 1, "Ahlman", 1))
        it("kysymykset ryhmitellään oikein", function() {
          var groupTitles = hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().groupTitles()
          expect(groupTitles).to.deep.equal([
            'Lisäkysymykset: Muut lisäkysymykset: Ahlmanin ammattiopisto - Ammattistartti',
            'Lisäkysymykset: Muut lisäkysymykset: Ahlmanin ammattiopisto - Maatalousalan perustutkinto, pk',
            'Lisäkysymykset: Muut lisäkysymykset:' ])
        })
      })
    })

    describe("Kun hakijalla on koulutus, joka edellyttää urheilijakysymyksiin vastausta", function() {
      before(
        page.resetDataAndOpen,
        hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).remove,
        hakemusYhteishakuKevat2014WithForeignBaseEducation.saveWaitSuccess,
        replacePreference(hakemusYhteishakuKevat2014WithForeignBaseEducation, 1, "Tampere", 0)
      )

      function answerAthleteQuestions() {
        return wait.until(function() { return hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().count() >= 2})()
          .then(function() { hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().enterAnswer(0, "Kyllä") })
          .then(function() { hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().enterAnswer(1, "Ei") })
          .then(wait.until(function() { return hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().count() == 21})).then(function() {
            hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().enterAnswer(7, "Suunnistus")
          })
          .then(wait.until(function() { return hakemusYhteishakuKevat2014WithForeignBaseEducation.saveError() == "" }))
          .then(wait.forAngular)
      }

      it("kysymykset näytetään", function() {
        var questionTitles = hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().titles()
        expect(questionTitles).to.deep.equal([
          'Haetko urheilijan ammatilliseen koulutukseen?',
          'Haluaisitko suorittaa lukion ja/tai ylioppilastutkinnon samaan aikaan kuin ammatillisen perustutkinnon?'
          ])
      })

      describe("Vastaaminen kun haetaan urheilijana", function() {
        before(answerAthleteQuestions)

        it("onnistuu", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().titles()[17].should.equal("Valmennusryhmä")
        })

        describe("Tallentamattoman urheilijatietoja sisältävän rivin siirto", function() {
          before(hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(1).moveUp)
          it("onnistuu", function() {
            hakemusYhteishakuKevat2014WithForeignBaseEducation.saveError().should.equal("")
          })

          it("siirretyn rivin tallentaminen onnistuu", function() {
            return hakemusYhteishakuKevat2014WithForeignBaseEducation.saveWaitSuccess()
          })

          it("tallennetun rivin siirtäminen onnistuu", function() {
            return hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).moveDown().then(function() {
              hakemusYhteishakuKevat2014WithForeignBaseEducation.saveWaitSuccess()
            })
          })

          it("vastaukset siirtyvät listan muokkauksen mukana", function() {
            hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().getAnswer(0).should.equal("Kyllä")
            hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().getAnswer(7).should.equal("Suunnistus")
          })
        })
      })

      describe("Hakutoiveen poiston jälkeen", function() {
        before(
          answerAthleteQuestions,
          hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).remove
        )
        it("urheilijatietoja sisältävät vastaukset näkyvät oikein", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().getAnswer(0).should.equal("Kyllä")
          hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().getAnswer(7).should.equal("Suunnistus")
        })
      })
    })

    describe("Kun hakijalla on koulutus, joka edellyttää sora kysymyksiin vastausta", function() {
      before(
        page.resetDataAndOpen,
        replacePreference(hakemusKorkeakoulu, 1, "Etelä-Savon ammattiopisto,  Otavankatu 4", 1)
      )

      function answerSoraQuestions() {
        return wait.until(function() { return hakemusKorkeakoulu.questionsForApplication().count() >= 2})()
          .then(function() { hakemusKorkeakoulu.questionsForApplication().enterAnswer(0, "Kyllä") })
          .then(function() { hakemusKorkeakoulu.questionsForApplication().enterAnswer(1, "Ei") })
          .then(function() { hakemusKorkeakoulu.questionsForApplication().enterAnswer(2, "Kyllä") })
          .then(wait.until(function() { return hakemusKorkeakoulu.saveError() == "" }))
          .then(wait.forAngular)
      }

      it("kysymykset näytetään", function() {
        var questionTitles = hakemusKorkeakoulu.questionsForApplication().titles()
        expect(questionTitles).to.deep.equal([
          'Tällä alalla on terveydentilavaatimuksia, jotka voivat olla opiskelijaksi ottamisen esteenä. Onko sinulla terveydellisiä tekijöitä, jotka voivat olla opiskelijaksi ottamisen esteenä?',
          'Tässä koulutuksessa opiskelijaksi ottamisen esteenä voi olla aiempi päätös opiskeluoikeuden peruuttamisessa. Onko opiskeluoikeutesi aiemmin peruutettu terveydentilasi tai muiden henkilöiden turvallisuuden vaarantamisen takia?',
          'Haluaisitko suorittaa lukion ja/tai ylioppilastutkinnon samaan aikaan kuin ammatillisen perustutkinnon?'
          ])
      })

      describe("Vastaaminen kun haetaan sora kohteeseen", function() {
        before(answerSoraQuestions)

        it("onnistuu", function() {
        })

        describe("Tallentamattoman soratietoja sisältävän rivin siirto", function() {
          before(hakemusKorkeakoulu.getPreference(1).moveUp)
          it("onnistuu", function() {
            hakemusKorkeakoulu.saveError().should.equal("")
          })

          it("siirretyn rivin tallentaminen onnistuu", function() {
            return hakemusKorkeakoulu.saveWaitSuccess()
          })

          it("tallennetun rivin siirtäminen onnistuu", function() {
            return hakemusKorkeakoulu.getPreference(0).moveDown().then(function() {
              hakemusKorkeakoulu.saveWaitSuccess()
            })
          })

          it("vastaukset siirtyvät listan muokkauksen mukana", function() {
            hakemusKorkeakoulu.questionsForApplication().getAnswer(0).should.equal("Kyllä")
            hakemusKorkeakoulu.questionsForApplication().getAnswer(1).should.equal("Ei")
            hakemusKorkeakoulu.questionsForApplication().getAnswer(2).should.equal("Kyllä")
          })
        })
      })

      describe("Hakutoiveen poiston jälkeen", function() {
        before(
            answerSoraQuestions,
            hakemusKorkeakoulu.getPreference(0).remove
        )
        it("soratietoja sisältävät vastaukset näkyvät oikein", function() {
          hakemusKorkeakoulu.questionsForApplication().getAnswer(0).should.equal("Kyllä")
          hakemusKorkeakoulu.questionsForApplication().getAnswer(1).should.equal("Ei")
          hakemusKorkeakoulu.questionsForApplication().getAnswer(2).should.equal("Kyllä")
        })
      })
    })

    describe("Kun hakijalla on koulutus, joka edellyttää ryhmäkohtaisiinkysymyksiin vastausta", function() {
      before(
        page.resetDataAndOpen,
        replacePreference(hakemusKorkeakoulu, 1, "Diakonia-ammattikorkeakoulu, Järvenpää")
      )

      function answerAoQuestions() {
        return wait.until(function() { return hakemusKorkeakoulu.questionsForApplication().count() >= 2})()
          .then(function() { hakemusKorkeakoulu.questionsForApplication().enterAnswer(0, "8,78") })
          .then(function() { hakemusKorkeakoulu.questionsForApplication().enterAnswer(2, "Ryhmä") })
          .then(hakemusKorkeakoulu.saveWaitSuccess)
      }

      it("kysymykset näytetään", function() {
        var questionTitles = hakemusKorkeakoulu.questionsForApplication().titles()
        expect(questionTitles).to.deep.equal([
          'Lukion päättötodistuksen keskiarvo',
          'Amk-osoiteryhmä',
          'Testikysymys II'
          ])
      })

      describe("Vastaaminen kun haetaan ryhmäkohtaiseen kohteeseen", function() {
        before(answerAoQuestions)

        it("onnistuu", function() {
        })
      })
    })

    describe("Lisäkysymykset", function() {
      describe("Kysymysten suodatus koulutuksen kielen perusteella", function() {
        before(
          page.resetDataAndOpen,
          hakemusLisaKevat2014WithForeignBaseEducation.getPreference(0).remove,
          replacePreference(hakemusLisaKevat2014WithForeignBaseEducation, 0, "Ammattiopisto Livia, fiskeri")
        )

        it("epäoleellisia kysymyksiä ei näytetä", function() {
          var questionTitles = hakemusLisaKevat2014WithForeignBaseEducation.questionsForApplication().titles()
          expect(questionTitles).to.deep.equal([
            'Oletko suorittanut yleisten kielitutkintojen ruotsin kielen tutkinnon kaikki osakokeet vähintään taitotasolla 3?',
            'Oletko suorittanut Valtionhallinnon kielitutkintojen ruotsin kielen suullisen ja kirjallisen tutkinnon vähintään taitotasolla tyydyttävä?' ])
        })

        it("suodatetun vastausjoukon tallentaminen onnistuu", function() {
          hakemusLisaKevat2014WithForeignBaseEducation.questionsForApplication().enterAnswer(0, "Kyllä")
          hakemusLisaKevat2014WithForeignBaseEducation.questionsForApplication().enterAnswer(1, "Ei")
          return hakemusLisaKevat2014WithForeignBaseEducation.saveWaitSuccess()
        })
      })

      var questions1 = [
        'Testikysymys, avaoin vastaus kenttä (pakollinen)?',
        'Valitse kahdesta vaihtoehdosta paremmin itsellesi sopiva?',
        'Mikä tai mitkä ovat mielestäsi parhaiten soveltuvat vastausket?',
        'Etelä-Savon ammattiopisto,  Otavankatu 4',
        'Kotikunta',
        'Minkä koulutuksen olet suorittanut ulkomailla?',
        'Valitse parhaat vaihtoehdot valittavista vaihtoehdoista?',
        'Testivalintakysymys arvosanat',
        'Testikysymys arvosanat, avoin vastaus',
        'Etelä-Savon ammattiopisto,  Otavankatu 4',
        'Testikysymys lupatiedot-kohta avoin vastaus',
        'Testikysymys valitse vaihtoehdoista paras tai parhaat',
        'Testikysymys valitse toinen vaihtoehdoista' ]

      var questions2 = [
        'Miksi haet kymppiluokalle?',
        'Haen ensisijaisesti kielitukikympille?',
        'Turun Kristillinen opisto',
        'Päättötodistuksen kaikkien oppiaineiden keskiarvo?',
        'Päättötodistukseni on' ]


      describe("Lisäkysymyksien näyttäminen", function() {
        before(
          page.resetDataAndOpen,
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).remove,
          hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).remove,
          hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitSuccess
        )

        describe("tallennetut hakutoiveet, joilla on lisäkysymyksiä", function() {
          it("lisäkysymyksiä ei näytetä", function() {
            expect(hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().data()).to.deep.equal([])
          })
        })

        describe("lisätty hakutoive, jolla on lisäkysymyksiä", function() {
          before(replacePreference(hakemusNivelKesa2013WithPeruskouluBaseEducation, 1, "Etelä-Savon ammattiopisto"))

          it("lisäkysymykset näytetään", function() {
            expect(hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().titles()).to.deep.equal(questions1)
          })

          describe("kun hakutoive poistetaan", function() {
            before(hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).remove)
            it("lisäkysymykset piilotetaan", function () {
              expect(hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().titles()).to.deep.equal([])
            })
          })
        })

        describe("lisätty kaksi hakutoivetta, jolla on lisäkysymyksiä", function() {
          before(replacePreference(hakemusNivelKesa2013WithPeruskouluBaseEducation, 1, "Etelä-Savon ammattiopisto"))
          before(replacePreference(hakemusNivelKesa2013WithPeruskouluBaseEducation, 2, "Turun Kristillinen"))

          it("molempien lisäkysymykset näytetään", function() {
            var questionTitles = hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().titles()
            expect(questionTitles).to.deep.equal(questions1.concat(questions2))
          })
        })
      })

      describe("Lisäkysymyksiin vastaaminen", function() {
        before(
          page.resetDataAndOpen,
          replacePreference(hakemusNivelKesa2013WithPeruskouluBaseEducation, 2, "Etelä-Savon ammattiopisto")
        )

        describe("Aluksi", function() {
          it("kysymykset näytetään", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().count().should.equal(13)
            hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().getAnswer(0).should.equal("")
          })
          it("pakolliset kentät korostetaan", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().validationMessages()[0].should.equal("*")
          })
        })

        describe("Kun tallennetaan vastaamatta pakollisiin kysymyksiin", function() {
          before(hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitError)
          it("näytetään tallennusvirhe", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("Ei tallennettu - vastaa ensin kaikkiin lisäkysymyksiin.")
          })

          it("näytetään kaikki validaatiovirheet", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().validationMessageCount().should.equal(11)
          })

          it("näytetään checkboxin minmax-validaatiovirhe", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().validationMessages()[2].should.equal("Virheellinen arvo")
          })

          it("näytetään required-validaatiovirhe", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().validationMessages()[0].should.equal("Pakollinen tieto.")
          })
        })

        describe("Onnistuneen tallennuksen jälkeen", function() {
          before(answerAllQuestions, hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitSuccess)

          describe("Tietokanta", function() {
            it("sisältää tallennetut tiedot", function() {
              return db.getApplications().then(function(data) {
                var answers = findApplicationById(data, hakemusNivelKesa2013WithPeruskouluBaseEducationId).answers
                var questions = hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().data()

                answers.hakutoiveet[questions[0].id].should.equal("tekstivastaus 1")
                answers.hakutoiveet[questions[1].id].should.equal("option_0")
                answers.hakutoiveet[questions[2].id + "-option_0"].should.equal("true")
                answers.hakutoiveet[questions[2].id + "-option_1"].should.equal("true")
                answers.osaaminen[questions[4].id].should.equal("152")
                answers.osaaminen[questions[5].id].should.equal("textarea-vastaus")
                answers.osaaminen[questions[6].id + "-option_0"].should.equal("true")
                answers.osaaminen[questions[6].id + "-option_1"].should.equal("true")
                answers.osaaminen[questions[7].id].should.equal("option_0")
                answers.osaaminen[questions[8].id].should.equal("tekstivastaus 2")
                answers.lisatiedot[questions[10].id].should.equal("tekstivastaus 3")
                answers.lisatiedot[questions[11].id + "-option_0"].should.equal("true")
                answers.lisatiedot[questions[12].id].should.equal("option_0")
              })
            })
          })

          describe("Käyttöliittymän tila", function() {
            it("kysymykset näytetään edelleen", function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().count().should.equal(13)
            })

            it("validaatiovirheitä ei ole", function() {
              _.all(hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().validationMessages(), function(item) {
                return item == ""
              }).should.be.true
            })

            it("aikaleima päivittyy", function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.changesSavedTimestamp().should.not.be.empty
            })

            it("tallennusnappi disabloituu", function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.saveButton().isEnabled().should.be.false
            })

            it("tallennusviesti näytetään", function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("")
              hakemusNivelKesa2013WithPeruskouluBaseEducation.statusMessage().should.equal("Kaikki muutokset tallennettu")
            })
            it("syötetty vastaus näytetään", function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().getAnswer(0).should.equal("tekstivastaus 1")
            })
          })

          describe("Kun ladataan sivu uudelleen", function() {
            before(page.openPage())
            it("valitut hakutoiveet näytetään", function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(2).opetuspiste().should.equal("Etelä-Savon ammattiopisto,  Otavankatu 4")
            })
            it("vastauksia ei näytetä", function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().count().should.equal(0)
            })
          })
          describe("Kun poistetaan hakutoive, tallennetaan ja lisätään se uudelleen", function() {
            before(
              hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(2).remove,
              hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitSuccess,
              page.openPage(),
              replacePreference(hakemusNivelKesa2013WithPeruskouluBaseEducation, 2, "Etelä-Savon ammattiopisto")
            )
            it("hakutoiveeseen liittyvien lisäkysymysten aiemmat vastaukset hävitetään", function() {
              hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().count().should.equal(13)
              hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().getAnswer(0).should.equal("")
            })
          })
        })

        describe("Kun poistetaan lisätty hakutoive, jolla lisäkysymyksiä, joihin ei vastattu", function() {
          before(
            hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(2).remove,
            hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitSuccess
          )

          it("Tallennus onnistuu", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("")
            hakemusNivelKesa2013WithPeruskouluBaseEducation.statusMessage().should.equal("Kaikki muutokset tallennettu")
          })
        })

        describe("Kun poistetaan lisätty hakutoive, jolla lisäkysymyksiä, joihin vastattiin", function() {
          before(
            page.resetDataAndOpen,
            replacePreference(hakemusNivelKesa2013WithPeruskouluBaseEducation, 2, "Etelä-Savon ammattiopisto"),
            answerAllQuestions,
            hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(2).remove,
            hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitSuccess
          )

          it("Tallennus onnistuu", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.saveError().should.equal("")
            hakemusNivelKesa2013WithPeruskouluBaseEducation.statusMessage().should.equal("Kaikki muutokset tallennettu")
          })
        })

        describe("Kun vastataan tallentamatta ja muokataan hakutoiveita", function() {
          before(
            page.resetDataAndOpen,
            replacePreference(hakemusNivelKesa2013WithPeruskouluBaseEducation, 2, "Etelä-Savon ammattiopisto"),
            answerAllQuestions,
            hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(1).remove,
            hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(2).searchOpetusPiste("qwer")
          )

          after(page.resetDataAndOpen)

          it("kysymykset pysyvät näkyvillä, jos muutoksilla ei vaikutusta kysymyksiin", function() {
            var questionTitles = hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().titles()
            expect(questionTitles).to.deep.equal(questions1)
          })

          it("vastaus pysyy näkyvillä, jos muutoksilla ei vaikutusta kysymyksiin", function() {
            hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().getAnswer(0).should.equal("tekstivastaus 1")
          })

        })
        function answerAllQuestions() {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(0, "tekstivastaus 1")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(1, "Vaihtoehto x 1")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(2, "Vaihtoehto 1")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(2, "Vaihtoehto 2")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(4, "Isokyrö")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(5, "textarea-vastaus")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(6, "Vaihtoehto yyy 1")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(6, "Vaihtoehto yyy 2")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(7, "Vaihtoehto arvosanat 1")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(8, "tekstivastaus 2")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(10, "tekstivastaus 3")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(11, "Vaihtoehto zzzz 1")
          hakemusNivelKesa2013WithPeruskouluBaseEducation.questionsForApplication().enterAnswer(12, "Vaihttoehto yksi")
          return hakemusNivelKesa2013WithPeruskouluBaseEducation.waitValidationOk()
        }
      })
    })

    describe("Hakemuslistauksen muokkaus", function () {
      before(
        page.applyFixtureAndOpen("peruskoulu"),
        replacePreference(hakemusYhteishakuKevat2014WithForeignBaseEducation, 1, "Diakonia-ammattikorkeakoulu, Helsingin toimipiste"),
        replacePreference(hakemusYhteishakuKevat2014WithForeignBaseEducation, 2, "Ahlman", 1),
        answerDiscretionaryQuestions,
        hakemusYhteishakuKevat2014WithForeignBaseEducation.saveWaitSuccess
      )

      endToEndTest("järjestys", "järjestys muuttuu nuolta klikkaamalla", function () {
        return hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(1).moveDown()
      }, function (dbStart, dbEnd) {
        dbStart.hakutoiveet[0].should.deep.equal(dbEnd.hakutoiveet[0])
        dbStart.hakutoiveet[1].should.deep.equal(dbEnd.hakutoiveet[2])
        dbStart.hakutoiveet[2].should.deep.equal(dbEnd.hakutoiveet[1])
      })

      endToEndTest("poisto", "hakutoiveen voi poistaa", function () {
        return hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).remove()
      }, function (dbStart, dbEnd) {
        dbEnd.hakutoiveet.should.deep.equal(_.flatten([_.rest(dbStart.hakutoiveet), {}]))
      })
      endToEndTest("lisäys", "hakutoiveen voi lisätä", replacePreference(hakemusYhteishakuKevat2014WithForeignBaseEducation, 2, "Turun"), function(dbStart, dbEnd) {
        var newOne = { 'Opetuspiste-id': '1.2.246.562.10.49832689993',
            Opetuspiste: 'Turun Kristillinen opisto',
            Koulutus: 'Kymppiluokka',
            'Koulutus-id-kaksoistutkinto': 'false',
            'Koulutus-id-sora': 'false',
            'Koulutus-id-vocational': 'true',
            'Koulutus-id-attachments': 'false',
            'Koulutus-id-lang': 'FI',
            'Koulutus-id-aoIdentifier': '019',
            'Koulutus-id-athlete': 'false',
            'Koulutus-educationDegree': '22',
            'Koulutus-id': '1.2.246.562.14.2014032812530780195965',
            'Koulutus-id-educationcode': 'koulutus_020075' }
        dbEnd.hakutoiveet.should.deep.equal(dbStart.hakutoiveet.slice(0, 2).concat(newOne).concat({}).concat({}))
      })
    })

    describe("Näytä hakemus -linkki", function() {
      describe("Kun hakemusta ei ole muokattu", function() {
        it("linkki avaa esikatselusivun", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.previewLink().text().should.equal("Näytä koko hakemus")
          hakemusYhteishakuKevat2014WithForeignBaseEducation.previewLink().hasClass("disabled").should.equal(false)
          hakemusYhteishakuKevat2014WithForeignBaseEducation.previewLink().attr("href").should.equal("/omatsivut/api/applications/preview/1.2.246.562.11.00000441368")
        })
      })
      describe("Kun hakemusta on muokattu", function() {
        before(hakemusYhteishakuKevat2014WithForeignBaseEducation.getPreference(0).remove)
        it("linkki on disabloitu", function() {
          hakemusYhteishakuKevat2014WithForeignBaseEducation.previewLink().hasClass("disabled").should.equal(true)
        })
      })
    })

    describe("Liitepyyntölinkki", function() {
      describe("Jos tallennettuun hakemukseen liittyy lisätietopyyntöjä", function() {
        before(
            replacePreference(hakemusKorkeakoulu, 1, "Diakonia-ammattikorkeakoulu, Järvenpää"),
            (function() { hakemusKorkeakoulu.questionsForApplication().enterAnswer(2, "Vastaus") } ),
            hakemusKorkeakoulu.saveWaitSuccess
        )
        it("liitepyyntö näytetään", function() {
          hakemusKorkeakoulu.calloutText().should.equal("Muista lähettää hakemuksen liitteet.")
        })
      })

      describe("Jos tallennettuun hakemukseen ei liity lisätietopyyntöjä", function() {
        before(hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).moveDown, hakemusNivelKesa2013WithPeruskouluBaseEducation.saveWaitSuccess)
        it("liitepyyntöjä ei näytetä", function() {
          hakemusNivelKesa2013WithPeruskouluBaseEducation.calloutText().should.equal("")
        })
      })
    })
  })

  function replacePreference(hakemus, index, searchString, koulutusIndex) {
    koulutusIndex = koulutusIndex || 0
    return function() {
      var pref = hakemus.getPreference(index)
      return pref.remove()
        .then(pref.selectOpetusPiste(searchString))
        .then(pref.selectKoulutus(koulutusIndex))
    }
  }

  function leaveOnlyOnePreference() {
    return hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).remove()
      .then(function() { return hakemusNivelKesa2013WithPeruskouluBaseEducation.getPreference(0).remove() })
  }

  function endToEndTest(descName, testName, manipulationFunction, dbCheckFunction) {
    describe(descName, function() {
      var applicationsBefore, applicationsAfter;
      before(
        function() {
          return db.getApplications().then(function(apps) {
            applicationsBefore = apps
          })
        },
        manipulationFunction,
        hakemusYhteishakuKevat2014WithForeignBaseEducation.saveWaitSuccess,
        function(done) {
          db.getApplications().then(function(apps) {
            applicationsAfter = apps
            done()
          })
        }
      )
      it(testName, function() {
        dbCheckFunction(findApplicationById(applicationsBefore, hakemusYhteishakuKevat2014WithForeignBaseEducationId), findApplicationById(applicationsAfter, hakemusYhteishakuKevat2014WithForeignBaseEducationId))
      })
    })
  }

  function answerDiscretionaryQuestions() {
    return wait.until(function() { return hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().count() >= 2})()
      .then(function() { hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().enterAnswer(0, "Kyllä") })
      .then(wait.until(function() { return hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().count() == 3})).then(function() {
        hakemusYhteishakuKevat2014WithForeignBaseEducation.questionsForApplication().enterAnswer(1, "Oppimisvaikeudet")
      })
      .then(wait.until(function() { return hakemusYhteishakuKevat2014WithForeignBaseEducation.saveError() == "" }))
      .then(wait.forAngular)
  }

  function findApplicationById(applications, id) {
    return _.find(applications, function(a) { return a.oid == id })
  }

  function diffKeys(arr1, arr2) {
    return _.flatten([_.difference(arr1, arr2), _.difference(arr2, arr1)])
  }
})()