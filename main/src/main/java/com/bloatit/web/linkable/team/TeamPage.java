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
package com.bloatit.web.linkable.team;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import com.bloatit.common.Log;
import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.exceptions.lowlevel.RedirectException;
import com.bloatit.framework.exceptions.lowlevel.UnauthorizedOperationException;
import com.bloatit.framework.utils.i18n.DateLocale.FormatStyle;
import com.bloatit.framework.webprocessor.annotations.Optional;
import com.bloatit.framework.webprocessor.annotations.ParamConstraint;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlList;
import com.bloatit.framework.webprocessor.components.HtmlListItem;
import com.bloatit.framework.webprocessor.components.HtmlParagraph;
import com.bloatit.framework.webprocessor.components.HtmlTitleBlock;
import com.bloatit.framework.webprocessor.components.advanced.HtmlTabBlock;
import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
import com.bloatit.framework.webprocessor.components.meta.HtmlMixedText;
import com.bloatit.framework.webprocessor.components.renderer.HtmlCachedMarkdownRenderer;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.PageNotFoundUrl;
import com.bloatit.model.Team;
import com.bloatit.model.right.Action;
import com.bloatit.model.visitor.Visitor;
import com.bloatit.web.WebConfiguration;
import com.bloatit.web.components.MoneyDisplayComponent;
import com.bloatit.web.components.SideBarButton;
import com.bloatit.web.linkable.documentation.SideBarDocumentationBlock;
import com.bloatit.web.linkable.money.AccountPage.SideBarLoadAccountBlock;
import com.bloatit.web.linkable.team.tabs.AccountTab;
import com.bloatit.web.linkable.team.tabs.ActivityTab;
import com.bloatit.web.linkable.team.tabs.MembersTab;
import com.bloatit.web.pages.master.Breadcrumb;
import com.bloatit.web.pages.master.HtmlDefineParagraph;
import com.bloatit.web.pages.master.MasterPage;
import com.bloatit.web.pages.master.sidebar.SideBarElementLayout;
import com.bloatit.web.pages.master.sidebar.TitleSideBarElementLayout;
import com.bloatit.web.pages.master.sidebar.TwoColumnLayout;
import com.bloatit.web.url.AccountChargingProcessUrl;
import com.bloatit.web.url.AccountPageUrl;
import com.bloatit.web.url.ModifyTeamPageUrl;
import com.bloatit.web.url.TeamPageUrl;

/**
 * <p>
 * Home page for handling teams
 * </p>
 */
@ParamContainer("team")
public final class TeamPage extends MasterPage {
    private final static String TEAM_TAB_PANE = "tab";
    private final static String MEMBERS_TAB = "members";
    private final static String ACTIVITY_TAB = "activity";
    private final static String ACCOUNT_TAB = "account";

    private final TeamPageUrl url;

    private ActivityTab activity;

    @RequestParam(name = "id", conversionErrorMsg = @tr("I cannot find the team number: ''%value%''."))
    @ParamConstraint(optionalErrorMsg = @tr("You have to specify a team number."))
    private final Team targetTeam;

    @RequestParam(name = TEAM_TAB_PANE)
    @Optional(MEMBERS_TAB)
    private String activeTabKey;

    public TeamPage(final TeamPageUrl url) {
        super(url);
        this.url = url;
        this.targetTeam = url.getTargetTeam();
        this.activeTabKey = url.getActiveTabKey();
    }

    @Override
    protected HtmlElement createBodyContent() throws RedirectException {
        final TwoColumnLayout layout = new TwoColumnLayout(false, url);
        final Visitor me = session.getAuthToken().getVisitor();

        layout.addLeft(generateTeamIDCard(me));
        layout.addLeft(generateMain(me));

        layout.addRight(generateContactBox());

        if (activeTabKey.equals(ACCOUNT_TAB)) {
            layout.addRight(new SideBarTeamWithdrawMoneyBlock());
            layout.addRight(new SideBarTeamChargeAccountBlock(targetTeam));
        } else if (activeTabKey.equals(ACTIVITY_TAB)) {
            layout.addRight(new SideBarDocumentationBlock("team_role"));
        } else if (activeTabKey.equals(MEMBERS_TAB)) {
            layout.addRight(new SideBarDocumentationBlock("team_role"));
        }
        return layout;
    }

