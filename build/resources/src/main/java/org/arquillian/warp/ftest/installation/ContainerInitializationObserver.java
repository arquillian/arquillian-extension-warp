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
package org.arquillian.warp.ftest.installation;

import org.jboss.arquillian.container.spi.event.container.AfterStop;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

public class ContainerInitializationObserver {

    @Inject
    private Event<InstallContainer> install;

    @Inject
    private Event<ConfigureContainer> configure;

    @Inject
    private Event<UninstallContainer> uninstall;

    public void installContainer(@Observes(precedence = 400) EventContext<BeforeSuite> ctx) {
        install.fire(new InstallContainer());
        configure.fire(new ConfigureContainer());
        ctx.proceed();
    }

    /**
     * Removes a managed container from the "target" directory.
     *
     * This must happen after the container was stopped, not in "AfterSuite" event (which seems to be fired before the container is stopped).
     *
     * @param ctx
     */
    public void uninstallContainer(@Observes(precedence = 400) EventContext<AfterStop> ctx) {
        uninstall.fire(new UninstallContainer());
    }
}