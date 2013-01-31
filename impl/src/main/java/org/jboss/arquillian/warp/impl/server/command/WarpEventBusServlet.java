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
package org.jboss.arquillian.warp.impl.server.command;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.container.test.spi.command.Command;
/**
 *
 * @author atzoum
 *
 */
@WebServlet(name = "WarpEventBus", urlPatterns = { "/WarpEventBus" })
public class WarpEventBusServlet extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = -6992268537681002870L;

    public static final String WARP_EVENT_BUS_SERVLET_NAME = "WarpEventBus";
    public static final String WARP_EVENT_BUS_SERVLET_MAPPING = "/" + WARP_EVENT_BUS_SERVLET_NAME;

    public static final String PARA_METHOD_NAME = "methodName";
    public static final String PARA_CLASS_NAME = "className";

    static ConcurrentHashMap<String, Command<?>> events;
    static String currentCall;

    @Override
    public void init() throws ServletException {
        events = new ConcurrentHashMap<String, Command<?>>();
        currentCall = "";
    }

    @Override
    public void destroy() {
        events.clear();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        executeEvent(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        executeEvent(request, response);
    }

    public void executeEvent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String className = null;
        String methodName = null;
        try {
            className = request.getParameter(PARA_CLASS_NAME);
            if (className == null) {
                throw new IllegalArgumentException(PARA_CLASS_NAME + " must be specified");
            }
            methodName = request.getParameter(PARA_METHOD_NAME);
            if (methodName == null) {
                throw new IllegalArgumentException(PARA_METHOD_NAME + " must be specified");
            }
            String eventKey = className + methodName;
            currentCall = eventKey;
            if (request.getContentLength() > 0) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(request.getInputStream()));
                Command<?> result = (Command<?>) input.readObject();
                events.put(eventKey, result);
            } else {
                if (events.containsKey(eventKey) && events.get(eventKey).getResult() == null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());
                    output.writeObject(events.remove(eventKey));
                    output.flush();
                    output.close();
                } else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
