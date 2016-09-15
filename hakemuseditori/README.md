## hakemuseditori

Tämä on [omatsivu](https://github.com/Opetushallitus/omatsivut) -palvelussa käytetty hakemuseditori, jolla käyttäjä tekee muutoksia hakemuksiinsa.

Tarkoituksena on ottaa editori käyttöön myös muissa sovelluksissa, kun se on sovitettu toimimaan ilman Omatsivut-applikaatiota.

## Client-side

### Tuotanto-build

Ensin

    npm install

Sitten

    ./gulp

Jonka tuloksena dist-hakemistoon rakentuu hakemuseditori.js ja hakemuseditori-templates.js.


### Kehitys-build

Kehitysbuild tuottaa tiedostot hakemistoon `dev`, jottei kehitysbuildeja vahingossa kommitoitaisi versionhallintaan.

Kertaluontoinen buildi

    ./gulp compile-dev

Jatkuva buildaus:

    ./gulp dev

Jatkuva buildaus omatsivut-applikaatioon (hakemistossa ../omatsivut):

    ./gulp omatsivut dev

### Esimerkki-applikaatio

Buildaa ja käynnistä

    npm install
    ./gulp compile-dev
    ./gulp compile-example
    example-application/server/example-server.js

Ja avaa selaimessa http://localhost:3000/

## Server-side

Hakemuseditorin palvelinkoodi sijaitsee src/main/scala -hakemistossa ja buildataan Mavenilla.
