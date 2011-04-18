package com.bloatit.web.linkable.money;

import java.math.BigDecimal;

import com.bloatit.common.Log;
import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.exceptions.lowlevel.UnauthorizedOperationException;
import com.bloatit.framework.mailsender.Mail;
import com.bloatit.framework.mailsender.MailServer;
import com.bloatit.framework.webprocessor.ModelAccessor;
import com.bloatit.framework.webprocessor.PaymentProcess;
import com.bloatit.framework.webprocessor.WebProcess;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.context.SessionManager;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.framework.webprocessor.url.UrlString;
import com.bloatit.framework.xcgiserver.HttpHeader;
import com.bloatit.model.Actor;
import com.bloatit.model.BankTransaction;
import com.bloatit.model.Member;
import com.bloatit.model.Payline;
import com.bloatit.model.Payline.Reponse;
import com.bloatit.model.Payline.TokenNotfoundException;
import com.bloatit.model.Team;
import com.bloatit.model.managers.MemberManager;
import com.bloatit.model.managers.TeamManager;
import com.bloatit.web.url.PaylineActionUrl;
import com.bloatit.web.url.PaylineNotifyActionUrl;
import com.bloatit.web.url.PaylineProcessUrl;
import com.bloatit.web.url.PaylineReturnActionUrl;

@ParamContainer("payline/process")
public class PaylineProcess extends WebProcess {

    @RequestParam
    private Actor<?> actor;

    @SuppressWarnings("unused")
    @RequestParam
    private PaymentProcess parentProcess;

    private boolean success = false;

    // Make the payment request.
    final Payline payline = new Payline();

    private final PaylineProcessUrl url;

    public PaylineProcess(final PaylineProcessUrl url) {
        super(url);
        this.url = url;
        actor = url.getActor();
    }

    public boolean isSuccessful() {
        return success;
    }

    @Override
    protected Url doProcess() {
        url.getParentProcess().addChildProcess(this);
        return new PaylineActionUrl(this);
    }

    @Override
    protected Url doProcessErrors() {
        return session.getLastVisitedPage();
    }

    @Override
    public void doLoad() {
        if (actor instanceof Member) {
            actor = MemberManager.getById(actor.getId());
        } else if (actor instanceof Team) {
            actor = TeamManager.getById(actor.getId());
        }
        // actor = (Actor<?>) DBRequests.getById(DaoActor.class,
        // actor.getId()).accept(new DataVisitorConstructor());
    }

    Url initiatePayment() {
        // Constructing the urls.
        final HttpHeader header = Context.getHeader().getHttpHeader();
        final PaylineReturnActionUrl paylineReturnActionUrl = new PaylineReturnActionUrl("ok", this);
        final String returnUrl = paylineReturnActionUrl.externalUrlString(header);
        final PaylineReturnActionUrl paylineReturnActionUrlCancel = new PaylineReturnActionUrl("cancel", this);
        final String cancelUrl = paylineReturnActionUrlCancel.externalUrlString(header);
        final PaylineNotifyActionUrl paylineNotifyActionUrl = new PaylineNotifyActionUrl(this);
        final String notificationUrl = paylineNotifyActionUrl.externalUrlString(header);

        if (payline.canMakePayment()) {
            Reponse reponse;
            try {
                reponse = payline.doPayment(actor, getAmount(), cancelUrl, returnUrl, notificationUrl);
                SessionManager.storeTemporarySession(reponse.getToken(), session);

                // Normal case It is accepted !
                if (reponse.isAccepted()) {
                    return new UrlString(reponse.getRedirectUrl());
                }
                session.notifyBad(reponse.getMessage());
            } catch (final UnauthorizedOperationException e) {
                throw new ShallNotPassException("Not authorized", e);
            }
        }
        return Context.getSession().pickPreferredPage();
    }

    private BigDecimal getAmount() {
        return ((PaymentProcess) getFather()).getAmountToPay();
    }

    void validatePayment(final String token) {
        try {
            final Reponse paymentDetails = payline.getPaymentDetails(token);
            final String message = paymentDetails.getMessage().replace("\n", ". ");
            if (paymentDetails.isAccepted()) {
                payline.validatePayment(token);

                // The payline process is critical. We must be sure the DB is
                // update right NOW!
                ModelAccessor.close();
                ModelAccessor.open();

                success = true;
                // Notify the user:
                session.notifyGood(Context.tr("Your account has been credited with {0}",
                                              Context.getLocalizator().getCurrency(BankTransaction.getByToken(token).getValue()).getLocaleString()));
                final String title = Context.tr("Accepted payment on elveos.org");
                final String memberName = session.getAuthToken().getMember().getDisplayName();
                final String content = Context.tr("Dear {0}, \nWe are pleased to announce that your payment {1} to http://elveos.org has been validated \nWe thank you for your trust.",
                                                  memberName,
                                                  Context.getLocalizator()
                                                         .getCurrency(BankTransaction.getByToken(token).getValuePaid())
                                                         .getLocaleString());
                final String email = session.getAuthToken().getMember().getEmail();
                final String mailSenderID = "payline-process";
                MailServer.getInstance().send(new Mail(email, title, content, mailSenderID));
            } else {
                payline.cancelPayement(token);
                Log.framework().info("Payline transaction failure. (Reason: " + message + ")");
                session.notifyBad("Payment canceled. Reason: " + message + ".");
            }
        } catch (final TokenNotfoundException e) {
            Log.web().fatal("Token not found.", e);
            session.notifyBad("Payment canceled. Reason: Internal error. Please report the bug.");
        } catch (final UnauthorizedOperationException e) {
            session.notifyBad("I cannot send the mail due to unexpected error. Please report this bug.");
            throw new ShallNotPassException(e);
        }
    }

    void refusePayment(final String token) {
        try {
            final Reponse paymentDetails = payline.getPaymentDetails(token);
            final String message = paymentDetails.getMessage().replace("\n", ". ");
            Log.framework().info("Payline transaction failure. (Reason: " + message + ")");
            session.notifyBad("Payment canceled. Reason: " + message + ".");
            payline.cancelPayement(token);
        } catch (final TokenNotfoundException e) {
            Log.web().fatal("Token not found.", e);
            session.notifyBad("Payment canceled. Reason: Payment refused. Please report the bug.");
        }
    }
}
