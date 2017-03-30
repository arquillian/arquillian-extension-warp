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

import static org.junit.Assert.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.reflect.Method;

import org.junit.Test;

@TestingAnnotation(clazz = TestExecutedMethod.class, elementType = ElementType.TYPE, integer = 5, string = "testing")
public class TestExecutedMethod {

    @Test
    public void testSerializedMethod() {
        // given
        Method method = this.getClass().getMethods()[0];

        // when
        SerializedMethod serializedMethod = new SerializedMethod(method);
        Method deserializedMethod = serializedMethod.getMethod();

        // then
        assertEquals(method, deserializedMethod);
    }

    @Test
    public void testSerializedAnnotation() {
        // given
        TestingAnnotation annotation = this.getClass().getAnnotation(TestingAnnotation.class);

        // when
        SerializedAnnotation serializedAnnotation = new SerializedAnnotation(annotation);
        TestingAnnotation deserializedAnnotation = (TestingAnnotation) serializedAnnotation.getAnnotation();

        // then
        assertEquals(annotation, deserializedAnnotation);
        assertEquals(annotation.string(), deserializedAnnotation.string());
        System.out.println(deserializedAnnotation);
    }
}
