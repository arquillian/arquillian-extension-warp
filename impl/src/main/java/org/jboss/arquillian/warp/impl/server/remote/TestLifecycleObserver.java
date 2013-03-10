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
package org.jboss.arquillian.warp.impl.server.remote;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.warp.impl.server.event.AfterSuiteRemoteEvent;
import org.jboss.arquillian.warp.impl.server.event.BeforeSuiteRemoteEvent;
/**
 * Executes {@link BeforeSuite} and {@link AfterSuite} events on container.
 *
 * @author Aris Tzoumas
 *
 */
public class TestLifecycleObserver {

    @Inject
    private Event<BeforeSuite> beforeSuite;

    @Inject
    private Event<AfterSuite> afterSuite;

    public void beforeSuite(@Observes BeforeSuiteRemoteEvent event) {
        beforeSuite.fire(new BeforeSuite());
    }

    public void afterSuite(@Observes AfterSuiteRemoteEvent event) {
        afterSuite.fire(new AfterSuite());
    }
}
