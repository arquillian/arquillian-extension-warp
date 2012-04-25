package org.jboss.arquillian.jsfunitng.assertion;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.arquillian.core.spi.Validate;

public class AssertionRegistry {

    private Set<Object> assertions = new LinkedHashSet<Object>(1);

    public void registerAssertion(Object assertion) {
        Validate.notNull(assertion, "assertion must not be null");
        assertions.add(assertion);
    }

    public void unregisterAssertion(Object assertion) {
        Validate.notNull(assertion, "assertion must not be null");
        assertions.remove(assertion);
    }

    public Collection<Object> getAssertions() {
        return assertions;
    }
}
