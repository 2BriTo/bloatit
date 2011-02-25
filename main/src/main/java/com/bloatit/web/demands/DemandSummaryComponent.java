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
package com.bloatit.web.demands;

import static com.bloatit.framework.webserver.Context.tr;

import java.text.NumberFormat;
import java.util.Locale;

import com.bloatit.framework.exceptions.UnauthorizedOperationException;
import com.bloatit.framework.utils.i18n.CurrencyLocale;
import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.components.HtmlDiv;
import com.bloatit.framework.webserver.components.HtmlImage;
import com.bloatit.framework.webserver.components.HtmlLink;
import com.bloatit.framework.webserver.components.HtmlParagraph;
import com.bloatit.framework.webserver.components.HtmlSpan;
import com.bloatit.framework.webserver.components.HtmlTitle;
import com.bloatit.framework.webserver.components.PlaceHolderElement;
import com.bloatit.framework.webserver.components.meta.HtmlElement;
import com.bloatit.model.Demand;
import com.bloatit.model.FileMetadata;
import com.bloatit.model.Member;
import com.bloatit.model.Offer;
import com.bloatit.model.Translation;
import com.bloatit.model.demand.DemandImplementation;
import com.bloatit.web.HtmlTools;
import com.bloatit.web.components.HtmlProgressBar;
import com.bloatit.web.members.MembersTools;
import com.bloatit.web.pages.master.HtmlPageComponent;
import com.bloatit.web.url.ContributePageUrl;
import com.bloatit.web.url.FileResourceUrl;
import com.bloatit.web.url.MemberPageUrl;
import com.bloatit.web.url.OfferPageUrl;
import com.bloatit.web.url.PopularityVoteActionUrl;
import com.bloatit.web.url.ProjectPageUrl;
import com.bloatit.web.url.ReportBugPageUrl;

public final class DemandSummaryComponent extends HtmlPageComponent {

    private static final String IMPORTANT_CSS_CLASS = "important";
    private final Demand demand;

    public DemandSummaryComponent(final Demand demand) {
        super();
        this.demand = demand;

        // Extract locales stuffs
        final Locale defaultLocale = Context.getLocalizator().getLocale();

        // ////////////////////
        // Div demand_summary
        final HtmlDiv demandSummary = new HtmlDiv("demand_summary");
        {
            // ////////////////////
            // Div demand_summary_top
            final HtmlDiv demandSummaryTop = new HtmlDiv("demand_summary_top");
            {
                // ////////////////////
                // Div demand_summary_left
                final HtmlDiv demandSummaryLeft = new HtmlDiv("demand_summary_left");
                {
                    // TODO: set a fixed size block to not depend of the image
                    // size
                    // Add project image
                    try {
                        FileMetadata image = demand.getProject().getImage();
                        if (image != null) {
                            final FileResourceUrl imageUrl = new FileResourceUrl(image);
                            final HtmlImage projectImage = new HtmlImage(imageUrl, image.getShortDescription(), "project_image");
                            demandSummaryLeft.add(projectImage);
                        } else {
                            // TODO: add a fallback image
                        }
                    } catch (final UnauthorizedOperationException e) {
                        // no right, no image
                    }

                }
                demandSummaryTop.add(demandSummaryLeft);

                // ////////////////////
                // Div demand_summary_center
                final HtmlDiv demandSummaryCenter = new HtmlDiv("demand_summary_center");
                {
                    // Try to display the title
                    try {
                        final Translation translatedDescription = demand.getDescription().getTranslationOrDefault(defaultLocale);
                        final HtmlSpan projectSpan = new HtmlSpan("demand_project_title");
                        projectSpan.add(new ProjectPageUrl(demand.getProject()).getHtmlLink(demand.getProject().getName()));
                        final HtmlTitle title = new HtmlTitle(1);
                        title.setCssClass("demand_title");
                        title.add(projectSpan);
                        title.addText(" – ");
                        title.addText(translatedDescription.getTitle());

                        demandSummaryCenter.add(title);

                    } catch (final UnauthorizedOperationException e) {
                        // no right no description and no title
                    }

                }
                demandSummaryTop.add(demandSummaryCenter);
            }
            demandSummary.add(demandSummaryTop);

            // ////////////////////
            // Div demand_summary_bottom
            final HtmlDiv demandSummaryBottom = new HtmlDiv("demand_sumary_bottom");
            {

                // ////////////////////
                // Div demand_summary_popularity
                final HtmlDiv demandSummaryPopularity = new HtmlDiv("demand_summary_popularity");
                {
                    final HtmlParagraph popularityText = new HtmlParagraph(Context.tr("Popularity"), "demand_popularity_text");
                    final HtmlParagraph popularityScore = new HtmlParagraph(HtmlTools.compressKarma(demand.getPopularity()),
                                                                            "demand_popularity_score");

                    demandSummaryPopularity.add(popularityText);
                    demandSummaryPopularity.add(popularityScore);

                    if (!demand.isOwner()) {
                        final int vote = demand.getUserVoteValue();
                        if (vote == 0) {
                            final HtmlDiv demandPopularityJudge = new HtmlDiv("demand_popularity_judge");
                            {

                                // Usefull
                                final PopularityVoteActionUrl usefulUrl = new PopularityVoteActionUrl(demand, true);
                                final HtmlLink usefulLink = usefulUrl.getHtmlLink("+");
                                usefulLink.setCssClass("useful");

                                // Useless
                                final PopularityVoteActionUrl uselessUrl = new PopularityVoteActionUrl(demand, false);
                                final HtmlLink uselessLink = uselessUrl.getHtmlLink("−");
                                uselessLink.setCssClass("useless");

                                demandPopularityJudge.add(usefulLink);
                                demandPopularityJudge.add(uselessLink);
                            }
                            demandSummaryPopularity.add(demandPopularityJudge);
                        } else {
                            // Already voted
                            final HtmlDiv demandPopularityJudged = new HtmlDiv("demand_popularity_judged");
                            {
                                if (vote > 0) {
                                    demandPopularityJudged.add(new HtmlParagraph("+" + vote, "useful"));
                                } else {
                                    demandPopularityJudged.add(new HtmlParagraph("−" + Math.abs(vote), "useless"));
                                }
                            }
                            demandSummaryPopularity.add(demandPopularityJudged);
                        }
                    } else {
                        final HtmlDiv demandPopularityNone = new HtmlDiv("demand_popularity_none");

                        demandSummaryPopularity.add(demandPopularityNone);
                    }

                }
                demandSummaryBottom.add(demandSummaryPopularity);

                HtmlDiv demandSummaryProgress;
                try {
                    demandSummaryProgress = generateProgressBlock(demand);
                    demandSummaryBottom.add(demandSummaryProgress);
                } catch (final UnauthorizedOperationException e) {
                    // display nothing
                }

                // ////////////////////
                // Div demand_summary_share
                final HtmlDiv demandSummaryShare = new HtmlDiv("demand_summary_share_button");
                {
                    final HtmlLink showHideShareBlock = new HtmlLink("javascript:showHide('demand_summary_share')", Context.tr("+ Share"));
                    demandSummaryShare.add(showHideShareBlock);
                }
                demandSummaryBottom.add(demandSummaryShare);

            }
            demandSummary.add(demandSummaryBottom);

            // ////////////////////
            // Div demand_summary_share
            final HtmlDiv demand_sumary_share = new HtmlDiv("demand_sumary_share", "demand_sumary_share");
            {

            }
            demandSummary.add(demand_sumary_share);

        }

        add(demandSummary);
    }

