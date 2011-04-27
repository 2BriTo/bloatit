//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.web.linkable.contribution;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import java.math.BigDecimal;

import com.bloatit.framework.exceptions.lowlevel.UnauthorizedOperationException;
import com.bloatit.framework.utils.Image;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlImage;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.Actor;
import com.bloatit.web.WebConfiguration;
import com.bloatit.web.linkable.members.MembersTools;

public class HtmlPrepaidLine extends HtmlDiv {

    protected HtmlPrepaidLine(final Actor<?> actor) throws UnauthorizedOperationException {
        super("quotation_detail_line");

        add(MembersTools.getMemberAvatarSmall(actor));

        add(new HtmlDiv("quotation_detail_line_money").addText(Context.getLocalizator()
                                                                      .getCurrency(actor.getInternalAccount().getAmount())
                                                                      .getSimpleEuroString()));
        add(new HtmlDiv().setCssClass("quotation_detail_line_money_image").add(new HtmlImage(new Image(WebConfiguration.getImgMoneyDownSmall()),
                                                                                             "money up")));
        add(new HtmlDiv("quotation_detail_line_money").addText(Context.getLocalizator().getCurrency(BigDecimal.ZERO).getSimpleEuroString()));

        add(new HtmlDiv("quotation_detail_line_categorie").addText(tr("Prepaid from internal account")));

        final HtmlDiv amountBlock = new HtmlDiv("quotation_detail_line_amount");

        amountBlock.add(new HtmlDiv("quotation_detail_line_amount_money").addText(Context.getLocalizator()
                                                                                         .getCurrency(actor.getInternalAccount()
                                                                                                           .getAmount()
                                                                                                           .negate())
                                                                                         .getTwoDecimalEuroString()));

        add(amountBlock);
    }
}