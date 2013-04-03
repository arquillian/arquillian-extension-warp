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
package org.jboss.arquillian.warp.impl.client.eventbus;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.warp.impl.server.command.CommandEventBusService;
import org.jboss.arquillian.warp.impl.server.command.OperationMode;
import org.jboss.arquillian.warp.impl.server.event.WarpRemoteEvent;
import org.jboss.arquillian.warp.impl.server.event.WarpRemoteCommand;

/**
 * <p>
 * Provides an event bus during test execution to listen for incoming {@link Command} events.
 * </p>
 *
 * <p>
 * Event Bus functionality is similar to ServletProtocol
 * </p>
 *
 * @author Aris Tzoumas
 */
public class CommandEventBus {

    @Inject
    private Instance<ProtocolMetaData> protocolMetadata;

    @Inject
    private Event<Object> eventExecutedRemotely;

    @Inject
    private Instance<ApplicationContext> applicationContextInst;

    @Inject
    private Instance<SuiteContext> suiteContextInst;

    @Inject
    private Instance<ClassContext> classContextInst;

    @Inject
    private Instance<TestContext> testContextInst;

    private static Timer eventBusTimer;

    private static ThreadLocal<String> channelUrl = new ThreadLocal<String>();

    /**
     * Starts the Event Bus.
     */
    void startEventBus(@Observes(precedence = 500) Before event) throws Exception {
        // Calculate eventUrl
        Collection<HTTPContext> contexts = protocolMetadata.get().getContexts(HTTPContext.class);

        Class<?> testClass = event.getTestInstance().getClass();

        HTTPContext context = locateHTTPContext(event.getTestMethod(), contexts);
        URI servletURI = locateCommandEventBusURI(context);

        String url = servletURI.toASCIIString() + "?className=" + testClass.getName() + "&methodName="
                + event.getTestMethod().getName();

        channelUrl.set(url);

        final String eventUrl = url + "&operationMode=" + OperationMode.GET.name();

        // Prepare CommandCallback
        final ApplicationContext applicationContext = applicationContextInst.get();
        final SuiteContext suiteContext = suiteContextInst.get();

        final ClassContext classContext = classContextInst.get();
        final Class<?> classContextId = classContext.getActiveId();

        final TestContext testContext = testContextInst.get();
        final Object testContextId = testContext.getActiveId();
        final CommandCallback callback = new CommandCallback() {
            @Override
            public void fired(Command<?> event) {
                applicationContext.activate();
                suiteContext.activate();
                classContext.activate(classContextId);
                testContext.activate(testContextId);
                try {
                    eventExecutedRemotely.fire(event);
                } finally {
                    testContext.deactivate();
                    classContext.deactivate();
                    suiteContext.deactivate();
                    applicationContext.deactivate();
                }
            }
        };

        // Start Timer
        if (eventBusTimer != null)
            eventBusTimer.cancel();

        try {
            eventBusTimer = new Timer();
            eventBusTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Object o = execute(eventUrl, Object.class, null);
                        if (o != null) {
                            if (o instanceof Command) {
                                Command<?> command = (Command<?>) o;
                                callback.fired(command);
                                execute(eventUrl, Object.class, command);
                            } else {
                                throw new RuntimeException("Received a non " + Command.class.getName()
                                        + " object on event channel");
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 100);
        } catch (Exception e) {
            throw new IllegalStateException("Error launching test " + testClass.getName() + " " + event.getTestMethod(), e);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stops the Event Bus
     */
    void stopEventBus(@Observes(precedence = -500) After event) {
        if (eventBusTimer != null) {
            eventBusTimer.cancel();
            eventBusTimer = null;
        }
    }

    /**
     * Executes a RemoteEvent Command from the client to the container.
     *
     * @param event the {@link WarpRemoteEvent} fired.
     */
    void executeCommandRemotely(@Observes WarpRemoteEvent event) {
        final String eventUrl = channelUrl.get() + "&operationMode=" + OperationMode.PUT.name();
        WarpRemoteCommand command = new WarpRemoteCommand(event);
        try {

            Object o = execute(eventUrl, Object.class, command);
            if (o != null) {
                if (o instanceof WarpRemoteCommand) {
                    command = (WarpRemoteCommand) o;
                    if (command.getThrowable() != null) {
                        throw new RuntimeException(command.getThrowable());
                    }
                } else {
                    throw new RuntimeException("Received a non " + WarpRemoteCommand.class.getName()
                            + " object on event channel");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Error executing remote command", e);
        }
    }

    /**
     * Executes the request to the remote url
     */
    private <T> T execute(String url, Class<T> returnType, Object requestObject) throws Exception {
        URLConnection connection = new URL(url).openConnection();
        if (!(connection instanceof HttpURLConnection)) {
            throw new IllegalStateException("Not an http connection! " + connection);
        }
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setUseCaches(false);
        httpConnection.setDefaultUseCaches(false);
        httpConnection.setDoInput(true);

        /*
         * With followRedirects enabled, simple URL redirects work as expected.
         * But with port redirects (http->https) followRedirects doesn't work and
         * a HTTP 302 code is returned instead (ARQ-1365).
         *
         * In order to handle all redirects in one place, followRedirects is
         * set to false and all HTTP 302 response codes are treated accordingly
         * within the execute method.
         *
         */
        httpConnection.setInstanceFollowRedirects(false);

        try {

            if (requestObject != null) {
                httpConnection.setRequestMethod("POST");
                httpConnection.setDoOutput(true);
                httpConnection.setRequestProperty("Content-Type", "application/octet-stream");
            }

            if (requestObject != null) {
                ObjectOutputStream ous = new ObjectOutputStream(httpConnection.getOutputStream());
                try {
                    ous.writeObject(requestObject);
                } catch (Exception e) {
                    throw new RuntimeException("Error sending request Object, " + requestObject, e);
                } finally {
                    ous.flush();
                    ous.close();
                }
            }

            try {
                httpConnection.getResponseCode();
            } catch (ConnectException e) {
                return null; // Could not connect
            }
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ObjectInputStream ois = new ObjectInputStream(httpConnection.getInputStream());
                Object o;
                try {
                    o = ois.readObject();
                } finally {
                    ois.close();
                }

                if (!returnType.isInstance(o)) {
                    throw new IllegalStateException("Error reading results, expected a " + returnType.getName() + " but got "
                            + o);
                }
                return returnType.cast(o);
            } else if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                return null;
            } else if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                String redirectUrl = httpConnection.getHeaderField("Location");
                return execute(redirectUrl, returnType, requestObject);
            } else if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                throw new IllegalStateException("Error launching test at " + url + ". " + "Got "
                        + httpConnection.getResponseCode() + " (" + httpConnection.getResponseMessage() + ")");
            }
        } finally {
            httpConnection.disconnect();
        }
        return null;
    }

    private HTTPContext locateHTTPContext(Method method, Collection<HTTPContext> contexts) {
        TargetsContainer targetContainer = method.getAnnotation(TargetsContainer.class);
        if (targetContainer != null) {
            String targetName = targetContainer.value();

            for (HTTPContext context : contexts) {
                if (targetName.equals(context.getName())) {
                    return context;
                }
            }
            throw new IllegalArgumentException("Could not determin HTTPContext from ProtocolMetadata for target: " + targetName
                    + ". Verify that the given target name in @" + TargetsContainer.class.getSimpleName()
                    + " match a name returned by the deployment container");
        }
        return contexts.toArray(new HTTPContext[] {})[0];
    }

    private URI locateCommandEventBusURI(HTTPContext context) {
        List<Servlet> contextServlets = context.getServlets();
        if (contextServlets == null) {
            throw new IllegalArgumentException("Could not determine URI for WarpFilter in context " + context
                    + ". There are no Servlets in context.");
        }
        Set<String> contextRoots = new HashSet<String>();
        for (Servlet servlet : contextServlets) {
            contextRoots.add(servlet.getContextRoot());
        }
        if (contextRoots.size() == 1) {
            try {
                URI baseURI = context.getServlets().get(0).getBaseURI();
                String path = baseURI.getPath();
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                path = path + CommandEventBusService.COMMAND_EVENT_BUS_MAPPING;
                return new URI("http", null, baseURI.getHost(), baseURI.getPort(), path, null, null);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Could not convert Servlet to URL, " + context.getServlets().get(0), e);
            }
        } else {
            try {
                return new URI("http", null, context.getHost(), context.getPort(),
                        CommandEventBusService.COMMAND_EVENT_BUS_MAPPING, null, null);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Could not convert HTTPContext to URL, " + context, e);
            }
        }
    }
}