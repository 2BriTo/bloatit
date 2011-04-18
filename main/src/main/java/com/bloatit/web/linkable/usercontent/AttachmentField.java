package com.bloatit.web.linkable.usercontent;

import com.bloatit.framework.webprocessor.components.PlaceHolderElement;
import com.bloatit.framework.webprocessor.components.form.FieldData;
import com.bloatit.framework.webprocessor.components.form.HtmlFileInput;
import com.bloatit.framework.webprocessor.components.form.HtmlTextField;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.web.url.UserContentActionUrl;

public class AttachmentField extends PlaceHolderElement {

    public AttachmentField(final UserContentActionUrl targetUrl, final String size) {
        this(targetUrl,
             Context.tr("Join a file"),
             Context.tr("Max size {0}. When you join a file, you have to add a description.", size),
             Context.tr("File description"),
             Context.tr("Input a short description of the file you want to upload."));
    }

    /**
     * Do not forget the: form.enableFileUpload();
     * 
     * @param targetUrl
     * @param attachmentLabel
     * @param attachmentComment
     * @param descriptionLabel
     * @param descriptionComment
     */
    public AttachmentField(final UserContentActionUrl targetUrl,
                           final String attachmentLabel,
                           final String attachmentComment,
                           final String descriptionLabel,
                           final String descriptionComment) {
        super();
        // Attachment
        final FieldData attachedFileData = targetUrl.getAttachmentParameter().pickFieldData();
        final HtmlFileInput attachedFileInput = new HtmlFileInput(attachedFileData.getName(), attachmentLabel);
        attachedFileInput.setDefaultValue(attachedFileData.getSuggestedValue());
        attachedFileInput.addErrorMessages(attachedFileData.getErrorMessages());
        attachedFileInput.setComment(attachmentComment);
        add(attachedFileInput);

        final FieldData attachmentDescriptiondData = targetUrl.getAttachmentDescriptionParameter().pickFieldData();
        final HtmlTextField attachmentDescriptionInput = new HtmlTextField(attachmentDescriptiondData.getName(), descriptionLabel);
        attachmentDescriptionInput.setDefaultValue(attachmentDescriptiondData.getSuggestedValue());
        attachmentDescriptionInput.addErrorMessages(attachmentDescriptiondData.getErrorMessages());
        attachmentDescriptionInput.setComment(descriptionComment);
        add(attachmentDescriptionInput);
    }

}