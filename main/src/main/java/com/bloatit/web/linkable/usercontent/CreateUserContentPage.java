//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.web.linkable.usercontent;

import com.bloatit.data.DaoTeamRight.UserTeamRight;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.components.form.HtmlForm;
import com.bloatit.model.Member;
import com.bloatit.web.pages.LoggedPage;
import com.bloatit.web.url.CreateUserContentPageUrl;
import com.bloatit.web.url.UserContentActionUrl;

/**
 * @author thomas
 */
@ParamContainer("usercontent/create")
public abstract class CreateUserContentPage extends LoggedPage {

    private final UserContentActionUrl targetUrl;

    public CreateUserContentPage(final CreateUserContentPageUrl url, final UserContentActionUrl targetUrl) {
        super(url);
        this.targetUrl = targetUrl;
    }

    @Override
    public boolean isStable() {
        return false;
    }

    /**
     * Add a asTeam field into the form (see: {@link #getForm()}).
     * 
     * @param me The user creating this userContent
     * @param right The type of action we are doing. For example if this
     *            userContent is a comment you should use
     *            {@link UserTeamRight#TALK} ; But if it is a contribution, you
     *            have to use the {@link UserTeamRight#BANK}.
     * @param label The asTeam field label.
     * @param comment The comment of the field.
     */
    protected void addAsTeamField(final HtmlForm form, final Member me, final UserTeamRight right, final String label, final String comment) {
        form.add(new AsTeamField(targetUrl, me, right, label, comment));
    }

    /**
     * Add a language selector into the {@link #getForm()} form.
     * 
     * @param label the label of the language selector.
     * @param comment the comment on the language selector field.
     */
    protected void addLanguageField(final HtmlForm form, final String label, final String comment) {
        form.add(new LanguageField(targetUrl, label, comment));
    }

    protected void addAddAttachmentField(final HtmlForm form, final String size) {
        form.add(new AttachmentField(targetUrl, size));
        form.enableFileUpload();
    }

    protected final UserContentActionUrl getTargetUrl() {
        return targetUrl;
    }
}