    @Override
    protected String createPageTitle() {
        return Context.tr("Consult team information");
    }

    @Override
    public boolean isStable() {
        return true;
    }

    private SideBarElementLayout generateContactBox() {
        final TitleSideBarElementLayout contacts = new TitleSideBarElementLayout();
        contacts.setTitle(Context.tr("How to contact {0}?", targetTeam.getDisplayName()));

        if (targetTeam.canAccessContact(Action.READ)) {
            try {
                contacts.add(new HtmlCachedMarkdownRenderer(targetTeam.getContact()));
            } catch (final UnauthorizedOperationException e) {
                session.notifyBad("An error prevented us from showing you team contact information. Please notify us.");
                throw new ShallNotPassException("User can't see team contact information while he should", e);
            }
        } else {
            contacts.add(new HtmlParagraph().addText("No public contact information available"));
        }

        return contacts;
    }

    private HtmlElement generateMain(Visitor me) {
        final HtmlDiv master = new HtmlDiv("team_tabs");

        final TeamPageUrl secondUrl = new TeamPageUrl(targetTeam);
        final HtmlTabBlock tabPane = new HtmlTabBlock(TEAM_TAB_PANE, activeTabKey, secondUrl);
        master.add(tabPane);

        tabPane.addTab(new MembersTab(targetTeam, tr("Members"), MEMBERS_TAB));
        if (targetTeam.canAccessBankTransaction()) {
            tabPane.addTab(new AccountTab(targetTeam, tr("Account"), ACCOUNT_TAB));
        }
        activity = new ActivityTab(targetTeam, tr("Activity"), ACTIVITY_TAB, url);
        tabPane.addTab(activity);

        return master;
    }

    /**
     * Generates the team block displaying it's ID
     * 
     * @param me the connected member
     * @return the ID card
     */
    private HtmlElement generateTeamIDCard(Visitor me) {
        HtmlDiv master = new HtmlDiv("padding_box");
        if (me.hasModifyTeamRight(targetTeam)) {
            // Link to change account settings
            final HtmlDiv modify = new HtmlDiv("float_right");
            master.add(modify);
            modify.add(new ModifyTeamPageUrl(targetTeam).getHtmlLink(Context.tr("Change team settings")));
        }

        // Title and team type
        HtmlTitleBlock titleBlock;
        titleBlock = new HtmlTitleBlock(targetTeam.getDisplayName(), 1);
        master.add(titleBlock);

        // Avatar
        titleBlock.add(new HtmlDiv("float_left").add(TeamTools.getTeamAvatar(targetTeam)));

        // Team informations
        final HtmlList informationsList = new HtmlList();

        // display name
        informationsList.add(new HtmlDefineParagraph(Context.tr("Unique name: "), targetTeam.getLogin()));
        // Visibility
        informationsList.add(new HtmlDefineParagraph(Context.tr("Visibility: "), (targetTeam.isPublic() ? Context.tr("Public")
                : Context.tr("Private"))));

        // Creation date
        try {
            informationsList.add(new HtmlDefineParagraph(Context.tr("Creation date: "), Context.getLocalizator()
                                                                                               .getDate(targetTeam.getDateCreation())
                                                                                               .toString(FormatStyle.LONG)));
        } catch (final UnauthorizedOperationException e) {
            // Should never happen
            Log.web().error("Not allowed to see team creation date in team page, should not happen", e);
        }

        // Member count
        informationsList.add(new HtmlDefineParagraph(Context.tr("Number of members: "), String.valueOf(targetTeam.getMembers().size())));

        // Features count
        final long featuresCount = getActivityCount();
        TeamPageUrl activityPage = new TeamPageUrl(targetTeam);
        activityPage.setActiveTabKey(ACTIVITY_TAB);
        HtmlMixedText mixed = new HtmlMixedText(Context.tr("{0} (<0::see details>)", featuresCount), activityPage.getHtmlLink());
        informationsList.add(new HtmlDefineParagraph(Context.tr("Team's recent activity: "), mixed));
        titleBlock.add(informationsList);

        // Description
        final HtmlTitleBlock description = new HtmlTitleBlock(Context.tr("Description"), 2);
        titleBlock.add(description);
        final HtmlCachedMarkdownRenderer hcmr = new HtmlCachedMarkdownRenderer(targetTeam.getDescription());
        description.add(hcmr);

        // Bank informations
        if (targetTeam.canGetInternalAccount() && targetTeam.canGetExternalAccount()) {
            try {
                final HtmlTitleBlock bankInformations = new HtmlTitleBlock(Context.tr("Bank informations"), 2);
                master.add(bankInformations);
                {
                    final HtmlList bankInformationsList = new HtmlList();
                    bankInformations.add(bankInformationsList);

                    // Account balance
                    final MoneyDisplayComponent amount = new MoneyDisplayComponent(targetTeam.getInternalAccount().getAmount(), true, targetTeam);
                    final AccountPageUrl accountPageUrl = new AccountPageUrl();
                    accountPageUrl.setTeam(targetTeam);
                    final HtmlListItem accountBalanceItem = new HtmlListItem(new HtmlDefineParagraph(Context.tr("Account balance: "),
                                                                                                     new HtmlMixedText(Context.tr("<0:amount (1000€):> (<1::view details>)"),
                                                                                                                       amount,
                                                                                                                       accountPageUrl.getHtmlLink())));
                    bankInformationsList.add(accountBalanceItem);

                }
            } catch (final UnauthorizedOperationException e) {
                // Should never happen
                Log.web().error("Cannot access to bank informations, should not happen", e);
            }
        }

        return master;
    }

