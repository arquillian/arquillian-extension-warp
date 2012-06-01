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
package org.jboss.arquillian.warp.extension.spring;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.warp.server.request.RequestScoped;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
 */
public class SpringWarpTestEnricher implements TestEnricher {

    @Inject
    private Instance<SpringMvcResult> mvcResult;

    @Override
    public void enrich(Object testCase) {

        List<Field> annotatedFields = SecurityActions.getFieldsWithAnnotation(testCase.getClass(),
                javax.inject.Inject.class);

        try {
            for (Field field : annotatedFields) {
                Object value = null;

                if (field.getType() == SpringMvcResult.class) {
                    value = mvcResult.get();
                }

                field.set(testCase, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not inject members", e);
        }

    }

    @Override
    public Object[] resolve(Method method) {

        Object[] values = new Object[method.getParameterTypes().length];

        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];

                Object value = null;

                if (parameterType == SpringMvcResult.class) {
                    value = mvcResult.get();
                }

                values[i] = value;
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not inject method parameters", e);
        }
        return values;
    }
}
