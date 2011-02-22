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
package com.bloatit.web.pages.demand;

import static com.bloatit.framework.webserver.Context.tr;

import com.bloatit.common.Log;
import com.bloatit.data.DaoDemand.DemandState;
import com.bloatit.data.queries.NullCollection;
import com.bloatit.framework.exceptions.UnauthorizedOperationException;
import com.bloatit.framework.utils.DateUtils;
import com.bloatit.framework.utils.PageIterable;
import com.bloatit.framework.utils.TimeRenderer;
import com.bloatit.framework.utils.i18n.DateLocale.FormatStyle;
import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.components.HtmlDiv;
import com.bloatit.framework.webserver.components.HtmlImage;
import com.bloatit.framework.webserver.components.HtmlLink;
import com.bloatit.framework.webserver.components.HtmlParagraph;
import com.bloatit.framework.webserver.components.HtmlSpan;
import com.bloatit.framework.webserver.components.HtmlTitle;
import com.bloatit.framework.webserver.components.meta.HtmlTagText;
import com.bloatit.framework.webserver.components.meta.XmlNode;
import com.bloatit.model.Batch;
import com.bloatit.model.Demand;
import com.bloatit.model.Offer;
import com.bloatit.model.demand.DemandImplementation;
import com.bloatit.web.HtmlTools;
import com.bloatit.web.components.HtmlProgressBar;
import com.bloatit.web.url.FileResourceUrl;
import com.bloatit.web.url.MemberPageUrl;
import com.bloatit.web.url.OfferPageUrl;
import com.bloatit.web.url.PopularityVoteActionUrl;
import com.bloatit.web.url.TeamPageUrl;

public class DemandOfferListComponent extends HtmlDiv {

    private final Demand demand;

    public DemandOfferListComponent(final Demand demand) {
        super();
        this.demand = demand;
        try {

            PageIterable<Offer> offers = new NullCollection<Offer>();
            offers = demand.getOffers();
            int unselectedOfferCount = offers.size();

            final Offer selectedOffer = demand.getSelectedOffer();
            if (selectedOffer != null) {
                unselectedOfferCount--;
            }

            final HtmlDiv offersBlock = new HtmlDiv("offers_block");

            if (selectedOffer != null) {
                final HtmlTitle selectedOfferTitle = new HtmlTitle(Context.tr("Selected offer"), 1);
                offersBlock.add(selectedOfferTitle);
                offersBlock.add(generateSelectedOfferTypeBlock(selectedOffer));
            } else {
                final HtmlTitle selectedOfferTitle = new HtmlTitle(Context.tr("No selected offer"), 1);
                offersBlock.add(selectedOfferTitle);
            }

            final HtmlTitle unselectedOffersTitle = new HtmlTitle(Context.trn("Unselected offer ({0})",
                                                                              "Unselected offers ({0})",
                                                                              unselectedOfferCount,
                                                                              unselectedOfferCount), 1);
            offersBlock.add(unselectedOffersTitle);

            offersBlock.add(generateUnselectedOffersTypeBlock(offers, selectedOffer));

            add(offersBlock);

        } catch (final UnauthorizedOperationException e) {
            // No right no current offer.
        }
    }

    private XmlNode generateSelectedOfferTypeBlock(final Offer selectedOffer) throws UnauthorizedOperationException {
        final HtmlDiv offerTypeBlock = new HtmlDiv("offer_type_block");

        final HtmlDiv offerTypeLeftColumn = new HtmlDiv("offer_type_left_column");
        {
            final HtmlDiv offerSelectedDescription = new HtmlDiv("offer_selected_description");
            {
                offerSelectedDescription.add(new HtmlParagraph(tr("The selected offer is the one with the more popularity.")));

                if (DateUtils.isInTheFuture(demand.getValidationDate())) {
                    TimeRenderer renderer = new TimeRenderer(DateUtils.elapsedMilliseconds(DateUtils.now(), demand.getValidationDate()));
                    offerSelectedDescription.add(new HtmlParagraph(tr("This offer will go into development in about ") + renderer.getTimeString()
                            + "."));
                } else {
                    offerSelectedDescription.add(new HtmlParagraph(tr("This offer is in development.")));
                }
            }
            offerTypeLeftColumn.add(offerSelectedDescription);
        }
        offerTypeBlock.add(offerTypeLeftColumn);

        final HtmlDiv offerTypeRightColumn = new HtmlDiv("offer_type_right_column");
        {
            offerTypeRightColumn.add(new OfferBlock(selectedOffer, true));

        }
        offerTypeBlock.add(offerTypeRightColumn);

        return offerTypeBlock;
    }

