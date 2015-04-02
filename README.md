# omatsivut #

Oppijan henkilökohtainen palvelu

## Teknologiat

- Scala 2.11 (servereillä yhä JRE7)
- Frontissa Angular.js, gulp.js, npm
- Testeissä Specs2, mocha, phantomjs. Kaikille toiminnoille automaattiset testit.

### JDK

- Käytetään kehityksessä JDK8:a, mutta target version yhä 1.7
- JCE (Java Cryptography Extension)
   - Ilman laajennosta tulee virhe: "java.security.InvalidKeyException: Illegal key size"
   - Lataa Oraclen sivuilta ja kopioi tiedostot $JAVA_HOME/jre/lib/security

    http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html

Lisää JAVA_HOME ympäristömuuttujat polkuun:

    export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)

## Webbuild

Buildaa fronttiapplikaatio (npm install + gulp)

    ./webbuild.sh

### IDE

Importoi IDEA:ssa projektin maven projektina.

Jos testeistä tulee IDE:llä yllättäviä `StackOverflow`-exceptioneita, anna JVM:lle suurempi stack 'VM parameters'-vivulla, esim. `-Xss4m`.

### Käynnistä sovellus IDEAsta

Aja JettyLauncher-luokka.

- jotta impersonointi/autentikoinnin ohitus onnistuu, anna parametri `-Domatsivut.profile=it`.
- jotta valinta-tulos-service suostuu käynnistymään anna parameteri `-DJAVA8_HOME=<home>`, missä <home> osoittaa Java8-installaatioon.

### Offline-käyttö (skipRaamit) ja käyttö IE9:llä

