package org.jboss.arquillian.jsfunitng.test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;

public class LifecycleTestDeenricher {

    private Map<Field, Object> backupAll = new HashMap<Field, Object>();
    private Map<Field, Object> backupUpdated = new HashMap<Field, Object>();

    public void beforeTest(@Observes(precedence = 100) EventContext<Before> context) {
        final Object testInstance = context.getEvent().getTestInstance();
        backupAllFields(testInstance);
        context.proceed();
        backupUpdatedFields(testInstance);
    }

    public void afterTest(@Observes(precedence = 100) EventContext<After> context) {
        final Object testInstance = context.getEvent().getTestInstance();
        context.proceed();
        restoreFields(testInstance);
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

                if (oldValue != newValue) {
                    backupUpdated.put(field, oldValue);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        backupAll.clear();
    }

    private void restoreFields(Object instance) {
        try {
            for (Entry<Field, Object> entry : backupUpdated.entrySet()) {
                Field field = entry.getKey();
                Object oldValue = entry.getValue();

                field.set(instance, oldValue);

            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        backupUpdated.clear();
    }
}
