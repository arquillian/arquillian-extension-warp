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

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.warp.spi.LifecycleManagerStore;
import org.jboss.arquillian.warp.spi.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.warp.spi.ObjectNotAssociatedException;

/**
 * Stores the bindings from auxiliary class instance to {@link LifecycleManagerImpl}.
 * 
 * @author Lukas Fryc
 * 
 */
public class LifecycleManagerStoreImpl extends LifecycleManagerStore {

    private static ConcurrentHashMap<Class<?>, Binding> STORE = new ConcurrentHashMap<Class<?>, Binding>();
    private static ConcurrentHashMap<LifecycleManagerImpl, Tracking> TRACK = new ConcurrentHashMap<LifecycleManagerImpl, Tracking>();

    @SuppressWarnings("serial")
    private static class Binding extends HashMap<Object, LifecycleManagerImpl> {
    }

    @SuppressWarnings("serial")
    private static class Tracking extends HashSet<Class<?>> {
    }

    @Inject
    Instance<LifecycleManagerImpl> manager;

    /**
     * Retrieves instance of {@link LifecycleManagerImpl} for given instance of given class.
     * 
     * @param clazz the class used as denominator during retrieval
     * @param boundObject the object used as key for retriving {@link LifecycleManagerImpl}
     * @return the bound instance of {@link LifecycleManagerImpl}
     * @throws ObjectNotAssociatedException when instance of no such class and class' instance was associated with any
     *         {@link LifecycleManagerImpl}
     */
    public static <T> LifecycleManagerImpl get(Class<T> clazz, T boundObject) throws ObjectNotAssociatedException {
        Validate.notNull(boundObject, "boundObject must not be null");
        Binding binding = STORE.get(clazz);
        if (binding == null) {
            throw new ObjectNotAssociatedException();
        }
        LifecycleManagerImpl manager = binding.get(boundObject);
        if (manager == null) {
            throw new ObjectNotAssociatedException();
        }
        return manager;
    }

    /**
     * Binds the current {@link LifecycleManagerImpl} with given object of given class.
     * 
     * @param clazz the class to be bound
     * @param object the object to be bound
     * @throws ObjectAlreadyAssociatedException when there is already object bound with {@link LifecycleManagerImpl} for given
     *         class.
     */
    public <T> void bind(Class<T> clazz, T object) throws ObjectAlreadyAssociatedException {
        final LifecycleManagerImpl manager = getManager();

        // update binding

        Binding binding = STORE.get(clazz);
        if (binding == null) {
            STORE.putIfAbsent(clazz, new Binding());
            binding = STORE.get(clazz);
        }

        if (binding.put(object, manager) != null) {
            throw new ObjectAlreadyAssociatedException();
        }

        // update tracking

        Tracking tracking = TRACK.get(manager);
        if (tracking == null) {
            TRACK.putIfAbsent(manager, new Tracking());
            tracking = TRACK.get(manager);
        }

        tracking.add(clazz);
    }

    /**
     * Unbinds the {@link LifecycleManagerImpl} for given class and given object.
     * 
     * @param clazz the bound class
     * @param object the bound class
     * @throws ObjectNotAssociatedException when no object bound with {@link LifecycleManagerImpl}.
     */
    public <T> void unbind(Class<T> clazz, T object) throws ObjectNotAssociatedException {
        final LifecycleManagerImpl manager = getManager();

        // cancel binding

        Binding binding = STORE.get(clazz);
        if (binding == null) {
            throw new ObjectNotAssociatedException();
        }
        if (!binding.containsKey(object)) {
            throw new ObjectNotAssociatedException();
        }
        binding.remove(object);

        // cancel tracking

        Tracking tracking = TRACK.get(manager);
        tracking.remove(clazz);
    }

    /**
     * Verifies that there is no object bound with current {@link LifecycleManagerImpl}.
     * 
     * @throws StoreHasAssociatedObjectsException when there is object bound with current {@link LifecycleManagerImpl}.
     */
    public <T> void verifyManagerUnbound() throws StoreHasAssociatedObjectsException {
        final LifecycleManagerImpl manager = getManager();

        Tracking tracking = TRACK.get(manager);
        if (tracking != null) {
            if (!tracking.isEmpty()) {
                throw new StoreHasAssociatedObjectsException();
            }
            TRACK.remove(manager);
        }
    }

    private LifecycleManagerImpl getManager() {
        return manager.get();
    }
}
