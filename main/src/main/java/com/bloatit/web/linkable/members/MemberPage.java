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
package com.bloatit.web.linkable.members;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import java.util.Locale;

import com.bloatit.data.DaoUserContent;
import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.exceptions.lowlevel.RedirectException;
import com.bloatit.framework.exceptions.lowlevel.UnauthorizedOperationException;
import com.bloatit.framework.webprocessor.annotations.Optional;
import com.bloatit.framework.webprocessor.annotations.ParamConstraint;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlList;
import com.bloatit.framework.webprocessor.components.HtmlSpan;
import com.bloatit.framework.webprocessor.components.HtmlTitleBlock;
import com.bloatit.framework.webprocessor.components.PlaceHolderElement;
import com.bloatit.framework.webprocessor.components.advanced.HtmlTabBlock;
import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.Member;
import com.bloatit.model.Team;
import com.bloatit.model.UserContent;
import com.bloatit.web.components.HtmlPagedList;
import com.bloatit.web.linkable.members.tabs.AccountTab;
import com.bloatit.web.linkable.members.tabs.ActivityTab;
import com.bloatit.web.linkable.members.tabs.InvitationsTab;
import com.bloatit.web.pages.master.Breadcrumb;
import com.bloatit.web.pages.master.MasterPage;
import com.bloatit.web.pages.master.sidebar.TitleSideBarElementLayout;
import com.bloatit.web.pages.master.sidebar.TwoColumnLayout;
import com.bloatit.web.url.MemberPageUrl;
import com.bloatit.web.url.ModifyMemberPageUrl;
import com.bloatit.web.url.TeamPageUrl;

/**
 * <p>
 * A page used to display member information.
 * </p>
 * <p>
 * If the consulted member is the same as the logged member, then this page will
 * propose to edit account parameters
 * </p>
 */
@ParamContainer("member")
public final class MemberPage extends MasterPage {
    private final MemberPageUrl url;

    public final static String MEMBER_TAB_PANE = "tab";
    public final static String INVITATIONS_TAB = "invitations";
    public final static String ACTIVITY_TAB = "activity";
    public final static String ACCOUNT_TAB = "account";


    private ActivityTab activity;

    @RequestParam(name = MEMBER_TAB_PANE)
    @Optional(ACTIVITY_TAB)
    private final String activeTabKey;

    @SuppressWarnings("unused")
    private HtmlPagedList<UserContent<? extends DaoUserContent>> pagedActivity;

    @ParamConstraint(optionalErrorMsg = @tr("You have to specify a member number."))
    @RequestParam(name = "id", conversionErrorMsg = @tr("I cannot find the member number: ''%value%''."))
    private final Member member;

    @SuppressWarnings("unused")
    @RequestParam(name = "name", role = Role.PRETTY, generatedFrom = "member")
    @Optional("john-do")
    private final String displayName;

    private boolean myPage;

    public MemberPage(final MemberPageUrl url) {
        super(url);
        this.url = url;
        this.member = url.getMember();
        if (!session.isLogged() || !member.equals(session.getAuthToken().getMember())) {
            this.myPage = false;
        } else {
            this.myPage = true;
        }
        this.displayName = url.getDisplayName();
        this.activeTabKey = url.getActiveTabKey();
    }

    @Override
    protected HtmlElement createBodyContent() throws RedirectException {
        final TwoColumnLayout layout = new TwoColumnLayout(false, url);
        layout.addLeft(generateMemberPageMain());

        if (myPage) {
            layout.addLeft(generateTabPane());
        }

        // Adding list of teams
        final TitleSideBarElementLayout teamBlock = new TitleSideBarElementLayout();
        try {
            if (myPage) {
                teamBlock.setTitle(Context.tr("My teams"));
            } else {
                teamBlock.setTitle(Context.tr("{0} teams", member.getDisplayName()));
            }

            final HtmlList teamList = new HtmlList();
            teamList.setCssClass("member_teams_list");
            teamBlock.add(teamList);

            for (final Team team : member.getTeams()) {
                teamList.add(new TeamPageUrl(team).getHtmlLink(team.getDisplayName()));
            }
        } catch (final UnauthorizedOperationException e) {
            throw new ShallNotPassException("Cannot access member team information", e);
        }
        layout.addRight(teamBlock);

        return layout;
    }

