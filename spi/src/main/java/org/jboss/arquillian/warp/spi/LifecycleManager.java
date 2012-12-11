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

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.spi.exception.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.warp.spi.exception.ObjectNotAssociatedException;
import org.jboss.arquillian.warp.spi.exception.StoreHasAssociatedObjectsException;

/**
 * <p>
 * Provides ability of executing events in Warp lifecycle from outside of Arquillian extensions.
 * </p>
 *
 * <p>
 * Lifecycle manager can execute either:
 * </p>
 *
 * <ul>
 * <li>{@link WarpLifecycleEvent} events - these will cause executing lifecycle verification methods in all associated
 * {@link ServerAssertion}s</li>
 * <li>any other Arquillian event</li>
 * </ul>
 *
 *
 * <p>
 * LifecycleManager can be retrieved from arbitrary objects associated with current request using {@link LifecycleManagerStore}.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * <code>
 * // having reference to request
 * ServletRequest request = ...;
 *
 * LifecycleManager manager = LifecycleManagerStore.get(ServletRequest.class, request);
 * manager.fireEvent(new CustomLifecycleEvent());
 * </code>
 * </pre>
 *
 * <p>
 * with following implementation of <tt>CustomLifecycleEvent</tt>
 * </p>
 *
 * <pre>
 * <code>
 * public class CustomLifecycleEvent extends WarpLifecycleEvent {
 *
 *     public Annotation getAnnotation() {
 *         return new CustomAnnotation() {
 *             public Class<? extends Annotation> annotationType() {
 *                 return CustomAnnotation.class;
 *             }
 *         };
 *     }
 *
 * }
 * </code>
 * </pre>
 *
 * <p>
 * and following implementation of <tt>CustomAnnotation</tt>
 * </p>
 *
 * <pre>
 * <code>
 * {@literal @}Retention(RUNTIME)
 * {@literal @}Target(ElementType.METHOD)
 * public {@literal @}interface CustomAnnotation {
 * }
 * </code>
 * </pre>
 *
 * This will cause executing following method in associated user-defined {@link ServerAssertion}:
 *
 * <pre>
 * <code>
 * {@literal @}CustomAnnotation
 * public void verifyCustomBehavior() {
 *     ...
 * }
 * </code>
 * </pre>
 *
 * <p>
 * For more details how to implement <tt>CustomLifecycleEvent</tt>, see {@link WarpLifecycleEvent}.
 *
 * <p>
 * For more info on how to retrieve {@link LifecycleManagerStore}, see {@link LifecycleManagerStore#get(Class, Object)}.
 * </p>
 *
 * @author Lukas Fryc
 */
public abstract class LifecycleManager {

    /**
     * <p>
     *
     * Executes either:
     * </p>
     *
     * <ul>
     * <li>{@link WarpLifecycleEvent} events - these will cause executing lifecycle verification methods in all associated
     * {@link ServerAssertion}s</li>
     * <li>any other Arquillian event</li>
     * </ul>
     *
     * @param event any Arquillian event or specifically {@link WarpLifecycleEvent}
     */
    public abstract void fireEvent(Object event);

    /**
     * Binds the current {@link LifecycleManager} with given object of given class.
     *
     * @param clazz the class to be bound
     * @param object the object to be bound
     * @throws ObjectAlreadyAssociatedException when there is already object bound with {@link LifecycleManager} for given
     *         class.
     */
    public final <T> void bindTo(Class<T> clazz, T object) throws ObjectAlreadyAssociatedException {
        LifecycleManagerStore.getCurrentStore().bind(this, clazz, object);
    }

    /**
     * Unbinds the {@link LifecycleManager} from given class and given object.
     *
     * @param clazz the bound class
     * @param object the bound class
     * @throws ObjectNotAssociatedException when no object bound with {@link LifecycleManager}.
     */
    public final <T> void unbindFrom(Class<T> clazz, T object) throws ObjectNotAssociatedException {
        LifecycleManagerStore.getCurrentStore().unbind(this, clazz, object);
    }

    /**
     * Verifies that there is no object bound with this {@link LifecycleManager}.
     *
     * @throws StoreHasAssociatedObjectsException when there is object bound with this {@link LifecycleManager}.
     */
    public final <T> void checkUnbound() throws StoreHasAssociatedObjectsException {
        LifecycleManagerStore.getCurrentStore().checkUnbound(this);
    }
}
