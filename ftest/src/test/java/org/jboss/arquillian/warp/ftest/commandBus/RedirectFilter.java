/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.ftest.commandBus;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.warp.impl.server.commandBus.CommandBusOnServer;
import org.jboss.arquillian.warp.impl.shared.command.OperationMode;

/**
 * @author Aris Tzoumas
 */
public class RedirectFilter implements Filter {
    private boolean redirect = true;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws IOException, ServletException {

        if (req instanceof HttpServletRequest && resp instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) resp;

            String servletPath = request.getServletPath();
            String operationMode = request.getParameter("operationMode");

            /*
             *  only redirect the first PUT operation - @BeforeSuiteRemoteEvent
             *  If this redirect fails the whole test will fail.
             */
            if (servletPath != null
                && servletPath.equals(CommandBusOnServer.COMMAND_EVENT_BUS_MAPPING)
                && OperationMode.PUT.name().equals(operationMode)
                && redirect) {
                redirect = false;
                response.sendRedirect(request.getRequestURL().toString() + "?" + request.getQueryString());
            } else {
                chain.doFilter(req, resp);
            }
        } else {
            chain.doFilter(req, resp);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}
