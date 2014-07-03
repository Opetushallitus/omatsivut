# omatsivut #

Oppijan henkilökohtainen palvelu

## Asetukset

Oletusasetukset ovat `reference.conf` tiedostossa versionhallinnassa. Oletusasetuksilla käytetään paikallista mongo-kantaa.
Ympäristökohtaisesti arvoja voidaan ylimäärittää `omatsivut.properties` tiedostolla.
`omatsivut.properties` tiedoston etsintäjärjestys:
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
