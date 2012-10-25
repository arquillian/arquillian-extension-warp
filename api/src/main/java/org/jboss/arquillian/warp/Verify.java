package org.jboss.arquillian.warp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies assertions which should be verified for each enriched request.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Verify {

    /**
     * The assertion to be verified during all enriched requests.
     */
    Class<? extends ServerAssertion>[] value();
}
