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
package com.bloatit.web.linkable.softwares;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import com.bloatit.framework.exceptions.lowlevel.RedirectException;
import com.bloatit.framework.utils.PageIterable;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.SubParamContainer;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlLink;
import com.bloatit.framework.webprocessor.components.HtmlRenderer;
import com.bloatit.framework.webprocessor.components.HtmlTitleBlock;
import com.bloatit.framework.webprocessor.components.advanced.HtmlClearer;
import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
import com.bloatit.framework.webprocessor.components.meta.XmlNode;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.ElveosUserToken;
import com.bloatit.model.Software;
import com.bloatit.model.managers.SoftwareManager;
import com.bloatit.web.components.HtmlPagedList;
import com.bloatit.web.linkable.documentation.SideBarDocumentationBlock;
import com.bloatit.web.pages.IndexPage;
import com.bloatit.web.pages.master.Breadcrumb;
import com.bloatit.web.pages.master.ElveosPage;
import com.bloatit.web.pages.master.sidebar.TwoColumnLayout;
import com.bloatit.web.url.CreateSoftwarePageUrl;
import com.bloatit.web.url.SoftwareListPageUrl;
import com.bloatit.web.url.SoftwarePageUrl;

@ParamContainer("softwares")
public final class SoftwareListPage extends ElveosPage {

    // Keep me here ! I am needed for the Url generation !
    @SubParamContainer
    private HtmlPagedList<Software> pagedSoftwareList;


    private final SoftwareListPageUrl url;

    public SoftwareListPage(final SoftwareListPageUrl url) {
        super(url);
        this.url = url;
    }

    @Override
    protected HtmlElement createBodyContent(final ElveosUserToken userToken) throws RedirectException {
        final TwoColumnLayout layout = new TwoColumnLayout(true, url);

        final HtmlTitleBlock pageTitle = new HtmlTitleBlock(Context.tr("Software list"), 1);

        final PageIterable<Software> softwareList = SoftwareManager.getAll();
        final HtmlRenderer<Software> softwareItemRenderer = new SoftwareRenderer();

        final SoftwareListPageUrl clonedUrl = url.clone();
        pagedSoftwareList = new HtmlPagedList<Software>(softwareItemRenderer, softwareList, clonedUrl, clonedUrl.getPagedSoftwareListUrl());

        pageTitle.add(new CreateSoftwarePageUrl().getHtmlLink(tr("Add a software")));
        pageTitle.add(pagedSoftwareList);
        pageTitle.add(new HtmlClearer());

        layout.addLeft(pageTitle);

        layout.addRight(new SideBarDocumentationBlock("software"));

        return layout;
    }

    @Override
    protected String createPageTitle() {
        return Context.tr("Software list");
    }

    @Override
    public boolean isStable() {
        return true;
    }

    private class SoftwareRenderer implements HtmlRenderer<Software> {

        public SoftwareRenderer() {
            super();
        }

        @Override
        public XmlNode generate(final Software software) {
            final SoftwarePageUrl softwareUrl = new SoftwarePageUrl(software);
            final HtmlDiv box = new HtmlDiv("software_box");

            box.add(new HtmlDiv("float_right").add(new SoftwaresTools.Logo(software)));

            final HtmlDiv textBox = new HtmlDiv("software_text");
            HtmlLink htmlLink;
            htmlLink = softwareUrl.getHtmlLink(software.getName());
            textBox.add(htmlLink);
            box.add(textBox);
            box.add(new HtmlClearer());

            return box;
        }
    };

    @Override
    protected Breadcrumb createBreadcrumb(final ElveosUserToken userToken) {
        return SoftwareListPage.generateBreadcrumb();
    }

    protected static Breadcrumb generateBreadcrumb() {
        final Breadcrumb breadcrumb = IndexPage.generateBreadcrumb();

        breadcrumb.pushLink(new SoftwareListPageUrl().getHtmlLink(tr("Softwares")));

        return breadcrumb;
    }

}
