package fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain;

import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.Application;
import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.ApplicationAttachment;
import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.ApplicationAttachmentRequest;

import java.util.ArrayList;
import java.util.List;

public class AttachmentUtil {
    public static List<ApplicationAttachment> resolveAttachments(Application application) {
        List<ApplicationAttachmentRequest> attachmentRequests = application.getAttachmentRequests();
        List<ApplicationAttachment> attachments = new ArrayList<ApplicationAttachment>(attachmentRequests.size());
        for (ApplicationAttachmentRequest attachmentRequest : attachmentRequests) {
            attachments.add(attachmentRequest.getApplicationAttachment());
        }
        return attachments;
    }
}
