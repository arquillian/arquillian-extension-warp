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
package org.jboss.arquillian.warp.impl.utils;

import java.lang.reflect.InvocationTargetException;

/**
 * Helper for rethrowing exceptions as unchecked exceptions
 *
 * @author Lukas Fryc
 */
public final class Rethrow {

    private Rethrow() {
    }

    /**
     * Checks whether given throwable is unchecked and if not, it will be wrapped in RuntimeException
     */
    public static void asUnchecked(Throwable t) {
        asUnchecked(t, RuntimeException.class);
    }

    /**
     * Checks whether given throwable is unchecked and if not, it will be wrapped as unchecked exception of given type
     */
    public static void asUnchecked(Throwable t, Class<? extends RuntimeException> checkedExceptionWrapper) {
        if (t instanceof AssertionError) {
            throw (AssertionError) t;
        } else if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            RuntimeException exception;
            try {
                exception = checkedExceptionWrapper.getConstructor(Throwable.class).newInstance(t);
            } catch (Exception e) {
                throw new RuntimeException(t);
            }
            throw exception;
        }
    }

    /**
     * Retrieves original cause from a stack of exceptions bound together with {@link Throwable#getCause()} references.
     */
    public static Throwable getOriginalCause(Throwable e) {
        if (e instanceof InvocationTargetException) {
            return getOriginalCause(((InvocationTargetException) e).getTargetException());
        }
        if (e.getCause() instanceof Throwable) {
            return getOriginalCause(e.getCause());
        }
        return e;
    }
}
