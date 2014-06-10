# omatsivut #

Oppijan henkilökohtainen palvelu

## Asetukset

Oletusasetukset `reference.conf` tiedostossa versionhallinnassa.
Ympäristökohtaiset asetukset `~/oph-configuration/omatsivut.properties` tiedostossa.
Kehitysympäristön konffit löytyy OPH wikistä projektin sivuston kehittäjän ohjeet sivulta.

## Käännä ja käynnistä ##

```sh
$ cd omatsivut
$ ./sbt
> container:start
> browse
```

Jos `browse` ei avaa selainta, niin avaa käsin [http://localhost:8080/](http://localhost:8080/).
