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
package org.jboss.arquillian.warp.impl.shared;

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.client.transformation.MigratedAssertion;
import org.jboss.arquillian.warp.impl.client.transformation.TransformedAssertion;

public class RequestPayload implements Externalizable {

    private static final long serialVersionUID = -5537112559937896153L;

    public static final long FAILURE_SERIAL_ID = -1L;

    private List<ServerAssertion> assertions;
    private long serialId;

    public RequestPayload() {
    }

    public RequestPayload(ServerAssertion... assertions) {
        this(Arrays.asList(assertions));
    }

    public RequestPayload(List<ServerAssertion> assertions) {
        this.assertions = assertions;
        this.serialId = UUID.randomUUID().getMostSignificantBits();
    }

    public List<ServerAssertion> getAssertions() {
        return assertions;
    }

    public long getSerialId() {
        return serialId;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        serialId = in.readLong();
        boolean isAnonymous = in.readBoolean();
        if (isAnonymous) {
            int size = in.read();

            for (int i = 0; i < size; i++) {

                byte[] classFile = (byte[]) in.readObject();
                byte[] obj = (byte[]) in.readObject();

                assertions = new ArrayList<ServerAssertion>(size);

                final DynamicClassLoader cl = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());

                final Class<?> clazz = cl.load(classFile);
                ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(obj)) {
                    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                        if (desc.getName().equals(clazz.getName())) {
                            return clazz;
                        }
                        return super.resolveClass(desc);
                    }
                };

                ServerAssertion assertion = (ServerAssertion) input.readObject();
                assertions.add(assertion);
            }
        } else {
            assertions = (List<ServerAssertion>) in.readObject();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (assertions.getClass().isAnonymousClass() || assertions.getClass().isMemberClass()) {

            try {
                out.writeLong(serialId);
                out.writeBoolean(true);
                out.write(assertions.size());

                for (ServerAssertion assertion : assertions) {
                    TransformedAssertion transformed = new TransformedAssertion(assertion);
                    MigratedAssertion migrated = new MigratedAssertion(transformed);

                    out.writeObject(migrated.toBytecode());
                    out.writeObject(migrated.toSerializedForm());
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not transform and replicate class " + assertions.getClass(), e);
            }
        } else {
            out.writeBoolean(false);
            out.writeObject(assertions);
        }
    }

    public static class DynamicClassLoader extends ClassLoader {
        public DynamicClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> load(byte[] classFile) {
            return defineClass(null, classFile, 0, classFile.length);
        }
    }
}
