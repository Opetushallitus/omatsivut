package fi.vm.sade.hakemuseditori.hakemus.hakuapp;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import java.util.Arrays;

/**
 * Typing of Applications' base education Map<String, String> to concrete
 * NON NULL types.
 */
public class BaseEducations {
    private static boolean variablesNotNull(Object... vars) {
        int nullsFound = 0;
        for (Object v : vars) {
            if (v == null) {
                nullsFound++;
            }
        }
        if (vars.length == nullsFound) {
            return false;
        } else if (nullsFound == 0) {
            return true;
        } else {
            // Thrown if fields partially null
            throw new AssertionError("Not-nullable class got null arguments. Arguments are: " + Arrays.toString(vars));
        }
    }

    public interface ProvideNimike {
        public String getNimike();
    }

    public static class SuomalainenAvoinKoulutus implements ProvideNimike {
        private static final String fieldPrefix = "pohjakoulutus_avoin";

        public final String nimike;

        @Override
        public String getNimike() {
            return nimike;
        }

        private SuomalainenAvoinKoulutus(String nimike) {
            this.nimike = nimike;
        }

        public static final Function<Types.MergedAnswers, ImmutableSet<SuomalainenAvoinKoulutus>> of = new Function<Types.MergedAnswers, ImmutableSet<SuomalainenAvoinKoulutus>>() {
            @Override
            public ImmutableSet<SuomalainenAvoinKoulutus> apply(Types.MergedAnswers answers) {
                String valinta = answers.get(fieldPrefix);

                if ("true".equals(valinta)) {
                    String nimike = answers.get(fieldPrefix + "_kokonaisuus");
                    variablesNotNull(nimike);
                    return ImmutableSet.of(new SuomalainenAvoinKoulutus(nimike));
                } else {
                    return ImmutableSet.of();
                }
            }
        };
    }

    // TODO: in reality a list
    public static class SuomalainenAmKoulutus implements ProvideNimike {
        private static final String fieldPrefix = "pohjakoulutus_am";

        public final String nimike;

        @Override
        public String getNimike() {
            return nimike;
        }

        private SuomalainenAmKoulutus(String nimike) {
            this.nimike = nimike;
        }

        public static final Function<Types.MergedAnswers, ImmutableSet<SuomalainenAmKoulutus>> of = new Function<Types.MergedAnswers, ImmutableSet<SuomalainenAmKoulutus>>() {
            @Override
            public ImmutableSet<SuomalainenAmKoulutus> apply(Types.MergedAnswers answers) {
                String valinta = answers.get(fieldPrefix);

                if ("true".equals(valinta)) {
                    String nimike = answers.get(fieldPrefix + "_nimike");
                    variablesNotNull(nimike);
                    return ImmutableSet.of(new SuomalainenAmKoulutus(nimike));
                } else {
                    return ImmutableSet.of();
                }
            }
        };
    }

    // TODO: in reality a list
    public static class SuomalainenAmtKoulutus implements ProvideNimike {
        private static final String fieldPrefix = "pohjakoulutus_amt";

        public final String nimike;

        @Override
        public String getNimike() {
            return nimike;
        }

        private SuomalainenAmtKoulutus(String nimike) {
            this.nimike = nimike;
        }

        public static final Function<Types.MergedAnswers, ImmutableSet<SuomalainenAmtKoulutus>> of = new Function<Types.MergedAnswers, ImmutableSet<SuomalainenAmtKoulutus>>() {
            @Override
            public ImmutableSet<SuomalainenAmtKoulutus> apply(Types.MergedAnswers answers) {
                String valinta = answers.get(fieldPrefix);

                if ("true".equals(valinta)) {
                    String nimike = answers.get(fieldPrefix + "_nimike");
                    variablesNotNull(nimike);
                    return ImmutableSet.of(new SuomalainenAmtKoulutus(nimike));
                } else {
                    return ImmutableSet.of();
                }
            }
        };
    }

    public static class UlkomainenKansainvalinenYo {
        private static final String fieldPrefix = "pohjakoulutus_yo_ulkomainen";

        public final Types.IsoCountryCode maa;
        public final String tutkinto;

        private UlkomainenKansainvalinenYo(Types.IsoCountryCode maa, String tutkinto) {
            this.maa = maa;
            this.tutkinto = tutkinto;
        }

