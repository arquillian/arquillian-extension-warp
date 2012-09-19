package org.jboss.arquillian.warp.client.execution;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.UUID;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.Modifier;
import javassist.bytecode.EnclosingMethodAttribute;

public class AssertionTransformer {
    public static Object cloneToNew(Object obj, byte[] clazzFile) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClassIfNew(new ByteArrayInputStream(clazzFile));
        Class<?> newClass = pool.toClass(ctClass);

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
    }

    public static byte[] transform(Class<?> clazz) throws Exception {
        String newClassName = "org.jboss.arquillian.warp.generated.A" + UUID.randomUUID().toString();
        ClassPool pool = ClassPool.getDefault();
        CtClass output = pool.getAndRename(clazz.getName(), newClassName);
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

        return output.toBytecode();
    }
}
