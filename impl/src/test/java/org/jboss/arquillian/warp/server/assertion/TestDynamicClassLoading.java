package org.jboss.arquillian.warp.server.assertion;

import org.jboss.arquillian.warp.ServerAssertion;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SeparateClassloaderTestRunner.class)
public class TestDynamicClassLoading {

    ServerAssertion assertion = new ServerAssertion() {
    };
    
    @Test
    public void test() {

    }
}
