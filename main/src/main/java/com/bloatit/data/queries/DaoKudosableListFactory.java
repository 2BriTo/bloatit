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
package com.bloatit.data.queries;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.bloatit.data.DaoKudosable;
import com.bloatit.data.DaoKudosable.PopularityState;
import com.bloatit.data.SessionManager;

class DaoKudosableListFactory<T extends DaoKudosable> extends DaoUserContentListFactory<T> {

    private static final String KUDOS = "kudos";
    private static final String STATE = "state";
    private static final String POPULARITY = "popularity";

    protected DaoKudosableListFactory(Criteria criteria) {
        super(criteria);
    }

    public DaoKudosableListFactory() {
        super(SessionManager.getSessionFactory().getCurrentSession().createCriteria(DaoKudosable.class));
    }

    public void orderByPopularity(DaoAbstractListFactory.OrderType order) {
        if (order == OrderType.ASC) {
            addOrder(Order.asc(POPULARITY));
        } else {
            addOrder(Order.desc(POPULARITY));
        }
    }

    public void popularity(Comparator cmp, int value) {
        add(createNbCriterion(cmp, POPULARITY, value));
    }

    public void stateEquals(PopularityState state) {
        add(Restrictions.eq(STATE, state));
    }

    public void kudosSize(Comparator cmp, int number) {
        add(createNbCriterion(cmp, KUDOS, number));
    }

}
