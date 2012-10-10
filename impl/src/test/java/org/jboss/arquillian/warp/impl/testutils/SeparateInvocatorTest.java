package org.jboss.arquillian.warp.impl.testutils;

import java.io.Serializable;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class SeparateInvocatorTest {

    @Test
    public void test() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(Implementation.class, Interface.class,
                Argument.class, Result.class, SeparateInvocatorTest.class);

        SeparateInvocator.<Interface, Implementation>invoke(Implementation.class, archive).run(new Argument(), "xyz");
    }

    public static interface Interface {
        Result run(Argument argument, String string);
    }

    public static class Implementation implements Interface {

        public Implementation() {
        }

        @Override
        public Result run(Argument argument, String string) {
            return new Result();
        }
    }

    public static class Result implements Serializable {
    }

    public static class Argument implements Serializable {
    }
}
