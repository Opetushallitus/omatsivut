# omatsivut #

Oppijan henkilökohtainen palvelu

## Teknologiat

- Serverillä Scala 2.11.1, JRE7 (ei tällä hetkellä toimi Java8:lla), SBT
- JCE (Java Cryptography Extension) - Lataa Oraclen sivuilta ja kopioi tiedostot $JAVA_HOME/jre/lib/security
- Frontissa Angular.js, gulp.js, npm
- Testeissä Specs2, mocha, phantomjs. Kaikille toiminnoille automaattiset testit.

## Get started

Buildaa fronttiapplikaatio (npm install + gulp)

    ./webbuild.sh

Lisää JAVA_HOME ja JAVA_HOME8 ympäristömuuttujat polkuun:

    export JAVA_HOME=$(/usr/libexec/java_home -v 1.7)
    export JAVA8_HOME=$(/usr/libexec/java_home -v 1.8)

### IDE

Projektissa tulee käyttää IDEA versiota 14 tai uudempaa.
Importoi projekti SBT projektina siihen.

### Käynnistä sovellus IDEAsta

Aja JettyLauncher-luokka. Jotta impersonointi/autentikoinnin ohitus onnistuu, anna parametri `-Domatsivut.profile=it`.

### Offline-käyttö (skipRaamit) ja käyttö IE9:llä

Kun sovellusta ajetaan `-Domatsivut.profile=it`-parametrillä, toimii se ilman verkkoyhteyttä vaikkapa junassa.
Selaimessa sovellus kuitenkin lataa "oppija-raamit" testiympäristön serveriltä, johon sinulla ei välttämättä ole pääsyä.
Tässä tapauksessa voit käyttää osoitetta [http://localhost:8080/omatsivut/index.html#skipRaamit], jolloin raamit jätetään
pois. Mocha-testit käyttävät samaa ratkaisua.

Myös IE9:llä pitää paikallisessa ympäristössä CORS:n takia käyttää skipRaamit tapaa.

### Valinta-tulos-service

Jotta hakemuksiin liittyvät valintatulokset voitaisiin näyttää, täytyy sovelluksen saada yhteys
[valinta-tulos-service](https://github.com/Opetushallitus/valinta-tulos-service) -palveluun. Voit käynnistää paikallisen
instanssin noudattamalla valinta-tulos-servicen README:ssa olevia ohjeita.

Myös mocha-testien onnistunut ajo edellyttää paikallisesti ajossa olevaa valinta-tulos-service -instanssia.

## SBT-buildi

### Testit

Näin ajat testit komentoriviltä.

`./sbt test`

Komento ajaa kaikki testit, mukaan lukien yksikkötestit, REST-palvelujen testit, mocha-selaintestit.


#### Yksikkötestit

Jos haluat ajaa pelkät ykikkötestit niin aja:

`./sbt unit:test`

Se skippaa mocha testit.

### War-paketointi

`./sbt package`

### Käännä ja käynnistä komentoriviltä

```sh
`./sbt "test:run-main fi.vm.sade.omatsivut.JettyLauncher" -Domatsivut.profile=it`
```

Avaa selaimessa [http://localhost:8080/omatsivut/](http://localhost:8080/omatsivut/).

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

### Aja testit

`./sbt mocha:test`

### Testien ajaminen selaimessa

Tomcat käyntiin (ks yllä) ja sitten [http://localhost:8080/omatsivut/test/runner.html](http://localhost:8080/omatsivut/test/runner.html)

Testien ajaminen onnistuneesti vaatii sitä, että tämän projektin rinnalta hakemistopuusta löytyy [valinta-tulos-service](https://github.com/Opetushallitus/valinta-tulos-service).

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

[http://localhost:8080/omatsivut/api-docs](http://localhost:8080/omatsivut/api-docs)
