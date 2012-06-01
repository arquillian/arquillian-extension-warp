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
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class TestDispatcherServlet extends DispatcherServlet {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {

        SpringMvcResult mvcResult = new SpringMvcResult();
        mvcResult.setModelAndView(mv);

        storeMvcResult(request, mvcResult);

        super.render(mv, request, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        ModelAndView mv = super.processHandlerException(request, response, handler, ex);

        SpringMvcResult mvcResult = new SpringMvcResult();
        mvcResult.setModelAndView(mv);
        mvcResult.setException(ex);

        storeMvcResult(request, mvcResult);

        return mv;
    }

    /**
     * <p>Stores the execution result.</p>
     *
     * @param mvcResult the mvc result
     */
    private void storeMvcResult(HttpServletRequest request, SpringMvcResult mvcResult) {

        request.setAttribute(Commons.SPRING_MVC_RESULT_ATTRIBUTE_NAME, mvcResult);
    }
}