    public HtmlDiv generateProgressBlock(final Demand demand) throws UnauthorizedOperationException {
        // ////////////////////
        // Div demand_summary_progress
        final HtmlDiv demandSummaryProgress = new HtmlDiv("demand_summary_progress");
        {
            float progressValue = 0;
            // Progress bar
            try {

                progressValue = (float) Math.floor(demand.getProgression());
                float cappedProgressValue = progressValue;
                if (cappedProgressValue > DemandImplementation.PROGRESSION_PERCENT) {
                    cappedProgressValue = DemandImplementation.PROGRESSION_PERCENT;
                }

                final HtmlProgressBar progressBar = new HtmlProgressBar(cappedProgressValue);
                demandSummaryProgress.add(progressBar);

                // Progress text
                demandSummaryProgress.add(generateProgressText(demand, progressValue));

            } catch (final UnauthorizedOperationException e) {
                // No right, no progress bar
            }

            // ////////////////////
            // Div demand_summary_actions
            final HtmlDiv actions = new HtmlDiv("demand_summary_actions");
            {
                final HtmlDiv actionsButtons = new HtmlDiv("demand_summary_actions_buttons");
                actions.add(actionsButtons);
                switch (demand.getDemandState()) {
                    case PENDING:
                        actionsButtons.add(new HtmlDiv("contribute_block").add(generatePendingRightActions()));
                        actionsButtons.add(new HtmlDiv("make_offer_block").add(generatePendingLeftActions()));
                        break;
                    case PREPARING:
                        actionsButtons.add(new HtmlDiv("contribute_block").add(generatePendingRightActions()));
                        actionsButtons.add(new HtmlDiv("make_offer_block").add(generatePendingLeftActions()));
                        break;
                    case DEVELOPPING:
                        actionsButtons.add(new HtmlDiv("developer_description_block").add(generateDevelopingRightActions()));
                        actionsButtons.add(new HtmlDiv("report_bug_block").add(generateDevelopingLeftActions()));
                        break;
                    case FINISHED:
                        // actionsButtons.add(new
                        // HtmlDiv("contribute_block").add(generatePendingRightActions()));
                        // actionsButtons.add(new
                        // HtmlDiv("make_offer_block").add(generatePendingLeftActions()));
                        break;
                    case DISCARDED:
                        // actionsButtons.add(new
                        // HtmlDiv("contribute_block").add(generatePendingRightActions()));
                        // actionsButtons.add(new
                        // HtmlDiv("make_offer_block").add(generatePendingLeftActions()));
                        break;
                    default:
                        break;
                }
            }
            demandSummaryProgress.add(actions);

        }
        return demandSummaryProgress;
    }