        public static final Function<Types.MergedAnswers, ImmutableSet<UlkomainenKansainvalinenYo>> of = new Function<Types.MergedAnswers, ImmutableSet<UlkomainenKansainvalinenYo>>() {
            @Override
            public ImmutableSet<UlkomainenKansainvalinenYo> apply(Types.MergedAnswers answers) {
                String valinta = answers.get(fieldPrefix);

                if ("true".equals(valinta)) {
                    String tutkinto = answers.get(fieldPrefix + "_tutkinto");
                    String maa = answers.get(fieldPrefix + "_maa");
                    variablesNotNull(tutkinto, maa);
                    return ImmutableSet.of(new UlkomainenKansainvalinenYo(Types.IsoCountryCode.of(maa), tutkinto));
                } else {
                    return ImmutableSet.of();
                }
            }
        };
    }

    public static class SuomalainenKansainvalinenYo {
        private static final String fieldPrefix = "pohjakoulutus_yo_kansainvalinen_suomessa";

        public final String tutkinto;

        private SuomalainenKansainvalinenYo(String tutkinto) {
            this.tutkinto = tutkinto;
        }

        public static final Function<Types.MergedAnswers, ImmutableSet<SuomalainenKansainvalinenYo>> of = new Function<Types.MergedAnswers, ImmutableSet<SuomalainenKansainvalinenYo>>() {
            @Override
            public ImmutableSet<SuomalainenKansainvalinenYo> apply(Types.MergedAnswers answers) {
                String valinta = answers.get(fieldPrefix);

                if ("true".equals(valinta)) {
                    String tutkinto = answers.get(fieldPrefix + "_tutkinto");
                    variablesNotNull(tutkinto);
                    return ImmutableSet.of(new SuomalainenKansainvalinenYo(tutkinto));
                } else {
                    return ImmutableSet.of();
                }
            }
        };
    }

    public static class SuomalainenYo {
        private static final String fieldPrefix = "pohjakoulutus_yo";

        public final String tutkinto;

        private SuomalainenYo(String tutkinto) {
            this.tutkinto = tutkinto;
        }

        public static final Function<Types.MergedAnswers, ImmutableSet<SuomalainenYo>> of = new Function<Types.MergedAnswers, ImmutableSet<SuomalainenYo>>() {
            @Override
            public ImmutableSet<SuomalainenYo> apply(Types.MergedAnswers answers) {
                String valinta = answers.get(fieldPrefix);

                if ("true".equals(valinta)) {
                    String tutkinto = answers.get(fieldPrefix + "_tutkinto");
                    variablesNotNull(tutkinto);
                    return ImmutableSet.of(new SuomalainenYo(tutkinto));
                } else {
                    return ImmutableSet.of();
                }
            }
        };
    }

    public static class SuomalainenYoAmmatillinen implements ProvideNimike {
        private static final String fieldPrefix = "pohjakoulutus_yo_ammatillinen";

        public final String nimike;

        @Override
        public String getNimike() {
            return nimike;
        }

        private SuomalainenYoAmmatillinen(String nimike) {
            this.nimike = nimike;
        }

        public static final Function<Types.MergedAnswers, ImmutableSet<SuomalainenYoAmmatillinen>> of = new Function<Types.MergedAnswers, ImmutableSet<SuomalainenYoAmmatillinen>>() {
            @Override
            public ImmutableSet<SuomalainenYoAmmatillinen> apply(Types.MergedAnswers answers) {
                String valinta = answers.get(fieldPrefix);

                if ("true".equals(valinta)) {
                    String nimike = answers.get(fieldPrefix + "_nimike");
                    variablesNotNull(nimike);
                    return ImmutableSet.of(new SuomalainenYoAmmatillinen(nimike));
                } else {
                    return ImmutableSet.of();
                }
            }
        };
    }

    public static class UlkomaalainenKoulutus implements ProvideNimike {
        private static final String fieldPrefix = "pohjakoulutus_ulk";

        public final Types.IsoCountryCode maa;
        public final String nimike;

        @Override
        public String getNimike() {
            return nimike;
        }

        private UlkomaalainenKoulutus(Types.IsoCountryCode maa, String nimike) {
            this.maa = maa;
            this.nimike = nimike;
        }

        public static final Function<Types.MergedAnswers, ImmutableSet<UlkomaalainenKoulutus>> of = new Function<Types.MergedAnswers, ImmutableSet<UlkomaalainenKoulutus>>() {
            @Override
            public ImmutableSet<UlkomaalainenKoulutus> apply(Types.MergedAnswers answers) {

                String valinta = answers.get(fieldPrefix);

                ImmutableSet.Builder<UlkomaalainenKoulutus> result = ImmutableSet.builder();

                if ("true".equals(valinta)) {
                    for (int i = 1; ; i++) {
                        String index = (i == 1) ? "" : Integer.toString(i);
                        String nimike = answers.get(fieldPrefix + "_nimike" + index);
                        String maa = answers.get(fieldPrefix + "_suoritusmaa" + index);

                        if (variablesNotNull(nimike, maa)) {
                            result.add(new UlkomaalainenKoulutus(Types.IsoCountryCode.of(maa), nimike));
                        } else {
                            break;
                        }
                    }
                }

                return result.build();
            }
        };
    }

