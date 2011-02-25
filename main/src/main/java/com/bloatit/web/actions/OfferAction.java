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
package com.bloatit.web.actions;

import java.math.BigDecimal;
import java.util.Locale;

import com.bloatit.framework.exceptions.UnauthorizedOperationException;
import com.bloatit.framework.utils.i18n.DateLocale;
import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.annotations.Optional;
import com.bloatit.framework.webserver.annotations.ParamConstraint;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.annotations.RequestParam.Role;
import com.bloatit.framework.webserver.annotations.tr;
import com.bloatit.framework.webserver.url.Url;
import com.bloatit.model.Batch;
import com.bloatit.model.Demand;
import com.bloatit.model.Group;
import com.bloatit.model.Member;
import com.bloatit.model.Offer;
import com.bloatit.web.linkable.demands.DemandTabPane;
import com.bloatit.web.url.DemandPageUrl;
import com.bloatit.web.url.OfferActionUrl;
import com.bloatit.web.url.OfferPageUrl;

/**
 * Class that will create a new offer based on data received from a form.
 */
@ParamContainer("action/offer")
public final class OfferAction extends LoggedAction {

    @RequestParam(role = Role.GET, conversionErrorMsg = @tr("The target idea is mandatory to make an offer."))
    private final Demand demand;

    @RequestParam(role = Role.GET)
    @Optional
    private final Offer draftOffer;

    @RequestParam(role = Role.POST, conversionErrorMsg = @tr("Invalid or missing value for price field."))
    private final BigDecimal price;

    @RequestParam(role = Role.POST)
    private final DateLocale expiryDate;

    @RequestParam(role = Role.POST)
    private final String description;

    @RequestParam(role = Role.POST)
    private final Locale locale;

    @RequestParam(role = Role.POST)
    @Optional
    private final Integer daysBeforeValidation;

    @RequestParam(role = Role.POST)
    @Optional
    @ParamConstraint(min = "0", minErrorMsg = @tr("''%param'' is a percent, and must be greater or equal to 0."), //
                     max = "100", maxErrorMsg = @tr("''%param'' is a percent, and must be lesser or equal to 100."))
    private final Integer percentFatal;

    @RequestParam(role = Role.POST)
    @Optional
    @ParamConstraint(min = "0", minErrorMsg = @tr("''%param'' is a percent, and must be greater or equal to 0."), //
                     max = "100", maxErrorMsg = @tr("''%param'' is a percent, and must be lesser or equal to 100."))
    private final Integer percentMajor;

    @RequestParam(role = Role.POST)
    private final Boolean isFinished;

    @RequestParam(role = Role.POST)
    @Optional
    private final Group group;

    private final OfferActionUrl url;

    public OfferAction(final OfferActionUrl url) {
        super(url);
        this.url = url;
        this.description = url.getDescription();
        this.locale = url.getLocale();
        this.expiryDate = url.getExpiryDate();
        this.price = url.getPrice();
        this.demand = url.getDemand();
        this.draftOffer = url.getDraftOffer();
        this.group = url.getGroup();
        this.daysBeforeValidation = url.getDaysBeforeValidation();
        this.percentFatal = url.getPercentFatal();
        this.percentMajor = url.getPercentMajor();
        this.isFinished = url.getIsFinished();
    }

    @Override
    public Url doProcessRestricted(Member authenticatedMember) {
        if ((percentFatal != null && percentMajor == null) || (percentFatal == null && percentMajor != null)) {
            session.notifyBad("You have to specify both the Major and Fatal percent.");
            return session.pickPreferredPage();
        }
        if (draftOffer != null && !draftOffer.isDraft()) {
            session.notifyBad("The specified offer is not editable. You cannot add a lot in it.");
            return session.pickPreferredPage();
        }
        try {
            Batch constructingBatch;
            if (draftOffer == null) {
                Offer offer = demand.addOffer(session.getAuthToken().getMember(),
                                              price,
                                              description,
                                              locale,
                                              expiryDate.getJavaDate(),
                                              daysBeforeValidation);
                if (group != null) {
                    offer.setAsGroup(group);
                }
                constructingBatch = offer.getBatches().iterator().next();
            } else {
                constructingBatch = draftOffer.addBatch(price, description, locale, expiryDate.getJavaDate(), daysBeforeValidation);
            }
            if (percentFatal != null && percentMajor != null) {
                constructingBatch.updateMajorFatalPercent(percentFatal, percentMajor);
            }

        } catch (final UnauthorizedOperationException e) {
            session.notifyBad(Context.tr("For obscure reasons, you are not allowed to make an offer on this idea."));
            return session.pickPreferredPage();
        }
        final DemandPageUrl demandPageUrl = new DemandPageUrl(demand);
        demandPageUrl.getDemandTabPaneUrl().setActiveTabKey(DemandTabPane.OFFERS_TAB);
        return demandPageUrl;
    }

    @Override
    protected Url doProcessErrors() {
        session.notifyList(url.getMessages());

        if (demand != null) {
            transmitParameters();
            final OfferPageUrl redirectUrl = new OfferPageUrl(demand);
            redirectUrl.setOffer(draftOffer);
            return redirectUrl;
        }
        return session.pickPreferredPage();
    }

    @Override
    protected String getRefusalReason() {
        return Context.tr("You must be logged to make an offer.");
    }

    @Override
    protected void transmitParameters() {
        session.addParameter(url.getDescriptionParameter());
        session.addParameter(url.getLocaleParameter());
        session.addParameter(url.getExpiryDateParameter());
        session.addParameter(url.getPriceParameter());
        session.addParameter(url.getGroupParameter());
        session.addParameter(url.getDaysBeforeValidationParameter());
        session.addParameter(url.getPercentFatalParameter());
        session.addParameter(url.getPercentMajorParameter());
        session.addParameter(url.getIsFinishedParameter());
    }
}
