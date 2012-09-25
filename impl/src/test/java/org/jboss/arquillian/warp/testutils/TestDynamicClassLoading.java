package org.jboss.arquillian.warp.testutils;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SeparatedClassloader.class)
public class TestDynamicClassLoading {

    private ServerAssertion assertion = new ServerAssertion() {
    };
    
    @SeparatedClassPath
    static JavaArchive getClasspath() {
        return ShrinkWrap.create(JavaArchive.class).addClass(ServerAssertion.class);
    }

    @Test
    public void test() {
        String test = "xyz";
        System.out.println(test);
    }
}
