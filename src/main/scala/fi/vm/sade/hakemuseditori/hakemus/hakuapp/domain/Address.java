package fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class Address implements Serializable {
    private final String recipient;
    private final String streetAddress;
    private final String streetAddress2;
    private final String postalCode;
    private final String postOffice;

    public static final Address EMPTY = new Address("", "", "", "", "");

    @JsonCreator
    public Address(@JsonProperty(value = "recipient") String recipient,
                   @JsonProperty(value = "streetAddress") String streetAddress,
                   @JsonProperty(value = "streetAddress2") String streetAddress2,
                   @JsonProperty(value = "postalCode") String postalCode,
                   @JsonProperty(value = "postOffice") String postOffice) {
        this.recipient = recipient;
        this.streetAddress = streetAddress;
        this.streetAddress2 = streetAddress2;
        this.postalCode = postalCode;
        this.postOffice = postOffice;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getPostOffice() {
        return postOffice;
    }

    public String getStreetAddress2() {
        return streetAddress2;
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Address)) {
            return false;
        }
        Address other = (Address)object;
        return addrEquals(this.recipient, other.recipient)
            && addrEquals(this.streetAddress, other.streetAddress)
            && addrEquals(this.streetAddress2, other.streetAddress2)
            && addrEquals(this.postalCode, other.postalCode)
            && addrEquals(this.postOffice, other.postOffice);
    }

    /**
     * Returns are two address equal, with support for ""==null
     */
    private boolean addrEquals(String addr1, String addr2) {
        if("".equals(addr1)) {
            addr1 = null;
        }
        if("".equals(addr2)) {
            addr2 = null;
        }
        return StringUtils.equals(addr1,addr2);
    }

    @Override
    public Address clone() {
        return new Address(recipient,streetAddress,streetAddress2,postalCode,postOffice);
    }


}