    @Override
    protected Breadcrumb createBreadcrumb() {
        return TeamPage.generateBreadcrumb(targetTeam);
    }

    public static Breadcrumb generateBreadcrumb(final Team team) {
        final Breadcrumb breadcrumb = TeamsPage.generateBreadcrumb();
        breadcrumb.pushLink(new TeamPageUrl(team).getHtmlLink(team.getDisplayName()));
        return breadcrumb;
    }

    private long getActivityCount() {
        return targetTeam.getRecentActivityCount();
    }

    private static class SideBarTeamWithdrawMoneyBlock extends TitleSideBarElementLayout {
        SideBarTeamWithdrawMoneyBlock() {
            setTitle(tr("Team account"));

            add(new HtmlParagraph(tr("Like users, teams have an elveos account where they can store money.")));
            add(new HtmlParagraph(tr("People with the talk right can decide to make developments under the name of the team to let it earn money.")));
            add(new HtmlParagraph(tr("People with the bank right can withdraw money from the elveos account back to the team bank account.")));
            // TODO good URL
            add(new SideBarButton(tr("Withdraw money"), new PageNotFoundUrl(), WebConfiguration.getImgAccountWithdraw()).asElement());

        }
    }

    private static class SideBarTeamChargeAccountBlock extends TitleSideBarElementLayout {
        SideBarTeamChargeAccountBlock(Team team) {
            setTitle(tr("Load account"));

            add(new HtmlParagraph(tr("You can charge your account with a credit card using the following link: ")));
            // TODO good URL
            final AccountChargingProcessUrl chargingAccountUrl = new AccountChargingProcessUrl();
            chargingAccountUrl.setTeam(team);
            add(new SideBarButton(tr("Charge your account"), chargingAccountUrl, WebConfiguration.getImgAccountCharge()).asElement());
            add(new HtmlDefineParagraph(tr("Note: "),
                                        tr("We have charge to pay every time you charge your account, hence we will perceive our 10% commission, even if you withdraw the money as soon as you have loaded it.")));
        }
    }
}
