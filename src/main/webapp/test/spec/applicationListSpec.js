var expect = chai.expect
var testFrame = window.parent.frames[0]
$ = function (selector) {
    return testFrame.jQuery(selector, testFrame.document)
}
testFrame.localStorage.clear()

describe('hakemuslistaus', function () {

    it('hakemuslistassa on hakemus', function () {
        expect($('#hakemus-list')).to.be.visible
    })
})