    private XmlNode
            generateUnselectedOffersTypeBlock(final PageIterable<Offer> offers, final Offer selectedOffer) throws UnauthorizedOperationException {
        final HtmlDiv offerTypeBlock = new HtmlDiv("offer_type_block");

        final HtmlDiv offerTypeLeftColumn = new HtmlDiv("offer_type_left_column");
        {
            final HtmlDiv offerUnselectedDescription = new HtmlDiv("offer_unselected_description");
            {
                offerUnselectedDescription.add(new OfferPageUrl(demand).getHtmlLink(Context.tr("Make a concurrent offer")));

                offerUnselectedDescription.add(new HtmlParagraph("The concurrent offers must be voted enought to become the selected offer."));
            }
            offerTypeLeftColumn.add(offerUnselectedDescription);
        }
        offerTypeBlock.add(offerTypeLeftColumn);

        final HtmlDiv offerTypeRightColumn = new HtmlDiv("offer_type_right_column");
        {
            for (final Offer offer : offers) {
                if (offer != selectedOffer) {
                    offerTypeRightColumn.add(new OfferBlock(offer, false));
                }
            }
        }
        offerTypeBlock.add(offerTypeRightColumn);

        return offerTypeBlock;
    }

    private class OfferBlock extends HtmlDiv {

        private final Offer offer;

