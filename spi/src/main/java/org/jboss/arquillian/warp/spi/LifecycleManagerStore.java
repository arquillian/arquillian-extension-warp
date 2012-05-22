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
package org.jboss.arquillian.warp.spi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Stores the bindings from auxiliary class instance to {@link LifecycleManager}.
 *
 * @author Lukas Fryc
 */
public abstract class LifecycleManagerStore {

    private static final String LIFECYCLE_MANAGER_STORE_IMPLEMENTATION = "org.jboss.arquillian.warp.server.lifecycle.LifecycleManagerStoreImpl";

    /**
     * Binds the current {@link LifecycleManager} with given object of given class.
     *
     * @param clazz the class to be bound
     * @param object the object to be bound
     * @throws ObjectAlreadyAssociatedException when there is already object bound with {@link LifecycleManager} for given
     *         class.
     */
    public abstract <T> void bind(Class<T> clazz, T object) throws ObjectAlreadyAssociatedException;

    /**
     * Unbinds the {@link LifecycleManager} for given class and given object.
     *
     * @param clazz the bound class
     * @param object the bound class
     * @throws ObjectNotAssociatedException when no object bound with {@link LifecycleManager}.
     */
    public abstract <T> void unbind(Class<T> clazz, T object) throws ObjectNotAssociatedException;

    /**
     * Retrieves instance of {@link LifecycleManagerImpl} for given instance of given class.
     *
     * @param clazz the class used as denominator during retrieval
     * @param boundObject the object used as key for retriving {@link LifecycleManagerImpl}
     * @return the bound instance of {@link LifecycleManagerImpl}
     * @throws ObjectNotAssociatedException when instance of no such class and class' instance was associated with any
     *         {@link LifecycleManagerImpl}
     */
    public static <T> LifecycleManager get(Class<T> clazz, T boundObject) throws ObjectNotAssociatedException {
        try {
            Class<?> implementation = Class.forName(LIFECYCLE_MANAGER_STORE_IMPLEMENTATION);
            Method getMethod = implementation.getMethod("get", Object.class.getClass(), Object.class);
            return (LifecycleManager) getMethod.invoke(null, clazz, boundObject);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof ObjectNotAssociatedException) {
                throw (ObjectNotAssociatedException) e.getCause();
            } else {
                throw new IllegalStateException(e);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("The " + LIFECYCLE_MANAGER_STORE_IMPLEMENTATION
                    + "is not available on classpath, check that you have arquillian-warp-impl.jar on classpath");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("The static method get was not found on LifecycleManagerStore: "
                    + LIFECYCLE_MANAGER_STORE_IMPLEMENTATION);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }
}
