/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.lifecycle;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.assertion.AssertionRegistry;
import org.jboss.arquillian.warp.spi.LifecycleEvent;
import org.jboss.arquillian.warp.spi.LifecycleManager;
import org.jboss.arquillian.warp.test.TestResultStore;

/**
 * The manager which can fire lifecycle event, which can in turn start verification on assertion registered using
 * {@link AssertionRegistry}.
 * 
 * @author Lukas Fryc
 * 
 */
public class LifecycleManagerImpl implements LifecycleManager {

    @Inject
    private Event<LifecycleEvent> lifecycleEvent;

    @Inject
    Instance<TestResultStore> testResultStore;

    /**
     * Fires lifecycle event, which can start verification on given assertion.
     * 
     * @param event the lifecycle event to fire
     */
    public void fireLifecycleEvent(LifecycleEvent event) {
        lifecycleEvent.fire(event);
    }
}
