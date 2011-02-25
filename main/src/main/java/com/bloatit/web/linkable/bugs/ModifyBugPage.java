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
package com.bloatit.web.linkable.bugs;

import java.util.EnumSet;

import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.annotations.RequestParam.Role;
import com.bloatit.framework.webserver.components.HtmlDiv;
import com.bloatit.framework.webserver.components.HtmlTitleBlock;
import com.bloatit.framework.webserver.components.form.FormFieldData;
import com.bloatit.framework.webserver.components.form.HtmlDropDown;
import com.bloatit.framework.webserver.components.form.HtmlForm;
import com.bloatit.framework.webserver.components.form.HtmlSubmit;
import com.bloatit.framework.webserver.components.form.HtmlTextArea;
import com.bloatit.framework.webserver.components.meta.HtmlElement;
import com.bloatit.model.Bug;
import com.bloatit.model.demand.DemandManager;
import com.bloatit.web.pages.LoggedPage;
import com.bloatit.web.url.ModifyBugActionUrl;
import com.bloatit.web.url.ModifyBugPageUrl;

/**
 * Page that hosts the form to create a new Idea
 */
@ParamContainer("demand/bug/modify")
public final class ModifyBugPage extends LoggedPage {

    private static final int BUG_CHANGE_COMMENT_INPUT_NB_LINES = 5;
    private static final int BUG_CHANGE_COMMENT_INPUT_NB_COLUMNS = 80;

    public static final String BUG = "bug";

    @RequestParam(name = BUG, role = Role.GET)
    private final Bug bug;

    public ModifyBugPage(final ModifyBugPageUrl modifyBugPageUrl) {
        super(modifyBugPageUrl);
        bug = modifyBugPageUrl.getBug();
    }

    @Override
    protected String getPageTitle() {
        return "Modify a bug";
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public HtmlElement createRestrictedContent() {
        if (DemandManager.canCreate(session.getAuthToken())) {
            return new HtmlDiv("padding_box").add(generateModifyBugForm());
        }
        return generateBadRightError();
    }

    private HtmlElement generateModifyBugForm() {
        final HtmlTitleBlock formTitle = new HtmlTitleBlock(Context.tr("Modify a bug"), 1);
        final ModifyBugActionUrl doModifyUrl = new ModifyBugActionUrl(bug);

        // Create the form stub
        final HtmlForm modifyBugForm = new HtmlForm(doModifyUrl.urlString());

        formTitle.add(modifyBugForm);

        // Level
        final FormFieldData levelFormFieldData = doModifyUrl.getLevelParameter().formFieldData();
        final HtmlDropDown levelInput = new HtmlDropDown(levelFormFieldData, Context.tr("New Level"));
        // TODO: IMPORTANT set the current value as default value
        levelInput.addDropDownElements(EnumSet.allOf(BindedLevel.class));
        levelInput.setComment(Context.tr("New level of the bug. Current level is ''{0}''.", BindedLevel.getBindedLevel(bug.getErrorLevel())));
        modifyBugForm.add(levelInput);

        // State
        final FormFieldData stateFormFieldData = doModifyUrl.getStateParameter().formFieldData();
        final HtmlDropDown stateInput = new HtmlDropDown(stateFormFieldData, Context.tr("New state"));
        // TODO: IMPORTANT set the current value as default value
        stateInput.addDropDownElements(EnumSet.allOf(BindedState.class));
        stateInput.setComment(Context.tr("New state of the bug. Current state is ''{0}''.", BindedState.getBindedState(bug.getState())));
        modifyBugForm.add(stateInput);

        // Create the fields that will describe the reason of bug change
        final FormFieldData descriptionFormFieldData = doModifyUrl.getReasonParameter().formFieldData();
        final HtmlTextArea descriptionInput = new HtmlTextArea(descriptionFormFieldData,
                                                               Context.tr("Reason"),
                                                               BUG_CHANGE_COMMENT_INPUT_NB_LINES,
                                                               BUG_CHANGE_COMMENT_INPUT_NB_COLUMNS);
        descriptionInput.setComment(Context.tr("Optional. Enter the reason of the bug."));
        modifyBugForm.add(descriptionInput);

        modifyBugForm.add(new HtmlSubmit(Context.tr("Modify the bug")));

        final HtmlDiv group = new HtmlDiv();
        group.add(formTitle);
        return group;
    }

    private HtmlElement generateBadRightError() {
        final HtmlDiv group = new HtmlDiv();

        return group;
    }

    @Override
    public String getRefusalReason() {
        return Context.tr("You must be logged to modify a bug.");
    }

}
