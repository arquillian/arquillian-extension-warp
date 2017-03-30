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
package org.jboss.arquillian.warp.impl.shared;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.warp.impl.utils.AnnotationInstanceProvider;

public class SerializedAnnotation implements Serializable {
    private static final long serialVersionUID = 1L;
    private Class<? extends Annotation> annotationType;
    private Map<String, Serializable> values = new HashMap<String, Serializable>();

    public SerializedAnnotation(Annotation annotation) {
        annotationType = annotation.annotationType();

        List<Method> declaredMethods = Arrays.asList(annotationType.getDeclaredMethods());
        Collections.sort(declaredMethods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (Method method : declaredMethods) {
            try {
                Serializable value = (Serializable) method.invoke(annotation);
                values.put(method.getName(), value);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public Annotation getAnnotation() {
        try {
            return AnnotationInstanceProvider.get(annotationType, values);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}