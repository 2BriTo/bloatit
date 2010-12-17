package com.bloatit.web.utils.url;

import com.bloatit.web.annotations.Message.Level;
import com.bloatit.web.annotations.RequestParam.Role;
import com.bloatit.web.utils.url.Parameter;
import com.bloatit.web.utils.annotations.Loaders;
import com.bloatit.web.utils.annotations.RequestParamSetter.ConversionErrorException;
import com.bloatit.web.exceptions.RedirectException;

@SuppressWarnings("unused")
public class AccountChargingPageUrl extends Url {
public static String getName() { return "AccountChargingPage"; }
public com.bloatit.web.html.pages.AccountChargingPage createPage() throws RedirectException{ 
    return new com.bloatit.web.html.pages.AccountChargingPage(this); }
public AccountChargingPageUrl(Parameters params) {
    super(getName());
    parseParameters(params);
}
public AccountChargingPageUrl() {
    super(getName());
}


@Override 
protected void doRegister() { 
}

public AccountChargingPageUrl clone() { 
    AccountChargingPageUrl other = new AccountChargingPageUrl();
    return other;
}
}
