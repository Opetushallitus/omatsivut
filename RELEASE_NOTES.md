# Tuotantoon viedyt ominaisuudet (storyt)

[Storyjen kuvaukset](https://confluence.oph.ware.fi/confluence/pages/viewpage.action?pageId=19955772)

## QA:lla, odottaa tuotantoon siirtoa

## Tuotannossa

### [release-1785](https://bamboo.oph.ware.fi/deploy/viewDeploymentVersion.action?versionId=110985364)
- Tuki useammalle ja hakukohdekohtaisille hakuajoille [OHP-110](https://jira.oph.ware.fi/jira/browse/OHP-110)

### [release-1711](https://bamboo.oph.ware.fi/deploy/viewDeploymentVersion.action?versionId=109478162)

- Tulostetaan lokiin cache statistiikkaa
- Linkki Oiliin (korkeakoulujen läsnoloilmoittaumiseen) [OHP-107](https://jira.oph.ware.fi/jira/browse/OHP-107)
  * tulee näkyviin vasta kun ilmoittautuminen enabloidaan valinta-tulos-service:n konfiguraatioista

### [release-1648](https://bamboo.oph.ware.fi/deploy/viewDeploymentVersion.action?versionId=109477953)

- Korjattu application system cache Hakemus-api:ssa [HAKU-BM-242](https://bamboo.oph.ware.fi/browse/HAKU-BM-242) [OVT-8894](https://jira.oph.ware.fi/jira/browse/OVT-8894)

### [release-1536](https://bamboo.oph.ware.fi/deploy/viewDeploymentVersion.action?versionId=107774338)

- Hakemuksen yhteystietojen muokkaus [OHP-115](https://jira.oph.ware.fi/jira/browse/OHP-115)
- Piwik web-analytiikka käyttöön [OHP-104](https://jira.oph.ware.fi/jira/browse/OHP-104)
- Shibboleth muutokseen liittyvä korjaus: näytetään oidittomalle hakijalle oikea ilmoitus virheilmoituksen sijaan

### [release-1454](https://bamboo.oph.ware.fi/deploy/viewDeploymentVersion.action?versionId=107774159)

- Shibboleth muutos: [OHP-106](https://jira.oph.ware.fi/jira/browse/OHP-106)
- Vielä keskeneräisiä storyjä ([OHP-110](https://jira.oph.ware.fi/jira/browse/OHP-110) & [OHP-115](https://jira.oph.ware.fi/jira/browse/OHP-115)) valmistelevia muutoksia. mm.:
  * haun tiedot haetaan nyt tarjonnasta eikä oteta hakemukselta
  * myös haun oid tallennetaan hakemuksen muutoksissa audit lokiin

### [release-1338](https://bamboo.oph.ware.fi/deploy/viewDeploymentVersion.action?versionId=104693941)

- Paikan vastaanotto 2. aste yhteishaku: [OHP-108](https://jira.oph.ware.fi/jira/browse/OHP-108)
- Kysymysten verbose help: [OHP-102](https://jira.oph.ware.fi/jira/browse/OHP-102)

### [release-1271](https://bamboo.oph.ware.fi/deploy/viewDeploymentVersion.action?versionId=99483667)

- Tuki hakijakohtaiselle paikan vastaanottoajalle (annetaan aikaa viimeisen tilamuutoksen jälkeen vastaanottaa paikka)
- Korjauksia ja tyylimuutoksia paikan vastaanottoon

### [release-1092](https://bamboo.oph.ware.fi/deploy/viewDeploymentVersion.action?versionId=93913126)

- Tuki valintatuloksen näyttämiselle: [OHP-97](https://jira.oph.ware.fi/jira/browse/OHP-97)
- Tuki opiskelupaikan vastaanottamisella: [OHP-98](https://jira.oph.ware.fi/jira/browse/OHP-98)
- Tallennuksessa muokataan auhtorization meta uusia hakutoiveita vastaavaksi
- Uusi hakemus-api versio käyttöön: liitteiden tiedot tallennetaan hakemukselle

### [release-970](https://bamboo.oph.ware.fi/deploy/viewDeploymentVersion.action?versionId=89980956)

- Korjattu bugi hakemuksen muokkauksessa
