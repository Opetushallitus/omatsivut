var Hakemus = require('./hakemuseditori').Hakemus

module.exports = function(app) {
  app.controller("MuokattavaHakemus", ["$scope", function($scope) {
    $scope.application = new Hakemus(muokattavaHakemusJson)
  }])

  app.controller("VastaanotettavaHakemus", ["$scope", function($scope) {
    $scope.application = new Hakemus(vastaanotettavaHakemusJson)
  }])

  app.controller("VastaanotettavaHakemusEhdollisestiVastaanotettu", ["$scope", function($scope) {
    $scope.application = new Hakemus(vastaanotettavaHakemusEhdollisestiVastaanotettuJson)
  }])


  var muokattavaHakemusJson = {
    "hakemus": {
      "oid": "1.2.246.562.11.00000877699",
      "received": 1407837465105,
      "updated": 1422531129057,
      "state": {"id": "ACTIVE"},
      "hakutoiveet": [{"hakemusData": {"Opetuspiste-id": "1.2.246.562.10.64213824028", "amkLiite": "true", "Opetuspiste": "Diakonia-ammattikorkeakoulu, Helsingin toimipiste", "Koulutus": "Terveydenhoitaja (AMK), monimuotototeutus", "Koulutus-id-attachmentgroups": "1.2.246.562.28.90373737623", "Koulutus-id-kaksoistutkinto": "false", "Koulutus-id-sora": "false", "Koulutus-id-vocational": "false", "Koulutus-id-lang": "FI", "Koulutus-id-aoIdentifier": "", "Koulutus-id-athlete": "false", "Koulutus-educationDegree": "koulutusasteoph2002_62", "Koulutus-id": "1.2.246.562.20.656632485510", "Opetuspiste-id-parents": "1.2.246.562.10.64213824028,1.2.246.562.10.64582714578,1.2.246.562.10.50822930082,1.2.246.562.10.00000000001", "Koulutus-id-educationcode": "koulutus_671103"}}, {}, {}, {}, {}, {}],
      "haku": {"oid": "1.2.246.562.29.173465377510", "name": "Korkeakoulujen yhteishaku syksy 2014", "applicationPeriods": [{"id": "5474", "start": 1404190831839, "end": 4131320431839, "active": true}], "tyyppi": "YHTEISHAKU", "korkeakouluhaku": true, "usePriority": true, "aikataulu": {}, "active": true},
      "educationBackground": {"baseEducation": null, "vocational": true},
      "answers": {
        "henkilotiedot": {"onkoSinullaSuomalainenHetu": "true", "syntymaaika": "01.01.1901", "aidinkieli": "FI", "sukupuoli": "2", "asuinmaa": "FIN", "lahiosoite": "Testikatu 4", "Sukunimi": "Testi", "koulusivistyskieli": "FI", "Postinumero": "00100", "preference1-amkLiite": "true", "kansalaisuus": "FIN", "Henkilotunnus_digest": "a8d9888c08032e0d42afe3a0700b78bd9a29be24556068838343e702908a1ab5", "Kutsumanimi": "Asia", "turvakielto": "false", "Etunimet": "Asia kas", "kotikunta": "020", "Sähköposti": "", "Henkilotunnus": "010100A939R", "matkapuhelinnumero1": ""},
        "koulutustausta": {"pohjakoulutus_yo_vuosi": "2012", "pohjakoulutus_muu": "true", "suoritusoikeus": "false", "preference1-amkLiite": "true", "pohjakoulutus_muu_vuosi": "2014", "pohjakoulutus_muu_kuvaus": "foo", "aiempitutkinto": "false", "pohjakoulutus_yo": "true", "pohjakoulutus_yo_tutkinto": "eb"},
        "osaaminen": {"lukion-paattotodistuksen-keskiarvo": "8,79", "preference1-amkLiite": "true"},
        "lisatiedot": {"asiointikieli": "suomi", "preference1-amkLiite": "true"},
        "hakutoiveet": {
          "preference1-Koulutus-id-vocational": "false",
          "preference1-Koulutus-id-athlete": "false",
          "preference1-Opetuspiste-id": "1.2.246.562.10.64213824028",
          "preference1-Koulutus-id": "1.2.246.562.20.656632485510",
          "preference5-Opetuspiste-id": "",
          "preference3-Koulutus-id": "",
          "preference1-Koulutus-id-lang": "FI",
          "preference1-Koulutus-id-kaksoistutkinto": "false",
          "preference4-Koulutus-id": "",
          "preference6-Opetuspiste-id": "",
          "preference1-Koulutus-id-attachmentgroups": "1.2.246.562.28.90373737623",
          "preference3-Opetuspiste-id": "",
          "preference6-Koulutus-id": "",
          "preference1-Koulutus-id-aoIdentifier": "",
          "preference2-Koulutus-id": "",
          "preference1-Koulutus-id-sora": "false",
          "preference1-amkLiite": "true",
          "preference5-Koulutus-id": "",
          "preference4-Opetuspiste-id": "",
          "preference1-Koulutus": "Terveydenhoitaja (AMK), monimuotototeutus",
          "preference1-Opetuspiste-id-parents": "1.2.246.562.10.64213824028,1.2.246.562.10.64582714578,1.2.246.562.10.50822930082,1.2.246.562.10.00000000001",
          "preference2-Opetuspiste-id": "",
          "preference1-Koulutus-educationDegree": "koulutusasteoph2002_62",
          "preference1-Koulutus-id-educationcode": "koulutus_671103",
          "preference1-Opetuspiste": "Diakonia-ammattikorkeakoulu, Helsingin toimipiste"
        }
      },
      "postOffice": "Helsinki",
      "requiresAdditionalInfo": false,
      "hasForm": true
    }, "errors": [], "questions": [{"title": "Diakonia-ammattikorkeakoulu, Helsingin toimipiste - Terveydenhoitaja (AMK), monimuotototeutus", "questions": [{"title": "Osaaminen - Osaaminen", "questions": [{"id": {"phaseId": "osaaminen", "questionId": "lukion-paattotodistuksen-keskiarvo"}, "title": "Lukion päättötodistuksen keskiarvo", "help": "Kirjoita keskiarvosi muodossa x,xx.", "verboseHelp": "", "required": true, "maxlength": 5, "questionType": "Text"}]}]}], "tulosOk": true
  }
  var vastaanotettavaHakemusJson = {

    "hakemus": {
      "oid": "1.2.246.562.11.00000441369",
      "received": 1375250400000,
      "updated": 1433929240327,
      "state": {
        "id": "HAKUKAUSIPAATTYNYT",
        "valintatulos": {
          "hakemusOid": "1.2.246.562.11.00000441369",
          "aikataulu": {"vastaanottoEnd": "2100-01-10T10:00:00Z", "vastaanottoBufferDays": 14},
          "hakutoiveet": [{"hakukohdeOid": "1.2.246.562.5.72607738902", "hakukohdeNimi": "Lukion ilmaisutaitolinja", "tarjoajaOid": "1.2.246.562.10.591352080610", "tarjoajaNimi": "Kallion lukio", "valintatila": "HYVAKSYTTY", "vastaanottotila": "KESKEN", "ilmoittautumistila": {"ilmoittautumisaika": {"loppu": "2100-01-10T21:59:59Z"}, "ilmoittautumistapa": {"nimi": {"fi": "Oili", "sv": "Oili", "en": "Oili"}, "url": "/oili/"}, "ilmoittautumistila": "EI_TEHTY", "ilmoittauduttavissa": false}, "vastaanotettavuustila": "VASTAANOTETTAVISSA_SITOVASTI", "vastaanottoDeadline": "2100-01-10T10:00:00Z", "jonosija": 1, "varasijojaKaytetaanAlkaen": "2014-08-26T16:05:23Z", "varasijojaTaytetaanAsti": "2014-08-26T16:05:23Z", "tilanKuvaukset": {}}, {
            "hakukohdeOid": "1.2.246.562.5.16303028779",
            "hakukohdeNimi": "Lukio",
            "tarjoajaOid": "1.2.246.562.10.455978782510",
            "tarjoajaNimi": "Salon lukio",
            "valintatila": "PERUUNTUNUT",
            "vastaanottotila": "KESKEN",
            "ilmoittautumistila": {"ilmoittautumisaika": {"loppu": "2100-01-10T21:59:59Z"}, "ilmoittautumistapa": {"nimi": {"fi": "Oili", "sv": "Oili", "en": "Oili"}, "url": "/oili/"}, "ilmoittautumistila": "EI_TEHTY", "ilmoittauduttavissa": false},
            "vastaanotettavuustila": "EI_VASTAANOTETTAVISSA",
            "tilanKuvaukset": {}
          }]
        }
      },
      "hakutoiveet": [{"hakemusData": {"Opetuspiste-id": "1.2.246.562.10.591352080610", "Opetuspiste": "Kallion lukio", "Koulutus": "Lukion ilmaisutaitolinja", "Koulutus-id-kaksoistutkinto": "false", "discretionary": "true", "Koulutus-id-sora": "false", "Koulutus-id-vocational": "false", "Koulutus-id-lang": "FI", "Koulutus-id-aoIdentifier": "803", "Koulutus-id-athlete": "false", "Koulutus-educationDegree": "", "discretionary-follow-up": "todistustenpuuttuminen", "Koulutus-id": "1.2.246.562.5.72607738902", "Opetuspiste-id-parents": "1.2.246.562.10.591352080610,1.2.246.562.10.69408568777,1.2.246.562.10.346830761110,1.2.246.562.10.00000000001", "Koulutus-id-educationcode": "koulutus_301101"}, "hakuaikaId": "5474"}, {
        "hakemusData": {
          "Opetuspiste-id": "1.2.246.562.10.455978782510",
          "Opetuspiste": "Salon lukio",
          "Koulutus": "Lukio",
          "Koulutus-id-kaksoistutkinto": "false",
          "discretionary": "true",
          "Koulutus-id-sora": "false",
          "Koulutus-id-vocational": "false",
          "Koulutus-id-lang": "FI",
          "Koulutus-id-aoIdentifier": "000",
          "Koulutus-id-athlete": "false",
          "Koulutus-educationDegree": "",
          "discretionary-follow-up": "todistustenpuuttuminen",
          "Koulutus-id": "1.2.246.562.5.16303028779",
          "Opetuspiste-id-parents": "1.2.246.562.10.455978782510,1.2.246.562.10.67667811417,1.2.246.562.10.98501788818,1.2.246.562.10.00000000001",
          "Koulutus-id-educationcode": "koulutus_301101"
        }, "hakuaikaId": "5474"
      }, {}, {}, {}],
      "haku": {"oid": "1.2.246.562.5.2013080813081926341928", "name": "Korkeakoulujen yhteishaku syksy 2014", "applicationPeriods": [{"id": "5474", "start": 1372654831000, "end": 1375279200000, "active": false}], "tyyppi": "YHTEISHAKU", "korkeakouluhaku": true, "usePriority": true, "aikataulu": {"julkistus": {"start": 1402462344918, "end": 1402462344918}, "hakukierrosPaattyy": 1513894390000}, "active": false},
      "educationBackground": {"baseEducation": "0", "vocational": true},
      "answers": {
        "henkilotiedot": {"onkoSinullaSuomalainenHetu": "true", "syntymaaika": "01.01.1901", "aidinkieli": "FI", "sukupuoli": "1", "asuinmaa": "FIN", "lahiosoite": "foobartie 1", "Sukunimi": "Testaaja", "Postinumero": "00100", "kansalaisuus": "FIN", "Henkilotunnus_digest": "a8d9888c08032e0d42afe3a0700b78bd9a29be24556068838343e702908a1ab5", "Kutsumanimi": "Teemu", "turvakielto": "false", "Etunimet": "Teemu", "kotikunta": "091", "Sähköposti": "", "Henkilotunnus": "010100A939R", "matkapuhelinnumero1": ""},
        "koulutustausta": {"POHJAKOULUTUS": "0"},
        "osaaminen": {},
        "lisatiedot": {"asiointikieli": "suomi"},
        "hakutoiveet": {
          "preference1-Koulutus-id-vocational": "false",
          "preference5-Koulutus-id-sora": "",
          "preference5-Opetuspiste": "",
          "preference3-Koulutus-id-vocational": "",
          "preference4-Koulutus-id-aoIdentifier": "",
          "preference1-Koulutus-id-athlete": "false",
          "preference3-Opetuspiste": "",
          "preference1-Opetuspiste-id": "1.2.246.562.10.591352080610",
          "preference2-Opetuspiste": "Salon lukio",
          "preference1-Koulutus-id": "1.2.246.562.5.72607738902",
          "preference5-Opetuspiste-id": "",
          "preference3-Koulutus-id": "",
          "preference2-Koulutus-id-educationcode": "koulutus_301101",
          "preference3-Koulutus-id-athlete": "",
          "preference1-Koulutus-id-lang": "FI",
          "preference1-Koulutus-id-kaksoistutkinto": "false",
          "preference3-Koulutus-id-educationcode": "",
          "preference4-Koulutus-id": "",
          "preference2-Koulutus-id-athlete": "false",
          "preference4-Koulutus-id-lang": "",
          "preference5-Koulutus-id-lang": "",
          "preference4-Koulutus-id-educationcode": "",
          "preference3-Koulutus-id-kaksoistutkinto": "",
          "preference4-discretionary": "",
          "preference5-discretionary-follow-up": "",
          "preference4-Koulutus-id-vocational": "",
          "preference4-Opetuspiste": "",
          "preference2-Koulutus-id-vocational": "false",
          "preference3-Opetuspiste-id": "",
          "preference4-Koulutus-id-athlete": "",
          "preference4-Koulutus": "",
          "preference1-Koulutus-id-aoIdentifier": "803",
          "preference2-Koulutus-id-sora": "false",
          "preference2-discretionary-follow-up": "todistustenpuuttuminen",
          "preference2-Koulutus-id": "1.2.246.562.5.16303028779",
          "preference4-Koulutus-id-sora": "",
          "preference2-Koulutus-id-lang": "FI",
          "preference1-Koulutus-id-sora": "false",
          "preference4-Koulutus-id-kaksoistutkinto": "",
          "preference5-Koulutus-id": "",
          "preference5-discretionary": "",
          "preference5-Koulutus-id-aoIdentifier": "",
          "preference5-Koulutus-id-educationcode": "",
          "preference2-discretionary": "true",
          "preference2-Koulutus-id-aoIdentifier": "000",
          "preference2-Koulutus": "Lukio",
          "preference2-Opetuspiste-id-parents": "1.2.246.562.10.455978782510,1.2.246.562.10.67667811417,1.2.246.562.10.98501788818,1.2.246.562.10.00000000001",
          "preference3-discretionary": "",
          "preference4-Opetuspiste-id": "",
          "preference2-Koulutus-id-kaksoistutkinto": "false",
          "preference4-discretionary-follow-up": "",
          "preference5-Koulutus-id-vocational": "",
          "preference3-Koulutus-educationDegree": "",
          "preference2-Koulutus-educationDegree": "",
          "preference1-Koulutus": "Lukion ilmaisutaitolinja",
          "preference5-Koulutus-id-kaksoistutkinto": "",
          "preference3-discretionary-follow-up": "",
          "preference3-Opetuspiste-id-parents": "",
          "preference1-Opetuspiste-id-parents": "1.2.246.562.10.591352080610,1.2.246.562.10.69408568777,1.2.246.562.10.346830761110,1.2.246.562.10.00000000001",
          "preference5-Koulutus-educationDegree": "",
          "preference2-Opetuspiste-id": "1.2.246.562.10.455978782510",
          "preference1-discretionary": "true",
          "preference1-discretionary-follow-up": "todistustenpuuttuminen",
          "preference5-Koulutus-id-athlete": "",
          "preference3-Koulutus-id-sora": "",
          "preference1-Koulutus-educationDegree": "",
          "preference1-Koulutus-id-educationcode": "koulutus_301101",
          "preference4-Koulutus-educationDegree": "",
          "preference3-Koulutus-id-lang": "",
          "preference1-Opetuspiste": "Kallion lukio",
          "preference3-Koulutus": "",
          "preference3-Koulutus-id-aoIdentifier": ""
        }
      },
      "postOffice": "Helsinki",
      "requiresAdditionalInfo": false,
      "hasForm": true
    }, "errors": [], "questions": [], "tulosOk": true

  }
  var vastaanotettavaHakemusEhdollisestiVastaanotettuJson = {
    "hakemus": {
      "oid": "1.2.246.562.11.00000441369",
      "received": 1375250400000,
      "updated": 1433929240327,
      "state": {
        "id": "HAKUKAUSIPAATTYNYT",
        "valintatulos": {
          "hakemusOid": "1.2.246.562.11.00000441369",
          "aikataulu": {"vastaanottoEnd": "2100-01-10T10:00:00Z", "vastaanottoBufferDays": 14},
          "hakutoiveet": [{"hakukohdeOid": "1.2.246.562.5.72607738902", "hakukohdeNimi": "Lukion ilmaisutaitolinja", "tarjoajaOid": "1.2.246.562.10.591352080610", "tarjoajaNimi": "Kallion lukio", "valintatila": "VARALLA", "vastaanottotila": "KESKEN", "ilmoittautumistila": {"ilmoittautumisaika": {"loppu": "2100-01-10T21:59:59Z"}, "ilmoittautumistapa": {"nimi": {"fi": "Oili", "sv": "Oili", "en": "Oili"}, "url": "/oili/"}, "ilmoittautumistila": "EI_TEHTY", "ilmoittauduttavissa": false}, "vastaanotettavuustila": "KESKEN", "vastaanottoDeadline": "2100-01-10T10:00:00Z", "jonosija": 1, "varasijojaKaytetaanAlkaen": "2014-08-26T16:05:23Z", "varasijojaTaytetaanAsti": "2014-08-26T16:05:23Z", "tilanKuvaukset": {}}, {
            "hakukohdeOid": "1.2.246.562.5.16303028779",
            "hakukohdeNimi": "Lukio",
            "tarjoajaOid": "1.2.246.562.10.455978782510",
            "tarjoajaNimi": "Salon lukio",
            "valintatila": "HYVAKSYTTY",
            "vastaanottotila": "EHDOLLISESTI_VASTAANOTTANUT",
            "ilmoittautumistila": {"ilmoittautumisaika": {"loppu": "2100-01-10T21:59:59Z"}, "ilmoittautumistapa": {"nimi": {"fi": "Oili", "sv": "Oili", "en": "Oili"}, "url": "/oili/"}, "ilmoittautumistila": "EI_TEHTY", "ilmoittauduttavissa": false},
            "vastaanotettavuustila": "EI_VASTAANOTETTAVISSA",
            "tilanKuvaukset": {}
          }]
        }
      },
      "hakutoiveet": [{"hakemusData": {"Opetuspiste-id": "1.2.246.562.10.591352080610", "Opetuspiste": "Kallion lukio", "Koulutus": "Lukion ilmaisutaitolinja", "Koulutus-id-kaksoistutkinto": "false", "discretionary": "true", "Koulutus-id-sora": "false", "Koulutus-id-vocational": "false", "Koulutus-id-lang": "FI", "Koulutus-id-aoIdentifier": "803", "Koulutus-id-athlete": "false", "Koulutus-educationDegree": "", "discretionary-follow-up": "todistustenpuuttuminen", "Koulutus-id": "1.2.246.562.5.72607738902", "Opetuspiste-id-parents": "1.2.246.562.10.591352080610,1.2.246.562.10.69408568777,1.2.246.562.10.346830761110,1.2.246.562.10.00000000001", "Koulutus-id-educationcode": "koulutus_301101"}, "hakuaikaId": "5474"}, {
        "hakemusData": {
          "Opetuspiste-id": "1.2.246.562.10.455978782510",
          "Opetuspiste": "Salon lukio",
          "Koulutus": "Lukio",
          "Koulutus-id-kaksoistutkinto": "false",
          "discretionary": "true",
          "Koulutus-id-sora": "false",
          "Koulutus-id-vocational": "false",
          "Koulutus-id-lang": "FI",
          "Koulutus-id-aoIdentifier": "000",
          "Koulutus-id-athlete": "false",
          "Koulutus-educationDegree": "",
          "discretionary-follow-up": "todistustenpuuttuminen",
          "Koulutus-id": "1.2.246.562.5.16303028779",
          "Opetuspiste-id-parents": "1.2.246.562.10.455978782510,1.2.246.562.10.67667811417,1.2.246.562.10.98501788818,1.2.246.562.10.00000000001",
          "Koulutus-id-educationcode": "koulutus_301101"
        }, "hakuaikaId": "5474"
      }, {}, {}, {}],
      "haku": {"oid": "1.2.246.562.5.2013080813081926341928", "name": "Korkeakoulujen yhteishaku syksy 2014", "applicationPeriods": [{"id": "5474", "start": 1372654831000, "end": 1375279200000, "active": false}], "tyyppi": "YHTEISHAKU", "korkeakouluhaku": true, "usePriority": true, "aikataulu": {"julkistus": {"start": 1402462344918, "end": 1402462344918}, "hakukierrosPaattyy": 1513894390000}, "active": false},
      "educationBackground": {"baseEducation": "0", "vocational": true},
      "answers": {
        "henkilotiedot": {"onkoSinullaSuomalainenHetu": "true", "syntymaaika": "01.01.1901", "aidinkieli": "FI", "sukupuoli": "1", "asuinmaa": "FIN", "lahiosoite": "foobartie 1", "Sukunimi": "Testaaja", "Postinumero": "00100", "kansalaisuus": "FIN", "Henkilotunnus_digest": "a8d9888c08032e0d42afe3a0700b78bd9a29be24556068838343e702908a1ab5", "Kutsumanimi": "Teemu", "turvakielto": "false", "Etunimet": "Teemu", "kotikunta": "091", "Sähköposti": "", "Henkilotunnus": "010100A939R", "matkapuhelinnumero1": ""},
        "koulutustausta": {"POHJAKOULUTUS": "0"},
        "osaaminen": {},
        "lisatiedot": {"asiointikieli": "suomi"},
        "hakutoiveet": {
          "preference1-Koulutus-id-vocational": "false",
          "preference5-Koulutus-id-sora": "",
          "preference5-Opetuspiste": "",
          "preference3-Koulutus-id-vocational": "",
          "preference4-Koulutus-id-aoIdentifier": "",
          "preference1-Koulutus-id-athlete": "false",
          "preference3-Opetuspiste": "",
          "preference1-Opetuspiste-id": "1.2.246.562.10.591352080610",
          "preference2-Opetuspiste": "Salon lukio",
          "preference1-Koulutus-id": "1.2.246.562.5.72607738902",
          "preference5-Opetuspiste-id": "",
          "preference3-Koulutus-id": "",
          "preference2-Koulutus-id-educationcode": "koulutus_301101",
          "preference3-Koulutus-id-athlete": "",
          "preference1-Koulutus-id-lang": "FI",
          "preference1-Koulutus-id-kaksoistutkinto": "false",
          "preference3-Koulutus-id-educationcode": "",
          "preference4-Koulutus-id": "",
          "preference2-Koulutus-id-athlete": "false",
          "preference4-Koulutus-id-lang": "",
          "preference5-Koulutus-id-lang": "",
          "preference4-Koulutus-id-educationcode": "",
          "preference3-Koulutus-id-kaksoistutkinto": "",
          "preference4-discretionary": "",
          "preference5-discretionary-follow-up": "",
          "preference4-Koulutus-id-vocational": "",
          "preference4-Opetuspiste": "",
          "preference2-Koulutus-id-vocational": "false",
          "preference3-Opetuspiste-id": "",
          "preference4-Koulutus-id-athlete": "",
          "preference4-Koulutus": "",
          "preference1-Koulutus-id-aoIdentifier": "803",
          "preference2-Koulutus-id-sora": "false",
          "preference2-discretionary-follow-up": "todistustenpuuttuminen",
          "preference2-Koulutus-id": "1.2.246.562.5.16303028779",
          "preference4-Koulutus-id-sora": "",
          "preference2-Koulutus-id-lang": "FI",
          "preference1-Koulutus-id-sora": "false",
          "preference4-Koulutus-id-kaksoistutkinto": "",
          "preference5-Koulutus-id": "",
          "preference5-discretionary": "",
          "preference5-Koulutus-id-aoIdentifier": "",
          "preference5-Koulutus-id-educationcode": "",
          "preference2-discretionary": "true",
          "preference2-Koulutus-id-aoIdentifier": "000",
          "preference2-Koulutus": "Lukio",
          "preference2-Opetuspiste-id-parents": "1.2.246.562.10.455978782510,1.2.246.562.10.67667811417,1.2.246.562.10.98501788818,1.2.246.562.10.00000000001",
          "preference3-discretionary": "",
          "preference4-Opetuspiste-id": "",
          "preference2-Koulutus-id-kaksoistutkinto": "false",
          "preference4-discretionary-follow-up": "",
          "preference5-Koulutus-id-vocational": "",
          "preference3-Koulutus-educationDegree": "",
          "preference2-Koulutus-educationDegree": "",
          "preference1-Koulutus": "Lukion ilmaisutaitolinja",
          "preference5-Koulutus-id-kaksoistutkinto": "",
          "preference3-discretionary-follow-up": "",
          "preference3-Opetuspiste-id-parents": "",
          "preference1-Opetuspiste-id-parents": "1.2.246.562.10.591352080610,1.2.246.562.10.69408568777,1.2.246.562.10.346830761110,1.2.246.562.10.00000000001",
          "preference5-Koulutus-educationDegree": "",
          "preference2-Opetuspiste-id": "1.2.246.562.10.455978782510",
          "preference1-discretionary": "true",
          "preference1-discretionary-follow-up": "todistustenpuuttuminen",
          "preference5-Koulutus-id-athlete": "",
          "preference3-Koulutus-id-sora": "",
          "preference1-Koulutus-educationDegree": "",
          "preference1-Koulutus-id-educationcode": "koulutus_301101",
          "preference4-Koulutus-educationDegree": "",
          "preference3-Koulutus-id-lang": "",
          "preference1-Opetuspiste": "Kallion lukio",
          "preference3-Koulutus": "",
          "preference3-Koulutus-id-aoIdentifier": ""
        }
      },
      "postOffice": "Helsinki",
      "requiresAdditionalInfo": false,
      "hasForm": true
    }, "errors": [], "questions": [], "tulosOk": true

  }

}
