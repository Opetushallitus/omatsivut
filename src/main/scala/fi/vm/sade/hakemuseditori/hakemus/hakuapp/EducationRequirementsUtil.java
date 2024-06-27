package fi.vm.sade.hakemuseditori.hakemus.hakuapp;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import fi.vm.sade.hakemuseditori.hakemus.hakuapp.Types.IsoCountryCode;

import static com.google.common.collect.Iterables.filter;

public class EducationRequirementsUtil {

    public static class Eligibility {
        public final String nimike;
        public final IsoCountryCode suoritusmaa;
        public final boolean pohjakoulutusVapauttaaHakumaksusta; // ...suoritusmaasta riippumatta

        private Eligibility(String nimike, IsoCountryCode suoritusmaa, boolean pohjakoulutusVapauttaaHakumaksusta) {
            this.nimike = nimike;
            this.suoritusmaa = suoritusmaa;
            this.pohjakoulutusVapauttaaHakumaksusta = pohjakoulutusVapauttaaHakumaksusta;
        }

        // IB, EB, RP
        public static Eligibility ulkomainenYo(String nimike, IsoCountryCode suoritusmaa) {
            return new Eligibility(nimike, suoritusmaa, true);
        }

        public static Eligibility ulkomainen(String nimike, IsoCountryCode suoritusmaa) {
            return new Eligibility(nimike, suoritusmaa, false);
        }

        public static Eligibility suomalainen(String nimike) {
            return new Eligibility(nimike, IsoCountryCode.of("FIN"), true);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Eligibility that = (Eligibility) o;

            if (nimike != null ? !nimike.equals(that.nimike) : that.nimike != null)
                return false;
            return !(suoritusmaa != null ? !suoritusmaa.equals(that.suoritusmaa) : that.suoritusmaa != null);

        }

