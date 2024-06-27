package fi.vm.sade.hakemuseditori.hakemus.hakuapp;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.Date;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class ApplicationAttachmentRequest implements Serializable {

    public static enum ReceptionStatus {
        ARRIVED,
        ARRIVED_LATE,
        NOT_RECEIVED
    }

    public static enum ProcessingStatus{
        CHECKED,
        NOT_CHECKED,
        INADEQUATE,
        COMPLEMENT_REQUESTED,
        UNNECESSARY
    }

    private final String id;
    private final String preferenceAoId;
    private final String preferenceAoGroupId;
    private ApplicationAttachmentRequest.ReceptionStatus receptionStatus;
    private ApplicationAttachmentRequest.ProcessingStatus processingStatus;
    private final ApplicationAttachment applicationAttachment;

    @JsonCreator
    public ApplicationAttachmentRequest(@JsonProperty(value = "id") final String id,
                                        @JsonProperty(value = "preferenceAoId") final String preferenceAoId,
                                        @JsonProperty(value = "preferenceAoGroupId") final String preferenceAoGroupId,
                                        @JsonProperty(value = "requestStatus") final ApplicationAttachmentRequest.ReceptionStatus receptionStatus,
                                        @JsonProperty(value = "processingStatus") final ApplicationAttachmentRequest.ProcessingStatus processingStatus,
                                        @JsonProperty(value = "applicationAttachment") final ApplicationAttachment applicationAttachment) {
        this.id = id;
        this.preferenceAoId = preferenceAoId;
        this.preferenceAoGroupId = preferenceAoGroupId;
        this.receptionStatus = receptionStatus;
        this.processingStatus = processingStatus;
        this.applicationAttachment = applicationAttachment;
    }

    public String getId(){
        return id;
    }

    public String getPreferenceAoId() {
        return preferenceAoId;
    }

    public String getPreferenceAoGroupId() {
        return preferenceAoGroupId;
    }

    public ApplicationAttachmentRequest.ReceptionStatus getReceptionStatus() {
        return receptionStatus;
    }

    public void setReceptionStatus(final ApplicationAttachmentRequest.ReceptionStatus receptionStatus) {
        this.receptionStatus = receptionStatus;
    }

    public ApplicationAttachmentRequest.ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(final ApplicationAttachmentRequest.ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public ApplicationAttachment getApplicationAttachment() {
        return applicationAttachment;
    }

    @Override
    public ApplicationAttachmentRequest clone() {
        ApplicationAttachment aa = null;
        if(this.applicationAttachment != null) {
            aa = this.applicationAttachment.clone();
        }
        return new ApplicationAttachmentRequest(this.id, this.preferenceAoId, this.preferenceAoGroupId, this.receptionStatus, this.processingStatus, aa);
    }

    @Override
    public boolean equals(Object object) {
        if(object == null) {
            return false;
        }

        if(!(object instanceof ApplicationAttachmentRequest)) {
            return false;
        }
        ApplicationAttachmentRequest newReq = (ApplicationAttachmentRequest)object;

        if(this.preferenceAoId == null && this.preferenceAoGroupId == null) {
            return false;
        } else if(this.preferenceAoId != null && !this.preferenceAoId.equals(newReq.preferenceAoId)) {
            return false;
        } else if(this.preferenceAoId == null && this.preferenceAoGroupId != null) {
            if(!this.preferenceAoGroupId.equals(newReq.preferenceAoGroupId) || !this.id.equals(newReq.id)) {
                return false;
            }
        }

        if(this.applicationAttachment == null && newReq.getApplicationAttachment() != null) {
            return false;
        } else if(this.applicationAttachment == null && newReq.getApplicationAttachment() == null) {
            return true;
        }
        return this.applicationAttachment.comparePartially(newReq.getApplicationAttachment(),true);
    }

}

