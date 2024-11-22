package fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.List;

public class SyntheticApplication {

    public final String hakukohdeOid;
    public final String hakuOid;
    public final String tarjoajaOid;
    public final List<Hakemus> hakemukset;

    @JsonCreator
    public SyntheticApplication(@JsonProperty(value = "hakukohdeOid") String hakukohdeOid,
                                @JsonProperty(value = "hakuOid") String hakuOid,
                                @JsonProperty(value = "tarjoajaOid") String tarjoajaOid,
                                @JsonProperty(value = "hakemukset") List<Hakemus> hakemukset) {

        this.hakukohdeOid = hakukohdeOid;
        this.hakemukset = hakemukset;
        this.tarjoajaOid = tarjoajaOid;
        this.hakuOid = hakuOid;
    }

    public static class Hakemus {

        public final String hakijaOid;
        public final String etunimi;
        public final String sukupuoli;
        public final String aidinkieli;
        public final String sukunimi;
        public final String henkilotunnus;
        public final String sahkoposti;
        public final String syntymaAika;
        public final String asiointikieli;
        public final String puhelinnumero;
        public final String osoite;
        public final String postinumero;
        public final String postitoimipaikka;
        public final String asuinmaa;
        public final String kansalaisuus;
        public final String kotikunta;
        public final Boolean toisenAsteenSuoritus;
        public final String toisenAsteenSuoritusmaa;
        public final String maksuvelvollisuus;


        @JsonCreator
        public Hakemus(@JsonProperty("hakijaOid") String hakijaOid,
                       @JsonProperty("etunimi") String etunimi,
                       @JsonProperty("sukunimi") String sukunimi,
                       @JsonProperty("sukupuoli") String sukupuoli,
                       @JsonProperty("aidinkieli") String aidinkieli,
                       @JsonProperty("henkilotunnus") String henkilotunnus,
                       @JsonProperty("sahkoposti") String sahkoposti,
                       @JsonProperty("syntymaAika") String syntymaAika,
                       @JsonProperty("asiointikieli") String asiointikieli,
                       @JsonProperty("puhelinnumero") String puhelinnumero,
                       @JsonProperty("osoite") String osoite,
                       @JsonProperty("postinumero") String postinumero,
                       @JsonProperty("postitoimipaikka") String postitoimipaikka,
                       @JsonProperty("asuinmaa") String asuinmaa,
                       @JsonProperty("kansalaisuus") String kansalaisuus,
                       @JsonProperty("kotikunta") String kotikunta,
                       @JsonProperty("toisenAsteenSuoritus") Boolean toisenAsteenSuoritus,
                       @JsonProperty("toisenAsteenSuoritusmaa") String toisenAsteenSuoritusmaa,
                       @JsonProperty("maksuvelvollisuus") String maksuvelvollisuus
        ) {
            this.hakijaOid = hakijaOid;
            this.etunimi = etunimi;
            this.sukupuoli = sukupuoli;
            this.aidinkieli = aidinkieli;
            this.sukunimi = sukunimi;
            this.henkilotunnus = henkilotunnus;
            this.sahkoposti = sahkoposti;
            this.syntymaAika = syntymaAika;
            this.asiointikieli = asiointikieli;
            this.puhelinnumero = puhelinnumero;
            this.osoite = osoite;
            this.postinumero = postinumero;
            this.postitoimipaikka = postitoimipaikka;
            this.asuinmaa = asuinmaa;
            this.kansalaisuus = kansalaisuus;
            this.kotikunta = kotikunta;
            this.toisenAsteenSuoritus = toisenAsteenSuoritus;
            this.toisenAsteenSuoritusmaa = toisenAsteenSuoritusmaa;
            this.maksuvelvollisuus = maksuvelvollisuus;
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);

    }
}


