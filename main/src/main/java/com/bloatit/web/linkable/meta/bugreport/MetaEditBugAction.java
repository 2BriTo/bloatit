/*
 * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * BloatIt is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details. You should have received a copy of the GNU Affero General Public
 * License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web.linkable.meta.bugreport;

import java.io.IOException;

import com.bloatit.framework.meta.MetaBugManager;
import com.bloatit.framework.webprocessor.annotations.ParamConstraint;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Member;
import com.bloatit.model.right.AuthToken;
import com.bloatit.web.actions.LoggedAction;
import com.bloatit.web.url.MetaBugsListPageUrl;
import com.bloatit.web.url.MetaEditBugActionUrl;

/**
 * A response to a form used to create a new feature
 */
@ParamContainer("meta/bug/doedit")
public final class MetaEditBugAction extends LoggedAction {

    private static final String BUG_DESCRIPTION = "bug_description";

    @RequestParam(name = BUG_DESCRIPTION, role = Role.POST)
    @ParamConstraint(max = "800",
                     maxErrorMsg = @tr("The title must be 800 chars length max."), //
                     min = "1", minErrorMsg = @tr("The title must have at least 10 chars."),
                     optionalErrorMsg = @tr("Error you forgot to write a title"))
    private final String description;

    @RequestParam
    private final String bugId;

    private final MetaEditBugActionUrl url;

    public MetaEditBugAction(final MetaEditBugActionUrl url) {
        super(url);
        this.url = url;
        this.description = url.getDescription();
        this.bugId = url.getBugId();
    }

    @Override
    protected Url doProcessRestricted(final Member me) {
        try {
            MetaBugManager.getById(bugId).update(description);
            session.notifyGood("Bug updated.");
        } catch (final IOException e) {
            session.notifyError("A problem occur during the bug update process! Please report this bug! :)");
            return doProcessErrors();
        }

        // TODO: add link system in documentation

        return new MetaBugsListPageUrl();
    }

    @Override
    protected Url doProcessErrors(AuthToken authToken) {
        session.addParameter(url.getDescriptionParameter());
        return session.getLastVisitedPage();
    }

    @Override
    protected void transmitParameters() {
        // nothing
    }

    @Override
    protected Url checkRightsAndEverything(final Member me) {
        return NO_ERROR;
    }

    @Override
    protected String getRefusalReason() {
        return Context.tr("You have to be logged in to edit a metabug.");
    }
}
