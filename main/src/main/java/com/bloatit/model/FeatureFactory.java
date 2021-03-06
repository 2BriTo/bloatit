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
package com.bloatit.model;

import com.bloatit.framework.utils.i18n.Language;
import com.bloatit.model.managers.MemberManager;
import com.bloatit.model.right.AuthToken;
import com.bloatit.model.right.UnauthorizedOperationException;

public class FeatureFactory {

    public static Feature createFeature(final Member author,
                                        final Team team,
                                        final Language language,
                                        final String title,
                                        final String description,
                                        final Software software) throws UnauthorizedOperationException {
        final FeatureImplementation featureImplementation = new FeatureImplementation(author, team, language, title, description, software);

        if (software != null) {
            for (final FollowSoftware s : software.getFollowers()) {
                AuthToken.temporaryAuthenticate(s.getFollower());
                final FollowFeature followFeature = s.getFollower().followOrGetFeature(featureImplementation);
                followFeature.setBugComment(true);
                followFeature.setFeatureComment(true);
                followFeature.setMail(followFeature.isMail());
                AuthToken.temporaryDeauthenticate();
            }
        }

        for (final Member member : MemberManager.getAllMembersFollowingAll()) {
            if (!member.isFollowing(software)) {
                AuthToken.temporaryAuthenticate(member);
                final FollowFeature followFeature = member.followOrGetFeature(featureImplementation);
                followFeature.setBugComment(true);
                followFeature.setFeatureComment(true);
                followFeature.setMail(member.isGlobalFollowWithMail());
                AuthToken.temporaryDeauthenticate();
            }
        }

        final FollowFeature followFeature = author.followOrGetFeature(featureImplementation);
        followFeature.setBugComment(true);
        followFeature.setFeatureComment(true);
        followFeature.setMail(true);

        return featureImplementation;
    }
}
