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
package com.bloatit.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;

import org.hibernate.Query;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.bloatit.data.queries.QueryCollection;
import com.bloatit.framework.utils.PageIterable;

/**
 * @author Thomas Guyard This represent a content that is Kudosable.
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//@formatter:off
@NamedQueries(value = { @NamedQuery(
                           name = "kudosable.getKudos.byMember.size",
                           query = "SELECT count(k) " +
                                   "FROM DaoKudosable as a " +
                                   "JOIN a.kudos as k " +
                                   "WHERE k.member = :member " +
                                   "AND a = :this"),
                                   
                       @NamedQuery(
                           name = "kudosable.getKudos",
                           query = "FROM DaoKudos WHERE kudosable = :this"),
                       @NamedQuery(
                           name = "kudosable.getKudos.size",
                           query = "FROM DaoKudos WHERE kudosable = :this"),
                       @NamedQuery(
                           name = "kudosable.getKudos.byMember.value",
                           query = "SELECT k.value " +
                                      "FROM DaoKudosable as a " +
                                      "JOIN a.kudos as k " +
                                      "WHERE k.member = :member " +
                                      "AND a = :this")
                       }
             )
// @formatter:on
public abstract class DaoKudosable extends DaoUserContent {

    /**
     * This is the state of a Kudosable content. PENDING means that there is not
     * enough kudos to take a decision. VALIDATE means that the popularity is
     * high enough to validate this content. REFUSED means that the popularity
     * is low enough to delete/reject this content. HIDDEN is a state between
     * pending and rejected.
     * <p>
     * <b>Do not change the order !</b>
     * </p>
     */
    public enum PopularityState {
        /** The VALIDATED. */
        VALIDATED,
        /** The PENDING. */
        PENDING,
        /** The HIDDEN. */
        HIDDEN,
        /** The REJECTED. */
        REJECTED,
    }

    /**
     * The popularity is the sum of each value attached to each kudos that
     * applies on this kudosable. it is a cached value (It could be calculated)
     */
    @Basic(optional = false)
    @Field(store = Store.NO, index = Index.UN_TOKENIZED)
    private int popularity;

    @OneToMany(mappedBy = "kudosable")
    @Cascade(value = { CascadeType.ALL })
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private final List<DaoKudos> kudos = new ArrayList<DaoKudos>();

    @Basic(optional = false)
    @Field(store = Store.NO, index = Index.UN_TOKENIZED)
    @Enumerated
    private PopularityState state;

    @Basic(optional = false)
    private boolean isPopularityLocked;

    /**
     * initial state is PENDING, and popularity is 0.
     * 
     * @param member the author.
     * @param team the team
     * @see DaoUserContent#DaoUserContent(DaoMember, DaoTeam)
     */
    protected DaoKudosable(final DaoMember member, final DaoTeam team) {
        super(member, team);
        this.popularity = 0;
        setState(PopularityState.PENDING);
        this.isPopularityLocked = false;
    }

    /**
     * Create a new DaoKudos and add it to the list of kudos.
     * 
     * @param member the member
     * @param team the team
     * @param value the value
     * @return the new popularity
     */
    public int addKudos(final DaoMember member, final DaoTeam team, final int value) {
        this.kudos.add(new DaoKudos(member, team, value, this));
        this.popularity += value;
        return this.popularity;
    }

    /**
     * Lock popularity. If the popularity is locked you cannot change it.
     */
    public void lockPopularity() {
        this.isPopularityLocked = true;
    }

    /**
     * Unlock popularity.
     */
    public void unlockPopularity() {
        this.isPopularityLocked = false;
    }

    /**
     * The state must be update from the framework layer.
     * 
     * @param state the new state
     */
    public void setState(final PopularityState state) {
        this.state = state;
    }

    /**
     * Use a HQL query to find if a member as already kudosed this kudosable.
     * 
     * @param member The member that could have kudosed this kudosable. (Don't
     *            even think of passing a null member)
     * @return true if member has kudosed, false otherwise.
     */
    public boolean hasKudosed(final DaoMember member) {
        final Query q = SessionManager.getNamedQuery("kudosable.getKudos.byMember.size");
        q.setEntity("member", member);
        q.setEntity("this", this);
        return (Long) q.uniqueResult() > 0;
    }

    /**
     * Checks if the popularity is locked.
     * 
     * @return true, if is popularity locked
     */
    public boolean isPopularityLocked() {
        return this.isPopularityLocked;
    }

    /**
     * Use a HQL query to find if a member as already kudosed this kudosable.
     * 
     * @param member The member that could have kudosed this kudosable. (Don't
     *            even think of passing a null member)
     * @return true if member has kudosed, false otherwise.
     */
    public int getVote(final DaoMember member) {
        final Query q = SessionManager.getNamedQuery("kudosable.getKudos.byMember.value");
        q.setEntity("member", member);
        q.setEntity("this", this);
        // return 0 if no vote
        final Integer vote = (Integer) q.uniqueResult();
        if (vote == null) {
            return 0;
        }
        return vote;
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public PopularityState getState() {
        return this.state;
    }

    /**
     * The popularity is the sum of each value attached to each kudos that
     * applies on this kudosable.
     * 
     * @return the popularity is the sum of each value attached to each kudos
     *         that applies on this kudosable
     */
    public int getPopularity() {
        return this.popularity;
    }

    public PageIterable<DaoKudos> getKudos() {
        return new QueryCollection<DaoKudos>("kudosable.getKudos").setEntity("this", this);
    }

    // ======================================================================
    // For hibernate mapping
    // ======================================================================

    /**
     * Instantiates a new dao kudosable.
     */
    protected DaoKudosable() {
        super();
    }

    // ======================================================================
    // equals and hashcode
    // ======================================================================

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

}
