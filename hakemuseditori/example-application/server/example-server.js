#!/usr/bin/env node
var express = require('express')
var port = process.env.PORT || 3000
var app = express()
var request = require('request')
var opetuspisteet = require('./opetuspisteet')
var koulutukset = require('./koulutukset')

app.use(express.compress())
app.use(express.json())
app.use('/', express.static(__dirname + '/..'))
app.use('/', express.static(__dirname + '/../../dev'))
app.get('/api/opetuspisteet/:query', function(req, res) {
  var query = req.params.query.substring(0, 1)
  res.send(opetuspisteet[query])
})
app.get('/api/koulutukset/:asId/:opetuspisteId', function(req, res) {
  var opetuspisteId = req.params.opetuspisteId
  res.send(koulutukset[opetuspisteId])
})
app.listen(port)
console.log("hakemuseditori example app running on http://localhost:" + port + "/")
