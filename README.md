# omatsivut #

Oppijan henkilökohtainen palvelu

## Teknologiat

- Scala 2.11.1
- Java 7 (ei tällä hetkellä toimi Java8:lla)
- Angular.js
- SBT

## SBT-buildi

### Generoi projekti

Eclipseen:

`./sbt eclipse`

... tai IDEAan:

`./sbt 'gen-idea no-sbt-build-module'`

### Yksikkötestit

`./sbt test`

### War-paketointi

`./sbt package`

### Käännä ja käynnistä (aina muutosten yhteydessä automaattisesti) ##

```sh
$ ./sbt
> ~container:start
```

Avaa selaimessa [http://localhost:8080/omatsivut/](http://localhost:8080/omatsivut/).

### Käynnistä IDEAsta/Eclipsestä

Aja TomcatRunner-luokka. Jotta impersonointi/autentikoinnin ohitus onnistuu, anna parametri `-Domatsivut.profile=dev`.

## Fronttidevaus

Frontti paketoidaan gulpilla ja browserifyllä. Paketointi tapahtuu mocha-testien ajon yhteydessä (`runtests.sh`).

Jatkuva fronttikäännös käyntiin näin (ei minimointia):

    node_modules/gulp/bin/gulp.js dev

Javascriptin minimointi:

    node_modules/gulp/bin/gulp.js compile

Tyylit tehty lessillä. Css-fileet src-puussa generoidaan siitä ja ovat ignoroitu gitissä.

## Impersonointi / autentikoinnin ohitus

Jos applikaatiota ajetaan "testimoodissa" eli esim. `-Domatsivut.profile=dev`, niin autentikointi on mahdollista ohittaa - vetuman sijaan näytetään "Vetuma Simulator"-näkymä, jossa voi syöttää haluamansa henkilötunnuksen.

## mocha-phantomjs -testit

### Asenna node package manager

```sh
brew install npm
npm install
```

### Aja testit

`./runtests.sh`

### Testien ajaminen selaimessa

Tomcat käyntiin (ks yllä) ja sitten [http://localhost:8080/omatsivut/test/runner.html](http://localhost:8080/omatsivut/test/runner.html)

## Asetukset

Sovellus tukee eri profiileita. Profiili määritellään `omatsivut.profile` system propertyllä, esim `-Domatsivut.profile=it`.
Profiili määrittää lähinnä, mistä propertyt haetaan, mutta sen avulla myös voidaan mockata palveluita. Ks `AppConfig.scala`.

### dev-profiili

Näillä asetuksilla käytetään paikallista mongo-kantaa ja mockattuja ulkoisia järjestelmiä. kehityskäyttöön soveltuvat arvot ovat `dev.conf` tiedostossa versionhallinnassa.

### it-profiili

It-profiililla käytetään embedded mongo-kantaa, joka käynnistetään serverin käynnistyksen yhteydessä porttiin 28018.

### default-profiili

Oletusasetuksilla käytetään ulkoista konfiguraatiotiedostoa `omatsivut.properties`. `omatsivut.properties` tiedoston etsintäjärjestys:
`omatsivut.configFile` system property  - kehityksessä IDE:stä käytettävä tapa, jos haluaa ajaa eri asetuksilla serveriä
`~/oph-configuration/omatsivut.properties` - sovelluspalvelimilla  käytettävä tapa

### templated-profiili

Templated profiililla voi käyttää konfiguraatiota, jossa template-konfiguraatioon asettaan arvot ulkoisesta konfiguraatiosta. Käytä system propertyä `-Domatsivut.profile=templated`
ja aseta muuttujat sisältävän tiedoston sijainti system propertyssä, esim. `-Domatsivut.vars={HAKEMISTO}/oph_vars.yml` - mallia vars-tiedostoon voi ottaa tiedostosta `src/main/resources/oph-configuration/dev-vars.yml`


## Paikallinen mongo

Käynnistys

`mongod --fork --config /usr/local/etc/mongod.conf`

## API-dokumentaatio

[http://localhost:8080/omatsivut/api-docs](http://localhost:8080/omatsivut/api-docs)
