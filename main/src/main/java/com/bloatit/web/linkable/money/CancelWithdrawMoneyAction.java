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
package com.bloatit.web.linkable.money;

import com.bloatit.common.Log;
import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.ElveosUserToken;
import com.bloatit.model.Member;
import com.bloatit.model.MoneyWithdrawal;
import com.bloatit.model.Team;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.web.actions.LoggedAction;
import com.bloatit.web.linkable.members.MemberPage;
import com.bloatit.web.linkable.team.TeamPage;
import com.bloatit.web.url.CancelWithdrawMoneyActionUrl;

@ParamContainer("money/docancelwithdraw")
public class CancelWithdrawMoneyAction extends LoggedAction {

    @RequestParam(role = Role.GET)
    private final MoneyWithdrawal moneyWithdrawal;

    private final CancelWithdrawMoneyActionUrl url;

    public CancelWithdrawMoneyAction(final CancelWithdrawMoneyActionUrl url) {
        super(url);
        this.url = url;
        moneyWithdrawal = url.getMoneyWithdrawal();
    }

    @Override
    protected Url checkRightsAndEverything(final Member me) {
        if (!moneyWithdrawal.canSetCanceled()) {
            session.notifyBad(Context.tr("Failed to cancel this withdrawal."));
            return getBestReturnUrl(me);
        }
        return NO_ERROR;
    }

    @Override
    protected Url doProcessRestricted(final Member me) {

        try {
            moneyWithdrawal.setCanceled();
        } catch (final UnauthorizedOperationException e) {
            Log.web().error("Fail to read the actor of a withdrawal");
            throw new ShallNotPassException("Fail to read the actor of a withdrawal", e);
        }
        return getBestReturnUrl(me);
    }

    @Override
    protected Url doProcessErrors(final ElveosUserToken userToken) {
        if (userToken.isAuthenticated()) {
            return getBestReturnUrl(userToken.getMember());
        }
        return session.pickPreferredPage();
    }

    private Url getBestReturnUrl(final Member me) {
        if (moneyWithdrawal != null) {
            try {
                if (moneyWithdrawal.getActor() instanceof Team) {
                    return TeamPage.AccountUrl((Team) moneyWithdrawal.getActor());
                }
            } catch (final UnauthorizedOperationException e) {
                throw new ShallNotPassException("Fail to read the actor of a withdrawal", e);
            }
        }
        return MemberPage.MyAccountUrl(me);
    }

    @Override
    protected String getRefusalReason() {
        return Context.tr("You must be logged to cancel a withdrawal.");
    }

    @Override
    protected void transmitParameters() {
    }

}