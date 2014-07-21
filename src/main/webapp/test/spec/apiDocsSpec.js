(function () {
  describe('api-docs', function () {
    var page = ApiDocsPage();

    before(
      page.openPage
    )

    describe("näyttäminen", function() {
      it('apissa on kolme endpointtia', function () {
        expect(page.endpoints().length).to.equal(3)
      })

      it('kaikilla endpointeilla on kuvaus', function () {
        var endpoints = page.endpoints()
        for (i = 0; i < endpoints.length; i++) {
          expect(endpoints[i].description).not.to.equal("")
        }        
      })
    })
  })
})()