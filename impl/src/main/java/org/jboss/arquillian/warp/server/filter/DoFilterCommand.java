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
package org.jboss.arquillian.warp.server.filter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Encapsulates command for execution of the filter chain, including context of request and response.
 *
 * @author Lukas Fryc
 *
 */
public abstract class DoFilterCommand {

    private ServletRequest request;
    private ServletResponse response;

    /**
     * Setup the context of request
     *
     * @param request the context of request
     */
    public void setRequest(ServletRequest request) {
        this.request = request;
    }

    /**
     * Setup the context of response
     *
     * @param request the context of response
     */
    public void setResponse(ServletResponse response) {
        this.response = response;
    }

    /**
     * Executes the implementation of filter chain processing with provided {@link #doFilter(ServletRequest, ServletResponse)}
     * method.
     *
     * @throws ServletException
     * @throws IOException
     */
    public void executeFilterChain() throws ServletException, IOException {
        doFilter(request, response);
    }

    /**
     * Executes filter chain processing
     *
     * @param request the request to be filtered
     * @param response the response to be filtered
     */
    protected abstract void doFilter(ServletRequest request, ServletResponse response) throws ServletException, IOException;
}
