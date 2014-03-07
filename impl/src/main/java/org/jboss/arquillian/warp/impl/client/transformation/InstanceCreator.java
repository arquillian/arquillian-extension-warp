/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.impl.client.transformation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InstanceCreator {

    static Object createInstance(Class<?> clazz) {
        Object instance = createUnsafeInstance(clazz);
        if(instance == null) {
            try {
                instance = clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Could not create new instance of Transformed class: " + clazz.getName(), e);
            }
        }
        return instance;
    }

    private static Object createUnsafeInstance(Class<?> clazz) {
        Object unsafe = getUnsafe();
        if(unsafe == null) {
            return null;
        }
        try {
            Method newInstance = unsafe.getClass().getMethod("allocateInstance", Class.class);
            return newInstance.invoke(unsafe, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getUnsafe() {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return field.get(null);
        }
        catch(Exception e) {
            return null;
        }
    }

}
