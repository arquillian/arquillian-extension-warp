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

import org.jboss.arquillian.warp.spi.exception.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.warp.spi.exception.ObjectNotAssociatedException;
import org.jboss.arquillian.warp.spi.exception.StoreHasAssociatedObjectsException;

public class TestingLifecycleManagerStore extends LifecycleManagerStore {

    @Override
    protected <T> void bind(LifecycleManager manager, Class<T> clazz, T object) throws ObjectAlreadyAssociatedException {
    }

    @Override
    protected <T> void unbind(LifecycleManager manager, Class<T> clazz, T object) throws ObjectNotAssociatedException {
    }

    @Override
    protected <T> void checkUnbound(LifecycleManager manager) throws StoreHasAssociatedObjectsException {
    }

    @Override
    protected <T> LifecycleManager obtain(Class<T> clazz, T object) throws ObjectNotAssociatedException {
        return new TestingLifecycleManager();
    }

}
