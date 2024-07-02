package fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain;

import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.elements.I18nText;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.Date;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class ApplicationAttachment implements Serializable {

    private final I18nText name;
    private final I18nText header;
    private final I18nText description;
    private final Date deadline;
    private final I18nText deliveryNote;
    private final Address address;
    private final String emailAddress;

    @JsonCreator
    public ApplicationAttachment(@JsonProperty(value = "name") I18nText name,
                                 @JsonProperty(value = "header") I18nText header,
                                 @JsonProperty(value = "description") I18nText description,
                                 @JsonProperty(value = "deadline") Date deadline,
                                 @JsonProperty(value = "deliveryNote") I18nText deliveryNote,
                                 @JsonProperty(value = "address") Address address,
                                 @JsonProperty(value = "emailAddress") String emailAddress) {
        this.name = name;
        this.header = header;
        this.description = description;
        this.deadline = deadline;
        this.deliveryNote = deliveryNote;
        this.address = address;
        this.emailAddress = emailAddress;
    }

    public I18nText getName() {
        return name;
    }

    public I18nText getHeader() {
        return header;
    }

    public I18nText getDescription() {
        return description;
    }

    public Date getDeadline() {
        return deadline;
    }

    public I18nText getDeliveryNote() {
        return deliveryNote;
    }

    public Address getAddress() {
        return address;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public ApplicationAttachment clone() {
        Date dl = null;
        if(this.deadline != null) {
            dl = (Date) this.deadline.clone();
        }
        Address addr = null;
        if(this.address != null) {
            addr = this.address.clone();
        }

        return new ApplicationAttachment(I18nText.copy(this.name), I18nText.copy(this.header), I18nText.copy(this.description), dl, I18nText.copy(this.deliveryNote), addr, this.emailAddress);
    }

    @Override
    public boolean equals(Object object) {
        return comparePartially(object, false);
    }

    public boolean comparePartially(Object object, boolean skipChanging) {
        if(object == null) {
            return false;
        }

        if(!(object instanceof ApplicationAttachment)) {
            return false;
        }
        ApplicationAttachment newObj = (ApplicationAttachment)object;

        if((!I18nText.compare(this.name, newObj.name)) ||
            (!I18nText.compare(this.header, newObj.header)) ||
            (!I18nText.compare(this.description, newObj.description))) {
            return false;
        }

        if(skipChanging == false && !I18nText.compare(this.deliveryNote, newObj.deliveryNote)) {
            return false;
        }

        if(deadline == null && newObj.deadline != null) {
            return false;
        } else if(deadline != null && !deadline.equals(newObj.deadline)) {
            return false;
        }

        if(emailAddress == null && newObj.emailAddress != null) {
            return false;
        } else if(emailAddress != null && !emailAddress.equals(newObj.emailAddress)) {
            return false;
        }

        if(address == null && newObj.address != null) {
            return false;
        } else if(address != null && !address.equals(newObj.address)) {
            return false;
        }

        return true;

    }

}

