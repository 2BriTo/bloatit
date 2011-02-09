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
package com.bloatit.web.actions;

import com.bloatit.data.DaoMember.ActivationState;
import com.bloatit.framework.exceptions.RedirectException;
import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.annotations.RequestParam.Role;
import com.bloatit.framework.webserver.masters.Action;
import com.bloatit.framework.webserver.url.Url;
import com.bloatit.model.Member;
import com.bloatit.model.managers.MemberManager;
import com.bloatit.web.url.IndexPageUrl;
import com.bloatit.web.url.LoginPageUrl;
import com.bloatit.web.url.MemberActivationActionUrl;

/**
 * A response to a form used to create a new idea
 */
@ParamContainer("member/activate")
public final class MemberActivationAction extends Action {

    public static final String MEMBER_CODE = "member";
    public static final String KEY_CODE = "key";

    @RequestParam(name = MEMBER_CODE, role = Role.GET)
    private final String login;

    @RequestParam(name = KEY_CODE, role = Role.GET)
    private final String key;

    private final MemberActivationActionUrl url;

    public MemberActivationAction(final MemberActivationActionUrl url) {
        super(url);
        this.url = url;

        this.login = url.getLogin();
        this.key = url.getKey();

    }

    @Override
    protected Url doProcess() throws RedirectException {
        session.notifyList(url.getMessages());

        Member member = MemberManager.getMemberByLogin(login);

        Url to = new LoginPageUrl();

        if (member != null) {

            if (member.getActivationState() == ActivationState.VALIDATING) {
                if (key.equals(member.getActivationKey())) {
                    member.activate();
                    session.notifyGood(Context.tr("Activation sucess, you can now login."));
                } else {
                    session.notifyBad(Context.tr("Wrong activation key for this member."));
                    to = new IndexPageUrl();
                }
            } else {
                session.notifyBad(Context.tr("No activation is necessary for this member."));
                to = new IndexPageUrl();
            }
        } else {
            session.notifyBad(Context.tr("Activation impossible on a no existing member."));
            to = new IndexPageUrl();
        }

        return to;
    }

    @Override
    protected Url doProcessErrors() throws RedirectException {
        session.notifyList(url.getMessages());

        return new IndexPageUrl();
    }
}
