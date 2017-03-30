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
package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.client.filter.http.HttpFilterBuilder;

/**
 * An abstraction of the Warp runtime. Provides single point of extension for this component.
 * <p/>
 * Concrete class will be registered during runtime into which all execution will be delegated.
 */
public abstract class WarpRuntime {

    /**
     * Provides thread-safe reference to {@link WarpRuntime}.
     */
    private static final ThreadLocal<WarpRuntime> runtime = new ThreadLocal<WarpRuntime>();

    /**
     * Creates new instance of {@link WarpRuntime} class.
     */
    protected WarpRuntime() {

        // empty constructor
    }

    /**
     * Retrieves an instance of {@link WarpActivityBuilder} from the warp runtime.
     *
     * @return instance of {@link WarpActivityBuilder}
     */
    public abstract WarpActivityBuilder getWarpActivityBuilder();

    /**
     * Retrieves an instance of {@link HttpFilterBuilder} from the warp runtime.
     *
     * @return instance of {@link HttpFilterBuilder}.
     */
    public abstract HttpFilterBuilder getHttpFilterBuilder();

    /**
     * Retrieves the instance of the warp runtime.
     *
     * @return the warp runtime
     */
    public static WarpRuntime getInstance() {

        return runtime.get();
    }

    /**
     * Sets the instance of warp runtime.
     *
     * @param warpRuntime the warp runtime
     */
    public static void setInstance(final WarpRuntime warpRuntime) {

        runtime.set(warpRuntime);
    }
}