    public static class UlkomaalainenKorkeakoulutus implements ProvideNimike {
        private static final String fieldPrefix = "pohjakoulutus_kk_ulk";

        public final String taso;
        public final Types.IsoCountryCode maa;
        public final String nimike;

        @Override
        public String getNimike() {
            return nimike;
        }

        private UlkomaalainenKorkeakoulutus(String taso, Types.IsoCountryCode maa, String nimike) {
            this.taso = taso;
            this.maa = maa;
            this.nimike = nimike;
        }

        public static final Function<Types.MergedAnswers, ImmutableSet<UlkomaalainenKorkeakoulutus>> of = new Function<Types.MergedAnswers, ImmutableSet<UlkomaalainenKorkeakoulutus>>() {
            @Override
            public ImmutableSet<UlkomaalainenKorkeakoulutus> apply(Types.MergedAnswers answers) {

                String valinta = answers.get(fieldPrefix);

                ImmutableSet.Builder<UlkomaalainenKorkeakoulutus> result = ImmutableSet.builder();

                if ("true".equals(valinta)) {
                    for (int i = 1; ; i++) {
                        String index = (i == 1) ? "" : Integer.toString(i);
                        String taso = answers.get(fieldPrefix + "_taso" + index);
                        String nimike = answers.get(fieldPrefix + "_nimike" + index);
                        String maa = answers.get(fieldPrefix + "_maa" + index);

                        if (variablesNotNull(taso, nimike, maa)) {
                            result.add(new UlkomaalainenKorkeakoulutus(taso, Types.IsoCountryCode.of(maa), nimike));
                        } else {
                            break;
                        }
                    }
                }

                return result.build();
            }
        };
    }

    public static class SuomalainenKorkeakoulutus implements ProvideNimike {
        private static final String fieldPrefix = "pohjakoulutus_kk";

        public final String taso;
        public final String nimike;

        @Override
        public String getNimike() {
            return nimike;
        }

        private SuomalainenKorkeakoulutus(String taso, String nimike) {
            this.taso = taso;
            this.nimike = nimike;
        }

        public static final Function<Types.MergedAnswers, ImmutableSet<SuomalainenKorkeakoulutus>> of = new Function<Types.MergedAnswers, ImmutableSet<SuomalainenKorkeakoulutus>>() {
            @Override
            public ImmutableSet<SuomalainenKorkeakoulutus> apply(Types.MergedAnswers answers) {
                String valinta = answers.get(fieldPrefix);

                ImmutableSet.Builder<SuomalainenKorkeakoulutus> result = ImmutableSet.builder();

                if ("true".equals(valinta)) {
                    for (int i = 1; ; i++) {
                        String index = (i == 1) ? "" : Integer.toString(i);
                        String taso = answers.get(fieldPrefix + "_taso" + index);
                        String nimike = answers.get(fieldPrefix + "_nimike" + index);

                        if (variablesNotNull(taso, nimike)) {
                            result.add(new SuomalainenKorkeakoulutus(taso, nimike));
                        } else {
                            break;
                        }
                    }
                }

                return result.build();
            }
        };
    }

    public static class HarkinnanvarainenTaiErivapaus {
        private static final String fieldPrefix = "pohjakoulutus_muu";
        public final String vuosi;
        public final String kuvaus;

        private HarkinnanvarainenTaiErivapaus(String vuosi, String kuvaus) {
            this.vuosi = vuosi;
            this.kuvaus = kuvaus;
        }

        public static final Function<Types.MergedAnswers, ImmutableSet<HarkinnanvarainenTaiErivapaus>> of = new Function<Types.MergedAnswers, ImmutableSet<HarkinnanvarainenTaiErivapaus>>() {
            @Override
            public ImmutableSet<HarkinnanvarainenTaiErivapaus> apply(Types.MergedAnswers answers) {
                String valinta = answers.get(fieldPrefix);

                if ("true".equals(valinta)) {
                    String vuosi = answers.get(fieldPrefix + "_vuosi");
                    String kuvaus = answers.get(fieldPrefix + "_kuvaus");
                    variablesNotNull(vuosi, kuvaus);
                    return ImmutableSet.of(new HarkinnanvarainenTaiErivapaus(vuosi, kuvaus));
                } else {
                    return ImmutableSet.of();
                }
            }
        };
    }
}

