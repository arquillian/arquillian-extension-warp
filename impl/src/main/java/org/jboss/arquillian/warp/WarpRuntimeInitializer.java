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
package org.jboss.arquillian.warp;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.warp.client.execution.WarpRuntime;
import org.jboss.arquillian.warp.client.filter.http.HttpFilterBuilder;
import org.jboss.arquillian.warp.impl.client.execution.DefaultWarpRuntime;
import org.jboss.arquillian.warp.impl.client.execution.WarpRequestSpecifier;
import org.jboss.arquillian.warp.impl.utils.WarpTestValidator;
import org.jboss.arquillian.warp.spi.WarpCommons;

/**
 * Injects instance of {@link WarpRequestSpecifier} to {@link Warp} API.
 *
 * @author Lukas Fryc
 */
public class WarpRuntimeInitializer {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    public void injectWarpRuntime(@Observes BeforeClass event) {
        TestClass testClass = event.getTestClass();
        if (WarpCommons.isWarpTest(testClass.getJavaClass())) {
            if (WarpTestValidator.isAnnotatedRunAsClient(testClass)) {
                if (WarpTestValidator.hasTestableDeployment(testClass)) {
                    DefaultWarpRuntime runtime = new DefaultWarpRuntime();
                    runtime.setWarpActivityBuilder(serviceLoader.get().onlyOne(WarpRequestSpecifier.class));
                    runtime.setHttpFilterBuilder(serviceLoader.get().onlyOne(HttpFilterBuilder.class));
                    WarpRuntime.setInstance(runtime);
                } else {
                    throw new IllegalArgumentException(
                        "The test " + testClass.getJavaClass() + " doesn't have declared any deployment "
                            + "or none of the declared deloyments is testable. "
                            + "Check that you have a deployment annotated by @Deployment(testable=true) and try it again.");
                }
            } else {
                throw new IllegalArgumentException(
                    "You are trying to run a Warp test " + testClass.getJavaClass()
                        + " which is NOT annotated by the annotation: @RunAsClient. "
                        + "Add the annotation and try it again.");
            }
        }
    }

    public void cleanWarpRuntime(@Observes AfterClass event) {
        WarpRuntime.setInstance(null);
    }
}
