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

  describe("Hakemuksen muokkaus väärällä tokenilla", function() {
    before(
      page.openPage("väärä")
    )

    it("näytetään virhe", function() {
      expect(page.alertMsg()).to.equal('Hakemustasi ei voida näyttää koska linkki on virheellinen.' )
    })
  })

  describe('Hakemuksen muokkaus, kun hakemusta ei löydy', function () {
    before(
      page.openPage("1.2.246.562.11.0")
    )

    it("näytetään ilmoitus", function() {
      expect(page.alertMsg()).to.equal('Hakemuksesi ei ole enää aktiivinen.' )
    })
  })

  describe('Hakemuksen muokkaus', function () {
    before(
      page.applyFixtureAndOpen({token: hakemusKorkeakouluKevatWithJazzId})
    )

    describe("Hakemuksen tietojen näyttäminen", function() {
      it('näkyy oikea hakemus', function () {
        expect(page.getApplication().name()).to.equal('Korkeakoulujen yhteishaku kevät 2015')
      })
    })
  })
})()
