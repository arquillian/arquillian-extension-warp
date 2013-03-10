/**
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
package org.jboss.arquillian.warp.impl.server.command;

import static org.jboss.arquillian.warp.impl.server.execution.WarpFilter.ARQUILLIAN_MANAGER_ATTRIBUTE;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.command.CommandService;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.warp.impl.server.delegation.RequestDelegationService;
import org.jboss.arquillian.warp.impl.server.event.ActivateManager;
import org.jboss.arquillian.warp.impl.server.event.PassivateManager;
import org.jboss.arquillian.warp.impl.server.event.WarpRemoteEvent;
import org.jboss.arquillian.warp.impl.server.event.WarpRemoteCommand;

/**
 * Processes {@link CommandService} requests.
 *
 * @author Aris Tzoumas
 */
public class CommandEventBusService implements
        RequestDelegationService {
    public static final String COMMAND_EVENT_BUS_PATH = "CommandEventBus";
    public static final String COMMAND_EVENT_BUS_MAPPING = "/" + COMMAND_EVENT_BUS_PATH;
    private static final String METHOD_NAME = "methodName";
    private static final String CLASS_NAME = "className";
    private static final String OPERATION_MODE = "operationMode";
    static ConcurrentHashMap<String, Command<?>> events = new ConcurrentHashMap<String, Command<?>>();
    static String currentCall = "";

    @Override
    public boolean canDelegate(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        return (servletPath != null && servletPath.equals(COMMAND_EVENT_BUS_MAPPING));
    }

    @Override
    public void delegate(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain) {
        try {
            executeEvent(request, response);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeEvent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String className = null;
        String methodName = null;
        String operationMode = null;
        try {
            className = request.getParameter(CLASS_NAME);
            if (className == null) {
                throw new IllegalArgumentException(CLASS_NAME + " must be specified");
            }
            methodName = request.getParameter(METHOD_NAME);
            if (methodName == null) {
                throw new IllegalArgumentException(METHOD_NAME + " must be specified");
            }

            operationMode = request.getParameter(OPERATION_MODE);
            if (operationMode == null) {
                throw new IllegalArgumentException(OPERATION_MODE + " must be specified");
            }

            String eventKey = className + methodName;
            currentCall = eventKey;

            if (OperationMode.GET.name().equals(operationMode)) {
                executeGetOperation(request, response);
            } else if (OperationMode.PUT.name().equals(operationMode)) {
                executePutOperation(request, response);
            } else {
                throw new IllegalArgumentException("Unsupported " + OPERATION_MODE + " parameter.");
            }

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Container-to-Client command execution
     */
    private void executeGetOperation(HttpServletRequest request, HttpServletResponse response) throws IOException, ClassNotFoundException {
        if (request.getContentLength() > 0) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(request.getInputStream()));
            Command<?> result = (Command<?>) input.readObject();
            events.put(currentCall, result);
        } else {
            if (events.containsKey(currentCall) && events.get(currentCall).getResult() == null) {
                response.setStatus(HttpServletResponse.SC_OK);
                ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());
                output.writeObject(events.remove(currentCall));
                output.flush();
                output.close();
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        }
    }

    /**
     * Client-to-Container event propagation
     */
    private void executePutOperation(HttpServletRequest request, HttpServletResponse response) throws IOException, ClassNotFoundException {
        if (request.getContentLength() > 0) {
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(request.getInputStream()));
            WarpRemoteCommand result = (WarpRemoteCommand) input.readObject();
            WarpRemoteEvent remoteEvent = result.getPayload();
            Manager manager = (Manager)request.getAttribute(ARQUILLIAN_MANAGER_ATTRIBUTE);
            // execute remote Event
            try{
                manager.fire(new ActivateManager(manager));
                manager.fire(remoteEvent);
                manager.fire(new PassivateManager(manager));
                result.setResult("SUCCESS");
            } catch (Throwable e) {
                result.setThrowable(e);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());
            output.writeObject(result);
            output.flush();
            output.close();

        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    public static ConcurrentHashMap<String, Command<?>> getEvents() {
        return events;
    }

    public static String getCurrentCall() {
        return currentCall;
    }
}
