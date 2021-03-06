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
package com.bloatit.framework.restprocessor.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.bloatit.framework.restprocessor.RestServer.RequestMethod;

/**
 * <p>
 * Annotation used to describe methods that can be used via ReST.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface REST {
    /**
     * The name of the REST method.
     * <p>
     * If you want a method to be invoked via .../features call it features
     * </p>
     */
    String name();

    /**
     * The http request method that will be used to call this function.
     * <p>
     * Can be :
     * <li>RequestMethod.GET</li>
     * <li>RequestMethod.POST</li>
     * <li>RequestMethod.PUT</li>
     * <li>RequestMethod.DELETE</li>
     * </p>
     */
    RequestMethod method();

    /**
     * The list of parameters that are expected to call this function.
     */
    public String[] params() default {};
}
