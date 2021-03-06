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
package com.bloatit.web.linkable.documentation;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import com.bloatit.framework.exceptions.lowlevel.RedirectException;
import com.bloatit.framework.webprocessor.PageNotFoundException;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.web.linkable.IndexPage;
import com.bloatit.web.linkable.documentation.HtmlDocumentationRenderer.DocumentationType;
import com.bloatit.web.linkable.master.BoxLayout;
import com.bloatit.web.linkable.master.Breadcrumb;
import com.bloatit.web.linkable.master.ElveosPage;
import com.bloatit.web.linkable.master.sidebar.TwoColumnLayout;
import com.bloatit.web.url.DocumentationPageUrl;
import com.bloatit.web.url.DocumentationRootPageUrl;

/**
 * <p>
 * A holding class for documentation
 * </p>
 * <p>
 * Documentation system is based on markdown files hosted on the server. This
 * page is a container used to view these markdown documents. <br />
 * Document to display is chosen via the GET parameter Documenvalue
 * tation#DOC_TARGET.
 * </p>
 */
@ParamContainer("documentation/%page%")
public final class DocumentationPage extends ElveosPage {

    final static String DEFAULT_DOC = "home";

    @RequestParam(role = Role.PAGENAME)
    private final String page;
    private final DocumentationPageUrl url;

    public DocumentationPage(final DocumentationPageUrl url) {
        super(url);
        this.url = url;
        page = url.getPage();
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    protected HtmlElement createBodyContent() throws RedirectException {
        final TwoColumnLayout layout = new TwoColumnLayout(url);
        final BoxLayout box = new BoxLayout();
        final HtmlDocumentationRenderer docRenderer = new HtmlDocumentationRenderer(DocumentationType.MAIN_DOC, page);
        if (!docRenderer.isExists()) {
            throw new PageNotFoundException();
        }

        box.add(docRenderer);
        layout.addLeft(box);
        return layout;
    }

    @Override
    protected String createPageTitle() {
        return Context.tr("Elveos documentation: {0}", page);
    }

    @Override
    protected Breadcrumb createBreadcrumb() {
        if (page.equals(DEFAULT_DOC)) {
            return DocumentationPage.generateBreadcrumb();
        }
        return DocumentationPage.generateBreadcrumbPage(page);
    }

    private static Breadcrumb generateBreadcrumb() {
        final Breadcrumb breadcrumb = IndexPage.generateBreadcrumb();
        breadcrumb.pushLink(new DocumentationRootPageUrl().getHtmlLink(tr("Documentation")));
        return breadcrumb;
    }

    private static Breadcrumb generateBreadcrumbPage(final String docTarget) {
        final Breadcrumb breadcrumb = DocumentationPage.generateBreadcrumb();

        final DocumentationPageUrl documentationPageUrl = new DocumentationPageUrl(docTarget);
        breadcrumb.pushLink(documentationPageUrl.getHtmlLink(docTarget));

        return breadcrumb;
    }
}
