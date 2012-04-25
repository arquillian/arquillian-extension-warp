package org.jboss.arquillian.jsfunitng.test;

import java.lang.annotation.Annotation;

public class AfterServletEvent extends LifecycleEvent {

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return AfterServlet.class;
    }

}
