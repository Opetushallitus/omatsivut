var expect = chai.expect
var iframe = window.parent.frames[0]
$ = function (selector) {
    return iframe.jQuery(selector, iframe.document)
}
iframe.localStorage.clear()

describe('hakemuslistaus', function () {

    it('hakemuslistassa on hakemus', function () {
        expect($('#hakemus-list')).to.be.visible
    })
})