Kun sovellusta ajetaan `-Domatsivut.profile=it`-parametrillä, toimii se ilman verkkoyhteyttä vaikkapa junassa.
Selaimessa sovellus kuitenkin lataa "oppija-raamit" testiympäristön serveriltä, johon sinulla ei välttämättä ole pääsyä.
Tässä tapauksessa voit käyttää osoitetta [http://localhost:7337/omatsivut/index.html#skipRaamit], jolloin raamit jätetään
pois. Mocha-testit käyttävät samaa ratkaisua.

Myös IE9:llä pitää paikallisessa ympäristössä CORS:n takia käyttää skipRaamit tapaa.

## Maven-buildi

### Testit

Näin ajat testit komentoriviltä.

`mvn test`

Komento ajaa kaikki testit, mukaan lukien yksikkötestit, REST-palvelujen testit, mocha-selaintestit.

### War-paketointi

`mvn package`

## Fronttidevaus

Frontti paketoidaan gulpilla ja browserifyllä. Skripti `webbuild.sh` tekee tämän helpoksi.

Paketoi frontti devausmoodissa (ei minifiointia):

    ./webbuild.sh compile-dev

Jatkuva fronttikäännös käyntiin näin (ei minimointia):

    ./webbuild.sh dev

Tuotantoa vastaava buildi

    ./webbuild.sh

Suuri osa fronttikoodista sijaitsee [hakemuseditori](https://github.com/Opetushallitus/hakemuseditori/tree/master/dist)
-repositoriossa, mistä käytetään tiedostoja `hakemuseditori.js` ja `hakemuseditori-templates.js`.
Nämä tiedostot haetaan paketoinnin yhteydessä npm:llä ja kopioidaan
gulp-buildissa hakemistoon `src/main/webapp`.

Jos muokkaat hakemuseditorin koodia ja haluat nopean kopioinnin omatsivut-applikaatioon
jokaisen muokkauksen yhteydessä, tee näin:

    cd ../hakemuseditori
    ./gulp omatsivut dev

Tyylit tehty lessillä. Css-fileet src-puussa generoidaan siitä ja ovat
ignoroitu gitissä.

Kun yllä oleva on tehty, jatkuva fronttikäännös ilman minimointia, joka huomaa muutokset myös paikallisessa hakemuseditori-repossa (../hakemuseditori) käynnistyy näin:

    ./webbuild.sh dev-local-editor

## Impersonointi / autentikoinnin ohitus

Jos applikaatiota ajetaan "testimoodissa" eli esim. `-Domatsivut.profile=dev`, niin autentikointi on mahdollista ohittaa - vetuman sijaan näytetään "Vetuma Simulator"-näkymä, jossa voi syöttää haluamansa henkilötunnuksen.

## mocha-phantomjs -testit

### Asenna node package manager

```sh
brew install npm
npm install
```
### Testien ajaminen selaimessa

Palvelin käyntiin (ks yllä) ja sitten [http://localhost:7337/omatsivut/test/runner.html](http://localhost:7337/omatsivut/test/runner.html)

Testien ajaminen onnistuneesti vaatii sitä, että tämän projektin rinnalta hakemistopuusta löytyy [valinta-tulos-service](https://github.com/Opetushallitus/valinta-tulos-service).

## Testidata

Testien ajaminen käynnistää embedded Mongo instanssin hakulomake-kannasta.
Siihen ladataan fixtuuridata
`haku/hakemus-api/src/main/resources/mongofixtures` kansiosta. Kyseisessä
datassa on hakemusten personOid kentät muutettu vastaamaan testien käyttämää
henkilöä. Fixtuurien lisäämistä varten löytyy scripti `haku/testfixtures`
kansiosta.

Testit käyttävät myös mokattuja ulkoisten palvelujen rajapintoja. Näiden
mokkien data löytyy pääsääntöisesti
`hakemuseditori/src/main/resources/hakemuseditorimockdata/` kansiosta,
poikkeuksena koulutusdata joka sijaitsee kansiossa
`haku/hakemus-api/src/main/resources/mockdata`.

## Sovellusprofiili

Sovellus tukee eri profiileita. Profiili määritellään `omatsivut.profile` system propertyllä, esim `-Domatsivut.profile=it`.
Profiili määrittää lähinnä, mistä propertyt haetaan, mutta sen avulla myös voidaan mockata palveluita. Ks `AppConfig.scala`.

### it-profiili

It-profiililla käytetään embedded mongo-kantaa, joka käynnistetään serverin käynnistyksen yhteydessä porttiin 28018.
Tämä on kätevin profiili kehityskäyttöön, ja ainoa profiili, jolla esimerkiksi mocha-testit voidaan onnistuneesti ajaa.

### dev-profiili

Tällä profiililla käytetään paikallista mongo-kantaa ja mockattuja ulkoisia järjestelmiä (pois lukien valinta-tulos-service).

Paikallisen mongon käynnistys:

`mongod --fork --config /usr/local/etc/mongod.conf`

### templated-profiili

Tällä profiililla sovelluksen asetukset generoidaan tuotantoympäristöä vastaavasti `omatsivut.properties.template` -tiedostosta
ja annetusta muuttujatiedostosta. Testi- ja tuotantoympäristöissä käytettävät muuttujatiedostot sijaitsevat erillisissä sisäisissä
 "environment" -repositorioissa.

IDE:ssä voit ajaa käyttämällä system propertyä `-Domatsivut.profile=templated` ja aseta muuttujat sisältävän tiedoston sijainti toisessa system propertyssä, esim. `-Domatsivut.vars={HAKEMISTO}/oph_vars.yml`
Mallia vars-tiedostoon voi ottaa tiedostosta `src/main/resources/oph-configuration/dev-vars.yml`

### default-profiili

Oletusasetuksilla käytetään ulkoista konfiguraatiotiedostoa `omatsivut.properties`, joka generoidaan deployn yhteydessä
 `omatsivut.properties.template` ja ulkoisesta muuttujatiedostosta. Tätä profiilia käytetään testi- ja
tuotantoympäristöissä.

## API-dokumentaatio

REST-rajapinnat dokumentoitu Swaggerilla.

[http://localhost:7337/omatsivut/api-docs](http://localhost:7337/omatsivut/api-docs)
