package com.bloatit.model.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.OrderBy;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import com.bloatit.common.FatalErrorException;
import com.bloatit.common.Log;
import com.bloatit.common.PageIterable;
import com.bloatit.model.data.util.NonOptionalParameterException;
import com.bloatit.model.data.util.SessionManager;
import com.bloatit.model.exceptions.NotEnoughMoneyException;

/**
 * A DaoDemand is a kudosable content. It has a translatable description, and can have a
 * specification and some offers. The state of the demand is managed by its super class
 * DaoKudosable. On a demand we can add some comment and some contriutions.
 */
@Entity
@Indexed
public final class DaoDemand extends DaoKudosable {

    /**
     * This is the state of the demand. It's used in the workflow modeling. The order is
     * important !
     */
    public enum DemandState {
        PENDING, PREPARING, DEVELOPPING, INCOME, DISCARDED, FINISHED
    }

    /**
     * This is a calculated value with the sum of the value of all contributions.
     */
    @Basic(optional = false)
    private BigDecimal contribution;

    @Basic(optional = false)
    @Enumerated
    private DemandState demandState;

    /**
     * A description is a translatable text with an title.
     */
    @OneToOne(optional = false)
    @Cascade(value = { CascadeType.ALL })
    @IndexedEmbedded
    private DaoDescription description;

    @OneToOne(mappedBy = "demand")
    @Cascade(value = { CascadeType.ALL })
    @IndexedEmbedded
    private DaoSpecification specification;

    @OneToMany(mappedBy = "demand")
    @Cascade(value = { CascadeType.ALL })
    @OrderBy(clause = "popularity desc")
    @IndexedEmbedded
    private final Set<DaoOffer> offers = new HashSet<DaoOffer>(0);

    @OneToMany(mappedBy = "demand")
    @OrderBy(clause = "creationDate DESC")
    @Cascade(value = { CascadeType.ALL })
    private final Set<DaoContribution> contributions = new HashSet<DaoContribution>(0);

    @OneToMany
    @Cascade(value = { CascadeType.ALL })
    @IndexedEmbedded
    private final Set<DaoComment> comments = new HashSet<DaoComment>(0);

    /**
     * The selected offer is the offer that is most likely to be validated and used. If an
     * offer is selected and has enough money and has a elapse time done then this offer
     * go into dev.
     */
    @ManyToOne(optional = true)
    @Cascade(value = { CascadeType.ALL })
    @IndexedEmbedded
    private DaoOffer selectedOffer;

    /**
     * @see #DaoDemand(DaoMember, DaoDescription)
     */
    public static DaoDemand createAndPersist(final DaoMember member, final DaoDescription description) {
        final Session session = SessionManager.getSessionFactory().getCurrentSession();
        final DaoDemand demand = new DaoDemand(member, description);
        try {
            session.save(demand);
        } catch (final HibernateException e) {
            session.getTransaction().rollback();
            session.beginTransaction();
            throw e;
        }
        return demand;
    }

    /**
     * @see #DaoDemand(DaoMember, DaoDescription, DaoOffer)
     */
    public static DaoDemand createAndPersist(final DaoMember member, final DaoDescription description, final DaoOffer offer) {
        final Session session = SessionManager.getSessionFactory().getCurrentSession();
        final DaoDemand demand = new DaoDemand(member, description, offer);
        try {
            session.save(demand);
        } catch (final HibernateException e) {
            session.getTransaction().rollback();
            session.beginTransaction();
            throw e;
        }
        return demand;
    }

    /**
     * Create a DaoDemand and set its state to the state PENDING.
     *
     * @param member is the author of the demand
     * @param description is the description ...
     * @throws NonOptionalParameterException if any of the parameter is null.
     */
    private DaoDemand(final DaoMember member, final DaoDescription description) {
        super(member);
        if (description == null) {
            throw new NonOptionalParameterException();
        }
        this.description = description;
        this.specification = null;
        this.setSelectedOffer(null);
        this.contribution = BigDecimal.ZERO;
        this.setDemandState(DemandState.PENDING);
    }

    /**
     * Create a DaoDemand, add an offer and set its state to the state
     * {@link DemandState#PREPARING}.
     *
     * @param member is the author of the demand
     * @param description is the description ...
     * @param offer
     * @throws NonOptionalParameterException if any of the parameter is null.
     */
    private DaoDemand(final DaoMember member, final DaoDescription description, final DaoOffer offer) {
        this(member, description);
        if (offer == null) {
            throw new NonOptionalParameterException();
        }
        this.offers.add(offer);
        this.setDemandState(DemandState.PREPARING);
    }

    /**
     * Delete this DaoDemand from the database. "this" will remain, but unmapped. (You
     * shoudn't use it then)
     */
    public void delete() {
        final Session session = SessionManager.getSessionFactory().getCurrentSession();
        session.delete(this);
    }

    /**
     * Create a specification.
     *
     * @param member author (must be non null).
     * @param content a string contain the specification (WARNING : UNTESTED)(must be non
     *        null).
     */
    public void createSpecification(final DaoMember member, final String content) {
        specification = new DaoSpecification(member, content, this);
    }

