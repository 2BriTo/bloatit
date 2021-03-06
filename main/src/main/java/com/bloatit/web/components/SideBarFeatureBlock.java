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
package com.bloatit.web.components;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import java.math.BigDecimal;

import com.bloatit.framework.webprocessor.components.HtmlParagraph;
import com.bloatit.model.Feature;
import com.bloatit.web.linkable.features.FeatureTabPane.FeatureTabKey;
import com.bloatit.web.linkable.features.FeaturesTools;
import com.bloatit.web.linkable.master.HtmlDefineParagraph;
import com.bloatit.web.linkable.master.sidebar.TitleSideBarElementLayout;
import com.bloatit.web.linkable.softwares.SoftwaresTools;
import com.bloatit.web.url.FeaturePageUrl;

public class SideBarFeatureBlock extends TitleSideBarElementLayout {

    public SideBarFeatureBlock(final Feature feature, final BigDecimal amount) {
        setTitle(tr("Feature abstract"));

        setFloatRight(new SoftwaresTools.Logo(feature.getSoftware()));
        add(new HtmlDefineParagraph(tr("Title: "), FeaturesTools.getTitle(feature)));
        add(new HtmlDefineParagraph(tr("Software: "), new SoftwaresTools.Link(feature.getSoftware())));
        add(new HtmlDefineParagraph(tr("Popularity: "), String.valueOf(feature.getPopularity())));

        add(new HtmlParagraph(FeaturesTools.generateProgress(feature, amount, FeaturesTools.FeatureContext.OTHER)));
        add(new HtmlParagraph(new FeaturePageUrl(feature, FeatureTabKey.description).getHtmlLink(tr("more details..."))));
    }

    public SideBarFeatureBlock(final Feature feature) {
        this(feature, BigDecimal.ZERO);
    }
}
