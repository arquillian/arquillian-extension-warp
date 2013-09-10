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
package org.jboss.arquillian.warp.impl.client.deployment;

import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.warp.spi.WarpCommons;

public class DeploymentValidator {

    @Inject
    private Instance<TestClass> testClass;

    /**
     * Verifies that deployments for Warp tests are all testable
     */
    public void verifyWarpDeployment(@Observes DeploymentScenario deploymentScenario) {
        if (WarpCommons.isWarpTest(testClass.get().getJavaClass())) {
            for (Deployment deployment : deploymentScenario.deployments()) {
                DeploymentDescription description = deployment.getDescription();
                if (!description.testable()) {
                    throw new IllegalArgumentException("Warp deployments must be testable: " + testClass.get().getJavaClass()
                            + " - check that you have @Deployment(testable=true)");
                }
            }
        }
    }
}
