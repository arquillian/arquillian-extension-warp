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
package org.jboss.arquillian.warp.impl.client.transformation;

import java.lang.reflect.Field;
import java.util.UUID;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.Modifier;
import javassist.bytecode.EnclosingMethodAttribute;
import javassist.bytecode.InnerClassesAttribute;

import org.jboss.arquillian.warp.Inspection;
import org.jboss.shrinkwrap.api.asset.NamedAsset;

/**
 * Allows to transform inner classes to be usable as top-level classes.
 *
 * @author Lukas Fryc
 */
public class TransformedInspection {

    private ClassPool classPool;

    private Class<?> originalClass;
    private CtClass transformed;
    private Class<Inspection> transformedClass;
    private Inspection transformedInspection;

    public TransformedInspection(Inspection inspection) throws InspectionTransformationException {
        this(inspection.getClass(), "org.jboss.arquillian.warp.generated.A" + UUID.randomUUID().toString(), inspection);
    }

    private TransformedInspection(Class<?> originalClass, String newClassName, Inspection serverInspection)
        throws InspectionTransformationException {
        this.classPool = ClassPool.getDefault();
        this.originalClass = originalClass;

        this.transformed = transform(newClassName);
        this.transformedClass = toClass();
        this.transformedInspection = cloneToNew(serverInspection);
    }

    private CtClass transform(String newClassName) throws InspectionTransformationException {

        try {
            CtClass output = classPool.getAndRename(originalClass.getName(), newClassName);

            // remove enclosing reference to the method.
            output.getClassFile2()
                .getAttributes()
                .remove(output.getClassFile2().getAttribute(EnclosingMethodAttribute.tag));
            output.getClassFile2().getAttributes().remove(output.getClassFile2().getAttribute(InnerClassesAttribute.tag));

            output.setModifiers(Modifier.PUBLIC);
            for (CtField field : output.getDeclaredFields()) {
                if (field.getName().equals("this$0")) {
                    output.removeField(field);
                }
            }

            CtField field = output.getField("serialVersionUID");
            if (field.getDeclaringClass() != output) {
                throw new NoSerialVersionUIDException("serialVersionUID for class " + originalClass.getName()
                    + " is not set; please set serialVersionUID to allow Warp work correctly");
            }
            for (CtConstructor constructor : output.getConstructors()) {
                output.removeConstructor(constructor);
            }
            output.addConstructor(CtNewConstructor.defaultConstructor(output));

            return output;
        } catch (Exception e) {
            throw new InspectionTransformationException(
                "Unable to transform inspection " + originalClass.getName() + ":\n" + e.getMessage(), e);
        }
    }

    private Inspection cloneToNew(Inspection obj) throws InspectionTransformationException {
        try {
            Class<? extends Inspection> oldClass = obj.getClass();
            Inspection newObj = transformedClass.newInstance();
            for (Field newF : transformedClass.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(newF.getModifiers())
                    && java.lang.reflect.Modifier.isFinal(newF.getModifiers())) {
                    continue;
                }
                Field oldF = oldClass.getDeclaredField(newF.getName());
                oldF.setAccessible(true);
                newF.setAccessible(true);
                newF.set(newObj, oldF.get(obj));
            }
            return newObj;
        } catch (Exception e) {
            throw new InspectionTransformationException("Unable to clone " + obj.getClass().getName() + " to "
                + transformed.getName(), e);
        }
    }

    public byte[] toBytecode() throws InspectionTransformationException {
        try {
            return transformed.toBytecode();
        } catch (Exception e) {
            throw new InspectionTransformationException("Unable to convert " + transformed.getName() + " to bytecode", e);
        }
    }

    public NamedAsset toShrinkWrapAsset() {
        return new CtClassAsset(transformed);
    }

    public Class<?> getTransformedClass() {
        return transformedClass;
    }

    public Inspection getTransformedInspection() {
        return transformedInspection;
    }

    @SuppressWarnings("unchecked")
    private Class<Inspection> toClass() throws InspectionTransformationException {
        try {
            return transformed.toClass();
        } catch (Exception e) {
            throw new InspectionTransformationException("Unable to convert " + transformed.getName() + " to class", e);
        }
    }

    public Class<?> getOriginalClass() {
        return originalClass;
    }
}
