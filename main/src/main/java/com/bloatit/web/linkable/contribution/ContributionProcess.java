package com.bloatit.web.linkable.contribution;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import java.math.BigDecimal;

import javax.mail.IllegalWriteException;

import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.exceptions.lowlevel.UnauthorizedOperationException;
import com.bloatit.framework.mailsender.Mail;
import com.bloatit.framework.mailsender.MailServer;
import com.bloatit.framework.webprocessor.WebProcess;
import com.bloatit.framework.webprocessor.WebProcess.PaymentProcess;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Feature;
import com.bloatit.model.Team;
import com.bloatit.model.feature.FeatureManager;
import com.bloatit.model.managers.TeamManager;
import com.bloatit.web.linkable.money.PaylineProcess;
import com.bloatit.web.url.CheckContributionPageUrl;
import com.bloatit.web.url.ContributePageUrl;
import com.bloatit.web.url.ContributionActionUrl;
import com.bloatit.web.url.ContributionProcessUrl;

@ParamContainer("contribution/process")
public class ContributionProcess extends PaymentProcess {

    @RequestParam
    private Feature feature;

    private Team team;
    private BigDecimal amount = new BigDecimal("0");
    private BigDecimal amountToPay = new BigDecimal("0");
    private BigDecimal amountToCharge = new BigDecimal("0");
    private String comment = "";
    private final ContributionProcessUrl url;

    private boolean locked = false;

    public ContributionProcess(final ContributionProcessUrl url) {
        super(url);
        this.url = url;
        feature = url.getFeature();
    }

    public String getComment() {
        return comment;
    }

    @Override
    public BigDecimal getAmountToPay() {
        return amountToPay;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setAmount(final BigDecimal amount) throws IllegalWriteException {
        if (locked) {
            throw new IllegalWriteException();
        }
        this.amount = amount;
    }

    public void setAmountToPay(final BigDecimal amount) throws IllegalWriteException {
        if (locked) {
            throw new IllegalWriteException();
        }
        this.amountToPay = amount;
    }

    public void setAmountToCharge(final BigDecimal amount) throws IllegalWriteException {
        if (locked) {
            throw new IllegalWriteException();
        }
        this.amountToCharge = amount;
    }

    public void setComment(final String comment) throws IllegalWriteException {
        if (locked) {
            throw new IllegalWriteException();
        }
        this.comment = comment;
    }

    @Override
    protected Url doProcess() {
        return new ContributePageUrl(this);
    }

    @Override
    protected Url doProcessErrors() {
        session.notifyList(url.getMessages());
        return session.getLastVisitedPage();
    }

    @Override
    public void load() {
        feature = FeatureManager.getFeatureById(feature.getId());
        if (team != null) {
            team = TeamManager.getById(team.getId());
        }
    }

    @Override
    public void beginSubProcess(final WebProcess subProcess) {
        if (subProcess.getClass().equals(PaylineProcess.class)) {
            locked = true;
        }
    }

    @Override
    public Url endSubProcess(final WebProcess subProcess) {
        if (subProcess.getClass().equals(PaylineProcess.class)) {
            final PaylineProcess subPro = (PaylineProcess) subProcess;
            if (subPro.isSuccessful()) {
                if (amountToCharge.compareTo(BigDecimal.ZERO) > 0) {
                    Context.getSession().notifyGood(tr("Your account has been credited."));
                }
                try {
                    final String title = Context.tr("Accepted payment on elveos.org");
                    final String memberName = session.getAuthToken().getMember().getDisplayName();
                    final String content = Context.tr("Dear {0}, \nWe are pleased to announce that your payment {1} to http://elveos.org has been validated \nWe thank you for your trust.",
                                                      memberName,
                                                      Context.getLocalizator().getCurrency(amountToCharge).getLocaleString());
                    sendMail(title, content);
                } catch (final UnauthorizedOperationException e) {
                    session.notifyError(Context.tr("An error prevented us from sending you a mail. Please notify us."));
                    throw new ShallNotPassException("Cannot access connecter user email.");
                }
                // Redirects to the contribution action which will perform the
                // actual contribution
                return new ContributionActionUrl(this);
            }
            locked = false;
            return new CheckContributionPageUrl(this);
        }
        return null;
    }

    public BigDecimal getAmountToCharge() {
        return amountToCharge;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setTeam(final Team team) throws IllegalWriteException {
        if (locked) {
            throw new IllegalWriteException();
        }
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    /**
     * Sends a payment notification email
     * 
     * @param title the title of the mail
     * @param content the content of the mail
     * @throws UnauthorizedOperationException when something unexpected happens
     */
    private void sendMail(final String title, final String content) throws UnauthorizedOperationException {
        final String email = session.getAuthToken().getMember().getEmail();
        final String mailSenderID = "payline-action";
        MailServer.getInstance().send(new Mail(email, title, content, mailSenderID));
    }
}
