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
package org.jboss.arquillian.warp.impl.server.lifecycle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.warp.spi.LifecycleManager;
import org.jboss.arquillian.warp.spi.LifecycleManagerStore;
import org.jboss.arquillian.warp.spi.exception.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.warp.spi.exception.ObjectNotAssociatedException;
import org.jboss.arquillian.warp.spi.exception.StoreHasAssociatedObjectsException;

/**
 * Stores the bindings from auxiliary class instance to {@link LifecycleManager}.
 *
 * @author Lukas Fryc
 *
 */
public class LifecycleManagerStoreImpl extends LifecycleManagerStore {

    private static ConcurrentHashMap<Class<?>, Binding> STORE = new ConcurrentHashMap<Class<?>, Binding>();
    private static ConcurrentHashMap<LifecycleManager, Tracking> TRACK = new ConcurrentHashMap<LifecycleManager, Tracking>();

    @SuppressWarnings("serial")
    private static class Binding extends HashMap<Object, LifecycleManager> {
    }

    @SuppressWarnings("serial")
    private static class Tracking extends HashSet<Class<?>> {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.warp.spi.LifecycleManagerStore#obtain(java.lang.Class, java.lang.Object)
     */
    @Override
    protected <T> LifecycleManager obtain(Class<T> clazz, T boundObject) throws ObjectNotAssociatedException {
        Validate.notNull(boundObject, "boundObject must not be null");
        Binding binding = STORE.get(clazz);
        if (binding == null) {
            throw new ObjectNotAssociatedException();
        }
        LifecycleManager manager = binding.get(boundObject);
        if (manager == null) {
            throw new ObjectNotAssociatedException();
        }
        return manager;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.warp.spi.LifecycleManagerStore#bind(org.jboss.arquillian.warp.spi.LifecycleManager,
     * java.lang.Class, java.lang.Object)
     */
    @Override
    protected <T> void bind(LifecycleManager manager, Class<T> clazz, T object) throws ObjectAlreadyAssociatedException {
        // update binding
        STORE.putIfAbsent(clazz, new Binding());
        Binding binding = STORE.get(clazz);

        if (binding.put(object, manager) != null) {
            throw new ObjectAlreadyAssociatedException();
        }

        // update tracking
        TRACK.putIfAbsent(manager, new Tracking());
        Tracking tracking = TRACK.get(manager);

        tracking.add(clazz);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.warp.spi.LifecycleManagerStore#unbind(org.jboss.arquillian.warp.spi.LifecycleManager,
     * java.lang.Class, java.lang.Object)
     */
    @Override
    protected <T> void unbind(LifecycleManager manager, Class<T> clazz, T object) throws ObjectNotAssociatedException {
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

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.warp.spi.LifecycleManagerStore#checkUnbound(org.jboss.arquillian.warp.spi.LifecycleManager)
     */
    @Override
    protected <T> void checkUnbound(LifecycleManager manager) throws StoreHasAssociatedObjectsException {
        Tracking tracking = TRACK.get(manager);
        if (tracking != null) {
            if (!tracking.isEmpty()) {
                throw new StoreHasAssociatedObjectsException();
            }
            TRACK.remove(manager);
        }
    }
}
