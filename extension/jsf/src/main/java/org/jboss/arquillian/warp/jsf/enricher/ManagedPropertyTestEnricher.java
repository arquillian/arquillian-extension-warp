/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.jsf.enricher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * Injections JSF managed beans and other objects using {@link ManagedProperty} annotation.
 *
 * @author Lukas Fryc
 */
public class ManagedPropertyTestEnricher implements TestEnricher {

    @Override
    public void enrich(Object testCase) {
        Class<?> clazz = testCase.getClass();
        List<Field> fields = SecurityActions.getFieldsWithAnnotation(clazz, ManagedProperty.class);

        for (Field field : fields) {

            ManagedProperty property = field.getAnnotation(ManagedProperty.class);
            Object resolvedValue = resolveValueExpression(property, field.getType(), field);

            try {
                SecurityActions.setFieldValue(clazz, testCase, field.getName(), resolvedValue);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public Object[] resolve(Method method) {
        return null;
    }

    private Object resolveValueExpression(ManagedProperty property, Class<?> expectedType, Object injectionPoint) {

        FacesContext context = FacesContext.getCurrentInstance();
        String expression = property.value();

        try {
            ValueExpression valueExpression = context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), expression, expectedType);
            return valueExpression.getValue(context.getELContext());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to resolve a value for @ManagedProperty(\"" + expression + "\") on "
                + injectionPoint + "\n" + e.getMessage(), e);
        }
    }
}
