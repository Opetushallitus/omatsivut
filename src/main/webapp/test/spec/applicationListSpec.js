describe('hakemuslistaus', function () {
    before(ApplicationListPage().openPage)

    it('hakemuslistassa on hakemus henkilölle 010101-123N', function () {
        expect(ApplicationListPage().applications()).to.deep.equal([{
            applicationSystemName: "MUUTettu TAAS Ammatillisen koulutuksen ja lukiokoulutuksen kevään 2014 yhteishaku"
        }])
    })
})
