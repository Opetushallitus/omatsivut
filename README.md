# omatsivut #

Oppijan henkilökohtainen palvelu

## Asetukset

Oletusasetukset `reference.conf` tiedostossa versionhallinnassa.
Ympäristökohtaiset asetukset `~/oph-configuration/omatsivut.properties` tiedostossa.
Kehitysympäristön konffit löytyy OPH wikistä projektin sivuston kehittäjän ohjeet sivulta.

## SBT buildi

### Generoi eclipse projekti

`./sbt eclipse`

### Yksikkötestit

`./sbt test`

### War paketointi

`./sbt package`

### Käännä ja käynnistä (aina muutosten yhteydessä automaattisesti) ##

```sh
$ ./sbt
> ~container:start
```

Avaa selaimessa [http://localhost:8080/](http://localhost:8080/).

## mocha-phantomjs testit

### Asenna node package manager ja grunt

```sh
brew install npm
npm install -g grunt-cli
npm install
```

## Aja testit

`grunt test`