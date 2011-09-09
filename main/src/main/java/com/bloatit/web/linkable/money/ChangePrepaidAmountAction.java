package com.bloatit.web.linkable.money;

import java.math.BigDecimal;

import javax.mail.IllegalWriteException;

import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.webprocessor.annotations.MaxConstraint;
import com.bloatit.framework.webprocessor.annotations.MinConstraint;
import com.bloatit.framework.webprocessor.annotations.NonOptional;
import com.bloatit.framework.webprocessor.annotations.Optional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.PrecisionConstraint;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.PageNotFoundUrl;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Member;
import com.bloatit.model.MoneyWithdrawal;
import com.bloatit.model.Team;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.web.WebConfiguration;
import com.bloatit.web.actions.AccountProcess;
import com.bloatit.web.actions.LoggedElveosAction;
import com.bloatit.web.linkable.contribution.ContributionProcess;
import com.bloatit.web.linkable.members.MemberPage;
import com.bloatit.web.linkable.team.TeamPage;
import com.bloatit.web.url.AccountChargingPageUrl;
import com.bloatit.web.url.ChangePrepaidAmountActionUrl;
import com.bloatit.web.url.CheckContributePageUrl;
import com.bloatit.web.url.WithdrawMoneyPageUrl;

@ParamContainer("account/changeprepaid/%process%")
public class ChangePrepaidAmountAction extends LoggedElveosAction {
    private final ChangePrepaidAmountActionUrl url;

    @RequestParam(role = Role.PAGENAME, message = @tr("The process is closed, expired, missing or invalid."))
    @NonOptional(@tr("The process is closed, expired, missing or invalid."))
    private final AccountProcess process;

    @Optional
    @RequestParam(message = @tr("The amount to load on your account must be a positive integer."))
    @MaxConstraint(max = 100000, message = @tr("We cannot accept such a generous offer."))
    @MinConstraint(min = 1, message = @tr("You must specify a positive value."))
    @PrecisionConstraint(precision = 0, message = @tr("Please do not use cents."))
    private BigDecimal preload;
    
    @Optional("false")
    @RequestParam
    private Boolean silent;

    public ChangePrepaidAmountAction(final ChangePrepaidAmountActionUrl url) {
        super(url);
        this.url = url;
        this.process = url.getProcess();
        this.preload = url.getPreload();
        if(url.getSilent() == null) {
            this.silent = false;
        } else {
            this.silent = url.getSilent();
        }
    }

    @Override
    protected Url doProcessRestricted(final Member me) {
        try {
            if (process instanceof AccountChargingProcess) {

                if (!process.getAccountChargingAmount().equals(preload) && preload != null) {

                    process.setAmountToCharge(preload);
                    process.setAmountToPayBeforeComission(preload);
                }
                if (process.getAccountChargingAmount().equals(BigDecimal.ZERO)) {

                    process.setAmountToCharge(WebConfiguration.getDefaultChargingAmount());
                    process.setAmountToPayBeforeComission(WebConfiguration.getDefaultChargingAmount());
                }
                return new AccountChargingPageUrl((AccountChargingProcess) process);
            } else if (process instanceof ContributionProcess) {
                if (!process.getAccountChargingAmount().equals(preload) && preload != null) {
                    process.setAmountToCharge(preload);
                }
                return new CheckContributePageUrl((ContributionProcess) process);
            }

        } catch (IllegalWriteException e) {
            if(!silent) {
                Context.getSession().notifyWarning(Context.tr("The preload amount is locked during the payment process."));
            }
        }

        return new PageNotFoundUrl();

    }

    @Override
    protected Url checkRightsAndEverything(final Member me) {
        return NO_ERROR;
    }

    @Override
    protected Url doProcessErrors() {
        
        if(silent) {
            session.flushNotifications();
        }
        
        if (process == null) {
            return new PageNotFoundUrl();
        }
        if (process instanceof AccountChargingProcess) {
            return new AccountChargingPageUrl((AccountChargingProcess)process);
        } else if (process instanceof ContributionProcess) {
            return new CheckContributePageUrl((ContributionProcess) process);
        }
        
        return new PageNotFoundUrl();
    }

    @Override
    protected String getRefusalReason() {
        return Context.tr("You must be logged to withdraw money.");
    }

    @Override
    protected void transmitParameters() {
        session.addParameter(url.getPreloadParameter());
    }
}
