package com.bloatit.web.linkable.team.tabs;

import java.util.EnumSet;
import java.util.Iterator;

import com.bloatit.data.DaoTeamRight.UserTeamRight;
import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.exceptions.lowlevel.UnauthorizedOperationException;
import com.bloatit.framework.utils.PageIterable;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlLink;
import com.bloatit.framework.webprocessor.components.HtmlParagraph;
import com.bloatit.framework.webprocessor.components.HtmlTitleBlock;
import com.bloatit.framework.webprocessor.components.PlaceHolderElement;
import com.bloatit.framework.webprocessor.components.advanced.HtmlTabBlock.HtmlTab;
import com.bloatit.framework.webprocessor.components.advanced.HtmlTable;
import com.bloatit.framework.webprocessor.components.advanced.HtmlTable.HtmlTableModel;
import com.bloatit.framework.webprocessor.components.meta.HtmlText;
import com.bloatit.framework.webprocessor.components.meta.XmlNode;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.context.Session;
import com.bloatit.model.Member;
import com.bloatit.model.Team;
import com.bloatit.model.visitor.Visitor;
import com.bloatit.web.url.GiveRightActionUrl;
import com.bloatit.web.url.JoinTeamActionUrl;
import com.bloatit.web.url.MemberPageUrl;
import com.bloatit.web.url.SendTeamInvitationPageUrl;

public class MembersTab extends HtmlTab {
    private final Team team;
    private final Session session = Context.getSession();

    public MembersTab(Team team, String title, String tabKey) {
        super(title, tabKey);
        this.team = team;
    }

    @Override
    public XmlNode generateBody() {
        final Visitor me = session.getAuthToken().getVisitor();

        HtmlDiv master = new HtmlDiv("tab_pane");

        // Members
        final HtmlTitleBlock memberTitle = new HtmlTitleBlock(Context.tr("Members ({0})", team.getMembers().size()), 2);
        master.add(memberTitle);

        if (me.hasInviteTeamRight(team)) {
            final SendTeamInvitationPageUrl sendInvitePage = new SendTeamInvitationPageUrl(team);
            final HtmlLink inviteMember = new HtmlLink(sendInvitePage.urlString(), Context.tr("Invite a member to this team"));
            memberTitle.add(new HtmlParagraph().add(inviteMember));
        }

        if (team.isPublic() && !me.isInTeam(team)) {
            final HtmlLink joinLink = new HtmlLink(new JoinTeamActionUrl(team).urlString(), Context.tr("Join this team"));
            memberTitle.add(joinLink);
        }

        final PageIterable<Member> members = team.getMembers();
        final HtmlTable membersTable = new HtmlTable(new MyTableModel(members));
        membersTable.setCssClass("members_table");
        memberTitle.add(membersTable);

        return master;
    }

    private class MyTableModel extends HtmlTableModel {
        private final PageIterable<Member> members;
        private Member member;
        private Iterator<Member> iterator;
        private Visitor visitor;
        private static final int CONSULT = 1;
        private static final int TALK = 2;
        private static final int MODIFY = 4;
        private static final int INVITE = 3;
        private static final int BANK = 5;
        private static final int PROMOTE = 6;

        public MyTableModel(final PageIterable<Member> members) {
            this.members = members;
            if (session.getAuthToken() != null) {
                this.visitor = session.getAuthToken().getVisitor();
            }
            iterator = members.iterator();
        }

        @Override
        public int getColumnCount() {
            return UserTeamRight.values().length + 1;
        }

        @Override
        public XmlNode getHeader(final int column) {
            if (column == 0) {
                return new HtmlText(Context.tr("Member name"));
            }
            final EnumSet<UserTeamRight> e = EnumSet.allOf(UserTeamRight.class);
            final UserTeamRight ugr = (UserTeamRight) e.toArray()[column - 1];
            switch (ugr) {
                case CONSULT:
                    return new HtmlText(Context.tr("Consult"));
                case TALK:
                    return new HtmlText(Context.tr("Talk"));
                case MODIFY:
                    return new HtmlText(Context.tr("Modify"));
                case INVITE:
                    return new HtmlText(Context.tr("Invite"));
                case BANK:
                    return new HtmlText(Context.tr("Bank"));
                case PROMOTE:
                    return new HtmlText(Context.tr("Promote"));
                default:
                    return new HtmlText("");
            }
        }

        @Override
        public XmlNode getBody(final int column) {
            switch (column) {
                case 0: // Name
                    return new HtmlLink(new MemberPageUrl(member).urlString(), member.getDisplayName());
                case CONSULT:
                    return getUserRightStatus(UserTeamRight.CONSULT);
                case TALK:
                    return getUserRightStatus(UserTeamRight.TALK);
                case MODIFY:
                    return getUserRightStatus(UserTeamRight.MODIFY);
                case INVITE:
                    return getUserRightStatus(UserTeamRight.INVITE);
                case PROMOTE:
                    return getUserRightStatus(UserTeamRight.PROMOTE);
                case BANK:
                    return getUserRightStatus(UserTeamRight.BANK);
                default:
                    return new HtmlText("");
            }
        }

        private XmlNode getUserRightStatus(final UserTeamRight right) {

            final PlaceHolderElement ph = new PlaceHolderElement();

            if (right == UserTeamRight.CONSULT) {
                if (member.canBeKickFromTeam(team, visitor.getMember())) {
                    if (member.equals(visitor)) {
                        ph.add(new GiveRightActionUrl(team, member, right, false).getHtmlLink(Context.tr("Leave")));
                    } else {
                        ph.add(new GiveRightActionUrl(team, member, right, false).getHtmlLink(Context.tr("Kick")));
                    }
                }
            } else {
                if (team.canChangeRight(visitor.getMember(), member, right, true)) {
                    ph.add(new GiveRightActionUrl(team, member, right, true).getHtmlLink(Context.tr("Grant")));
                } else if (team.canChangeRight(visitor.getMember(), member, right, false)) {
                    ph.add(new GiveRightActionUrl(team, member, right, false).getHtmlLink(Context.tr("Remove")));
                }
            }

            return ph;
        }

        @Override
        public String getColumnCss(final int column) {
            switch (column) {
                case 0: // Name
                    return "name";
                case CONSULT:
                    return getUserRightStyle(UserTeamRight.CONSULT);
                case TALK:
                    return getUserRightStyle(UserTeamRight.TALK);
                case MODIFY:
                    return getUserRightStyle(UserTeamRight.MODIFY);
                case INVITE:
                    return getUserRightStyle(UserTeamRight.INVITE);
                case PROMOTE:
                    return getUserRightStyle(UserTeamRight.PROMOTE);
                case BANK:
                    return getUserRightStyle(UserTeamRight.BANK);
                default:
                    return "";
            }
        }

        private String getUserRightStyle(final UserTeamRight right) {
            if (member.hasTeamRight(team, right)) {
                return "can";
            }
            return "";
        }

        @Override
        public boolean next() {
            if (iterator == null) {
                iterator = members.iterator();
            }

            if (iterator.hasNext()) {
                member = iterator.next();
                return true;
            }
            return false;
        }
    }

}