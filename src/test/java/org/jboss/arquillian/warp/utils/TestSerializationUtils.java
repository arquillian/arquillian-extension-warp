/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.utils;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.jboss.arquillian.warp.utils.SerializationUtils;
import org.junit.Test;

/**
 * @author Lukas Fryc
 */
public class TestSerializationUtils {

    @Test
    public void testObjectSerializationToBytes() {
        CustomObject object = new CustomObject(CustomObject.class.getName());
        byte[] serialized = SerializationUtils.serializeToBytes(object);
        System.out.println(serialized);
        CustomObject deserialized = SerializationUtils.deserializeFromBytes(serialized);
        assertEquals(CustomObject.class.getName(), deserialized.payload);
    }

    @Test
    public void testObjectSerializationToBase64() {
        CustomObject object = new CustomObject(CustomObject.class.getName());
        String serialized = SerializationUtils.serializeToBase64(object);
        System.out.println(serialized);
        CustomObject deserialized = SerializationUtils.deserializeFromBase64(serialized);
        assertEquals(CustomObject.class.getName(), deserialized.payload);
    }

    @SuppressWarnings("serial")
    private static class CustomObject implements Serializable {

        private String payload;

        public CustomObject(String payload) {
            this.payload = payload;
        }
    }
}