    /**
     * Add a new offer for this demand.
     *
     * @param member the author of the offer
     * @param amount the amount that the author want to make the offer
     * @param description this is a description of the offer
     * @param dateExpir this is when the offer should be finish ?
     * @return the newly created offer.
     */
    public DaoOffer addOffer(final DaoMember member, final BigDecimal amount, final DaoDescription description, final Date dateExpir) {
        final DaoOffer offer = new DaoOffer(member, this, amount, description, dateExpir);
        offers.add(offer);
        return offer;
    }

    /**
     * delete offer from this demand AND FROM DB !
     *
     * @param Offer the offer we want to delete.
     */
    public void removeOffer(final DaoOffer offer) {
        offers.remove(offer);
        SessionManager.getSessionFactory().getCurrentSession().delete(offer);
    }

    /**
     * Add a contribution to a demand.
     *
     * @param member the author of the contribution
     * @param amount the > 0 amount of euros on this contribution
     * @param comment a <= 144 char comment on this contribution
     * @throws NotEnoughMoneyException
     */
    public void addContribution(final DaoMember member, final BigDecimal amount, final String comment) throws NotEnoughMoneyException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            Log.data().fatal("Cannot create a contribution with this amount " + amount.toEngineeringString() + " by member " + member.getId());
            throw new FatalErrorException("The amount of a contribution cannot be <= 0.", null);
        }
        if (comment != null && comment.length() > DaoContribution.COMMENT_MAX_LENGTH) {
            Log.data().fatal("The comment of a contribution must be <= 144 chars long.");
            throw new FatalErrorException("Comments lenght of Contribution must be < 144.", null);
        }

        contributions.add(new DaoContribution(member, this, amount, comment));
        contribution = contribution.add(amount);
    }

    public DaoSpecification getSpecification() {
        return specification;
    }

    public DaoDescription getDescription() {
        return description;
    }

    /**
     * Use a HQL query to get the offers as a PageIterable collection
     */
    public PageIterable<DaoOffer> getOffersFromQuery() {
        return new QueryCollection<DaoOffer>("from DaoOffer as f where f.demand = :this").setEntity("this", this);
    }

    /**
     * The current offer is the offer with the max popularity then the min amount.
     *
     * @return the current offer for this demand, or null if there is no offer.
     */
    @Deprecated
    public DaoOffer getCurrentOffer() {
        // First try to find a validated offer.
        final String validatedQueriStr = "FROM DaoOffer " + //
                "WHERE demand = :this AND state = :state " + //
                "ORDER BY amount ASC, creationDate DESC ";

        final Query validateQuery = SessionManager.createQuery(validatedQueriStr).setEntity("this", this)
                .setParameter("state", DaoKudosable.State.VALIDATED);
        if (validateQuery.iterate().hasNext()) {
            return (DaoOffer) validateQuery.iterate().next();
        }

        // If there is no validated offer then we try to find a pending offer
        final String queryString = "FROM DaoOffer " + //
                "WHERE demand = :this " + //
                "AND state = :state " + //
                "AND popularity = (select max(popularity) from DaoOffer where demand = :this) " + //
                "ORDER BY amount ASC, creationDate DESC";
        try {
            return (DaoOffer) SessionManager.createQuery(queryString).setEntity("this", this).setParameter("state", DaoKudosable.State.PENDING)
                    .iterate().next();
        } catch (final NoSuchElementException e) {
            return null;
        }
    }

    public Set<DaoOffer> getOffers() {
        return offers;
    }

    public void setDemandState(DemandState demandState) {
        this.demandState = demandState;
    }

    public DemandState getDemandState() {
        return demandState;
    }

    /**
     * Use a HQL query to get the contributions as a PageIterable collection
     */
    public PageIterable<DaoContribution> getContributionsFromQuery() {
        return new QueryCollection<DaoContribution>("from DaoContribution as f where f.demand = :this").setEntity("this", this);
    }

    /**
     * Use a HQL query to get the first level comments as a PageIterable collection
     */
    public PageIterable<DaoComment> getCommentsFromQuery() {
        return new QueryCollection<DaoComment>(SessionManager.getSessionFactory().getCurrentSession().createFilter(comments, ""), SessionManager
                .getSessionFactory().getCurrentSession().createFilter(comments, "select count(*)"));
    }

    public DaoOffer getSelectedOffer() {
        return selectedOffer;
    }

    public void setSelectedOffer(DaoOffer selectedOffer) {
        this.selectedOffer = selectedOffer;
    }

    public void addComment(final DaoComment comment) {
        comments.add(comment);
    }

    public BigDecimal getContribution() {
        return contribution;
    }

    /**
     * @return the minimum value of the contribution on this demand.
     */
    public BigDecimal getContributionMin() {
        return (BigDecimal) SessionManager.createQuery("select min(f.amount) from DaoContribution as f where f.demand = :this")
                .setEntity("this", this).uniqueResult();
    }

    /**
     * @return the maximum value of the contribution on this demand.
     */
    public BigDecimal getContributionMax() {
        return (BigDecimal) SessionManager.createQuery("select max(f.amount) from DaoContribution as f where f.demand = :this")
                .setEntity("this", this).uniqueResult();
    }

    // ======================================================================
    // For hibernate mapping
    // ======================================================================

    protected DaoDemand() {
        super();
    }

}