        @Override
        public int hashCode() {
            int result = nimike != null ? nimike.hashCode() : 0;
            result = 31 * result + (suoritusmaa != null ? suoritusmaa.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Eligibility{" +
                "nimike='" + nimike + '\'' +
                ", suoritusmaa=" + suoritusmaa +
                '}';
        }
    }


    private static <T> ImmutableSet<T> toImmutable(Iterable<T> it) {
        return ImmutableSet.<T>builder().addAll(it).build();
    }

    private static <T> Function<Types.MergedAnswers, ImmutableSet<Eligibility>> wrapSetWhere(final Function<Types.MergedAnswers, ImmutableSet<T>> set,
                                                                                                                                                              final Predicate<T> filter,
                                                                                                                                                              final Function<T, Eligibility> transform) {
        return new Function<Types.MergedAnswers, ImmutableSet<Eligibility>>() {
            @Override
            public ImmutableSet<Eligibility> apply(Types.MergedAnswers answers) {
                return toImmutable(Iterables.transform(filter(set.apply(answers), filter), transform));
            }
        };
    }

    private static <T> Function<Types.MergedAnswers, ImmutableSet<Eligibility>> wrapSet(final Function<Types.MergedAnswers, ImmutableSet<T>> set, final Function<T, Eligibility> transform) {
        return wrapSetWhere(set, Predicates.<T>alwaysTrue(), transform);
    }

    private static Function<Types.MergedAnswers, ImmutableSet<Eligibility>> multipleChoiceKkEquals(final String value) {
        return wrapSetWhere(
            BaseEducations.SuomalainenKorkeakoulutus.of,
            new Predicate<BaseEducations.SuomalainenKorkeakoulutus>() {
                @Override
                public boolean apply(BaseEducations.SuomalainenKorkeakoulutus input) {
                    return input.taso.equals(value);
                }
            },
            EducationRequirementsUtil.<BaseEducations.SuomalainenKorkeakoulutus>transformSuomalainenKoulutusWithNimike());
    }

    private static Function<Types.MergedAnswers, ImmutableSet<Eligibility>> multipleChoiceKkUlkEquals(final String value) {
        return wrapSetWhere(
            BaseEducations.UlkomaalainenKorkeakoulutus.of,
            new Predicate<BaseEducations.UlkomaalainenKorkeakoulutus>() {
                @Override
                public boolean apply(BaseEducations.UlkomaalainenKorkeakoulutus input) {
                    return input.taso.equals(value);
                }
            },
            new Function<BaseEducations.UlkomaalainenKorkeakoulutus, Eligibility>() {
                @Override
                public Eligibility apply(BaseEducations.UlkomaalainenKorkeakoulutus koulutus) {
                    return Eligibility.ulkomainen(koulutus.nimike, koulutus.maa);
                }
            });
    }


    private static Function<Types.MergedAnswers, ImmutableSet<Eligibility>> suomalainenYo(final String value) {
        return wrapSetWhere(
            BaseEducations.SuomalainenYo.of,
            new Predicate<BaseEducations.SuomalainenYo>() {
                @Override
                public boolean apply(BaseEducations.SuomalainenYo input) {
                    return input.tutkinto.equals(value);
                }
            },
            new Function<BaseEducations.SuomalainenYo, Eligibility>() {
                @Override
                public Eligibility apply(BaseEducations.SuomalainenYo koulutus) {
                    return Eligibility.suomalainen(koulutus.tutkinto);
                }
            });
    }

    private static Function<Types.MergedAnswers, ImmutableSet<Eligibility>> suomalainenKansainvalinenYo(final String value) {
        return wrapSetWhere(
            BaseEducations.SuomalainenKansainvalinenYo.of,
            new Predicate<BaseEducations.SuomalainenKansainvalinenYo>() {
                @Override
                public boolean apply(BaseEducations.SuomalainenKansainvalinenYo input) {
                    return input.tutkinto.equals(value);
                }
            },
            new Function<BaseEducations.SuomalainenKansainvalinenYo, Eligibility>() {
                @Override
                public Eligibility apply(BaseEducations.SuomalainenKansainvalinenYo koulutus) {
                    return Eligibility.suomalainen(koulutus.tutkinto);
                }
            });
    }

    private static Function<Types.MergedAnswers, ImmutableSet<Eligibility>> ulkomainenKansainvalinenYo(final String value) {
        return wrapSetWhere(
            BaseEducations.UlkomainenKansainvalinenYo.of,
            new Predicate<BaseEducations.UlkomainenKansainvalinenYo>() {
                @Override
                public boolean apply(BaseEducations.UlkomainenKansainvalinenYo input) {
                    return input.tutkinto.equals(value);
                }
            },
            new Function<BaseEducations.UlkomainenKansainvalinenYo, Eligibility>() {
                @Override
                public Eligibility apply(BaseEducations.UlkomainenKansainvalinenYo koulutus) {
                    return Eligibility.ulkomainenYo(koulutus.tutkinto, koulutus.maa);
                }
            });
    }

    private static final Function<Types.MergedAnswers, ImmutableSet<Eligibility>> ulkomainenPohjakoulutus = wrapSet(
        BaseEducations.UlkomaalainenKoulutus.of,
        new Function<BaseEducations.UlkomaalainenKoulutus, Eligibility>() {
            @Override
            public Eligibility apply(BaseEducations.UlkomaalainenKoulutus koulutus) {
                return Eligibility.ulkomainen(koulutus.nimike, koulutus.maa);
            }
        });

    private static <T extends BaseEducations.ProvideNimike> Function<T, Eligibility> transformSuomalainenKoulutusWithNimike() {
        return new Function<T, Eligibility>() {
            @Override
            public Eligibility apply(T koulutus) {
                return Eligibility.suomalainen(koulutus.getNimike());
            }
        };
    }

    private static Function<Types.MergedAnswers, ImmutableSet<Eligibility>> mergeEligibilities(final Function<Types.MergedAnswers, ImmutableSet<Eligibility>>... validators) {
        return new Function<Types.MergedAnswers, ImmutableSet<Eligibility>>() {
            @Override
            public ImmutableSet<Eligibility> apply(Types.MergedAnswers answers) {
                ImmutableSet.Builder<Eligibility> results = ImmutableSet.builder();
                for (Function<Types.MergedAnswers, ImmutableSet<Eligibility>> v : validators) {
                    results.addAll(v.apply(answers));
                }
                return results.build();
            }
        };
    }

    private final static Function<Types.MergedAnswers, ImmutableSet<Eligibility>> harkinnanvaraisuusTaiMuuErivapaus = wrapSetWhere(
        BaseEducations.HarkinnanvarainenTaiErivapaus.of,
        new Predicate<BaseEducations.HarkinnanvarainenTaiErivapaus>() {
            @Override
            public boolean apply(BaseEducations.HarkinnanvarainenTaiErivapaus input) {
                return true;
            }
        },
        new Function<BaseEducations.HarkinnanvarainenTaiErivapaus, Eligibility>() {
            @Override
            public Eligibility apply(BaseEducations.HarkinnanvarainenTaiErivapaus erivapaus) {
                return Eligibility.suomalainen(erivapaus.kuvaus);
            }
        });


    private final static Function<Types.MergedAnswers, ImmutableSet<Eligibility>> ignore = new Function<Types.MergedAnswers, ImmutableSet<Eligibility>>() {
        @Override
        public ImmutableSet<Eligibility> apply(Types.MergedAnswers answers) {
            return ImmutableSet.of();
        }
    };

    private static final Function<Types.MergedAnswers, ImmutableSet<Eligibility>> suomalainenYoAmmatillinen = wrapSet(BaseEducations.SuomalainenYoAmmatillinen.of, EducationRequirementsUtil.<BaseEducations.SuomalainenYoAmmatillinen>transformSuomalainenKoulutusWithNimike());
    private static final Function<Types.MergedAnswers, ImmutableSet<Eligibility>> suomalainenAvoinTutkinto = wrapSet(BaseEducations.SuomalainenAvoinKoulutus.of, EducationRequirementsUtil.<BaseEducations.SuomalainenAvoinKoulutus>transformSuomalainenKoulutusWithNimike());
    private static final Function<Types.MergedAnswers, ImmutableSet<Eligibility>> opistoTaiAmmatillisenKorkeaAsteenTutkinto = wrapSet(BaseEducations.SuomalainenAmKoulutus.of, EducationRequirementsUtil.<BaseEducations.SuomalainenAmKoulutus>transformSuomalainenKoulutusWithNimike());
    private static final Function<Types.MergedAnswers, ImmutableSet<Eligibility>> ammattiTaiErikoisammattitutkinto = wrapSet(BaseEducations.SuomalainenAmtKoulutus.of, EducationRequirementsUtil.<BaseEducations.SuomalainenAmtKoulutus>transformSuomalainenKoulutusWithNimike());
    private static final Function<Types.MergedAnswers, ImmutableSet<Eligibility>> suomalaisenLukionOppimaaaraTaiYlioppilastutkinto = mergeEligibilities(
        suomalainenYo("lk"),
        suomalainenYo("fi"),
        suomalainenYo("lkOnly"));
    private static final Function<Types.MergedAnswers, ImmutableSet<Eligibility>> europeanBaccalaureateTutkinto = mergeEligibilities(
        suomalainenKansainvalinenYo("eb"),
        ulkomainenKansainvalinenYo("eb"));
    private static final Function<Types.MergedAnswers, ImmutableSet<Eligibility>> internationalBaccalaureateTutkinto = mergeEligibilities(
        suomalainenKansainvalinenYo("ib"),
        ulkomainenKansainvalinenYo("ib"));
    private static final Function<Types.MergedAnswers, ImmutableSet<Eligibility>> suomalainenYlioppilastutkinto = mergeEligibilities(
        suomalainenYo("fi"),
        suomalainenYo("lkOnly"));
    private static final Function<Types.MergedAnswers, ImmutableSet<Eligibility>> reifeprufungTutkinto = mergeEligibilities(
        suomalainenKansainvalinenYo("rp"),
        ulkomainenKansainvalinenYo("rp"));

    /* Determine which ApplicationSystem fields fullfills the given base education requirements */
    // Pohjakoulutuskoodit: https://testi.virkailija.opintopolku.fi/koodisto-service/rest/codeelement/codes/pohjakoulutusvaatimuskorkeakoulut/1
    public static final ImmutableMap<String, Function<Types.MergedAnswers, ImmutableSet<Eligibility>>> kkBaseEducationRequirements = ImmutableMap.<String, Function<Types.MergedAnswers, ImmutableSet<Eligibility>>>builder()
        // Ylempi korkeakoulututkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_103", multipleChoiceKkEquals("4"))
        // Ylempi ammattikorkeakoulututkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_119", multipleChoiceKkEquals("3"))
        // Ulkomainen korkeakoulututkinto (Master)
        .put("pohjakoulutusvaatimuskorkeakoulut_117", mergeEligibilities(
            multipleChoiceKkUlkEquals("3"),
            multipleChoiceKkUlkEquals("4")))
        // Ulkomainen korkeakoulututkinto (Bachelor)
        .put("pohjakoulutusvaatimuskorkeakoulut_116", mergeEligibilities(
            multipleChoiceKkUlkEquals("1"),
            multipleChoiceKkUlkEquals("2")))
        // Lisensiaatin tutkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_120", mergeEligibilities(
            multipleChoiceKkUlkEquals("5"),
            multipleChoiceKkEquals("5")))
        // Alempi korkeakoulututkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_102", multipleChoiceKkEquals("2"))
        // Ammattikorkeakoulututkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_101", multipleChoiceKkEquals("1"))
        // Ammatillinen perustutkinto tai vastaava aikaisempi tutkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_104", opistoTaiAmmatillisenKorkeaAsteenTutkinto)
        // Ammatti- tai erikoisammattitutkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_105", ammattiTaiErikoisammattitutkinto)
        // Avoimen ammattikorkeakoulun opinnot
        .put("pohjakoulutusvaatimuskorkeakoulut_115", suomalainenAvoinTutkinto)
        // Avoimen yliopiston opinnot
        .put("pohjakoulutusvaatimuskorkeakoulut_118", suomalainenAvoinTutkinto)
        // European baccalaureate -tutkinto
        // TODO: tällä ei ole _nimike-kenttää
        .put("pohjakoulutusvaatimuskorkeakoulut_110", europeanBaccalaureateTutkinto)
        // Harkinnanvaraisuus tai erivapaus
        .put("pohjakoulutusvaatimuskorkeakoulut_106", harkinnanvaraisuusTaiMuuErivapaus)
        // International Baccalaureate -tutkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_112", internationalBaccalaureateTutkinto)
        // Opisto- tai ammatillisen korkea-asteen tutkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_108", opistoTaiAmmatillisenKorkeaAsteenTutkinto)
        // Reifeprüfung-tutkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_111", reifeprufungTutkinto)
        // Suomalainen ylioppilastutkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_109", suomalainenYlioppilastutkinto)
        // Suomalaisen lukion oppimäärä tai ylioppilastutkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_122", suomalaisenLukionOppimaaaraTaiYlioppilastutkinto)
        // Tohtorin tutkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_121", mergeEligibilities(
            multipleChoiceKkUlkEquals("5"),
            multipleChoiceKkEquals("5")))
        // Ulkomainen toisen asteen tutkinto
        .put("pohjakoulutusvaatimuskorkeakoulut_114", ulkomainenPohjakoulutus)
        // Yleinen ammattikorkeakoulukelpoisuus
        .put("pohjakoulutusvaatimuskorkeakoulut_100", mergeEligibilities(
            suomalaisenLukionOppimaaaraTaiYlioppilastutkinto,
            opistoTaiAmmatillisenKorkeaAsteenTutkinto,
            ammattiTaiErikoisammattitutkinto,
            europeanBaccalaureateTutkinto,
            internationalBaccalaureateTutkinto,
            reifeprufungTutkinto,
            ulkomainenPohjakoulutus))
        // Yleinen yliopistokelpoisuus
        .put("pohjakoulutusvaatimuskorkeakoulut_123", mergeEligibilities(
            suomalainenYlioppilastutkinto,
            europeanBaccalaureateTutkinto,
            internationalBaccalaureateTutkinto,
            reifeprufungTutkinto,
            opistoTaiAmmatillisenKorkeaAsteenTutkinto,
            ammattiTaiErikoisammattitutkinto,
            ulkomainenPohjakoulutus))
        // Ylioppilastutkinto ja ammatillinen perustutkinto (120 ov)
        .put("pohjakoulutusvaatimuskorkeakoulut_107", suomalainenYoAmmatillinen)
        .build();

    public static boolean answersFulfillBaseEducationRequirements(Types.MergedAnswers answers, ImmutableSet<String> baseEducationRequirements) {
        for (String baseEducationRequirement : baseEducationRequirements) {
            if (kkBaseEducationRequirements.containsKey(baseEducationRequirement)) {
                ImmutableSet<EducationRequirementsUtil.Eligibility> allEligibilities = kkBaseEducationRequirements.get(baseEducationRequirement).apply(answers);
                if (!allEligibilities.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

}
