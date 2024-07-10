package fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.*;


public class Application implements Serializable {

    public Application() {}

    public Application(
        String oid,
        String applicationSystemId,
        String personOid) {
        this.oid = oid;
        this.applicationSystemId = applicationSystemId;
        this.personOid = personOid;
    }
    private static final String[] EXCLUDED_FIELDS = new String[]{"id"};

    public enum State {
        ACTIVE, PASSIVE, INCOMPLETE, SUBMITTED, DRAFT
    }
    public static final String VAIHE_ID = "phaseId";

    private String oid;
    private Application.State state;
    private String applicationSystemId; // hakuOid

    private String phaseId; // käytetään testifixtureissa, ei välttämättä tarpeen
    private String personOid;
    private Date received;
    private Date updated;
    private Map<String, Map<String, String>> answers = new HashMap<String, Map<String, String>>();

    public Map<String, Map<String, String>> getAnswers() {
        return answers;
    }

    public String getOid() {
        return oid;
    }

    public Application setOid(String oid) {
        this.oid = oid; return this;
    }

    public Application setState(Application.State state) {
        this.state = state; return this;
    }

    public Application.State getState() {
        return this.state;
    }

    public String getApplicationSystemId() {
        return applicationSystemId;
    }

    public String getPersonOid() {
        return personOid;
    }

    public Application setPersonOid(String personOid) {
        this.personOid = personOid; return this;
    }

    public Application setReceived(Date received) {
        this.received = received;
        if(this.updated == null){
            this.updated = received;
        }
        return this;
    }

    public Date getReceived() {
        return received;
    }

    public Application setUpdated(Date updated) {
        this.updated = updated; return this;
    }

    public Date getUpdated() { return updated; }


    public Map<String, String> getPhaseAnswers(final String phaseId) {
        Map<String, String> phaseAnswers = this.answers.get(phaseId);
        if (phaseAnswers != null && !phaseAnswers.isEmpty()) {
            return Collections.unmodifiableMap(phaseAnswers);
        }
        return new HashMap<String, String>();
    }

    // käytetään testifixtureissa
    public final Application setVaiheenVastauksetAndSetPhaseId(final String phaseId, Map<String, String> answers) {
        this.phaseId = answers.get(VAIHE_ID);
        Map<String, String> answersWithoutPhaseId = new HashMap<String, String>(
            Maps.filterKeys(answers, Predicates.not(Predicates.equalTo(VAIHE_ID))));
        this.answers.put(phaseId, answersWithoutPhaseId);
        return this;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, null, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        return EqualsBuilder.reflectionEquals(this, o, false, null, EXCLUDED_FIELDS);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(7, 23, this, false, null, EXCLUDED_FIELDS);
    }
}

