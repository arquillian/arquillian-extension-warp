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
package org.jboss.arquillian.warp.impl.server.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.warp.Inspection;

/**
 * Watches the {@link Inspection} objects and on {@link Before} event, it stores all fields which have been changed during
 * that event.
 *
 * These changes are the {@link Inspection} enrichment actions.
 *
 * Then on {@link After} method, it restores all the changed fields, which deenrich the instance.
 *
 * @author Lukas Fryc
 *
 */
public class LifecycleTestEnrichmentWatcher {

    private Map<Field, Object> backupAll = new HashMap<Field, Object>();
    private Map<Field, Object> backupUpdated = new HashMap<Field, Object>();

    public void rememberFieldValues(@Observes(precedence = 100) EventContext<Before> context) {
        final Object testInstance = context.getEvent().getTestInstance();
        backupAllFields(testInstance);
        try {
            context.proceed();
        } finally {
            backupUpdatedFields(testInstance);
        }
    }

    public void restoreOriginalFieldValues(@Observes(precedence = 100) EventContext<After> context) {
        final Object testInstance = context.getEvent().getTestInstance();
        try {
            context.proceed();
        } finally {
            restoreFields(testInstance);
        }
    }

    private void backupAllFields(Object instance) {
        try {
            List<Field> fields = SecurityActions.getFields(instance.getClass());
            for (Field field : fields) {
                backupAll.put(field, field.get(instance));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void backupUpdatedFields(Object instance) {
        try {
            for (Entry<Field, Object> entry : backupAll.entrySet()) {
                Field field = entry.getKey();
                Object oldValue = entry.getValue();

                Object newValue = field.get(instance);

                if (hasChanged(field, oldValue, newValue)) {
                    backupUpdated.put(field, oldValue);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        backupAll.clear();
    }

    private boolean hasChanged(Field field, Object oldValue, Object newValue) {
        if (field.getType().isPrimitive()) {
            return !oldValue.equals(newValue);
        } else {
            return oldValue != newValue;
        }
    }

    private void restoreFields(Object instance) {
        try {
            for (Entry<Field, Object> entry : backupUpdated.entrySet()) {
                Field field = entry.getKey();
                Object oldValue = entry.getValue();

                if (!validateIfFieldCanBeSetAndSerialized(field)) {
                    continue;
                }

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                field.set(instance, oldValue);

            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        backupUpdated.clear();
    }

    private boolean validateIfFieldCanBeSetAndSerialized(Field field) {
        if (Modifier.isTransient(field.getModifiers())
                || (Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers()))) {
            return false;
        }

        return true;
    }
}
