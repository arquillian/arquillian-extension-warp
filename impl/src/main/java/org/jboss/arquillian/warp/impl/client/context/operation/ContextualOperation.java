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
package org.jboss.arquillian.warp.impl.client.context.operation;

/**
 * An operation which takes argument of A type and returns result of R
 * <p>
 * This operation has the "current" context unless you use {@link Contextualizer} to put it in another context.
 *
 * @param <A> the argument of operation
 * @param <R> the result of operation (can be Void)
 * @author Lukas Fryc
 */
public interface ContextualOperation<A, R> {

    /**
     * Performs an operation with argument A and result R
     *
     * @return result R
     */
    R performInContext(A argument);
}