package fi.vm.sade.hakemuseditori.hakemus.hakuapp;

import java.util.Map;

public final class Types {
    protected static class Base<T> {
        private final T value;

        protected Base(T value) {
            if (value == null) {
                throw new IllegalArgumentException("Typed value cannot be initialized to null");
            }
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public String toString() {
            return value.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Base<?> base = (Base<?>) o;

            return !(value != null ? !value.equals(base.value) : base.value != null);

        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    public static final class ApplicationOptionOid extends Oid {
        private ApplicationOptionOid(String value) {
            super(value);
        }

        public static ApplicationOptionOid of(String value) {
            return new ApplicationOptionOid(value);
        }
    }

    public static final class ApplicationOid extends Oid {
        private ApplicationOid(String value) {
            super(value);
        }

        public static ApplicationOid of(String value) {
            return new ApplicationOid(value);
        }
    }

    public static final class PersonOid extends Oid {
        private PersonOid(String value) {
            super(value);
        }

        public static PersonOid of(String value) {
            return new PersonOid(value);
        }
    }

    public static final class IsoCountryCode extends SafeString {
        private IsoCountryCode(String value) {
            super(value);
            if (value.length() != 3) {
                throw new IllegalArgumentException("Country code must be 3 characters long, got '" + value + "'");
            }
        }

        public static IsoCountryCode of(String value) {
            return new IsoCountryCode(value);
        }
    }

    public static final class ApplicationSystemOid extends Oid {
        private ApplicationSystemOid(String value) {
            super(value);
        }

        public static ApplicationSystemOid of(String value) {
            return new ApplicationSystemOid(value);
        }
    }

    public static final class MergedAnswers extends Base<Map<String, String>> {
        private MergedAnswers(Map<String, String> value) {
            super(value);
        }

        public static MergedAnswers of(Map<String, String> value) {
            return new MergedAnswers(value);
        }

        public static MergedAnswers of(Application application) {
            return of(application.getVastauksetMerged());
        }

        public String get(String field) {
            return getValue().get(field);
        }

        public String getOrElse(String field, String def) {
            if (getValue().containsKey(field))
                return getValue().get(field);
            else return def;
        }
    }

    public static class SafeString extends Base<String> {
        private SafeString(String value) {
            super(value);
            if (value.isEmpty()) {
                throw new IllegalArgumentException("Safe String cannot be empty");
            }
        }

        public static SafeString of(String value) {
            return new SafeString(value);
        }
    }

    // Extend "Oid" to reflect real purpose of the OID
    private static abstract class Oid extends SafeString {
        private Oid(String value) {
            super(value);
            if (!value.matches("[0-9]+(.[.0-9]+)*")) {
                throw new IllegalArgumentException("OID must consist of dot-separated integers, got: '" + value + "'");
            }
        }
    }
}
