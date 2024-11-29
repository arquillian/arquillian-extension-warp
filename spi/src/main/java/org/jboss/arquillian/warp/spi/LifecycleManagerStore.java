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
package org.jboss.arquillian.warp.spi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.arquillian.warp.spi.exception.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.warp.spi.exception.ObjectNotAssociatedException;
import org.jboss.arquillian.warp.spi.exception.StoreHasAssociatedObjectsException;

/**
 * <p>
 * Holds associations of {@link LifecycleManager} with arbitrary objects.
 * </p>
 * <p>
 * <p>
 * In order to store binding, one can need to use object and its class/interface as key.
 * </p>
 * <p>
 * <p>
 * Once the binding is stored, one can use that object together with key to restore associated {@link LifecycleManager}.
 * </p>
 * <p>
 * <p>
 * This way, {@link LifecycleManager} can be propagated to arbitrary contexts without need of shared store (particularly this
 * principle avoids use of {@link ThreadLocal} store.
 * </p>
 * <p>
 * <p>
 * Storing current references:
 * </p>
 * <p>
 * <pre><code>
 *
 *
 * @author Lukas Fryc
 */
public abstract class LifecycleManagerStore {

    private static AtomicReference<LifecycleManagerStore> INSTANCE = new AtomicReference<LifecycleManagerStore>();

    /**
     * Retrieves instance of {@link LifecycleManager} for given instance of given class.
     *
     * @param clazz       the class used as denominator during retrieval
     * @param boundObject the object used as key for retriving {@link LifecycleManager}
     * @return the bound instance of {@link LifecycleManager}
     * @throws ObjectNotAssociatedException when instance of no such class and class' instance was associated with any
     *                                      {@link LifecycleManager}
     */
    public static <T> LifecycleManager get(Class<T> type, T boundObject) throws ObjectNotAssociatedException {
        return getCurrentStore().obtain(type, boundObject);
    }

    /**
     * Provides implementation of binding a current {@link LifecycleManager} with given object of given class.
     *
     * @param clazz  the class to be bound
     * @param object the object to be bound
     * @throws ObjectAlreadyAssociatedException when there is already object bound with {@link LifecycleManager} for given
     *                                          class.
     */
    protected abstract <T> void bind(LifecycleManager manager, Class<T> clazz, T object)
        throws ObjectAlreadyAssociatedException;

    /**
     * Provides implementation of binding a {@link LifecycleManager} for given class and given object.
     *
     * @param clazz  the bound class
     * @param object the bound class
     * @throws ObjectNotAssociatedException when no object bound with {@link LifecycleManager}.
     */
    protected abstract <T> void unbind(LifecycleManager manager, Class<T> clazz, T object)
        throws ObjectNotAssociatedException;

    /**
     * <p>
     * Checks that given manager does not have any associations in context of current store.
     * </p>
     * <p>
     * <p>
     * It ensures that all managers associated with {@link #bind(LifecycleManager, Class, Object)} was properly unbound with
     * {@link #unbind(LifecycleManager, Class, Object)}.
     * </p>
     *
     * @param manager
     * @throws ObjectNotAssociatedException
     */
    protected abstract <T> void checkUnbound(LifecycleManager manager) throws StoreHasAssociatedObjectsException;

    /**
     * Provides implementation of obtaining {@link LifecycleManager} for given instance of given class.
     *
     * @param clazz       the class used as denominator during retrieval
     * @param boundObject the object used as key for retriving {@link LifecycleManager}
     * @return the bound instance of {@link LifecycleManager}
     * @throws ObjectNotAssociatedException when instance of no such class and class' instance was associated with any
     *                                      {@link LifecycleManager}
     */
    protected abstract <T> LifecycleManager obtain(Class<T> clazz, T object) throws ObjectNotAssociatedException;

    /**
     * Retrieves instance of {@link LifecycleManager} for given instance of given class.
     *
     * @param clazz       the class used as denominator during retrieval
     * @param boundObject the object used as key for retriving {@link LifecycleManager}
     * @return the bound instance of {@link LifecycleManager}
     * @throws ObjectNotAssociatedException when instance of no such class and class' instance was associated with any
     *                                      {@link LifecycleManager}
     */
    static LifecycleManagerStore getCurrentStore() {
        LifecycleManagerStore store = INSTANCE.get();
        if (store != null) {
            return store;
        }

        try {
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("META-INF/services/" + LifecycleManagerStore.class.getName());

            if (resourceAsStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
                String type = reader.readLine();
                store = (LifecycleManagerStore) Class.forName(type).getDeclaredConstructor().newInstance();
                INSTANCE.compareAndSet(null, store);
                return INSTANCE.get();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load " + LifecycleManagerStore.class.getSimpleName() + " service", e);
        }

        throw new IllegalStateException("No " + LifecycleManagerStore.class.getSimpleName() + " service is defined");
    }
}
