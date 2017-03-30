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
package org.jboss.arquillian.warp.impl.client.verification;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.impl.client.event.VerifyResponsePayload;
import org.jboss.arquillian.warp.impl.shared.ExecutedMethod;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.WarpCommons;

/**
 * Verifies that {@link ResponsePayload} which came from server is valid
 *
 * @author Lukas Fryc
 */
public class ResponsePayloadVerifier {

    public void verifyTestResult(@Observes VerifyResponsePayload event) {
        ResponsePayload responsePayload = event.getResponsePayload();

        if (responsePayload.getTestResult().getStatus() == null) {
            throw new IllegalStateException("ResponsePayload must have TestResult set");
        }
    }

    public void verifyAllLifecycleTestsExecuted(@Observes VerifyResponsePayload event) {
        ResponsePayload responsePayload = event.getResponsePayload();

        if (responsePayload.getTestResult().getStatus() != TestResult.Status.FAILED) {

            Set<ExecutedMethod> executedMethods = responsePayload.getExecutedMethods();
            Set<ExecutedMethod> specifiedMethods = new HashSet<ExecutedMethod>();

            for (Inspection inspection : responsePayload.getInspections()) {
                List<Method> methods = SecurityActions.getMethodsWithAnnotation(inspection.getClass());

                for (Method method : methods) {
                    for (Annotation annotation : method.getDeclaredAnnotations()) {
                        Class<? extends Annotation> annotationType = annotation.annotationType();

                        if (WarpCommons.isWarpLifecycleTest(annotationType)) {
                            specifiedMethods.add(
                                new ExecutedMethod(method, Arrays.asList(method.getDeclaredAnnotations())));
                        }
                    }
                }
            }

            specifiedMethods.removeAll(executedMethods);

            for (ExecutedMethod notExecutedMethod : specifiedMethods) {
                throw new InspectionMethodWasNotInvokedException(notExecutedMethod);
            }
        }
    }
}
