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

import static com.bloatit.framework.webprocessor.context.Context.tr;

import com.bloatit.framework.exceptions.lowlevel.RedirectException;
import com.bloatit.framework.webprocessor.PageNotFoundException;
import com.bloatit.framework.webprocessor.annotations.ParamConstraint;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlParagraph;
import com.bloatit.framework.webprocessor.components.HtmlTitle;
import com.bloatit.framework.webprocessor.components.form.HtmlForm;
import com.bloatit.framework.webprocessor.components.form.HtmlSubmit;
import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
import com.bloatit.framework.webprocessor.components.meta.XmlNode;
import com.bloatit.framework.webprocessor.components.renderer.HtmlRawTextRenderer;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.Bug;
import com.bloatit.model.FileMetadata;
import com.bloatit.web.linkable.features.FeaturePage;
import com.bloatit.web.linkable.usercontent.AttachmentField;
import com.bloatit.web.linkable.usercontent.CreateCommentForm;
import com.bloatit.web.pages.master.Breadcrumb;
import com.bloatit.web.pages.master.MasterPage;
import com.bloatit.web.pages.tools.CommentTools;
import com.bloatit.web.url.AddAttachementActionUrl;
import com.bloatit.web.url.BugPageUrl;
import com.bloatit.web.url.CreateCommentActionUrl;
import com.bloatit.web.url.FileResourceUrl;
import com.bloatit.web.url.ModifyBugPageUrl;

@ParamContainer("feature/bug")
public final class BugPage extends MasterPage {

    @ParamConstraint(optionalErrorMsg = @tr("You have to specify a bug number."))
    @RequestParam(name = "id", conversionErrorMsg = @tr("I cannot find the bug number: ''%value''."))
    private final Bug bug;

    private final BugPageUrl url;

    public BugPage(final BugPageUrl url) {
        super(url);
        this.url = url;
        this.bug = url.getBug();
    }

    @Override
    protected HtmlElement createBodyContent() throws RedirectException {
        if (url.getMessages().hasMessage()) {
            throw new PageNotFoundException();
        }

        final HtmlDiv box = new HtmlDiv("padding_box");

        HtmlTitle bugTitle;
        bugTitle = new HtmlTitle(bug.getTitle(), 1);
        box.add(bugTitle);

        box.add(new HtmlParagraph(tr("State: {0}", BindedState.getBindedState(bug.getState()))));
        box.add(new HtmlParagraph(tr("Level: {0}", BindedLevel.getBindedLevel(bug.getErrorLevel()))));

        box.add(new ModifyBugPageUrl(bug).getHtmlLink(tr("Modify the bug's properties")));

        final HtmlParagraph description = new HtmlParagraph(new HtmlRawTextRenderer(bug.getDescription()));
        box.add(description);

        // Attachments

        for (final FileMetadata attachment : bug.getFiles()) {
            final HtmlParagraph attachmentPara = new HtmlParagraph();
            attachmentPara.add(new FileResourceUrl(attachment).getHtmlLink(attachment.getFileName()));
            attachmentPara.addText(tr(": ") + attachment.getShortDescription());
            box.add(attachmentPara);
        }

        if (bug.isOwner()) {
            box.add(generateNewAttachementForm());
        }

        box.add(CommentTools.generateCommentList(bug.getComments()));
        box.add(new CreateCommentForm(new CreateCommentActionUrl(bug)));

        return box;
    }

    @Override
    protected String createPageTitle() {
        if (bug != null) {
            return tr("Bug - ") + bug.getTitle();
        }
        return tr("Bug - No bug");
    }

    @Override
    public boolean isStable() {
        return true;
    }

    private XmlNode generateNewAttachementForm() {
        final AddAttachementActionUrl targetUrl = new AddAttachementActionUrl(bug);
        final HtmlForm addAttachementForm = new HtmlForm(targetUrl.urlString());
        addAttachementForm.enableFileUpload();

        addAttachementForm.add(new AttachmentField(targetUrl, "2 Gio"));

        addAttachementForm.add(new HtmlSubmit(Context.tr("Add attachment")));
        return addAttachementForm;
    }

    @Override
    protected Breadcrumb createBreadcrumb() {
        return BugPage.generateBreadcrumb(bug);
    }

    public static Breadcrumb generateBreadcrumb(final Bug bug) {
        final Breadcrumb breadcrumb = FeaturePage.generateBreadcrumbBugs(bug.getFeature());

        breadcrumb.pushLink(new BugPageUrl(bug).getHtmlLink(tr("Bug #") + bug.getId()));

        return breadcrumb;
    }

}
