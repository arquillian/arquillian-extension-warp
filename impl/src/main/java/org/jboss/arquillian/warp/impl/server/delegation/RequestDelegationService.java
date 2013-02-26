/**
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.impl.server.delegation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.warp.impl.server.execution.WarpFilter;

/**
 * <p>
 * Service Interface for delegating request processing inside {@link WarpFilter}
 * .
 * </p>
 * <p>
 * Delegates will be asked to handle request processing (through
 * {@link RequestDelegationService#canDelegate(HttpServletRequest)}).
 * Delegates who decide to actually process the request, will need to perform
 * all processing inside
 * {@link RequestDelegationService#delegate(HttpServletRequest, HttpServletResponse)}
 * .
 * </p>
 * <p>
 * Request processing is performed outside Arquillian's context. No
 * {@link Manager} instance is created.
 * </p>
 *
 * @author Aris Tzoumas
 *
 */
public interface RequestDelegationService {

    /**
     * <p>
     * Method to decide if delegate can handle the request
     * </p>
     *
     * @param request
     *            the incoming {@link HttpServletRequest}.
     * @return <code>true</code> if delegate can handle the request.
     *         <code>false</code> otherwise.
     */
    boolean canDelegate(HttpServletRequest request);

    /**
     * <p>
     * Actual request processing for delegate.
     * </p>
     * <p>
     * This method will be called only if
     * {@link RequestDelegationService#canDelegate(HttpServletRequest)}
     * returned <code>true</code>
     * </p>
     *
     * @param request
     *            the incoming {@link HttpServletRequest}.
     * @param response
     *            the {@link HttpServletResponse} to send.
     */
    void delegate(HttpServletRequest request, HttpServletResponse response);

}
