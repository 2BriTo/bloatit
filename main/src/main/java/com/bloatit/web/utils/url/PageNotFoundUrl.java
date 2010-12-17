package com.bloatit.web.utils.url;

import com.bloatit.web.annotations.Message.Level;
import com.bloatit.web.annotations.RequestParam.Role;
import com.bloatit.web.utils.url.Parameter;
import com.bloatit.web.utils.annotations.Loaders;
import com.bloatit.web.utils.annotations.RequestParamSetter.ConversionErrorException;
import com.bloatit.web.exceptions.RedirectException;

@SuppressWarnings("unused")
public class PageNotFoundUrl extends Url {
public static String getName() { return "PageNotFound"; }
public com.bloatit.web.html.pages.PageNotFound createPage() throws RedirectException{ 
    return new com.bloatit.web.html.pages.PageNotFound(this); }
public PageNotFoundUrl(Parameters params) {
    super(getName());
    parseParameters(params);
}
public PageNotFoundUrl() {
    super(getName());
}


@Override 
protected void doRegister() { 
}

public PageNotFoundUrl clone() { 
    PageNotFoundUrl other = new PageNotFoundUrl();
    return other;
}
}
