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

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.bloatit.framework.exceptions.NonOptionalParameterException;

/**
 * This represent an invitation to join a group.
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DaoJoinGroupInvitation extends DaoIdentifiable {

    /**
     * <p>
     * The state of an invitation track its time line. (First PENDING, then
     * REFUSED or ACCEPTED)
     * </p>
     * <p>
     * The state DISCARDED means the invitation have been refused cause the user
     * accepted another invitation for the same group, cancelling all other
     * invitations for this group. It is therefore an alternative to REFUSED or
     * ACCEPTED
     * </p>
     */
    public enum State {
        ACCEPTED, REFUSED, PENDING, DISCARDED
    }

    @ManyToOne(optional = false)
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    private DaoMember sender;
    
    @ManyToOne(optional = false)
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    private DaoMember receiver;
    
    @ManyToOne(optional = false)
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    private DaoGroup group;
    
    @Basic(optional = false)
    @Enumerated
    private State state;

    // ======================================================================
    // Static HQL requests
    // ======================================================================

    /**
     * Gets the invitation to join a group.
     * 
     * @param group the group this invitation is on.
     * @param member the member this invitation was sent to.
     * @return the invitation, are null if there is no invitation on that
     *         <code>group</code> sent to this <code>member</code>.
     */
    public static DaoJoinGroupInvitation getInvitation(final DaoGroup group, final DaoMember member) {
        return (DaoJoinGroupInvitation) SessionManager.createQuery("from DaoJoinGroupInvitation where group = :group and receiver = :member")
                                                      .setEntity("group", group)
                                                      .setEntity("member", member)
                                                      .uniqueResult();
    }

    // ======================================================================
    // Construction.
    // ======================================================================

    public static DaoJoinGroupInvitation createAndPersist(final DaoMember sender, final DaoMember reciever, final DaoGroup group) {
        final Session session = SessionManager.getSessionFactory().getCurrentSession();
        final DaoJoinGroupInvitation joinDemand = new DaoJoinGroupInvitation(sender, reciever, group);
        try {
            session.save(joinDemand);
        } catch (final HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        }
        return joinDemand;
    }

    /**
     * Create a new invitation. Set the state to PENDING.
     * 
     * @throws NonOptionalParameterException if any of the parameters are null.
     */
    private DaoJoinGroupInvitation(final DaoMember sender, final DaoMember receiver, final DaoGroup group) {
        super();
        if (sender == null || receiver == null || group == null) {
            throw new NonOptionalParameterException();
        }
        this.sender = sender;
        this.receiver = receiver;
        this.group = group;
        this.state = State.PENDING;
    }

    /**
     * Set the state to accepted and add the receiver into the list of members
     * of this.group. If the state is not PENDING then do nothing.
     */
    public void accept() {
        if (this.state == State.PENDING) {
            this.receiver.addToGroup(this.group);
            this.state = State.ACCEPTED;
        }
    }

    /**
     * Set the state to refused. If the state is not PENDING then do nothing.
     */
    public void refuse() {
        if (this.state == State.PENDING) {
            this.state = State.REFUSED;
        }
    }

    /**
     * <p>
     * Sets the state to DISCARDED.
     * </p>
     * <p>
     * If the current state is not PENDING, nothing happens
     * </p>
     */
    public void discard() {
        if (this.state == State.PENDING) {
            this.state = State.DISCARDED;
        }
    }

    // ======================================================================
    // Visitor.
    // ======================================================================

    @Override
    public <ReturnType> ReturnType accept(final DataClassVisitor<ReturnType> visitor) {
        return visitor.visit(this);
    }

    // ======================================================================
    // Getters
    // ======================================================================

    public DaoMember getSender() {
        return this.sender;
    }

    public DaoMember getReceiver() {
        return this.receiver;
    }

    public State getState() {
        return this.state;
    }

    public DaoGroup getGroup() {
        return this.group;
    }

    // ======================================================================
    // For hibernate mapping
    // ======================================================================

    protected DaoJoinGroupInvitation() {
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.group == null) ? 0 : this.group.hashCode());
        result = prime * result + ((this.receiver == null) ? 0 : this.receiver.hashCode());
        result = prime * result + ((this.sender == null) ? 0 : this.sender.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DaoJoinGroupInvitation)) {
            return false;
        }
        final DaoJoinGroupInvitation other = (DaoJoinGroupInvitation) obj;
        if (this.group == null) {
            if (other.group != null) {
                return false;
            }
        } else if (!this.group.equals(other.group)) {
            return false;
        }
        if (this.receiver == null) {
            if (other.receiver != null) {
                return false;
            }
        } else if (!this.receiver.equals(other.receiver)) {
            return false;
        }
        if (this.sender == null) {
            if (other.sender != null) {
                return false;
            }
        } else if (!this.sender.equals(other.sender)) {
            return false;
        }
        return true;
    }

}
