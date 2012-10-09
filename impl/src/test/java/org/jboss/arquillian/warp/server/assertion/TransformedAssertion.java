package org.jboss.arquillian.warp.server.assertion;

import java.lang.reflect.Field;
import java.util.UUID;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.Modifier;
import javassist.bytecode.EnclosingMethodAttribute;

public class TransformedAssertion {

    private ClassPool classPool;

    private Class<?> originalClass;
    private CtClass transformedClass;

    public TransformedAssertion(Class<?> originalClass) throws Exception {
        this(originalClass, "org.jboss.arquillian.warp.generated.A" + UUID.randomUUID().toString());
    }

    public TransformedAssertion(Class<?> originalClass, String newClassName) throws Exception {
        this.classPool = ClassPool.getDefault();
        this.originalClass = originalClass;

        this.transformedClass = transform(newClassName);
    }

    private CtClass transform(String newClassName) throws AssertionTransformationException {

        try {
            CtClass output = classPool.getAndRename(originalClass.getName(), newClassName);

            // remove enclosing reference to the method.
            output.getClassFile2().getAttributes().remove(output.getClassFile2().getAttribute(EnclosingMethodAttribute.tag));
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

    public Object cloneToNew(Object obj) throws AssertionTransformationException {
        try {
            Class<?> newClass = toClass();

            Class<?> oldClass = obj.getClass();
            Object newObj = newClass.newInstance();
            for (Field newF : newClass.getDeclaredFields()) {
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
                    + transformedClass.getName(), e);
        }
    }

    public byte[] toBytecode() throws AssertionTransformationException {
        try {
            return transformedClass.toBytecode();
        } catch (Exception e) {
            throw new AssertionTransformationException("Unable to convert " + transformedClass.getName() + " to bytecode", e);
        }
    }

    public Class<?> toClass() throws AssertionTransformationException {
        try {
            return transformedClass.toClass();
        } catch (Exception e) {
            throw new AssertionTransformationException("Unable to convert " + transformedClass.getName() + " to bytecode", e);
        }
    }

}
