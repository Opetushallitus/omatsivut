package fi.vm.sade.hakemuseditori.hakemus.hakuapp;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.*;


@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
@JsonIgnoreProperties({ "type", "personOidChecked", "studentOidChecked" })
public class Application implements Serializable {

    private static final String[] EXCLUDED_FIELDS = new String[]{"id"};

    public enum State {
        ACTIVE, PASSIVE, INCOMPLETE, SUBMITTED, DRAFT
    }

    public enum PostProcessingState {
        NOMAIL, FULL, DONE, FAILED
    }

    public enum PaymentState {
        NOTIFIED, // Successfully notified payment system
        OK, // Payment done according to payment system
        NOT_OK // Payment system indicated that payment will not be made
    }

    private PaymentState requiredPaymentState;

    public boolean paymentIsOk() {
        return requiredPaymentState == null || requiredPaymentState == PaymentState.OK;
    }


    private static final long serialVersionUID = -7491168801255850954L;

    @JsonProperty(value = "_id")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL, using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private org.bson.types.ObjectId id; //NOSONAR Json-sarjallistajan käyttämä.

    private String oid;
    private Application.State state;
    private String applicationSystemId;
    private String personOid;
    private Date received;
    private Date updated;
    private Application.PostProcessingState redoPostProcess;
    private Map<String, Map<String, String>> answers = new HashMap<String, Map<String, String>>();

    private Map<String, String> meta = new HashMap<String, String>(); // TODO remove
    private List<ApplicationAttachmentRequest> attachmentRequests = new ArrayList<ApplicationAttachmentRequest>();


    @JsonIgnore
    public boolean isActive() {
        return state != null && state.equals(Application.State.ACTIVE);
    }

    @JsonIgnore
    public boolean isIncomplete() {
        return state != null && state.equals(Application.State.INCOMPLETE);
    }

    @JsonIgnore
    public boolean isSubmitted() {
        return state != null && state.equals(Application.State.SUBMITTED);
    }

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

    @JsonIgnore
    public Map<String, String> getVastauksetMerged() {
        Map<String, String> answers = new HashMap<String, String>(200);
        for (Map<String, String> phaseAnswers : this.answers.values()) {
            answers.putAll(phaseAnswers);
        }
        answers = addMetaToAnswers(answers);
        return Collections.unmodifiableMap(answers);
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

    public PostProcessingState getRedoPostProcess() {
        return redoPostProcess;
    }
    public Application setUpdated(Date updated) {
        this.updated = updated; return this;
    }

    public Date getUpdated() { return updated; }


    public List<ApplicationAttachmentRequest> getAttachmentRequests() {
        return attachmentRequests;
    }

    public Application.PaymentState getRequiredPaymentState() {
        return requiredPaymentState;
    }

    private Map<String, String> addMetaToAnswers(Map<String, String> answers) {
        for (Map.Entry<String, String> entry : meta.entrySet()) {
            String key = "_meta_" + entry.getKey();
            String value = entry.getValue();
            answers.put(key, value);
        }
        return answers;
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

