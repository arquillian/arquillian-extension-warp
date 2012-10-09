package org.jboss.arquillian.warp.server.assertion;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;

public class RunTransformationToOriginalName {

    public static void run(String oldClassName, String newClassName, byte[] oldlassFile) throws Exception {

        ClassPool pool = ClassPool.getDefault();
        
        CtClass oldClass = pool.makeClassIfNew(new ByteArrayInputStream(oldlassFile));
        
        oldClass.replaceClassName(oldClassName, newClassName);
        
        Class<?> class1 = oldClass.toClass();
        System.out.println(class1);
        
        Object object = class1.newInstance();
        Method getMethod = class1.getMethod("get");
        
        getMethod.invoke(object);
    }
}
