/*
 * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free software: you
 * can redistribute it and/or modify it under the terms of the GNU Affero General Public
 * License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version. BloatIt is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details. You should have received a copy of the GNU Affero General
 * Public License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web.pages;

import static com.bloatit.framework.webserver.Context.tr;

import com.bloatit.framework.exceptions.general.ShallNotPassException;
import com.bloatit.framework.exceptions.specific.RedirectException;
import com.bloatit.framework.exceptions.specific.UnauthorizedOperationException;
import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.components.HtmlDiv;
import com.bloatit.framework.webserver.components.HtmlTitle;
import com.bloatit.framework.webserver.components.form.FieldData;
import com.bloatit.framework.webserver.components.form.HtmlForm;
import com.bloatit.framework.webserver.components.form.HtmlSubmit;
import com.bloatit.framework.webserver.components.form.HtmlTextArea;
import com.bloatit.framework.webserver.components.meta.HtmlElement;
import com.bloatit.model.Comment;
import com.bloatit.web.actions.CommentCommentAction;
import com.bloatit.web.linkable.bugs.BugPage;
import com.bloatit.web.linkable.features.FeaturePage;
import com.bloatit.web.linkable.release.ReleasePage;
import com.bloatit.web.pages.master.Breadcrumb;
import com.bloatit.web.url.CommentCommentActionUrl;
import com.bloatit.web.url.CommentReplyPageUrl;

/**
 * Page that hosts the form used to reply to an existing comment
 */
@ParamContainer("comment/reply")
public final class CommentReplyPage extends LoggedPage {

    private static final int NB_LINES = 10;
    private static final int NB_COLUMNS = 80;

    private final CommentReplyPageUrl url;

    @RequestParam(name = CommentCommentAction.COMMENT_TARGET)
    private final Comment targetComment;

    public CommentReplyPage(final CommentReplyPageUrl url) {
        super(url);
        this.url = url;
        this.targetComment = url.getTargetComment();
    }

    @Override
    public void processErrors() throws RedirectException {
        session.notifyList(url.getMessages());
    }

    @Override
    public HtmlElement createRestrictedContent() throws RedirectException {


        final HtmlDiv box = new HtmlDiv("padding_box");

        final HtmlTitle title = new HtmlTitle(Context.tr("Reply to a comment"), 1);

        final CommentCommentActionUrl commentCommentActionUrl = new CommentCommentActionUrl(targetComment);
        final HtmlForm form = new HtmlForm(commentCommentActionUrl.urlString());

        final FieldData commentData = commentCommentActionUrl.getCommentParameter().pickFieldData();
        final HtmlTextArea commentInput = new HtmlTextArea(commentData.getName(), Context.tr("Content"), NB_LINES, NB_COLUMNS);
        commentInput.setDefaultValue(commentData.getSuggestedValue());
        commentInput.addErrorMessages(commentData.getErrorMessages());
        form.add(commentInput);

        final HtmlSubmit submit = new HtmlSubmit(Context.tr("Submit"));
        form.add(submit);

        box.add(title);
        box.add(form);

        return box;
    }

    @Override
    public String getRefusalReason() {
        return Context.tr("you must be logged in to comment.");
    }

    @Override
    protected String getPageTitle() {
        return Context.tr("Reply to a comment");
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    protected Breadcrumb getBreadcrumb() {
        return CommentReplyPage.generateBreadcrumb(targetComment);
    }

    public static Breadcrumb generateBreadcrumb(Comment comment) {

        Breadcrumb breadcrumb;

        switch(comment.getRootParentType()) {
            case BUG:
                breadcrumb = BugPage.generateBreadcrumb(comment.getRootComment().getParentBug());
                break;
            case FEATURE:
                breadcrumb = FeaturePage.generateBreadcrumb(comment.getRootComment().getParentFeature());
                break;
            case RELEASE:
                breadcrumb = ReleasePage.generateBreadcrumb(comment.getRootComment().getParentRelease());
                break;
            default:
                breadcrumb = new Breadcrumb();
        }

        try {
            breadcrumb.pushLink(new CommentReplyPageUrl(comment).getHtmlLink(tr("Reply to {0}''s comment",comment.getAuthor().getDisplayName())));
        } catch (UnauthorizedOperationException e) {
            Context.getSession().notifyBad("For an obscure reason you cannot see a user name, please warn us of the bug");
            throw new ShallNotPassException("Error displaying a user name",e);
        }

        return breadcrumb;
    }


}
