# omatsivut #

Oppijan henkilökohtainen palvelu

## Asetukset

Oletusasetukset ovat `reference.conf` tiedostossa versionhallinnassa. Oletusasetuksilla käytetään paikallista mongo-kantaa.
Ympäristökohtaisesti arvoja voidaan ylimäärittää `omatsivut.properties` tiedostolla.
`omatsivut.properties` tiedoston etsintäjärjestys:
`omatsivut.confiFile` system property  - CI palvelimella käytettävä tapa)
`~/oph-configuration/omatsivut.properties` - sovelluspalvelimilla  käytettävä tapa
`../module-install-parent/config/common/omatsivut/omatsivut.properties` - kehityksessä käytettävä tapa, joten sinun tulee hakea `module-install-parent` tämän projektin rinnalle.

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

Avaa selaimessa [http://localhost:8080/](http://localhost:8080/).

## mocha-phantomjs -testit

### Asenna node package manager

```sh
brew install npm
npm install
```

### Aja testit

`./runtests.sh`

## Paikallinen mongo

Käynnistys

`mongod --fork --config /usr/local/etc/mongod.conf`

## API-dokumentaatio

[http://localhost:8080/api-docs](http://localhost:8080/api-docs)
