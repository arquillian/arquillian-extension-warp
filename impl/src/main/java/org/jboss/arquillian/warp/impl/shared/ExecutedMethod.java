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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.core.spi.Validate;

public class ExecutedMethod implements Externalizable {

    private Method method;
    private List<Annotation> qualifiers;

    public ExecutedMethod() {
    }

    public ExecutedMethod(Method method, List<Annotation> qualifiers) {
        Validate.notNull(method, "method must not be null");
        Validate.notNull(qualifiers, "qualifiers must not be null");
        this.method = method;
        this.qualifiers = qualifiers;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<Annotation> getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(List<Annotation> qualifiers) {
        this.qualifiers = qualifiers;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qualifiers == null) ? 0 : qualifiers.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExecutedMethod other = (ExecutedMethod) obj;
        if (qualifiers == null) {
            if (other.qualifiers != null)
                return false;
        } else if (!qualifiers.equals(other.qualifiers))
            return false;
        if (method == null) {
            if (other.method != null)
                return false;
        } else if (!method.equals(other.method))
            return false;
        return true;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(new SerializedMethod(method));
        out.writeInt(qualifiers.size());
        for (Annotation qualifier : qualifiers) {
            out.writeObject(new SerializedAnnotation(qualifier));
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        method = ((SerializedMethod) in.readObject()).getMethod();
        int size = in.readInt();
        qualifiers = new ArrayList<Annotation>(size);
        for (int i = 0; i < size; i++) {
            qualifiers.add(((SerializedAnnotation) in.readObject()).getAnnotation());
        }
    }
}
