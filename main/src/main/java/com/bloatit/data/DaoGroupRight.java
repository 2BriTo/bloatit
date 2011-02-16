package com.bloatit.data;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

/**
 * This class is for Hibernate only
 * <p>
 * Describes what a member can do in a given group
 * </p>
 */
@Entity
public class DaoGroupRight extends DaoIdentifiable {
    
    public enum UserGroupRight {
        CONSULT, TALK, INVITE, MODIFY, PROMOTE, BANK, 
    }

    @ManyToOne(optional = false)
    private DaoGroupMembership membership;

    @Basic(optional = false)
    @Enumerated
    private UserGroupRight userStatus;

    protected DaoGroupRight(DaoGroupMembership membership, UserGroupRight userStatus) {
        super();
        this.membership = membership;
        this.userStatus = userStatus;
    }

    protected DaoGroupMembership getMembership() {
        return membership;
    }

    protected UserGroupRight getUserStatus() {
        return userStatus;
    }

    // ======================================================================
    // For hibernate mapping
    // ======================================================================

    protected DaoGroupRight() {
        super();
    }

    // ======================================================================
    // equals and hashcode
    // ======================================================================

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((membership == null) ? 0 : membership.hashCode());
        result = prime * result + ((userStatus == null) ? 0 : userStatus.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DaoGroupRight other = (DaoGroupRight) obj;
        if (membership == null) {
            if (other.membership != null)
                return false;
        } else if (!membership.equals(other.membership))
            return false;
        if (userStatus != other.userStatus)
            return false;
        return true;
    }
}