    private HtmlElement generateMemberPageMain() {
        final HtmlDiv master = new HtmlDiv("member_page");

        if (myPage) {
            // Link to change account settings
            final HtmlDiv modify = new HtmlDiv("float_right");
            master.add(modify);
            modify.add(new ModifyMemberPageUrl().getHtmlLink(Context.tr("Change account settings")));
        }

        // Title
        final String title = (myPage) ? Context.tr("My page") : Context.tr("Member page");
        final HtmlTitleBlock tBlock = new HtmlTitleBlock(title, 1);
        master.add(tBlock);

        final HtmlDiv main = new HtmlDiv("member");
        master.add(main);

        // Member ID card
        final HtmlDiv memberId = new HtmlDiv("member_id");

        // Avatar
        final HtmlDiv avatarDiv = new HtmlDiv("float_left");
        avatarDiv.add(MembersTools.getMemberAvatar(member));
        memberId.add(avatarDiv);
        main.add(memberId);

        try {
            final HtmlList memberIdList = new HtmlList();
            memberId.add(memberIdList);

            if (myPage) {
                // Login
                final HtmlSpan login = new HtmlSpan("id_category");
                login.addText(Context.trc("login (noun)", "Login: "));
                memberIdList.add(new PlaceHolderElement().add(login).addText(member.getLogin()));

                // Fullname
                final HtmlSpan fullname = new HtmlSpan("id_category");
                fullname.addText(Context.tr("Fullname: "));
                if (member.getFullname() != null) {
                    memberIdList.add(new PlaceHolderElement().add(fullname).addText(member.getFullname()));
                } else {
                    memberIdList.add(new PlaceHolderElement().add(fullname));
                }

                // Email
                final HtmlSpan email = new HtmlSpan("id_category");
                email.addText(Context.tr("Email: "));
                memberIdList.add(new PlaceHolderElement().add(email).addText(member.getEmail()));

            } else {
                final HtmlSpan name = new HtmlSpan("id_category");
                name.addText(Context.tr("Name: "));
                memberIdList.add(new PlaceHolderElement().add(name).addText(member.getDisplayName()));
            }

            final Locale userLocale = Context.getLocalizator().getLocale();
            // Country
            final HtmlSpan country = new HtmlSpan("id_category");
            country.addText(Context.tr("Country: "));
            memberIdList.add(new PlaceHolderElement().add(country).addText(member.getLocale().getDisplayCountry(userLocale)));

            // Language
            final HtmlSpan language = new HtmlSpan("id_category");
            language.addText(Context.tr("Language: "));
            memberIdList.add(new PlaceHolderElement().add(language).addText(member.getLocale().getDisplayLanguage(userLocale)));

            // Karma
            final HtmlSpan karma = new HtmlSpan("id_category");
            karma.addText(Context.tr("Karma: "));
            memberIdList.add(new PlaceHolderElement().add(karma).addText("" + member.getKarma()));
        } catch (final UnauthorizedOperationException e) {
            session.notifyError("An error prevented us from displaying user information. Please notify us.");
            throw new ShallNotPassException("Error while gathering user information", e);
        }

        if (!myPage) {
            // Displaying list of user recent activity
            final HtmlTitleBlock recent = new HtmlTitleBlock(Context.tr("Recent activity"), 2);
            main.add(recent);
            recent.add(ActivityTab.generateActivities(member, url));
        }

        return master;
    }

    private HtmlElement generateTabPane() {
        final HtmlDiv master = new HtmlDiv("member_tabs");

        final MemberPageUrl secondUrl = new MemberPageUrl(member);
        final HtmlTabBlock tabPane = new HtmlTabBlock(MEMBER_TAB_PANE, activeTabKey, secondUrl);
        master.add(tabPane);

        activity =  new ActivityTab(member, tr("Activity"), ACTIVITY_TAB, url);
        tabPane.addTab(activity);
        tabPane.addTab(new AccountTab(member, tr("Account"), ACCOUNT_TAB));
        long nb;
        if((nb = member.getInvitationCount()) > 0) {
            tabPane.addTab(new InvitationsTab(member, tr("Invitations&nbsp;({0})",nb), INVITATIONS_TAB));
        }


        return master;
    }




    @Override
    protected String createPageTitle() {
        return tr("Member - ") + member.getDisplayName();
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    protected Breadcrumb createBreadcrumb() {
        return MemberPage.generateBreadcrumb(member);
    }

    public static Breadcrumb generateBreadcrumb(final Member member) {
        final Breadcrumb breadcrumb = MembersListPage.generateBreadcrumb();
        breadcrumb.pushLink(new MemberPageUrl(member).getHtmlLink(member.getDisplayName()));
        return breadcrumb;
    }

    public static Breadcrumb generateAccountBreadcrumb(final Member member) {
        final Breadcrumb breadcrumb = MemberPage.generateBreadcrumb(member);
        MemberPageUrl memberPageUrl = new MemberPageUrl(member);
        memberPageUrl.setActiveTabKey(ACCOUNT_TAB);
        breadcrumb.pushLink(memberPageUrl.getHtmlLink(Context.tr("Account")));
        return breadcrumb;
    }

    public static MemberPageUrl AccountUrl(Member member) {
        MemberPageUrl memberPageUrl = new MemberPageUrl(member);
        memberPageUrl.setActiveTabKey(ACCOUNT_TAB);
        //memberPageUrl.setAnchor(MEMBER_TAB_PANE);
        return memberPageUrl;
    }

    public static MemberPageUrl MyAccountUrl() {
        return AccountUrl(Context.getSession().getAuthToken().getMember());
    }

    public static MemberPageUrl MyMessagesUrl() {
        MemberPageUrl memberPageUrl = new MemberPageUrl(Context.getSession().getAuthToken().getMember());
        memberPageUrl.setActiveTabKey(INVITATIONS_TAB);
        //memberPageUrl.setAnchor(MEMBER_TAB_PANE);
        return memberPageUrl;
    }
}
