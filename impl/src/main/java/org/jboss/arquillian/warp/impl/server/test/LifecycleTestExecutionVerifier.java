/*
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
package org.jboss.arquillian.warp.impl.server.test;

import java.lang.annotation.Annotation;
import java.util.List;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.Test;
import org.jboss.arquillian.warp.impl.shared.ExecutedMethod;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

/**
 * Construct list of executed life-cycle test them to {@link ResponsePayload} in order to
 *
 * @author Lukas Fryc
 */
public class LifecycleTestExecutionVerifier {

    @Inject
    private Instance<ResponsePayload> responsePayload;

    public void observeTestExecution(@Observes EventContext<Test> context) {
        Test event = context.getEvent();

        try {
            context.proceed();
        } finally {
            if (event.getTestMethodExecutor() instanceof LifecycleMethodExecutor) {
                List<Annotation> qualifiers = ((LifecycleMethodExecutor) event.getTestMethodExecutor()).getQualifiers();

                ExecutedMethod executedMethod = new ExecutedMethod(event.getTestMethod(), qualifiers);

                responsePayload.get().getExecutedMethods().add(executedMethod);
            }
        }
    }
}
