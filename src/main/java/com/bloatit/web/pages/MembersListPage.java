/*
 * Copyright (C) 2010 BloatIt.
 *
 * This file is part of BloatIt.
 *
 * BloatIt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BloatIt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */

package com.bloatit.web.pages;

import java.util.HashMap;
import java.util.Map;

import com.bloatit.common.PageIterable;
import com.bloatit.framework.Member;
import com.bloatit.framework.managers.MemberManager;
import com.bloatit.web.htmlrenderer.HtmlTools;
import com.bloatit.web.htmlrenderer.htmlcomponent.HtmlComponent;
import com.bloatit.web.htmlrenderer.htmlcomponent.HtmlList;
import com.bloatit.web.htmlrenderer.htmlcomponent.HtmlListItem;
import com.bloatit.web.htmlrenderer.htmlcomponent.HtmlTitle;
import com.bloatit.web.server.Page;
import com.bloatit.web.server.Session;


public class MembersListPage extends Page {

    public MembersListPage(Session session, Map<String, String> parameters) {
        super(session, parameters);
    }

    public MembersListPage(Session session) {
        this(session, new HashMap<String, String>());
    }

    @Override
    protected HtmlComponent generateContent() {

        HtmlTitle pageTitle = new HtmlTitle("Members list", "");

        PageIterable<Member> memberList = MemberManager.getMembers();

        HtmlList htmlMemberList = new HtmlList();
        pageTitle.add(htmlMemberList);

        for(Member member:memberList) {

            MemberPage memberPage = new MemberPage(session, member);
            HtmlListItem item = new HtmlListItem(HtmlTools.generateLink(this.session, member.getFullname() ,memberPage)+ "<span class=\"karma\">"+ HtmlTools.compressKarma(member.getKarma()) + "</span>");
            htmlMemberList.addItem(item);
        }
        

        return pageTitle;

    }

    @Override
    public String getCode() {
        return "members_list";
    }

    @Override
    protected String getTitle() {
        return "Members list";
    }
}