        public OfferBlock(final Offer offer, final boolean selected) throws UnauthorizedOperationException {
            super((selected ? "offer_selected_block" : "offer_unselected_block"));
            this.offer = offer;

            final HtmlDiv offerTopBlock = new HtmlDiv("offer_top_block");
            {
                final HtmlDiv offerLeftTopColumn = new HtmlDiv("offer_left_top_column");
                {
                    offerLeftTopColumn.add(generateAvatarBlock());
                }
                offerTopBlock.add(offerLeftTopColumn);

                final HtmlDiv offerRightTopColumn = new HtmlDiv("offer_right_top_column");
                {
                    final HtmlDiv offerPriceBlock = new HtmlDiv("offer_price_block");
                    {
                        final HtmlSpan priceLabel = new HtmlSpan("offer_block_label");
                        priceLabel.addText(Context.tr("Total price: "));
                        offerPriceBlock.add(priceLabel);

                        final HtmlSpan price = new HtmlSpan("offer_block_price");
                        price.addText(Context.getLocalizator().getCurrency(offer.getAmount()).getLocaleString());
                        offerPriceBlock.add(price);
                    }
                    offerRightTopColumn.add(offerPriceBlock);

                    final HtmlParagraph authorPara = new HtmlParagraph();
                    authorPara.setCssClass("offer_block_para");
                    {
                        final HtmlSpan authorLabel = new HtmlSpan("offer_block_label");
                        authorLabel.addText(Context.tr("Author: "));
                        authorPara.add(authorLabel);

                        final HtmlLink author = new MemberPageUrl(offer.getAuthor()).getHtmlLink(offer.getAuthor().getDisplayName());
                        author.setCssClass("offer_block_author");
                        authorPara.add(author);
                        if (offer.getAsGroup() != null) {
                            authorPara.addText(Context.tr(" on the behalf of "));
                            authorPara.add(new TeamPageUrl(offer.getAsGroup()).getHtmlLink(offer.getAsGroup().getLogin()));
                        }
                    }
                    offerRightTopColumn.add(authorPara);

                    final HtmlDiv progressPara = new HtmlDiv("offer_block_para");
                    {
                        final HtmlSpan progressLabel = new HtmlSpan("offer_block_label");
                        progressLabel.addText(Context.tr("Funding: "));
                        progressPara.add(progressLabel);

                        final int progression = (int) Math.floor(offer.getProgression());
                        final HtmlSpan progress = new HtmlSpan("offer_block_progress");
                        progress.addText(Context.tr("{0} %", String.valueOf(progression)));
                        progressPara.add(progress);

                        int cappedProgressValue = progression;
                        if (cappedProgressValue > DemandImplementation.PROGRESSION_PERCENT) {
                            cappedProgressValue = DemandImplementation.PROGRESSION_PERCENT;
                        }

                        final HtmlProgressBar progressBar = new HtmlProgressBar(cappedProgressValue);

                        progressPara.add(progressBar);

                    }
                    offerRightTopColumn.add(progressPara);

                }
                offerTopBlock.add(offerRightTopColumn);

            }
            add(offerTopBlock);

            // TODO: choose to display the title or not

            final HtmlDiv offerBottomBlock = new HtmlDiv("offer_bottom_block");
            {
                final HtmlDiv offerLeftBottomColumn = new HtmlDiv("offer_left_bottom_column");
                {
                    offerLeftBottomColumn.add(generatePopularityBlock());
                }
                offerBottomBlock.add(offerLeftBottomColumn);

                final HtmlDiv offerRightBottomColumn = new HtmlDiv("offer_right_bottom_column");
                {
                    // Lots
                    final PageIterable<Batch> lots = offer.getBatches();
                    if (lots.size() == 1) {
                        final Batch lot = lots.iterator().next();

                        final HtmlParagraph datePara = new HtmlParagraph();
                        datePara.setCssClass("offer_block_para");
                        {
                            final HtmlSpan dateLabel = new HtmlSpan("offer_block_label");
                            dateLabel.addText(Context.tr("Delivery Date: "));
                            datePara.add(dateLabel);

                            // TODO: use scheduled release date
                            final HtmlSpan date = new HtmlSpan("offer_block_date");
                            date.addText(Context.getLocalizator().getDate(lot.getExpirationDate()).toString(FormatStyle.MEDIUM));
                            datePara.add(date);
                        }
                        offerRightBottomColumn.add(datePara);

                        final HtmlParagraph description = new HtmlParagraph();
                        {
                            final HtmlSpan descriptionLabel = new HtmlSpan("offer_block_label");
                            descriptionLabel.addText(Context.tr("Offer's description: "));
                            description.add(descriptionLabel);
                            description.add(new HtmlTagText("<br />"));
                            description.addText(lot.getDescription());
                        }
                        offerRightBottomColumn.add(description);
                    } else {
                        int i = 0;
                        for (final Batch lot : lots) {
                            i++;
                            final HtmlDiv lotBlock = new HtmlDiv("offer_lot_block");
                            {
                                final HtmlDiv offerLotPriceBlock = new HtmlDiv("offer_price_block");
                                {
                                    final HtmlSpan priceLabel = new HtmlSpan("offer_block_label");
                                    priceLabel.addText(Context.tr("Price: "));
                                    offerLotPriceBlock.add(priceLabel);

                                    final HtmlSpan price = new HtmlSpan("offer_block_price_lot");
                                    price.addText(Context.getLocalizator().getCurrency(lot.getAmount()).getLocaleString());
                                    offerLotPriceBlock.add(price);
                                }
                                lotBlock.add(offerLotPriceBlock);

                                final HtmlTitle lotTitle = new HtmlTitle(Context.tr("Lot {0} - ", i) + getLotState(lot), 2);
                                lotBlock.add(lotTitle);

                                if (isDeveloper() && demand.getDemandState() == DemandState.DEVELOPPING
                                        && demand.getSelectedOffer().getCurrentBatch().equals(lot)) {
                                    new HtmlLink("plop", tr("add a release"));
                                }

                                final HtmlParagraph datePara = new HtmlParagraph();
                                datePara.setCssClass("offer_block_para");
                                {
                                    final HtmlSpan dateLabel = new HtmlSpan("offer_block_label");
                                    dateLabel.addText(Context.tr("Delivery Date: "));
                                    datePara.add(dateLabel);

                                    // TODO: use scheduled release date
                                    final HtmlSpan date = new HtmlSpan("offer_block_date");
                                    date.addText(Context.getLocalizator().getDate(lot.getExpirationDate()).toString(FormatStyle.MEDIUM));
                                    datePara.add(date);
                                }
                                lotBlock.add(datePara);

                                final HtmlParagraph description = new HtmlParagraph();
                                description.addText(lot.getDescription());
                                lotBlock.add(description);

                            }
                            offerRightBottomColumn.add(lotBlock);
                        }
                    }
                }
                offerBottomBlock.add(offerRightBottomColumn);
            }
            add(offerBottomBlock);

        }

