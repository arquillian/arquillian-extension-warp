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

/*
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassPath;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassloaderRunner;
import org.jboss.arquillian.warp.spi.exception.ObjectNotAssociatedException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@RunWith(SeparatedClassloaderRunner.class)
public class TestLifecycleManagerStore {

    @SeparatedClassPath
    public static JavaArchive classPath() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClasses(LifecycleManager.class, TestingLifecycleManager.class, ObjectNotAssociatedException.class)
            .addClasses(LifecycleManagerStore.class, TestingLifecycleManagerStore.class)
            .addAsServiceProvider(LifecycleManagerStore.class, TestingLifecycleManagerStore.class);
    }

    @Test
    public void testObtainingCurrentStore() {
        LifecycleManagerStore instance = LifecycleManagerStore.getCurrentStore();

        if (!(instance instanceof TestingLifecycleManagerStore)) {
            throw new AssertionError();
        }
    }

    @Test
    public void testObrainingCurrentManager() throws ObjectNotAssociatedException {
        LifecycleManager lifecycleManager = LifecycleManagerStore.get(null, null);

        if (!(lifecycleManager instanceof TestingLifecycleManager)) {
            throw new AssertionError();
        }
    }
}*/