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
package org.jboss.arquillian.warp.extension.spring;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.warp.extension.servlet.AfterServletEvent;
import org.jboss.arquillian.warp.server.request.BeforeRequest;
import org.jboss.arquillian.warp.server.request.RequestScoped;

import javax.servlet.ServletRequest;

/**
 *
 */
public class AfterDispatcherServlet {

    @Inject
    @RequestScoped
    private InstanceProducer<ServletRequest> servletRequest;

    @Inject
    @RequestScoped
    private InstanceProducer<SpringMvcResult> mvcResult;

    public void beforeRequest(@Observes BeforeRequest beforeRequest) {

        servletRequest.set(beforeRequest.getRequest());
    }

    public void afterDispatcherServlet(@Observes EventContext<AfterServletEvent> context) {

        // retrieves the servlet request
        if (servletRequest.get() != null) {

            mvcResult.set((SpringMvcResult) servletRequest.get().getAttribute(Commons.SPRING_MVC_RESULT_ATTRIBUTE_NAME));
        }

        context.proceed();
    }
}