        private boolean isDeveloper() throws UnauthorizedOperationException {
            return Context.getSession().isLogged() && Context.getSession().getAuthToken().getMember().equals(demand.getSelectedOffer().getAuthor());
        }

        private String getLotState(Batch lot) {
            switch (lot.getBatchState()) {
                case PENDING:
                    return "";
                case DEVELOPING:
                    return tr("Developing");
                case UAT:
                    return tr("Released");
                case VALIDATED:
                    return tr("Validated");
                case CANCELED:
                    return tr("Canceled");
                default:
                    break;
            }
            Log.web().fatal("Lot not found in getLotState ! this is an implementation bug");
            return "";
        }

        private XmlNode generateAvatarBlock() {
            final HtmlDiv avatarBlock = new HtmlDiv("offer_avatar");

            // Add project image
            try {
                final FileResourceUrl imageUrl = new FileResourceUrl(offer.getDemand().getProject().getImage());
                // TODO: use avatar
                final HtmlImage projectImage = new HtmlImage(imageUrl, "avatar_image");
                avatarBlock.add(projectImage);
            } catch (final UnauthorizedOperationException e) {
                // no right, no image
            }

            return avatarBlock;
        }

        private XmlNode generatePopularityBlock() {

            final HtmlDiv offerSummaryPopularity = new HtmlDiv("offer_popularity");
            {
                final HtmlParagraph popularityText = new HtmlParagraph(Context.tr("Popularity"), "offer_popularity_text");
                final HtmlParagraph popularityScore = new HtmlParagraph(HtmlTools.compressKarma(offer.getPopularity()), "offer_popularity_score");

                offerSummaryPopularity.add(popularityText);
                offerSummaryPopularity.add(popularityScore);

                if (!offer.isOwner()) {
                    final int vote = offer.getUserVoteValue();
                    if (vote == 0) {
                        final HtmlDiv offerPopularityJudge = new HtmlDiv("offer_popularity_judge");
                        {

                            // Usefull
                            final PopularityVoteActionUrl usefullUrl = new PopularityVoteActionUrl(offer, true);
                            final HtmlLink usefullLink = usefullUrl.getHtmlLink("+");
                            usefullLink.setCssClass("usefull");

                            // Useless
                            final PopularityVoteActionUrl uselessUrl = new PopularityVoteActionUrl(offer, false);
                            final HtmlLink uselessLink = uselessUrl.getHtmlLink("−");
                            uselessLink.setCssClass("useless");

                            offerPopularityJudge.add(usefullLink);
                            offerPopularityJudge.add(uselessLink);
                        }
                        offerSummaryPopularity.add(offerPopularityJudge);
                    } else {
                        // Already voted
                        final HtmlDiv offerPopularityJudged = new HtmlDiv("offer_popularity_judged");
                        {
                            if (vote > 0) {
                                offerPopularityJudged.add(new HtmlParagraph("+" + vote, "usefull"));
                            } else {
                                offerPopularityJudged.add(new HtmlParagraph("−" + Math.abs(vote), "useless"));
                            }
                        }
                        offerSummaryPopularity.add(offerPopularityJudged);
                    }
                } else {
                    final HtmlDiv offerPopularityNone = new HtmlDiv("offer_popularity_none");

                    offerSummaryPopularity.add(offerPopularityNone);
                }

            }
            return offerSummaryPopularity;
        }

    }
}
