# omatsivut #

Oppijan henkilökohtainen palvelu

## Alkuvuoden 2025 tilanne

Palvelun lokaali ajaminen ei toistaiseksi toimi tai ole kunnolla koestettu, joten tämä kuvaus voi olla vanhentunut siltä osin.

## Teknologiat

- Scala 2.12 (servereillä JRE17)
- Frontissa AngularJS, webpack, npm
- Testeissä Specs2, ~~mocha~~, phantomjs.

### JDK

- Käytetään kehityksessä ja sovelluksen ajamiseen JDK 17:a
- JCE (Java Cryptography Extension)
   - Ilman laajennosta tulee virhe: "java.security.InvalidKeyException: Illegal key size"
   - Lataa Oraclen sivuilta ja kopioi tiedostot `$JAVA_HOME/jre/lib/security`

    http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html

Lisää JAVA_HOME ympäristömuuttujat polkuun:

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 17.0)
```

## Tarvittavat build-työkalut

Asenna NodeJS haluamallasi tavalla. Asenna sen avulla projektin kehitystyökalut komennolla:

```sh
npm install
```

Ensimmäisellä kerralla esimerkiksi testejä ajettaessa buildataan Docker-kontit:

```sh
cd postgresql/docker/
docker build --tag omatsivut-postgres .
docker build --tag valintarekisteri-postgres .
```


## IDE-buildi

Importoi IDEA:ssa projektin maven projektina.

Jos testeistä tulee IDE:llä yllättäviä `StackOverflow`-exceptioneita, anna JVM:lle suurempi stack 'VM parameters'-vivulla, esim. `-Xss4m`.

### Käynnistä sovellus IDEAsta

```sh
npm install
mvn clean install
```

Tämä luo ./target-kansion alle valinta-tulos-service.war -tiedoston

Aja JettyLauncher-luokka.

- jotta impersonointi/autentikoinnin ohitus onnistuu, anna parametri `-Domatsivut.profile=it` (JVM parametri, ei CLI parametri).
- lisäksi `-Xss2M`, ettei stäkki lopu fikstuuridatalla, jossa on erittäin syviä rakenteita

### Ajo oikealla Jettyllä

Valmistele ensin sopiva, ympäristökohtainen common.properties. Valitse, käytätkö tietokantoja omalta koneeltasi vai palvelinympäristöltä.

Tässä yhdet esimerkkitweakkaukset.

```
11c11,12
< host.haku=testiopintopolku.fi
---
> #host.haku=testiopintopolku.fi
> host.haku=http://localhost:8080
36c37,38
< omatsivut.valinta-tulos-service.url=https://virkailija.testiopintopolku.fi/valinta-tulos-service
---
> #omatsivut.valinta-tulos-service.url=https://virkailija.testiopintopolku.fi/valinta-tulos-service
> omatsivut.valinta-tulos-service.url=http://david.devaaja.devaa.opintopolku.fi:18888/valinta-tulos-service  # huom, /etc/hosts :issa pitää olla 127.0.0.1 david.devaaja.devaa.opintopolku.fi
55c57,58
< omatsivut.db.url=jdbc:postgresql://omatsivut.db.testiopintopolku.fi:5432/omatsivut
---
> #omatsivut.db.url=jdbc:postgresql://omatsivut.db.testiopintopolku.fi:5432/omatsivut
> omatsivut.db.url=jdbc:postgresql://localhost:25440/omatsivut  # huom, localhost:25440 on ssh-putkitettu omatsivut.db.testiopintopolku.fi:5432:hen
99c102
---

