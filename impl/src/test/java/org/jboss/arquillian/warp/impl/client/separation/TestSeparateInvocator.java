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
package org.jboss.arquillian.warp.impl.client.separation;

import java.io.Serializable;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class TestSeparateInvocator {

    @Test
    public void test() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(Implementation.class, Interface.class,
                Argument.class, Result.class, TestSeparateInvocator.class);

        SeparateInvocator.<Interface, Implementation>invoke(Implementation.class, archive).run(new Argument(), "xyz");
    }

    public interface Interface {
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
