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
package com.bloatit.framework.webprocessor.components.renderer;

import com.bloatit.framework.exceptions.highlevel.BadProgrammerException;
import com.bloatit.framework.utils.StringUtils;
import com.bloatit.framework.utils.cache.MemoryCache;
import com.bloatit.framework.utils.parsers.MarkdownParser;
import com.bloatit.framework.utils.parsers.ParsingException;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.meta.XmlText;
import com.bloatit.web.HtmlTools;

public class HtmlCachedMarkdownRenderer extends HtmlDiv {
    /**
     * Creates a new MarkdownRenderer based on markdown or html text
     * 
     * @param text the content to display, must be markdown text if
     *            <code>alreadyRenderer</code> is <code>true</code> or html text
     *            if <code>alreadyRenderer</code> is <code>false</code>
     */
    public HtmlCachedMarkdownRenderer(final String text) {
        super("markdown_block");

        final String cached = MemoryCache.getInstance().get(text);
        if (cached == null) {
            final MarkdownParser parser = new MarkdownParser();
            try {
                final String renderered = parser.parse(HtmlTools.escape(text));
                MemoryCache.getInstance().cache(text, renderered);
                add(new XmlText(renderered));
            } catch (final ParsingException e) {
                throw new BadProgrammerException("An error occured during markdown parsing", e);
            }
        } else {
            add(new XmlText(cached));
        }
    }
}