    public PlaceHolderElement generatePendingRightActions() {
        PlaceHolderElement element = new PlaceHolderElement();
        final HtmlParagraph contributeText = new HtmlParagraph(Context.tr("You share this need and you want participate financially?"));
        element.add(contributeText);

        final HtmlLink link = new ContributePageUrl(demand).getHtmlLink(Context.tr("Contribute"));
        link.setCssClass("button");
        element.add(link);
        return element;
    }

    public PlaceHolderElement generatePendingLeftActions() {
        PlaceHolderElement element = new PlaceHolderElement();
        final HtmlParagraph makeOfferText = new HtmlParagraph(Context.tr("You are a developer and want to be paid to achieve this request?"));
        element.add(makeOfferText);

        final HtmlLink link = new OfferPageUrl(demand).getHtmlLink(Context.tr("Make an offer"));
        link.setCssClass("button");
        element.add(link);
        return element;
    }

    public PlaceHolderElement generateDevelopingRightActions() throws UnauthorizedOperationException {
        PlaceHolderElement element = new PlaceHolderElement();
        Member author = demand.getSelectedOffer().getAuthor();
        element.add(MembersTools.getMemberAvatar(author));
        element.add(new HtmlParagraph(tr("The developer of this feature is:")));
        element.add(new MemberPageUrl(author).getHtmlLink(author.getDisplayName()));
        return element;
    }

    public PlaceHolderElement generateDevelopingLeftActions() throws UnauthorizedOperationException {
        PlaceHolderElement element = new PlaceHolderElement();
        if (!demand.getSelectedOffer().hasRelease()) {
            element.add(new HtmlParagraph(tr("This is feature currently in development. Read the comments to have an overview of the progress.")));
        } else {
            element.add(new HtmlParagraph(tr("This is feature has at least one release. Test it and report bugs.")));
            final HtmlLink link = new ReportBugPageUrl(demand.getSelectedOffer()).getHtmlLink(Context.tr("Report a bug"));
            link.setCssClass("button");
            element.add(link);
        }
        return element;
    }

    private HtmlElement generateProgressText(final Demand demand, final float progressValue) {

        Offer currentOffer = null;
        try {
            currentOffer = demand.getSelectedOffer();
        } catch (final UnauthorizedOperationException e1) {
            // Nothing.
        }
        if (currentOffer == null) {

            final HtmlSpan amount = new HtmlSpan();
            amount.setCssClass(IMPORTANT_CSS_CLASS);

            CurrencyLocale currency;
            try {
                currency = Context.getLocalizator().getCurrency(demand.getContribution());

                amount.addText(currency.getDefaultString());

            } catch (final UnauthorizedOperationException e) {
                // No right, no display
            }
            final HtmlParagraph progressText = new HtmlParagraph();
            progressText.setCssClass("demand_progress_text");

            progressText.add(amount);
            progressText.addText(Context.tr(" no offer"));

            return progressText;
        }
        // Amount
        CurrencyLocale amountCurrency;
        try {
            amountCurrency = Context.getLocalizator().getCurrency(demand.getContribution());
            final HtmlSpan amount = new HtmlSpan();
            amount.setCssClass(IMPORTANT_CSS_CLASS);
            amount.addText(amountCurrency.getDefaultString());

            // Target
            final CurrencyLocale targetCurrency = Context.getLocalizator().getCurrency(currentOffer.getAmount());
            final HtmlSpan target = new HtmlSpan();
            target.setCssClass(IMPORTANT_CSS_CLASS);
            target.addText(targetCurrency.getDefaultString());

            // Progress
            final HtmlSpan progress = new HtmlSpan();
            progress.setCssClass(IMPORTANT_CSS_CLASS);
            final NumberFormat format = NumberFormat.getNumberInstance();
            format.setMinimumFractionDigits(0);
            progress.addText("" + format.format(progressValue) + " %");

            final HtmlParagraph progressText = new HtmlParagraph();
            progressText.setCssClass("demand_progress_text");

            progressText.add(amount);
            progressText.addText(Context.tr(" i.e. "));
            progressText.add(progress);
            progressText.addText(Context.tr(" of "));
            progressText.add(target);
            progressText.addText(Context.tr(" requested "));

            return progressText;
        } catch (final UnauthorizedOperationException e) {
            // No right, no display
            return new PlaceHolderElement();
        }

    }
}