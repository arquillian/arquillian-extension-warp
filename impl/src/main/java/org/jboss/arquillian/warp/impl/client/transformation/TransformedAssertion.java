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

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.shrinkwrap.api.asset.NamedAsset;

public class TransformedAssertion {

    private ClassPool classPool;

    private Class<?> originalClass;
    private CtClass transformed;
    private Class<ServerAssertion> transformedClass;
    private ServerAssertion transformedAssertion;

    public TransformedAssertion(ServerAssertion serverAssertion) throws AssertionTransformationException {
        this(serverAssertion.getClass(), "org.jboss.arquillian.warp.generated.A" + UUID.randomUUID().toString(),
                serverAssertion);
    }

    private TransformedAssertion(Class<?> originalClass, String newClassName, ServerAssertion serverAssertion)
            throws AssertionTransformationException {
        this.classPool = ClassPool.getDefault();
        this.originalClass = originalClass;

        this.transformed = transform(newClassName);
        this.transformedClass = toClass();
        this.transformedAssertion = cloneToNew(serverAssertion);
    }

    private CtClass transform(String newClassName) throws AssertionTransformationException {

        try {
            CtClass output = classPool.getAndRename(originalClass.getName(), newClassName);

            // remove enclosing reference to the method.
            output.getClassFile2().getAttributes().remove(output.getClassFile2().getAttribute(EnclosingMethodAttribute.tag));
            output.getClassFile2().getAttributes().remove(output.getClassFile2().getAttribute(InnerClassesAttribute.tag));

            output.setModifiers(Modifier.PUBLIC);
            for (CtField field : output.getDeclaredFields()) {
                if (field.getName().equals("this$0")) {
                    output.removeField(field);
                }
            }

            CtField field = output.getField("serialVersionUID");
            if (field.getDeclaringClass() != output) {
                output.addField(CtField.make("private static final long serialVersionUID = 1L;", output));
            }
            for (CtConstructor constructor : output.getConstructors()) {
                output.removeConstructor(constructor);
            }
            output.addConstructor(CtNewConstructor.defaultConstructor(output));

            return output;
        } catch (Exception e) {
            throw new AssertionTransformationException("Unable to transform assertion " + originalClass.getName(), e);
        }
    }

    private ServerAssertion cloneToNew(ServerAssertion obj) throws AssertionTransformationException {
        try {
            Class<? extends ServerAssertion> oldClass = obj.getClass();
            ServerAssertion newObj = transformedClass.newInstance();
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
            throw new AssertionTransformationException("Unable to clone " + obj.getClass().getName() + " to "
                    + transformed.getName(), e);
        }
    }

    public byte[] toBytecode() throws AssertionTransformationException {
        try {
            return transformed.toBytecode();
        } catch (Exception e) {
            throw new AssertionTransformationException("Unable to convert " + transformed.getName() + " to bytecode", e);
        }
    }

    public NamedAsset toShrinkWrapAsset() {
        return new CtClassAsset(transformed);
    }

    public Class<?> getTransformedClass() {
        return transformedClass;
    }

    public ServerAssertion getTransformedAssertion() {
        return transformedAssertion;
    }

    private Class<ServerAssertion> toClass() throws AssertionTransformationException {
        try {
            return transformed.toClass();
        } catch (Exception e) {
            throw new AssertionTransformationException("Unable to convert " + transformed.getName() + " to class", e);
        }
    }

    public Class<?> getOriginalClass() {
        return originalClass;
    }

}
