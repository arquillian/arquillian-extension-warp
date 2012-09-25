package org.jboss.arquillian.warp.server.assertion;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.testutils.SeparatedClassloader;
import org.jboss.arquillian.warp.testutils.SeparatedClassPath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.runner.RunWith;

public class TestAssertionLoading {
    
    @SeparatedClassPath
    public static JavaArchive getClasspath() {
        return ShrinkWrap.create(JavaArchive.class).addClass(ServerAssertion.class);
    }
    
    public void test() {
        
    }
    
    
}
