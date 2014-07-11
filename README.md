# omatsivut #

Oppijan henkilökohtainen palvelu

## Asetukset

Sovellus tukee eri profiileita. Profiili määritellään `omatsivut.profile` system propertyllä, esim `-Domatsivut.profile=it`.
Profiili määrittää lähinnä, mistä propertyt haetaan (huom! reference.conf ladataan aina), mutta sen avulla myös
voidaan mockata palveluita. Ks `AppConfig.scala`.

### dev-profiili

Näillä asetuksilla käytetään paikallista mongo-kantaa ja mockattuja ulkoisia järjestelmiä. kehityskäyttöön soveltuvat arvot ovat `dev.conf` tiedostossa versionhallinnassa.

### it-profiili

It-profiililla käytetään embedded mongo-kantaa, joka käynnistetään serverin käynnistyksen yhteydessä porttiin 28018.

### default-profiili

Oletusasetuksilla käytetään ulkoista konfiguraatiotiedostoa `omatsivut.properties`. `omatsivut.properties` tiedoston etsintäjärjestys:
`omatsivut.confiFile` system property  - kehityksessä IDE:stä käytettävä tapa, jos haluaa ajaa eri asetuksilla serveriä
`~/oph-configuration/omatsivut.properties` - sovelluspalvelimilla  käytettävä tapa
`../module-install-parent/config/common/omatsivut/omatsivut.properties` - kehityksessä käytettävä oletustapa, joten sinun tulee hakea `module-install-parent` tämän projektin rinnalle.
`./module-install-parent/config/common/omatsivut/omatsivut.properties` - CI palvelimella käytettävä tapa

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

Jatkuva fronttikäännös käyntiin näin:

    node_modules/gulp/bin/gulp.js dev

Tyylit tehty lessillä. Css-fileet src-puussa generoidaan siitä ja ovat ignoroitu gitissä.

## Impersonointi / autentikoinnin ohitus

Jos applikaatiota ajetaan "testimoodissa" eli esim. `-Domatsivut.profile=dev`, niin autentikointi on mahdollista ohittaa.

Tämä tapahtuu menemällä urliin http://localhost:8080/omatsivut/util/fakesession?hetu=010101-123N

## mocha-phantomjs -testit

### Asenna node package manager

```sh
brew install npm
npm install
```

### Aja testit

`./runtests.sh`

### Testien ajaminen selaimessa

Tomcat käyntiin (ks yllä) ja sitten http://localhost:8080/omatsivut/test/runner.html

## Paikallinen mongo

Käynnistys

`mongod --fork --config /usr/local/etc/mongod.conf`

## API-dokumentaatio

[http://localhost:8080/api-docs](http://localhost:8080/api-docs)
