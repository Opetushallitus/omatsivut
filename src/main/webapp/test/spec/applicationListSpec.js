describe('hakemuslistaus', function () {
    before(ApplicationListPage().openPage)

    it('hakemuslistassa on hakemus henkilölle 010101-123N', function () {
        expect(ApplicationListPage().applications()).to.deep.equal([{
            applicationSystemName: "MUUTettu TAAS Ammatillisen koulutuksen ja lukiokoulutuksen kevään 2014 yhteishaku"
        }])
    })

    it("henkilön 010101-123N hakutoiveet ovat näkyvissä", function() {
        expect(ApplicationListPage().preferencesForApplication(0).map(function(item) { return item.data()})).to.deep.equal([
            {
                "hakutoive.Opetuspiste": "Ahlmanin ammattiopisto",
                "hakutoive.Koulutus": "Eläintenhoidon koulutusohjelma, pk (Maatalousalan perustutkinto)"
            },
            {
                "hakutoive.Opetuspiste": "Ammatti-instituutti Iisakki",
                "hakutoive.Koulutus": "Kone- ja metallialan perustutkinto, pk"
            },
            {
                "hakutoive.Koulutus": "Musiikin koulutusohjelma, pk (Musiikkialan perustutkinto)",
                "hakutoive.Opetuspiste": "Ammattiopisto Lappia,  Pop & Jazz Konservatorio Lappia"
            }
        ]);
    })

    it("järjestys muuttuu nuolta klikkaamalla", function(done) {
        function getPreference(index) { return ApplicationListPage().preferencesForApplication(0)[index]};
        var pref1 = getPreference(0)
        var pref2 = getPreference(1)
        pref1.arrowDown().click()
        wait.until(function() {
            return pref1.element().index() === 1 && pref2.element().index() === 0 && pref1.number().text() === "2."
        })(done)
    })
})
