package com.bloatit.web.actions;

import com.bloatit.common.Log;
import com.bloatit.framework.exceptions.RedirectException;
import com.bloatit.framework.webserver.annotations.Message.Level;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.masters.Action;
import com.bloatit.framework.webserver.url.Url;
import com.bloatit.model.Payline;
import com.bloatit.model.Payline.Reponse;
import com.bloatit.model.Payline.TokenNotfoundException;
import com.bloatit.web.url.IndexPageUrl;
import com.bloatit.web.url.PaylineNotifyActionUrl;

@ParamContainer("payline/donotify")
public final class PaylineNotifyAction extends Action {

    @RequestParam(name = "token", level = Level.INFO)
    private final String token;

    public PaylineNotifyAction(final PaylineNotifyActionUrl url) {
        super(url);
        token = url.getToken();
    }

    @Override
    public Url doProcess() throws RedirectException {
        Log.web().info("Get a payline notification: " + token);
        final Payline payline = new Payline();
        try {
            final Reponse paymentDetails = payline.getPaymentDetails(token);
            if (paymentDetails.isAccepted()) {
                payline.validatePayment(token);
            } else {
                payline.cancelPayement(token);
                Log.web().error("Payment is not accepted: " + token);
            }
        } catch (final TokenNotfoundException e) {
            Log.web().error("Token not found ! ", e);
        }
        return new IndexPageUrl();
    }

    @Override
    public Url doProcessErrors() throws RedirectException {
        Log.web().error("Payline notification with parameter errors ! ");
        return new IndexPageUrl();
    }
}