```

Starttaa sitten [`OmatsivutServerLauncher`](src/test/scala/fi/vm/sade/omatsivut/OmatsivutServerLauncher.scala) sopivilla parametreilla, esim `-Xss2m -Duser.home=/home/thrantal/oph-confs/omatsivut-pallero-local-oph-configuration/`.
Huomaa, että `oph-configuration` -hakemiston tulee olla `user.home`-hakemiston alla.

Sisäänkirjautumisen saa feikattua tekemällä URLiin http://localhost:8080/omatsivut/initsession pyyntö, jossa `hetu`-headeriin annetaan halutun käyttäjän henkilötunnus.

#### Offline-käyttö (skipRaamit) ja käyttö IE9:llä

Kun sovellusta ajetaan `-Domatsivut.profile=it`-parametrillä, toimii se ilman verkkoyhteyttä vaikkapa junassa.
Selaimessa sovellus kuitenkin lataa "oppija-raamit" testiympäristön serveriltä, johon sinulla ei välttämättä ole pääsyä.
Tässä tapauksessa voit käyttää osoitetta [http://localhost:7337/omatsivut/index.html#skipRaamit](http://localhost:7337/omatsivut/index.html#skipRaamit), jolloin raamit jätetään
pois. Mocha-testit käyttävät samaa ratkaisua.

Myös IE9:llä pitää paikallisessa ympäristössä CORS:n takia käyttää skipRaamit tapaa.

## Maven-buildi

### Testit

Näin ajat testit komentoriviltä.

```sh
npm install
mvn test
```

Komento ajaa kaikki testit, mukaan lukien yksikkötestit, REST-palvelujen testit, mocha-selaintestit.

### Fatjar-paketointi

```sh
./webbuild.sh
mvn package
```

## Fronttidevaus

Kehitystä varten oleva buildi (ei minimointia):

```sh
npm run dev
```

Jatkuva fronttikäännös käyntiin näin (ei minimointia):

```sh
npm run watch
```

Tuotantoa vastaava buildi

```sh
npm run build
```

Fronttikoodit sijaitsevat `src/main` kansion alla hieman sekalaisessa järjestyksessä.
Tyyleissä on käytetty lessiä. Niiden perusteella generoidaan css-tiedostot kansioon `src/main/webapp/css`.
HTML templatet on ladattu mukaan direktiiveihin merkkijonoina.
Fronttikoodit paketoidaan webpackilla bundle.js tiedostoksi.

## Impersonointi / autentikoinnin ohitus

Jos applikaatiota ajetaan "testimoodissa" eli esim. `-Domatsivut.profile=dev`, niin autentikointi on mahdollista ohittaa syöttämällä haluamansa henkilötunnuksen.

## mocha-phantomjs -testit

### Testien ajaminen selaimessa

Palvelin käyntiin (it-profiili ja JettyLauncher, ks yllä) ja sitten [http://localhost:7337/omatsivut/test/runner.html](http://localhost:7337/omatsivut/test/runner.html)

Testien ajaminen onnistuneesti vaatii sitä, että tämän projektin rinnalta hakemistopuusta löytyy [valinta-tulos-service](https://github.com/Opetushallitus/valinta-tulos-service).

Valinta-tulos-service.war muodostuu target:n alle komennolla `mvn clean compile`

Testien ajaminen aikaansaa myös sisäänkirjautumisen, jonka jälkeen sovelluksen voi halutessaan avata lokaalisti osoitteessa [http://localhost:7337/omatsivut](http://localhost:7337/omatsivut),
 tosin tällöin sovellus on siinä tilassa mihin testit ovat sen jättäneet.

## Testidata

Aikaisempi testidata perustui haku-appin ja valinta-tulos-servicen repositorioista ladattuihin fixtureihin.
Koska vanhan hakemuspalvelun hakemukset eivät enää ole käytössä ja datamalli on vanhentunut,
nämä data-alustukset on poistettu ja resursointisyistä korvaavia end-to-end-testejä ei ole toteutettu.

## Sovellusprofiilit

Sovellus tukee eri profiileita. Profiili määritellään `omatsivut.profile` system propertyllä, esim `-Domatsivut.profile=it`.
Profiili määrittää lähinnä, mistä propertyt haetaan, mutta sen avulla myös voidaan mockata palveluita. Ks `AppConfig.scala`.

### it-profiili

Tämä on kätevin profiili kehityskäyttöön, ja ainoa profiili, jolla esimerkiksi mocha-testit voidaan onnistuneesti ajaa.
It-profiililla käytetään:
- paikallista postgres-kantaa, toistaiseksi session tallentamisen takia, joka myöskin käynnistetään serverin käynnistyksen
yhteydessä


### dev-profiili

Tällä profiililla käytetään mockattuja ulkoisia järjestelmiä (pois lukien valinta-tulos-service).

### templated-profiili

Tällä profiililla sovelluksen asetukset generoidaan tuotantoympäristöä vastaavasti `common.properties.template` -tiedostosta
ja annetusta muuttujatiedostosta. Testi- ja tuotantoympäristöissä käytettävät muuttujatiedostot sijaitsevat erillisissä sisäisissä
 "environment" -repositorioissa.

IDE:ssä voit ajaa käyttämällä system propertyä `-Domatsivut.profile=templated` ja aseta muuttujat sisältävän tiedoston sijainti toisessa system propertyssä, esim. `-Domatsivut.vars={HAKEMISTO}/oph_vars.yml`
Mallia vars-tiedostoon voi ottaa tiedostosta `src/main/resources/oph-configuration/dev-vars.yml`

### default-profiili

Oletusasetuksilla käytetään ulkoista konfiguraatiotiedostoa `omatsivut.properties`, joka generoidaan deployn yhteydessä
 `common.properties.template` ja ulkoisesta muuttujatiedostosta. Tätä profiilia käytetään testi- ja
tuotantoympäristöissä.

### cloud-profiili

Tällä profiililla sovellus käyttää pilviympäristöön sopivia asetuksia ja komponentteja.
Muuten suunnilleen sama kuin default-profiili, mutta esim. tiedostot käyttävät suoran levyjärjestelmän sijaan S3 palvelua.

## API-dokumentaatio

REST-rajapinnat dokumentoitu Swaggerilla.

[http://localhost:7337/omatsivut/api-docs](http://localhost:7337/omatsivut/api-docs)
