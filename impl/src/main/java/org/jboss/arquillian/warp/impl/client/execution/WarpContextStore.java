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
package org.jboss.arquillian.warp.impl.client.execution;

import java.util.concurrent.atomic.AtomicReference;

import org.jboss.arquillian.core.spi.Validate;

/**
 * <p>
 * This class stores current context and makes that available for other threads.
 * </p>
 *
 * <p>
 * Implementation ensures that there can be only one context active, because context needs to be {@link #reset()} before it can
 * be again {@link #set(WarpContext)}.
 * </p>
 *
 * @author Lukas Fryc
 */
public class WarpContextStore {

    private static final AtomicReference<WarpContext> reference = new AtomicReference<WarpContext>();

    /**
     * <p>
     * Sets context store up.
     * </p>
     *
     * <p>
     *
     *
     * @param warpContext
     */
    static void set(WarpContext warpContext) {
        Validate.notNull(warpContext, "WarpContext for setting to store can't be null");

        try {
            while (!reference.compareAndSet(null, warpContext)) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * <p>
     * Clears current context store.
     * </p>
     *
     * <p>
     * This method can't be called only when context was previously set by calling {@link #set(WarpContext)} and that call
     * wasn't followed by another call to {@link #reset()}.
     */
    static void reset() {
        WarpContext context = reference.get();
        Validate.notNull(context, "WarpContext in store can't be null when resetting");
        reference.compareAndSet(context, null);
    }

    /**
     * <p>
     * Returns current warp context.
     * </p>
     *
     * @return current warp context or null if there is no active context
     */
    static WarpContext get() {
        return reference.get();
    }
}
