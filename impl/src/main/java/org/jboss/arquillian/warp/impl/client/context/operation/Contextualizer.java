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
package org.jboss.arquillian.warp.impl.client.context.operation;

/**
 * {@link Contextualizer} is able to wrap operation so that it can be run in another thread with given contexts activated.
 *
 * @author Lukas Fryc
 */
public class Contextualizer {

    /**
     * Contextualizes operation with contexts given by {@link OperationalContext}
     */
    public static <A, T, X extends ContextualOperation<A, T>> X contextualize(final OperationalContext context, final X operation) {

        OperationalContextRetriver retriever = new OperationalContextRetriver() {
            @Override
            public OperationalContext retrieve() {
                return context;
            }
        };

        return contextualize(retriever, operation);
    }

    /**
     * Contextualizes operation with contexts given by {@link OperationalContext} which is given by provided
     * {@link OperationalContextRetriver}
     */
    @SuppressWarnings("unchecked")
    public static <A, T, X extends ContextualOperation<A, T>> X contextualize(final OperationalContextRetriver retriver, final X operation) {

        return (X) new ContextualOperation<A, T>() {
            @Override
            public T performInContext(A argument) {
                OperationalContext context = retriver.retrieve();
                context.activate();
                try {
                    return operation.performInContext(argument);
                } finally {
                    context.deactivate();
                }
            }
        };
    }
}
