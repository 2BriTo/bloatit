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
package com.bloatit.model.right;

import java.util.EnumSet;

import com.bloatit.common.Log;
import com.bloatit.data.DaoExternalServiceMembership.RightLevel;
import com.bloatit.model.Rights;

/**
 * The class Accessor is a class that test if a member as the right to access a
 * <code>property</code>.
 * <p>
 * It implements two methods : <code>canAccess</code> that return a boolean if
 * you can access the feature and <code>tryAccess</code> that throw an
 * {@link UnauthorizedOperationException} if you cannot.
 * </p>
 * <p>
 * This class implement the template method pattern so that child classes just
 * have to implement the can method. You have to create an
 * {@link GenericAccessor} child class for each feature.
 * </p>
 * For example you could create an {@link GenericAccessor} on the Fullname
 * property like this:
 * 
 * <pre>
 * class MemberCanAccessFullname extends {@link GenericAccessor} {
 *     &#064;Override
 *     protected boolean can(EnumSet&lt;Role&gt; role, Action action) {
 *         return role == OWNER || (action == READ);
 *     }
 * }
 * </pre>
 * 
 * There are some useful functions and code organization to manage the creation
 * of Accessor classes see {@link RightManager}.
 * 
 * @see RightManager
 */
public abstract class GenericAccessor<T extends UnauthorizedOperationException> {

    /**
     * Can is the method you have to implement in the template method pattern.
     * It is used in the {@link #canAccess(RestrictedObject, Action)} and
     * {@link #tryAccess(RestrictedObject, Action)} methods
     * 
     * @param object is the object on which we want to do the
     *            <code>action</code>
     * @param action is the action.
     * @return true, if we have the right to access the RestrictedObject, false
     *         otherwise.
     */
    protected abstract boolean can(Rights object, Action action);

    protected abstract T exception(Action action);

    /**
     * If the current user use a weak auth token, and this method return false
     * then there is no way the user will be granted for this access.
     * 
     * @param rights
     * @param action
     * @return true (Overload me if you want)
     */
    protected boolean authorizeWeakAccess(EnumSet<RightLevel> rights, final Action action) {
        return true;
    }

    /**
     * CanAccess call the abstract {@link #can(Rights, Action)} method to know
     * if the user has the right to access the <code>object</code>.
     * 
     * @param object is the object on which we want to do the
     *            <code>action</code>
     * @param action is the action.
     * @return true, if we have the right to access the RestrictedObject, false
     *         otherwise.
     */
    public final boolean canAccess(final Rights object, final Action action) {
        if (object.hasAdminUserPrivilege()) {
            Log.model().trace("Admin access");
            return true;
        }
        if (can(object, action)) {
            if (object.isAuthenticated() && object.isWeak()) {
                return authorizeWeakAccess(object.getWeakRights(), action);
            }
            return true;
        }
        return false;
    }

    /**
     * Throws an {@link UnauthorizedOperationException} if the
     * {@link #can(Rights, Action)} return false.
     * 
     * @param object is the object on which we want to do the
     *            <code>action</code>
     * @param action is the action.
     * @throws T the unauthorized operation exception
     */
    public final void tryAccess(final Rights object, final Action action) throws T {
        if (!canAccess(object, action)) {
            throw exception(action);
        }
    }
}
