describe('hakemuslistaus', function () {
    before(openPage('/?hetu=010101-123N', ApplicationListPage().visible))

    it('hakemuslistassa on hakemus', function () {
        expect(ApplicationListPage().applications()).to.deep.equal([{
            applicationSystemName: "MUUTettu TAAS Ammatillisen koulutuksen ja lukiokoulutuksen kevään 2014 yhteishaku"
        }])
    })
})
