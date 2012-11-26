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
package org.jboss.arquillian.warp.impl.server.assertion;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.warp.ServerAssertion;

/**
 * The registry for registered assertions registered for current requests.
 *
 * @author Lukas Fryc
 */
public class AssertionRegistry {

    private Set<ServerAssertion> assertions = new LinkedHashSet<ServerAssertion>(1);

    public void registerAssertions(ServerAssertion... assertions) {
        registerAssertions(Arrays.asList(assertions));
    }

    public void registerAssertions(Collection<ServerAssertion> assertions) {
        validateNotNull(assertions);
        this.assertions.addAll(assertions);
    }

    public void unregisterAssertions(ServerAssertion... assertions) {
        unregisterAssertions(Arrays.asList(assertions));
    }

    public void unregisterAssertions(Collection<ServerAssertion> assertions) {
        validateNotNull(assertions);
        this.assertions.removeAll(assertions);
    }

    public Collection<ServerAssertion> getAssertions() {
        return this.assertions;
    }

    private void validateNotNull(Collection<ServerAssertion> assertions) {
        for (ServerAssertion assertion : assertions) {
            Validate.notNull(assertion, "assertion must not be null");
        }
    }
}
