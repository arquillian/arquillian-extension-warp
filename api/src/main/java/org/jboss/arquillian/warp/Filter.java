package org.jboss.arquillian.warp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.arquillian.warp.client.filter.RequestFilter;

/**
 * Specifies request filter to be applied on all requests in whole annotated method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Filter {

    /**
     * Specifies request filter which should be applied on all requests in whole annotated method.
     */
    Class<? extends RequestFilter<?>> value();
}
