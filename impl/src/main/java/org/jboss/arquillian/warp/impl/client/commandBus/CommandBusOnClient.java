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
package org.jboss.arquillian.warp.impl.client.commandBus;

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
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.warp.impl.client.commandBus.CommandBusObserver.StartBus;
import org.jboss.arquillian.warp.impl.client.context.operation.ContextualOperation;
import org.jboss.arquillian.warp.impl.client.context.operation.Contextualizer;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContexts;
import org.jboss.arquillian.warp.impl.server.commandBus.CommandBusOnServer;
import org.jboss.arquillian.warp.impl.shared.command.Command;
import org.jboss.arquillian.warp.impl.shared.command.CommandPayload;
import org.jboss.arquillian.warp.impl.shared.command.OperationMode;
import org.jboss.arquillian.warp.impl.utils.Rethrow;
import org.jboss.arquillian.warp.spi.WarpCommons;

/**
 * <p>
 * Provides an event bus during test execution to execute commands on the server
 * </p>
 *
 * <p>
 * Event Bus functionality is similar to ServletProtocol
 * </p>
 *
 * @author Aris Tzoumas
 */
public class CommandBusOnClient {

    private final Logger log = Logger.getLogger(CommandBusOnClient.class.getName());

    @Inject
    private Event<Object> eventExecutedRemotely;

    @Inject
    private Instance<ProtocolMetaData> protocolMetadata;

    @Inject
    private Instance<OperationalContexts> operationalContexts;

    @Inject
    private Instance<Injector> injector;

    @Inject
    private Instance<TestClass> testClass;

    private String channelUrl;

    private static Timer eventBusTimer;

    public void startBus(StartBus event) {
        if (!WarpCommons.isWarpTest(testClass.get().getJavaClass())){
            return;
        }
        if (protocolMetadata.get() == null) {
            log.warning(
                "There is no protocol metadata - possible causes: "
                    + "URL wasn't resolved because of some failure or there is no testable deployment");
            return;
        }

        Class<?> testClass = event.getTestInstance().getClass();
        Method testMethod = event.getTestMethod();

        // Calculate eventUrl
        Collection<HTTPContext> contexts = protocolMetadata.get().getContexts(HTTPContext.class);

        HTTPContext context = locateHTTPContext(testMethod, contexts);
        URI servletURI = locateCommandEventBusURI(context);

        String url = servletURI.toASCIIString() + "?className=" + testClass.getName() + "&methodName=" + testMethod.getName();

        channelUrl = url;

        final String eventUrlForGet = url + "&operationMode=" + OperationMode.GET.name();

        // Prepare CommandCallback
        final ContextualOperation<Command, Void> operation = operationForExecutingEventRemotelyOnCurrentContext();

        // Start Timer
        if (eventBusTimer != null)
            eventBusTimer.cancel();

        try {
            eventBusTimer = new Timer();
            eventBusTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        CommandPayload payload = execute(eventUrlForGet, CommandPayload.class, null);
                        if (payload != null) {
                            Command command = payload.getCommand();
                            try {
                                operation.performInContext(command);
                            } catch (Throwable e) {
                                payload.setThrowable(e);
                            }
                            payload.setExecuted();
                            execute(eventUrlForGet, Object.class, payload);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 100);

        } catch (Exception e) {
            throw new IllegalStateException("Error launching test " + testClass.getName() + " " + testMethod, e);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopBus() {
        if (eventBusTimer != null) {
            eventBusTimer.cancel();
            eventBusTimer = null;
        }
    }

    private ContextualOperation<Command, Void> operationForExecutingEventRemotelyOnCurrentContext() {
        OperationalContext context = operationalContexts.get().test();

        return Contextualizer.contextualize(context, new ContextualOperation<Command, Void>() {
            public Void performInContext(Command command) {
                injector.get().inject(command);
                command.perform();
                return null;
            }
        }, ContextualOperation.class);
    }

    public Command executeRemotely(Command command) {
        final String eventUrlForPut = channelUrl + "&operationMode=" + OperationMode.PUT.name();
        final CommandPayload payload = new CommandPayload(command);
        try {
            CommandPayload result = execute(eventUrlForPut, CommandPayload.class, payload);
            if (result.getThrowable() != null) {
                Rethrow.asUnchecked(result.getThrowable());
            }
            return result.getCommand();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
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
         * With followRedirects enabled, simple URL redirects work as expected. But with port redirects (http->https)
         * followRedirects doesn't work and a HTTP 302 code is returned instead (ARQ-1365).
         *
         * In order to handle all redirects in one place, followRedirects is set to false and all HTTP 302 response codes are
         * treated accordingly within the execute method.
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
                path = path + CommandBusOnServer.COMMAND_EVENT_BUS_MAPPING;
                return new URI("http", null, baseURI.getHost(), baseURI.getPort(), path, null, null);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Could not convert Servlet to URL, " + context.getServlets().get(0), e);
            }
        } else {
            try {
                return new URI("http", null, context.getHost(), context.getPort(),
                        CommandBusOnServer.COMMAND_EVENT_BUS_MAPPING, null, null);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Could not convert HTTPContext to URL, " + context, e);
            }
        }
    }

}
