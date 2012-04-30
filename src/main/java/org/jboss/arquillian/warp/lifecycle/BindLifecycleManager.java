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

import org.jboss.arquillian.core.spi.Validate;

/**
 * The event which binds auxiliary object with {@link LifecycleManager} instance,
 * 
 * so it can be retrieved using {@link LifecycleManagerStore#get(Class, Object)} method.
 * 
 * @author Lukas Fryc
 * 
 * @param <T> the type of auxiliary object bounds
 */
public class BindLifecycleManager<T> {

    private Class<T> determinator;
    private T boundObject;

    /**
     * @param determinator the class used as determinator when retrieving instance of {@link LifecycleManager} from
     *        {@link LifecycleManagerStore}
     * @param boundObject the instance which is used as key for retriving instance of {@link LifecycleManager}.
     */
    public BindLifecycleManager(Class<T> determinator, T boundObject) {
        Validate.notNull(determinator, "determinator must not be null");
        Validate.notNull(boundObject, "boundObject must not be null");
        this.determinator = determinator;
        this.boundObject = boundObject;
    }

    public Class<T> getDeterminator() {
        return determinator;
    }

    public T getBoundObject() {
        return boundObject;
    }
}
