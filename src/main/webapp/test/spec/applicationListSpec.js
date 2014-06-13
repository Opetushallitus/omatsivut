var expect = chai.expect
var testFrame = window.parent.frames[0]
$ = function (selector) {
    return testFrame.jQuery(selector, testFrame.document)
}
testFrame.localStorage.clear()

goToUrl = function(url) {
    testFrame.location.replace(url)
}

describe('hakemuslistaus', function () {
    goToUrl('/?hetu=010101-123N')

    it('hakemuslistassa on hakemus', function () {
        expect($('#hakemus-list')).to.be.visible
    })
})
