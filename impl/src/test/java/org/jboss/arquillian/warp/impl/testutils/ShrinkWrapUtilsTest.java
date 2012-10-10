package org.jboss.arquillian.warp.impl.testutils;

import static org.junit.Assert.assertNotNull;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class ShrinkWrapUtilsTest {

    @Test
    public void testClasspathPropagation() throws Throwable {
        JavaArchive archive = ShrinkWrapUtils.getJavaArchiveFromClass(Test.class);

        assertNotNull(archive.get("/org/junit/Test.class"));
        assertNotNull(archive.get("/org/junit/Ignore.class"));
    }
}
